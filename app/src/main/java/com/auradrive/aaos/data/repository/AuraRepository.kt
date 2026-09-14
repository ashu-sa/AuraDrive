package com.auradrive.aaos.data.repository

import com.auradrive.aaos.data.local.dao.SessionDao
import com.auradrive.aaos.data.local.entity.SessionEntity
import com.auradrive.aaos.model.DrivingSession
import com.auradrive.aaos.model.DriverPersonality
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class AuraRepository(
    private val sessionDao: SessionDao,
    private val gson: Gson
) {
    val allSessions: Flow<List<DrivingSession>> = sessionDao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveSession(session: DrivingSession) {
        sessionDao.insertSession(session.toEntity())
    }

    private fun SessionEntity.toDomain(): DrivingSession {
        return DrivingSession(
            id = id,
            startTime = Instant.ofEpochMilli(startTime),
            endTime = endTime?.let { Instant.ofEpochMilli(it) },
            distanceMeters = distanceMeters,
            events = emptyList(), 
            auraChange = auraChange,
            score = score,
            personality = try { DriverPersonality.valueOf(personality) } catch(e: Exception) { DriverPersonality.CHILL_DRIVER }
        )
    }

    private fun DrivingSession.toEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            startTime = startTime.toEpochMilli(),
            endTime = endTime?.toEpochMilli(),
            distanceMeters = distanceMeters,
            auraChange = auraChange,
            score = score,
            personality = personality.name,
            eventsJson = gson.toJson(events)
        )
    }
}
