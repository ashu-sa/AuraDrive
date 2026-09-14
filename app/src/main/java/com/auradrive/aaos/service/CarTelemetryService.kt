package com.auradrive.aaos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.car.Car
import android.car.VehicleAreaSeat
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.auradrive.aaos.data.repository.TripRepository
import com.auradrive.aaos.telemetry.TripAggregator
import com.auradrive.aaos.worker.RoastGenerationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CarTelemetryService : Service() {

    @Inject lateinit var tripAggregator: TripAggregator
    @Inject lateinit var tripRepository: TripRepository
    @Inject lateinit var workManager: WorkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private var isTripActive = false
    private val degradedProperties = mutableSetOf<Int>()

    private val carPropertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            serviceScope.launch {
                when (value.propertyId) {
                    VehiclePropertyIds.GEAR_SELECTION -> {
                        val gear = value.value as? Int ?: return@launch
                        handleGearChange(gear)
                    }
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        val speedMps = (value.value as? Float) ?: return@launch
                        tripAggregator.recordSpeed(speedMps)
                    }
                    PROP_BRAKE_PEDAL_POSITION -> {
                        val brake = (value.value as? Float) ?: return@launch
                        tripAggregator.recordBrakePosition(brake)
                    }
                    PROP_ACCELERATOR_PEDAL_POSITION -> {
                        val accel = (value.value as? Float) ?: return@launch
                        tripAggregator.recordAcceleratorPosition(accel)
                    }
                    VehiclePropertyIds.HVAC_TEMPERATURE_SET -> {
                        val temp = (value.value as? Float) ?: return@launch
                        tripAggregator.recordHvacTemperature(temp)
                    }
                }
            }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.w(TAG, "CarProperty onErrorEvent: propId=$propId, zone=$zone. Flagged as degraded.")
            degradedProperties.add(propId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("Antigravity Telemetry Active"))
        initCarService()
    }

    private fun initCarService() {
        if (car != null && car?.isConnected == true) return

        try {
            car = Car.createCar(
                this,
                null,
                Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER
            ) { carInstance, ready ->
                if (ready) {
                    try {
                        carPropertyManager = carInstance.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
                        registerProperties()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to get CarPropertyManager from Car instance", e)
                    }
                } else {
                    Log.w(TAG, "CarService disconnected by system.")
                    carPropertyManager = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Car.createCar invocation", e)
        }
    }

    private fun registerProperties() {
        val manager = carPropertyManager ?: return

        safeRegister(manager, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_NORMAL)
        safeRegister(manager, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
        safeRegister(manager, PROP_BRAKE_PEDAL_POSITION, CarPropertyManager.SENSOR_RATE_NORMAL)
        safeRegister(manager, PROP_ACCELERATOR_PEDAL_POSITION, CarPropertyManager.SENSOR_RATE_NORMAL)

        // Register HVAC for driver seat or first available zone
        try {
            val hvacConfig = manager.getCarPropertyConfig(VehiclePropertyIds.HVAC_TEMPERATURE_SET)
            if (hvacConfig != null) {
                val areaIds = hvacConfig.areaIds
                val targetArea = if (areaIds.isNotEmpty()) areaIds[0] else VehicleAreaSeat.SEAT_ROW_1_LEFT
                manager.registerCallback(carPropertyCallback, VehiclePropertyIds.HVAC_TEMPERATURE_SET, CarPropertyManager.SENSOR_RATE_NORMAL)
            }
        } catch (e: Exception) {
            Log.w(TAG, "HVAC temperature property unsupported on this OEM vehicle build: ${e.message}")
            degradedProperties.add(VehiclePropertyIds.HVAC_TEMPERATURE_SET)
        }
    }

    private fun safeRegister(manager: CarPropertyManager, propertyId: Int, rate: Float) {
        try {
            manager.registerCallback(carPropertyCallback, propertyId, rate)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for property $propertyId", e)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Property $propertyId not supported by this vehicle's VHAL.")
            degradedProperties.add(propertyId)
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error registering property $propertyId", e)
        }
    }

    private suspend fun handleGearChange(gear: Int) {
        if (gear == VehicleGear.GEAR_PARK) {
            if (isTripActive) {
                isTripActive = false
                val finalizedTrip = tripAggregator.finishTrip()
                if (finalizedTrip != null && finalizedTrip.durationSeconds >= MIN_TRIP_DURATION_SEC) {
                    tripRepository.saveTrip(finalizedTrip)
                    enqueueRoastWorker(finalizedTrip.tripId)
                }
            }
        } else {
            // GEAR_DRIVE, GEAR_REVERSE, GEAR_NEUTRAL, GEAR_LOW
            if (!isTripActive) {
                isTripActive = true
                tripAggregator.startTrip()
            }
        }
    }

    private fun enqueueRoastWorker(tripId: String) {
        val inputData = Data.Builder()
            .putString(RoastGenerationWorker.KEY_TRIP_ID, tripId)
            .build()

        val roastWorkRequest = OneTimeWorkRequestBuilder<RoastGenerationWorker>()
            .setInputData(inputData)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueue(roastWorkRequest)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Guarantee OS restart resiliency
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        carPropertyManager?.unregisterCallback(carPropertyCallback)
        car?.disconnect()
        serviceScope.cancel()
    }

    private fun createNotification(contentText: String): Notification {
        val channelId = "antigravity_telemetry_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Antigravity Driving Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors vehicle dynamics for trip scoring"
        }
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Antigravity Telemetry")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "AntigravityTelemetry"
        private const val NOTIFICATION_ID = 9001
        private const val MIN_TRIP_DURATION_SEC = 5L // Filter trivial parking lot repositioning

        // Standard AAOS VHAL Property IDs (System/OEM exposed)
        // VehicleProperty:BRAKE_PEDAL_POSITION (float percentage or mm)
        const val PROP_BRAKE_PEDAL_POSITION = 0x11700201
        // VehicleProperty:ACCELERATOR_PEDAL_POSITION (float percentage or mm)
        const val PROP_ACCELERATOR_PEDAL_POSITION = 0x11700202
    }
}
