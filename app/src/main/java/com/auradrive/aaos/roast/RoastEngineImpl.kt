package com.auradrive.aaos.roast

import com.auradrive.aaos.model.DriverPersonality
import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.Roast
import java.time.Instant

class RoastEngineImpl(
    private val repository: RoastRepository
) : RoastEngine {

    private val lastRoastTimes = mutableMapOf<String, Instant>()
    private val globalCooldownSeconds = 30L
    private var lastGlobalRoastTime = Instant.MIN

    override fun generateRoast(event: DrivingEvent, personality: DriverPersonality): Roast? {
        val now = Instant.now()
        val type = event::class.simpleName ?: ""

        // Global cooldown
        if (now.isBefore(lastGlobalRoastTime.plusSeconds(globalCooldownSeconds))) {
            return null
        }

        // Per-event cooldown (10 minutes for safety events, 2 minutes for funny ones)
        val eventCooldown = if (event is DrivingEvent.LowBattery || event is DrivingEvent.LowFuel) 600L else 120L
        val lastEventTime = lastRoastTimes[type] ?: Instant.MIN
        if (now.isBefore(lastEventTime.plusSeconds(eventCooldown))) {
            return null
        }

        val roast = repository.getRoast(event, personality)
        
        lastGlobalRoastTime = now
        lastRoastTimes[type] = now
        
        return roast
    }
}
