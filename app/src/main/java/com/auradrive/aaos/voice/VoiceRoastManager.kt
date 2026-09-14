package com.auradrive.aaos.voice

import com.auradrive.aaos.model.Roast

interface VoiceRoastManager {
    var isMuted: Boolean
    fun speak(roast: Roast)
    fun stop()
}
