package com.auradrive.aaos.achievement

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DrivingSession

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false
)

class AchievementEngine {
    
    fun evaluateAchievements(session: DrivingSession): List<Achievement> {
        val eventCounts = session.events.groupingBy { it::class }.eachCount()
        val achievements = mutableListOf<Achievement>()

        if ((eventCounts[DrivingEvent.HardBraking::class] ?: 0) >= 7) {
            achievements.add(Achievement("brake_criminal", "BRAKE CRIMINAL", "Threshold deceleration detected multiple times.", "🛑"))
        }

        if ((eventCounts[DrivingEvent.ColdCabin::class] ?: 0) >= 3) {
            achievements.add(Achievement("antarctica_ceo", "ANTARCTICA CEO", "Passenger comfort was never an option.", "🥶"))
        }
        
        if (session.score > 90) {
            achievements.add(Achievement("aura_farmer", "AURA FARMER", "Locked in. Maximum charisma maintained.", "👑"))
        }

        return achievements
    }
}
