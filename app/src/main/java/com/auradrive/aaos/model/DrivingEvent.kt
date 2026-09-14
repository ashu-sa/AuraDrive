package com.auradrive.aaos.model

import java.time.Instant

sealed class DrivingEvent {
    abstract val timestamp: Instant

    data class HardAcceleration(override val timestamp: Instant, val gForce: Float) : DrivingEvent()
    data class HardBraking(override val timestamp: Instant, val gForce: Float) : DrivingEvent()
    data class SmoothDriving(override val timestamp: Instant) : DrivingEvent()
    data class ColdCabin(override val timestamp: Instant, val temperature: Float) : DrivingEvent()
    data class LongDrive(override val timestamp: Instant, val durationMinutes: Long) : DrivingEvent()
    data class LowFuel(override val timestamp: Instant, val percent: Float) : DrivingEvent()
    data class LowBattery(override val timestamp: Instant, val percent: Float) : DrivingEvent()
}
