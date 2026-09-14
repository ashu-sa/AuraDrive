package com.auradrive.aaos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.auradrive.aaos.data.local.dao.SessionDao
import com.auradrive.aaos.data.local.dao.TripDao
import com.auradrive.aaos.data.local.entity.SessionEntity
import com.auradrive.aaos.data.local.entity.TripEntity

@Database(entities = [SessionEntity::class, TripEntity::class], version = 2, exportSchema = false)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun tripDao(): TripDao
}
