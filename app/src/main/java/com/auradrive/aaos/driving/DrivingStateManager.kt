package com.auradrive.aaos.driving

import kotlinx.coroutines.flow.StateFlow

interface DrivingStateManager {
    val currentState: StateFlow<DrivingState>
}
