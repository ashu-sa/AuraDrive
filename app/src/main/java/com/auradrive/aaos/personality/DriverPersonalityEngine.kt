package com.auradrive.aaos.personality

import com.auradrive.aaos.model.DriverPersonality
import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DrivingSession

class DriverPersonalityEngine {

    fun calculatePersonality(session: DrivingSession): DriverPersonality {
        val eventCounts = session.events.groupingBy { it::class }.eachCount()
        
        val hardBrakes = eventCounts[DrivingEvent.HardBraking::class] ?: 0
        val hardAccels = eventCounts[DrivingEvent.HardAcceleration::class] ?: 0
        val coldCabin = eventCounts[DrivingEvent.ColdCabin::class] ?: 0
        
        return when {
            hardBrakes > 5 && hardAccels > 5 -> DriverPersonality.CHAOS_MERCHANT
            hardAccels > 8 -> DriverPersonality.F1_WANNABE
            coldCabin > 3 -> DriverPersonality.ANTARCTICA_CEO
            hardBrakes == 0 && hardAccels < 2 -> DriverPersonality.AURA_FARMER
            session.distanceMeters > 50000 -> DriverPersonality.NPC // Long distance, boring NPC mode
            else -> DriverPersonality.CHILL_DRIVER
        }
    }
}
