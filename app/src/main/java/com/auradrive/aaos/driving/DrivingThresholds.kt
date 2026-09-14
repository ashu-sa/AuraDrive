package com.auradrive.aaos.driving

data class DrivingThresholds(
    val hardAccelerationG: Float = 4.0f,
    val hardBrakingG: Float = -6.0f,
    val coldCabinCelsius: Float = 16.0f,
    val lowBatteryPercent: Float = 20.0f,
    val lowFuelPercent: Float = 15.0f,
    val longDriveMinutes: Long = 60
)
