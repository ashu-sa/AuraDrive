package com.auradrive.aaos.model

import java.time.Instant

data class DrivingSession(
    val id: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val distanceMeters: Float = 0f,
    val events: List<DrivingEvent> = emptyList(),
    val auraChange: Int = 0,
    val score: Int = 0,
    val personality: DriverPersonality = DriverPersonality.CHILL_DRIVER
)

enum class DriverPersonality {
    NPC,
    F1_WANNABE,
    CHAOS_MERCHANT,
    CHILL_DRIVER,
    ANTARCTICA_CEO,
    AURA_FARMER
}
