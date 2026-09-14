package com.auradrive.aaos.telemetry

import com.auradrive.aaos.data.local.entity.TripEntity
import com.auradrive.aaos.data.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * In-memory test fake for TripRepository to verify data persistence.
 */
class FakeTripRepository : TripRepository {
    private val trips = MutableStateFlow<List<TripEntity>>(emptyList())

    val currentList: List<TripEntity>
        get() = trips.value

    override fun getLatestTrip(): Flow<TripEntity?> {
        return trips.map { it.maxByOrNull { trip -> trip.endTimeEpochMs } }
    }

    override fun getAllTrips(): Flow<List<TripEntity>> = trips.asStateFlow()

    override suspend fun saveTrip(trip: TripEntity): Long {
        trips.value = trips.value + trip
        return trips.value.size.toLong()
    }

    override suspend fun attachRoast(tripId: String, roast: String, tone: String) {
        trips.value = trips.value.map {
            if (it.tripId == tripId) {
                it.copy(roastText = roast, roastTone = tone, isRoastGenerated = true)
            } else it
        }
    }
}

/**
 * End-to-end integration test simulating the CarTelemetry ingestion pipeline:
 * CarService/VHAL events -> TripAggregator -> Persistence -> Roasting
 */
class CarTelemetryPipelineTest {

    private lateinit var tripAggregator: TripAggregator
    private lateinit var fakeRepository: FakeTripRepository

    @Before
    fun setUp() {
        tripAggregator = TripAggregator()
        fakeRepository = FakeTripRepository()
    }

    @Test
    fun `simulated real-car driving sequence from PARK to DRIVE to PARK saves trip accurately`() = runTest {
        // --- 1. Vehicle is initially parked in driveway ---
        // Shifting from PARK (4) to DRIVE (8) triggers trip start
        val currentTripId = tripAggregator.startTrip()
        assertNotNull(currentTripId)

        // --- 2. Driving telemetry arrives from CarPropertyService ---
        // Accelerating out of neighborhood
        tripAggregator.recordSpeed(8.33f)  // ~30 km/h
        tripAggregator.recordSpeed(13.88f) // ~50 km/h
        tripAggregator.recordHvacTemperature(19.5f) // Set AC to 19.5°C

        // Entering highway: Driver floors accelerator (92% pedal position)
        tripAggregator.recordAcceleratorPosition(0.92f)
        tripAggregator.recordSpeed(33.33f) // ~120 km/h
        tripAggregator.recordSpeed(38.88f) // ~140 km/h (speed spike!)

        // Traffic slowdown: Hard brake applied (82% pedal position)
        tripAggregator.recordBrakePosition(0.82f)
        tripAggregator.recordSpeed(15f) // down to ~54 km/h

        // Driver arrives at destination
        tripAggregator.recordSpeed(0f)

        // --- 3. Vehicle shifts back to GEAR_PARK (4) ---
        val finalizedTrip = tripAggregator.finishTrip()
        assertNotNull("Trip must finalize when shifting to PARK", finalizedTrip)

        // Persist to repository
        fakeRepository.saveTrip(finalizedTrip!!)

        // Verify saved trip
        val allTrips = fakeRepository.currentList
        assertEquals(1, allTrips.size)

        val savedTrip = allTrips[0]
        assertEquals(currentTripId, savedTrip.tripId)
        assertTrue("Max speed should capture 140 km/h peak", savedTrip.maxSpeedKmh >= 139.9f)
        assertEquals("Hard brake count should be 1", 1, savedTrip.hardBrakeCount)
        assertEquals("Rapid launch count should be 1", 1, savedTrip.rapidAccelerationCount)
        assertEquals("Average HVAC temp should be 19.5°C", 19.5f, savedTrip.averageHvacTempCelsius, 0.1f)
    }

    @Test
    fun `degraded sensor scenario - missing brake sensor still completes and stores trip`() = runTest {
        // Vehicle shifts into DRIVE
        val tripId = tripAggregator.startTrip()

        // Car sends speed and accelerator, but Brake sensor triggers onErrorEvent / is absent
        tripAggregator.recordSpeed(16.66f) // 60 km/h
        tripAggregator.recordSpeed(22.22f) // 80 km/h
        tripAggregator.recordAcceleratorPosition(0.50f)
        tripAggregator.recordHvacTemperature(21.0f)

        // No brake data sent because sensor is degraded
        val trip = tripAggregator.finishTrip()
        assertNotNull(trip)
        fakeRepository.saveTrip(trip!!)

        val savedTrip = fakeRepository.currentList.first()
        assertEquals(tripId, savedTrip.tripId)
        assertEquals(0, savedTrip.hardBrakeCount)
        assertTrue(savedTrip.maxSpeedKmh >= 79.9f)
    }

    @Test
    fun `attaching post-trip roast updates the persisted trip entity`() = runTest {
        val trip = TripEntity(
            tripId = "trip-final-1",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 50000L,
            durationSeconds = 49L,
            maxSpeedKmh = 142f,
            averageSpeedKmh = 85f,
            hardBrakeCount = 3,
            rapidAccelerationCount = 2,
            averageHvacTempCelsius = 16.5f,
            isRoastGenerated = false
        )

        fakeRepository.saveTrip(trip)

        // Simulate RoastGenerationWorker completing analysis
        fakeRepository.attachRoast(
            tripId = "trip-final-1",
            roast = "Did you think the accelerator was an elevator button to the moon? Max speed was 142 km/h!",
            tone = "BRUTAL"
        )

        val updatedTrip = fakeRepository.currentList.first()
        assertTrue(updatedTrip.isRoastGenerated)
        assertEquals("BRUTAL", updatedTrip.roastTone)
        assertNotNull(updatedTrip.roastText)
        assertTrue(updatedTrip.roastText!!.contains("elevator button to the moon"))
    }
}
