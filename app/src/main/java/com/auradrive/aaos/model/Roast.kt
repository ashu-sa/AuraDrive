package com.auradrive.aaos.model

data class Roast(
    val id: String,
    val text: String,
    val priority: RoastPriority,
    val personality: DriverPersonality,
    val eventType: String
)

enum class RoastPriority {
    CRITICAL,
    SAFETY,
    MAJOR,
    FUNNY,
    MINOR
}
