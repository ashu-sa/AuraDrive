package com.auradrive.aaos.di

import android.content.Context
import androidx.room.Room
import com.auradrive.aaos.data.local.dao.SessionDao
import com.auradrive.aaos.data.local.dao.TripDao
import com.auradrive.aaos.data.local.db.AuraDatabase
import com.auradrive.aaos.data.repository.TripRepository
import com.auradrive.aaos.data.repository.TripRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {
    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraDatabase {
        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            "aura-db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideSessionDao(database: AuraDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideTripDao(database: AuraDatabase): TripDao {
        return database.tripDao()
    }
}
