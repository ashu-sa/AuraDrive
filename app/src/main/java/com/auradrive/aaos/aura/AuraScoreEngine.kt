package com.auradrive.aaos.aura

import com.auradrive.aaos.model.AuraScore
import com.auradrive.aaos.model.DrivingEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuraScoreEngine {
    private val _score = MutableStateFlow(AuraScore(1000, 0, 0))
    val score: StateFlow<AuraScore> = _score.asStateFlow()

    fun onEvent(event: DrivingEvent) {
        val delta = when (event) {
            is DrivingEvent.HardAcceleration -> -50
            is DrivingEvent.HardBraking -> -100
            is DrivingEvent.SmoothDriving -> 20
            is DrivingEvent.ColdCabin -> -10
            is DrivingEvent.LongDrive -> 100
            is DrivingEvent.LowBattery -> -200
            is DrivingEvent.LowFuel -> -200
        }
        
        _score.update { 
            it.copy(
                currentAura = it.currentAura + delta,
                tripAuraChange = it.tripAuraChange + delta,
                weeklyAuraChange = it.weeklyAuraChange + delta
            )
        }
    }

    fun resetTrip() {
        _score.update { it.copy(tripAuraChange = 0) }
    }
}
