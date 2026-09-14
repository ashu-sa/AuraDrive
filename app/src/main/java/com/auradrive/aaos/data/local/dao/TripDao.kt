package com.auradrive.aaos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.auradrive.aaos.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE tripId = :tripId LIMIT 1")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("SELECT * FROM trips ORDER BY endTimeEpochMs DESC LIMIT 1")
    fun getLatestTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips ORDER BY endTimeEpochMs DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("UPDATE trips SET roastText = :roast, roastTone = :tone, isRoastGenerated = 1 WHERE tripId = :tripId")
    suspend fun attachRoast(tripId: String, roast: String, tone: String)
}
