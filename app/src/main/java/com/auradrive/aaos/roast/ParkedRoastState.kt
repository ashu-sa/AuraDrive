package com.auradrive.aaos.roast

import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.DrivingSession

sealed interface ParkedRoastState {
    object Idle : ParkedRoastState
    data class Intro(val session: DrivingSession) : ParkedRoastState
    data class RevealEvent(val event: DrivingEvent, val index: Int, val total: Int) : ParkedRoastState
    data class RevealScore(val finalScore: Int, val auraDelta: Int) : ParkedRoastState
    data class RevealPersonality(val personality: String) : ParkedRoastState
    object Swiping : ParkedRoastState
    object Complete : ParkedRoastState
}
