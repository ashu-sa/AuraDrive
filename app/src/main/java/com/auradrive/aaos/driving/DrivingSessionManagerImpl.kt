package com.auradrive.aaos.driving

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DrivingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID

class DrivingSessionManagerImpl(
    private val repository: com.auradrive.aaos.data.repository.AuraRepository? = null
) : DrivingSessionManager {
    private val _currentSession = MutableStateFlow<DrivingSession?>(null)
    override val currentSession: StateFlow<DrivingSession?> = _currentSession.asStateFlow()

    private val _allSessions = MutableStateFlow<List<DrivingSession>>(emptyList())
    override val allSessions: StateFlow<List<DrivingSession>> = _allSessions.asStateFlow()

    override fun startSession() {
        _currentSession.value = DrivingSession(
            id = UUID.randomUUID().toString(),
            startTime = Instant.now()
        )
    }

    override fun endSession() {
        _currentSession.value?.let { session ->
            val completedSession = session.copy(endTime = Instant.now())
            _allSessions.update { it + completedSession }
            // Save to repository in a background process if needed
            // For prototype, we'll assume it's handled or we'd need a scope here.
            _currentSession.value = null
        }
    }

    override fun recordEvent(event: DrivingEvent) {
        _currentSession.update { session ->
            session?.copy(events = session.events + event)
        }
    }
}
