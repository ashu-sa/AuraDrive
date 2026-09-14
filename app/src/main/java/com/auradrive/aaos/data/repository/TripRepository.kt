package com.auradrive.aaos.data.repository

import com.auradrive.aaos.data.local.dao.TripDao
import com.auradrive.aaos.data.local.entity.TripEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface TripRepository {
    fun getLatestTrip(): Flow<TripEntity?>
    fun getAllTrips(): Flow<List<TripEntity>>
    suspend fun saveTrip(trip: TripEntity): Long
    suspend fun attachRoast(tripId: String, roast: String, tone: String)
}

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TripRepository {

    override fun getLatestTrip(): Flow<TripEntity?> = tripDao.getLatestTrip()

    override fun getAllTrips(): Flow<List<TripEntity>> = tripDao.getAllTrips()

    override suspend fun saveTrip(trip: TripEntity): Long = withContext(ioDispatcher) {
        tripDao.insertTrip(trip)
    }

    override suspend fun attachRoast(tripId: String, roast: String, tone: String) = withContext(ioDispatcher) {
        tripDao.attachRoast(tripId, roast, tone)
    }
}
