package com.auradrive.aaos.telemetry

import com.auradrive.aaos.data.local.entity.TripEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class LiveTripSnapshot(
    val isActive: Boolean = false,
    val currentSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val averageSpeedKmh: Float = 0f,
    val hardBrakeCount: Int = 0,
    val rapidAccelerationCount: Int = 0,
    val currentHvacTempCelsius: Float = 21.0f,
    val averageHvacTempCelsius: Float = 21.0f
)

@Singleton
class TripAggregator @Inject constructor() {

    private val mutex = Mutex()

    private var currentTripId: String? = null
    private var startTimeMs: Long = 0L

    // Speed metrics
    private var maxSpeedMps: Float = 0f
    private var speedSumMps: Double = 0.0
    private var speedSampleCount: Long = 0

    // Pedal & Event metrics
    private var hardBrakes: Int = 0
    private var lastHardBrakeTimestamp: Long = 0L
    private var rapidAccels: Int = 0
    private var lastRapidAccelTimestamp: Long = 0L

    // HVAC metrics
    private var hvacTempSum: Double = 0.0
    private var hvacSampleCount: Long = 0
    private var latestHvacTemp: Float = 21.0f

    private val _liveSnapshot = MutableStateFlow(LiveTripSnapshot())
    val liveSnapshot: StateFlow<LiveTripSnapshot> = _liveSnapshot.asStateFlow()

    suspend fun startTrip(): String = mutex.withLock {
        val newTripId = UUID.randomUUID().toString()
        currentTripId = newTripId
        startTimeMs = System.currentTimeMillis()

        maxSpeedMps = 0f
        speedSumMps = 0.0
        speedSampleCount = 0

        hardBrakes = 0
        lastHardBrakeTimestamp = 0L
        rapidAccels = 0
        lastRapidAccelTimestamp = 0L

        hvacTempSum = 0.0
        hvacSampleCount = 0

        _liveSnapshot.value = LiveTripSnapshot(
            isActive = true,
            currentHvacTempCelsius = latestHvacTemp,
            averageHvacTempCelsius = latestHvacTemp
        )
        return@withLock newTripId
    }

    suspend fun recordSpeed(speedMps: Float) = mutex.withLock {
        if (currentTripId == null) return@withLock
        val validSpeed = if (speedMps < 0f) 0f else speedMps

        if (validSpeed > maxSpeedMps) {
            maxSpeedMps = validSpeed
        }
        speedSumMps += validSpeed
        speedSampleCount++

        val currentKmh = validSpeed * 3.6f
        val maxKmh = maxSpeedMps * 3.6f
        val avgKmh = ((speedSumMps / speedSampleCount) * 3.6).toFloat()

        _liveSnapshot.value = _liveSnapshot.value.copy(
            currentSpeedKmh = currentKmh,
            maxSpeedKmh = maxKmh,
            averageSpeedKmh = avgKmh
        )
    }

    suspend fun recordBrakePosition(percentage: Float) = mutex.withLock {
        if (currentTripId == null) return@withLock
        val now = System.currentTimeMillis()
        // Threshold: Brake pressed > 70% with 1.5s debounce to avoid micro-count chatter
        if (percentage >= 0.70f && (now - lastHardBrakeTimestamp > 1500L)) {
            hardBrakes++
            lastHardBrakeTimestamp = now
            _liveSnapshot.value = _liveSnapshot.value.copy(hardBrakeCount = hardBrakes)
        }
    }

    suspend fun recordAcceleratorPosition(percentage: Float) = mutex.withLock {
        if (currentTripId == null) return@withLock
        val now = System.currentTimeMillis()
        // Threshold: Gas pedal stomped > 85% with 2s debounce
        if (percentage >= 0.85f && (now - lastRapidAccelTimestamp > 2000L)) {
            rapidAccels++
            lastRapidAccelTimestamp = now
            _liveSnapshot.value = _liveSnapshot.value.copy(rapidAccelerationCount = rapidAccels)
        }
    }

    suspend fun recordHvacTemperature(tempCelsius: Float) = mutex.withLock {
        latestHvacTemp = tempCelsius
        if (currentTripId == null) return@withLock

        hvacTempSum += tempCelsius
        hvacSampleCount++

        val avgTemp = (hvacTempSum / hvacSampleCount).toFloat()
        _liveSnapshot.value = _liveSnapshot.value.copy(
            currentHvacTempCelsius = tempCelsius,
            averageHvacTempCelsius = avgTemp
        )
    }

    suspend fun finishTrip(): TripEntity? = mutex.withLock {
        val tripId = currentTripId ?: return@withLock null
        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - startTimeMs) / 1000L).coerceAtLeast(1L)

        val avgSpeedKmh = if (speedSampleCount > 0) {
            ((speedSumMps / speedSampleCount) * 3.6).toFloat()
        } else 0f

        val avgHvac = if (hvacSampleCount > 0) {
            (hvacTempSum / hvacSampleCount).toFloat()
        } else latestHvacTemp

        val entity = TripEntity(
            tripId = tripId,
            startTimeEpochMs = startTimeMs,
            endTimeEpochMs = endTime,
            durationSeconds = durationSec,
            maxSpeedKmh = maxSpeedMps * 3.6f,
            averageSpeedKmh = avgSpeedKmh,
            hardBrakeCount = hardBrakes,
            rapidAccelerationCount = rapidAccels,
            averageHvacTempCelsius = avgHvac,
            isRoastGenerated = false
        )

        // Reset state
        currentTripId = null
        _liveSnapshot.value = LiveTripSnapshot(isActive = false)

        return@withLock entity
    }
}
