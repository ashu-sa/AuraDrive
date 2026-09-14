package com.auradrive.aaos.telemetry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripAggregatorTest {

    private lateinit var aggregator: TripAggregator

    @Before
    fun setUp() {
        aggregator = TripAggregator()
    }

    @Test
    fun `when no trip active, recording data does not affect snapshot or crash`() = runTest {
        aggregator.recordSpeed(25f) // 90 km/h
        aggregator.recordBrakePosition(0.85f)
        aggregator.recordAcceleratorPosition(0.95f)
        aggregator.recordHvacTemperature(16.5f)

        val snapshot = aggregator.liveSnapshot.value
        assertFalse(snapshot.isActive)
        assertEquals(0f, snapshot.maxSpeedKmh, 0.01f)
        assertEquals(0, snapshot.hardBrakeCount)
        assertEquals(0, snapshot.rapidAccelerationCount)

        val finishedTrip = aggregator.finishTrip()
        assertNull("Finish trip should return null when no trip was active", finishedTrip)
    }

    @Test
    fun `speed aggregation computes max speed and running average accurately`() = runTest {
        aggregator.startTrip()

        // Speeds in m/s:
        // 10 m/s = 36 km/h
        // 20 m/s = 72 km/h
        // 30 m/s = 108 km/h
        // 40 m/s = 144 km/h (speeding!)
        aggregator.recordSpeed(10f)
        aggregator.recordSpeed(20f)
        aggregator.recordSpeed(30f)
        aggregator.recordSpeed(40f)

        val snapshot = aggregator.liveSnapshot.value
        assertTrue(snapshot.isActive)
        assertEquals(144f, snapshot.currentSpeedKmh, 0.1f)
        assertEquals(144f, snapshot.maxSpeedKmh, 0.1f)
        // Average of 10, 20, 30, 40 is 25 m/s = 90 km/h
        assertEquals(90f, snapshot.averageSpeedKmh, 0.1f)
    }

    @Test
    fun `hard brake detection triggers on threshold and enforces debounce window`() = runTest {
        aggregator.startTrip()

        // 1. Normal gentle braking (30%) - should NOT trigger hard brake
        aggregator.recordBrakePosition(0.30f)
        assertEquals(0, aggregator.liveSnapshot.value.hardBrakeCount)

        // 2. Hard slam on brake (80%) - should trigger hard brake #1
        aggregator.recordBrakePosition(0.80f)
        assertEquals(1, aggregator.liveSnapshot.value.hardBrakeCount)

        // 3. Immediate jitter/chatter within 500ms (90%) - should be debounced
        aggregator.recordBrakePosition(0.90f)
        assertEquals("Subsequent brake events within 1.5s must be debounced", 1, aggregator.liveSnapshot.value.hardBrakeCount)
    }

    @Test
    fun `rapid acceleration detection triggers on threshold and debounces`() = runTest {
        aggregator.startTrip()

        // Normal gradual pedal (40%) - no event
        aggregator.recordAcceleratorPosition(0.40f)
        assertEquals(0, aggregator.liveSnapshot.value.rapidAccelerationCount)

        // Floor the pedal (95%) - triggers rapid accel #1
        aggregator.recordAcceleratorPosition(0.95f)
        assertEquals(1, aggregator.liveSnapshot.value.rapidAccelerationCount)

        // Immediate subsequent reading (90%) - should debounce
        aggregator.recordAcceleratorPosition(0.90f)
        assertEquals(1, aggregator.liveSnapshot.value.rapidAccelerationCount)
    }

    @Test
    fun `HVAC temperature updates compute correct running average`() = runTest {
        aggregator.startTrip()

        aggregator.recordHvacTemperature(16.0f) // Freezing
        aggregator.recordHvacTemperature(18.0f)
        aggregator.recordHvacTemperature(20.0f)

        // Average of 16, 18, 20 = 18.0°C
        val snapshot = aggregator.liveSnapshot.value
        assertEquals(20.0f, snapshot.currentHvacTempCelsius, 0.1f)
        assertEquals(18.0f, snapshot.averageHvacTempCelsius, 0.1f)
    }

    @Test
    fun `full trip lifecycle accurately aggregates and produces TripEntity`() = runTest {
        val tripId = aggregator.startTrip()
        assertNotNull(tripId)

        // Vehicle drives with various inputs
        aggregator.recordSpeed(15f) // 54 km/h
        aggregator.recordSpeed(35f) // 126 km/h
        aggregator.recordBrakePosition(0.75f) // Hard brake #1
        aggregator.recordAcceleratorPosition(0.88f) // Rapid launch #1
        aggregator.recordHvacTemperature(17.0f)

        val trip = aggregator.finishTrip()
        assertNotNull(trip)
        assertEquals(tripId, trip!!.tripId)
        assertEquals(126f, trip.maxSpeedKmh, 0.1f)
        assertEquals(90f, trip.averageSpeedKmh, 0.1f)
        assertEquals(1, trip.hardBrakeCount)
        assertEquals(1, trip.rapidAccelerationCount)
        assertEquals(17.0f, trip.averageHvacTempCelsius, 0.1f)
        assertFalse("Live snapshot should be inactive after trip finishes", aggregator.liveSnapshot.value.isActive)
    }

    @Test
    fun `thread safety test - simultaneous multi-threaded telemetry updates do not race`() = runTest {
        aggregator.startTrip()

        // Launch 100 concurrent coroutines simulating bursts from multiple Binder IPC threads
        val jobs = List(100) { index ->
            launch(Dispatchers.Default) {
                aggregator.recordSpeed(20f + (index % 10))
                aggregator.recordHvacTemperature(19f + (index % 5))
            }
        }
        jobs.joinAll()

        val snapshot = aggregator.liveSnapshot.value
        assertTrue("Snapshot should remain active and coherent", snapshot.isActive)
        assertTrue("Max speed should be greater than 20 km/h", snapshot.maxSpeedKmh > 72f)
        assertTrue("Average speed should be non-zero", snapshot.averageSpeedKmh > 0f)

        val finalized = aggregator.finishTrip()
        assertNotNull(finalized)
        assertEquals(0, aggregator.finishTrip()?.durationSeconds ?: 0)
    }
}
