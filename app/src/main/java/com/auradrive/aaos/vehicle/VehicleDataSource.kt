package com.auradrive.aaos.vehicle

import com.auradrive.aaos.model.VehicleData
import kotlinx.coroutines.flow.Flow

interface VehicleDataSource {
    val vehicleData: Flow<VehicleData>
    
    suspend fun start()
    suspend fun stop()
}
