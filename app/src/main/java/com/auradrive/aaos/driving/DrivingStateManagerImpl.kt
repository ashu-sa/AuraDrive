package com.auradrive.aaos.driving

import com.auradrive.aaos.model.VehicleGear
import com.auradrive.aaos.vehicle.VehicleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

class DrivingStateManagerImpl(
    vehicleDataSource: VehicleDataSource,
    scope: CoroutineScope
) : DrivingStateManager {

    override val currentState: StateFlow<DrivingState> = vehicleDataSource.vehicleData
        .scan(DrivingState.UNKNOWN) { lastState, data ->
            when (data.gear) {
                VehicleGear.PARK -> DrivingState.PARKED
                VehicleGear.REVERSE -> DrivingState.REVERSE
                VehicleGear.DRIVE -> {
                    if (data.speedMps > 0.1f) {
                        DrivingState.DRIVING
                    } else {
                        // If we are in DRIVE but not moving:
                        // 1. If we were already DRIVING, we are likely stopped at a light. Stay DRIVING.
                        // 2. If we were PARKED/UNKNOWN, we haven't started moving yet. Stay PARKED.
                        if (lastState == DrivingState.DRIVING) DrivingState.DRIVING else DrivingState.PARKED
                    }
                }
                else -> DrivingState.UNKNOWN
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = DrivingState.UNKNOWN
        )
}
