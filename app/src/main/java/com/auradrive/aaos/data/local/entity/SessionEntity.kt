package com.auradrive.aaos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auradrive.aaos.model.DriverPersonality

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long?,
    val distanceMeters: Float,
    val auraChange: Int,
    val score: Int,
    val personality: String,
    val eventsJson: String // Serialized DrivingEvent list
)
