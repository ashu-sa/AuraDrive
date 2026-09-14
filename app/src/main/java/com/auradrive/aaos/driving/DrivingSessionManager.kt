package com.auradrive.aaos.driving

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DrivingSession
import kotlinx.coroutines.flow.StateFlow

interface DrivingSessionManager {
    val currentSession: StateFlow<DrivingSession?>
    val allSessions: StateFlow<List<DrivingSession>>
    
    fun startSession()
    fun endSession()
    fun recordEvent(event: DrivingEvent)
}
