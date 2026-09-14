package com.auradrive.aaos.driving

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.vehicle.VehicleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Instant

class DrivingEventDetector(
    private val vehicleDataSource: VehicleDataSource,
    private val thresholds: DrivingThresholds,
    private val scope: CoroutineScope
) {
    private val _events = MutableSharedFlow<DrivingEvent>()
    val events: Flow<DrivingEvent> = _events.asSharedFlow()

    init {
        vehicleDataSource.vehicleData.onEach { data ->
            detectEvents(data)
        }.launchIn(scope)
    }

    private suspend fun detectEvents(data: com.auradrive.aaos.model.VehicleData) {
        val now = Instant.now()

        if (data.acceleration > thresholds.hardAccelerationG) {
            _events.emit(DrivingEvent.HardAcceleration(now, data.acceleration))
        }

        if (data.acceleration < thresholds.hardBrakingG) {
            _events.emit(DrivingEvent.HardBraking(now, data.acceleration))
        }

        if (data.cabinTemperatureCelsius <= thresholds.coldCabinCelsius) {
            _events.emit(DrivingEvent.ColdCabin(now, data.cabinTemperatureCelsius))
        }

        if (data.batteryLevelPercent <= thresholds.lowBatteryPercent) {
            _events.emit(DrivingEvent.LowBattery(now, data.batteryLevelPercent))
        }

        if (data.fuelLevelPercent <= thresholds.lowFuelPercent) {
            _events.emit(DrivingEvent.LowFuel(now, data.fuelLevelPercent))
        }
        
        // Smooth driving logic could be periodic, for now we emit if speed is decent but acceleration is low
        if (data.speedMps > 10f && Math.abs(data.acceleration) < 0.5f) {
             // To avoid spam, we'd normally have a timer here
             // _events.emit(DrivingEvent.SmoothDriving(now))
        }
    }
}
