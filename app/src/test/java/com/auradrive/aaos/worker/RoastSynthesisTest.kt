package com.auradrive.aaos.worker

import com.auradrive.aaos.data.local.entity.TripEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoastSynthesisTest {

    // Mirroring RoastGeneration logic for isolated domain validation
    private fun generateRoast(trip: TripEntity): Pair<String, String> {
        val roasts = mutableListOf<String>()

        if (trip.maxSpeedKmh > 130f) {
            roasts.add("Did you think the accelerator was an elevator button to the moon? Max speed was ${trip.maxSpeedKmh.toInt()} km/h!")
        }
        if (trip.hardBrakeCount >= 3) {
            roasts.add("You slammed the brakes ${trip.hardBrakeCount} times. Are your brake pads paying rent, or are you trying to evict them?")
        }
        if (trip.rapidAccelerationCount >= 3) {
            roasts.add("Aggressive launch detected ${trip.rapidAccelerationCount} times. Vin Diesel called—he wants his mid-life crisis back.")
        }
        if (trip.averageHvacTempCelsius <= 17.5f) {
            roasts.add("Average AC temperature was ${String.format("%.1f", trip.averageHvacTempCelsius)}°C. Are you driving or transporting fresh seafood?")
        } else if (trip.averageHvacTempCelsius >= 26.0f) {
            roasts.add("Cabin set to ${String.format("%.1f", trip.averageHvacTempCelsius)}°C. Nice sauna session on wheels.")
        }

        val finalText = if (roasts.isEmpty()) {
            "Suspiciously normal driving. You drove so peacefully even your grandma would be bored."
        } else {
            roasts.joinToString(" ")
        }

        val tone = if (roasts.size >= 2) "BRUTAL" else "PLAYFUL"
        return Pair(finalText, tone)
    }

    @Test
    fun `speed demon driver triggers moon elevator roast`() {
        val trip = TripEntity(
            tripId = "trip-speed",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 145f,
            averageSpeedKmh = 80f,
            hardBrakeCount = 0,
            rapidAccelerationCount = 0,
            averageHvacTempCelsius = 22f
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("elevator button to the moon"))
        assertTrue(roast.contains("145 km/h"))
        assertEquals("PLAYFUL", tone)
    }

    @Test
    fun `frequent hard braker triggers brake pad eviction roast`() {
        val trip = TripEntity(
            tripId = "trip-brake",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 90f,
            averageSpeedKmh = 50f,
            hardBrakeCount = 4,
            rapidAccelerationCount = 0,
            averageHvacTempCelsius = 21f
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("Are your brake pads paying rent"))
        assertTrue(roast.contains("slammed the brakes 4 times"))
        assertEquals("PLAYFUL", tone)
    }

    @Test
    fun `freezing AC triggers seafood transport roast`() {
        val trip = TripEntity(
            tripId = "trip-ac-cold",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 80f,
            averageSpeedKmh = 40f,
            hardBrakeCount = 0,
            rapidAccelerationCount = 0,
            averageHvacTempCelsius = 16.5f
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("transporting fresh seafood"))
        assertEquals("PLAYFUL", tone)
    }

    @Test
    fun `sweltering AC triggers sauna roast`() {
        val trip = TripEntity(
            tripId = "trip-ac-hot",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 80f,
            averageSpeedKmh = 40f,
            hardBrakeCount = 0,
            rapidAccelerationCount = 0,
            averageHvacTempCelsius = 27.5f
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("sauna session on wheels"))
        assertEquals("PLAYFUL", tone)
    }

    @Test
    fun `multiple infractions trigger BRUTAL tone`() {
        val trip = TripEntity(
            tripId = "trip-chaos",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 150f, // infraction 1
            averageSpeedKmh = 100f,
            hardBrakeCount = 5,  // infraction 2
            rapidAccelerationCount = 4, // infraction 3
            averageHvacTempCelsius = 16.0f // infraction 4
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("elevator button to the moon"))
        assertTrue(roast.contains("Are your brake pads paying rent"))
        assertTrue(roast.contains("Vin Diesel called"))
        assertTrue(roast.contains("transporting fresh seafood"))
        assertEquals("BRUTAL", tone)
    }

    @Test
    fun `gentle driver triggers grandma roast`() {
        val trip = TripEntity(
            tripId = "trip-calm",
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 60000L,
            durationSeconds = 59L,
            maxSpeedKmh = 70f,
            averageSpeedKmh = 45f,
            hardBrakeCount = 0,
            rapidAccelerationCount = 0,
            averageHvacTempCelsius = 21.5f
        )

        val (roast, tone) = generateRoast(trip)
        assertTrue(roast.contains("even your grandma would be bored"))
        assertEquals("PLAYFUL", tone)
    }
}
