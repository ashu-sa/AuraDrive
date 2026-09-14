package com.auradrive.aaos.roast

import com.auradrive.aaos.model.DriverPersonality
import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.Roast
import com.auradrive.aaos.model.RoastPriority

class RoastRepository {
    private val roasts = mapOf(
        "HardAcceleration" to listOf(
            "Okay Verstappen, relax.",
            "Petrol was apparently free.",
            "Bro thinks this is F1.",
            "My neck actually hurt from that.",
            "Are we late for your own funeral?"
        ),
        "HardBraking" to listOf(
            "That brake was personal.",
            "Bro discovered the brake pedal.",
            "You good?",
            "ABS exists for a reason, but chill.",
            "My sensors just saw my life flash by."
        ),
        "ColdCabin" to listOf(
            "Again with the Antarctica settings?",
            "Bro is freezing the passengers.",
            "Are we transporting penguins?",
            "I can see my own code freezing."
        ),
        "SmoothDriving" to listOf(
            "Okayyy, we're cooking.",
            "Not gonna lie, that was clean.",
            "Aura +1000 for that move.",
            "Chef's kiss on that turn."
        ),
        "LowBattery" to listOf(
            "Pushing it, aren't we?",
            "Living life on the edge of a tow truck.",
            "Charge me up, bro. I'm fading."
        )
    )

    fun getRoast(event: DrivingEvent, personality: DriverPersonality): Roast {
        val type = event::class.simpleName ?: "Unknown"
        val variants = roasts[type] ?: listOf("Interesting move.")
        val text = variants.random()
        
        return Roast(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            priority = when (event) {
                is DrivingEvent.LowBattery, is DrivingEvent.LowFuel -> RoastPriority.SAFETY
                is DrivingEvent.HardBraking -> RoastPriority.MAJOR
                else -> RoastPriority.FUNNY
            },
            personality = personality,
            eventType = type
        )
    }
}
