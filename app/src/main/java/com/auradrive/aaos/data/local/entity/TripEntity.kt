package com.auradrive.aaos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted snapshot of a completed driving trip, used by Antigravity Roast Driver.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val tripId: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationSeconds: Long,
    val maxSpeedKmh: Float,
    val averageSpeedKmh: Float,
    val hardBrakeCount: Int,
    val rapidAccelerationCount: Int,
    val averageHvacTempCelsius: Float,
    val roastText: String? = null,
    val roastTone: String? = null,
    val isRoastGenerated: Boolean = false
)
