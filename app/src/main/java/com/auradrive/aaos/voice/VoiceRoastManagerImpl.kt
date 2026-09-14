package com.auradrive.aaos.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.auradrive.aaos.model.Roast
import java.util.Locale

class VoiceRoastManagerImpl(private val context: Context) : VoiceRoastManager, TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    override var isMuted: Boolean = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
        }
    }

    override fun speak(roast: Roast) {
        if (!isMuted && isInitialized) {
            tts?.speak(roast.text, TextToSpeech.QUEUE_FLUSH, null, roast.id)
        }
    }

    override fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.shutdown()
        tts = null
    }
}
