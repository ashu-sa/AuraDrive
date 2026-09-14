package com.auradrive.aaos.vehicle

import com.auradrive.aaos.model.VehicleData
import com.auradrive.aaos.model.VehicleGear
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockVehicleDataSource : VehicleDataSource {

    private val _vehicleData = MutableStateFlow(VehicleData())
    override val vehicleData: Flow<VehicleData> = _vehicleData.asStateFlow()

    private var isRunning = false

    override suspend fun start() {
        isRunning = true
    }

    override suspend fun stop() {
        isRunning = false
    }

    // Developer controls for simulation
    fun updateGear(gear: VehicleGear) {
        _vehicleData.update { it.copy(gear = gear) }
    }

    fun updateSpeed(speedKph: Float) {
        // Convert to MPS for internal consistency
        val mps = speedKph / 3.6f
        _vehicleData.update { it.copy(speedMps = mps) }
    }

    fun triggerHardAcceleration() {
        _vehicleData.update { it.copy(acceleration = 5.0f) }
        // Reset acceleration after a short delay to simulate an impulse
    }

    fun triggerHardBraking() {
        _vehicleData.update { it.copy(brakePressure = 0.9f, acceleration = -8.0f) }
    }

    fun resetDynamics() {
        _vehicleData.update { it.copy(acceleration = 0f, brakePressure = 0f) }
    }

    fun updateCabinTemperature(temp: Float) {
        _vehicleData.update { it.copy(cabinTemperatureCelsius = temp) }
    }

    fun updateEnergyLevels(fuel: Float, battery: Float) {
        _vehicleData.update { it.copy(fuelLevelPercent = fuel, batteryLevelPercent = battery) }
    }
}
