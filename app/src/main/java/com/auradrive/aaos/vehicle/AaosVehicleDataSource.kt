package com.auradrive.aaos.vehicle

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import com.auradrive.aaos.model.VehicleData
import com.auradrive.aaos.model.VehicleGear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Real AAOS implementation using CarPropertyManager.
 * Connects to the vehicle's hardware properties via the Car Service.
 */
class AaosVehicleDataSource(private val context: Context) : VehicleDataSource {

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private var lastSpeedMps = 0f
    private var lastSpeedTimestamp = 0L

    private val _vehicleData = MutableStateFlow(VehicleData())
    override val vehicleData: Flow<VehicleData> = _vehicleData.asStateFlow()

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            when (value.propertyId) {
                VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                    val speedMps = value.value as? Float ?: 0f
                    val now = System.currentTimeMillis()
                    
                    var acceleration = 0f
                    if (lastSpeedTimestamp != 0L) {
                        val dtSeconds = (now - lastSpeedTimestamp) / 1000f
                        if (dtSeconds > 0) {
                            val dvMps = speedMps - lastSpeedMps
                            acceleration = dvMps / dtSeconds
                        }
                    }
                    
                    _vehicleData.update { it.copy(
                        speedMps = speedMps,
                        acceleration = acceleration / 9.81f // Convert to G-force for thresholds
                    ) }
                    
                    lastSpeedMps = speedMps
                    lastSpeedTimestamp = now
                }
                VehiclePropertyIds.GEAR_SELECTION -> {
                    val gear = value.value as? Int ?: 0
                    _vehicleData.update { it.copy(gear = mapGear(gear)) }
                }
                VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                    val level = value.value as? Float ?: 0f
                    _vehicleData.update { it.copy(batteryLevelPercent = level) }
                }
                VehiclePropertyIds.FUEL_LEVEL -> {
                    val level = value.value as? Float ?: 0f
                    _vehicleData.update { it.copy(fuelLevelPercent = level) }
                }
                VehiclePropertyIds.HVAC_TEMPERATURE_SET -> {
                    val temp = value.value as? Float ?: 20f
                    _vehicleData.update { it.copy(cabinTemperatureCelsius = temp) }
                }
            }
        }

        override fun onErrorEvent(propId: Int, areaId: Int) {
            // Handle property error
        }
    }

    override suspend fun start() {
        if (car != null) return

        try {
            car = Car.createCar(context)
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager

            carPropertyManager?.let { manager ->
                // Speed sensor
                tryRegister(manager, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
                // Gear selection
                tryRegister(manager, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
                // Battery level for EVs
                tryRegister(manager, VehiclePropertyIds.EV_BATTERY_LEVEL, CarPropertyManager.SENSOR_RATE_NORMAL)
                // Fuel level
                tryRegister(manager, VehiclePropertyIds.FUEL_LEVEL, CarPropertyManager.SENSOR_RATE_NORMAL)
                // Cabin Temperature
                tryRegister(manager, VehiclePropertyIds.HVAC_TEMPERATURE_SET, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tryRegister(manager: CarPropertyManager, propId: Int, rate: Float) {
        try {
            manager.registerCallback(propertyCallback, propId, rate)
        } catch (e: SecurityException) {
            android.util.Log.e("AaosVehicleDataSource", "No permission for property $propId: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun stop() {
        carPropertyManager?.unregisterCallback(propertyCallback)
        car?.disconnect()
        car = null
        carPropertyManager = null
    }

    private fun mapGear(carGear: Int): VehicleGear {
        // AAOS VehicleGear constants
        return when (carGear) {
            4 -> VehicleGear.PARK     // android.car.VehicleGear.GEAR_PARK
            8 -> VehicleGear.DRIVE    // android.car.VehicleGear.GEAR_DRIVE
            2 -> VehicleGear.REVERSE  // android.car.VehicleGear.GEAR_REVERSE
            1 -> VehicleGear.NEUTRAL  // android.car.VehicleGear.GEAR_NEUTRAL
            else -> VehicleGear.UNKNOWN
        }
    }
}
