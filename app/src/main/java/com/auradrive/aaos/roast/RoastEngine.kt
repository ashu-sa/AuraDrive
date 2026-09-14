package com.auradrive.aaos.roast

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DriverPersonality
import com.auradrive.aaos.model.Roast

interface RoastEngine {
    fun generateRoast(event: DrivingEvent, personality: DriverPersonality): Roast?
}
