package com.auradrive.aaos.model

data class VehicleData(
    val gear: VehicleGear = VehicleGear.PARK,
    val speedMps: Float = 0f,
    val acceleration: Float = 0f,
    val brakePressure: Float = 0f,
    val cabinTemperatureCelsius: Float = 20f,
    val batteryLevelPercent: Float = 100f,
    val fuelLevelPercent: Float = 100f
)
