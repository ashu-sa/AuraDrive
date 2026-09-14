package com.auradrive.aaos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.auradrive.aaos.data.local.entity.TripEntity
import com.auradrive.aaos.data.repository.TripRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
class RoastGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TripRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tripId = inputData.getString(KEY_TRIP_ID) ?: return@withContext Result.failure()

        val allTrips = repository.getAllTrips().firstOrNull() ?: return@withContext Result.retry()
        val trip = allTrips.firstOrNull { it.tripId == tripId } ?: return@withContext Result.retry()

        val (roastText, roastTone) = generateRoast(trip)
        repository.attachRoast(trip.tripId, roastText, roastTone)

        return@withContext Result.success()
    }

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

    companion object {
        const val KEY_TRIP_ID = "key_trip_id"
    }
}
