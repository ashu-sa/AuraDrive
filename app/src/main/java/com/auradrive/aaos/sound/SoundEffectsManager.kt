package com.auradrive.aaos.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.auradrive.aaos.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import kotlin.math.*

/**
 * High-fidelity automotive audio & haptics engine for AuraDrive.
 * Generates smooth, luxury electric vehicle (EV) sound effects procedurally via PCM AudioTrack.
 * Completely replaces telecom ToneGenerator beeps with authentic EV propulsion whooshes,
 * regenerative braking glides, precision cockpit switch clicks, and cabin welcome chimes.
 */
class SoundEffectsManager(private val context: Context) {

    var isAudioEnabled: Boolean = true
    private val executor = Executors.newSingleThreadExecutor()

    private val sampleRate = 44100

    // Pre-synthesized AudioTrack instances for instant, zero-latency playback
    private var trackSwipeRight: AudioTrack? = null
    private var trackSwipeLeft: AudioTrack? = null
    private var trackClick: AudioTrack? = null
    private var trackSnapBack: AudioTrack? = null
    private var trackStartup: AudioTrack? = null
    private var trackSuccess: AudioTrack? = null

    // Media player for authentic high-fidelity playback without emulator SoundPool jitter
    private var mediaPlayerFahhh: MediaPlayer? = null
    private var mediaPlayerBruh: MediaPlayer? = null

    // Low-latency SoundPool for authentic recorded "FAHHHH" and "BRUH" meme audio clips
    private var soundPool: SoundPool? = null
    private var soundIdFahhh: Int = 0
    private var soundIdBruh: Int = 0

    init {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // 1. Initialize native MediaPlayers
            mediaPlayerFahhh = MediaPlayer.create(context, R.raw.fahhh)?.apply {
                setAudioAttributes(attrs)
            }
            mediaPlayerBruh = MediaPlayer.create(context, R.raw.bruh)?.apply {
                setAudioAttributes(attrs)
            }

            // 2. Also initialize SoundPool with 48kHz uncompressed audio
            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build()
            soundIdFahhh = soundPool?.load(context, R.raw.fahhh, 1) ?: 0
            soundIdBruh = soundPool?.load(context, R.raw.bruh, 1) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        executor.execute {
            try {
                trackSwipeRight = createStaticTrack(synthesizeFahhhMeme())
                trackSwipeLeft = createStaticTrack(synthesizeEvRegenBrake())
                trackClick = createStaticTrack(synthesizeCockpitSwitch())
                trackSnapBack = createStaticTrack(synthesizeMagneticLatch())
                trackStartup = createStaticTrack(synthesizeCabinWelcomeChime())
                trackSuccess = createStaticTrack(synthesizeVictoryChime())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Authentic "FAHHHH" meme vocal reaction sound effect
     * Played when reacting with "ZEEZ, YOU GOT ME" / Swiping Right
     */
    fun playSwipeRight() {
        if (!isAudioEnabled) return
        executor.execute {
            // First priority: Native MediaPlayer (pure original studio audio, zero emulator robotization)
            try {
                mediaPlayerFahhh?.let { player ->
                    if (player.isPlaying) {
                        player.seekTo(0)
                    }
                    player.start()
                    return@execute
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Second priority: SoundPool with 48kHz uncompressed audio
            if (soundIdFahhh != 0 && soundPool != null) {
                val streamId = soundPool?.play(soundIdFahhh, 1.0f, 1.0f, 1, 0, 1.0f) ?: 0
                if (streamId != 0) return@execute
            }

            // Third priority: Instant on-the-fly MediaPlayer
            try {
                val mp = MediaPlayer.create(context, R.raw.fahhh)
                mp?.setOnCompletionListener { it.release() }
                mp?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        vibrateSmooth(VibeType.ACCELERATION)
    }

    fun playFahhh() {
        playSwipeRight()
    }

    /**
     * Authentic "BRUH" meme vocal reaction sound effect
     * Played when reacting with "NAH, IT AIN'T ME" / Swiping Left
     */
    fun playSwipeLeft() {
        playSwipeRight()
    }

    fun playBruh() {
        playSwipeRight()
    }

    /**
     * Magnetic Console Latch / Docking Thud
     * Played when a card snaps back into the center
     */
    fun playSnapBack() {
        if (!isAudioEnabled) return
        executor.execute {
            playTrack(trackSnapBack)
        }
        vibrateSmooth(VibeType.DOCK)
    }

    /**
     * Precision Automotive Cockpit Capacitive Switch Click
     * High-end damped acoustic click (replaces phone beeps)
     */
    fun playClick() {
        if (!isAudioEnabled) return
        executor.execute {
            playTrack(trackClick)
        }
        vibrateSmooth(VibeType.CLICK)
    }

    /**
     * Luxury EV Cabin Welcome Chime (Warm major chord pad)
     * Played when revealing recap or entering drive modes
     */
    fun playEvStartup() {
        if (!isAudioEnabled) return
        executor.execute {
            playTrack(trackStartup)
        }
        vibrateSmooth(VibeType.CHIME)
    }

    /**
     * EV Victory / Final Report Fanfare
     */
    fun playSuccess() {
        if (!isAudioEnabled) return
        executor.execute {
            playTrack(trackSuccess)
        }
        vibrateSmooth(VibeType.CHIME)
    }

    private fun playTrack(track: AudioTrack?) {
        try {
            track?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.reloadStaticData()
                it.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createStaticTrack(pcmData: ByteArray): AudioTrack {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(pcmData.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcmData, 0, pcmData.size)
        return track
    }

    // ==========================================
    // Procedural Audio Synthesis Waveform Engines
    // ==========================================

    /**
     * Synthesizes the authentic "FAHHHH" meme vocal reaction sound effect:
     * - Initial breathy "fff" fricative burst (0 - 100ms) with bandpass filtered noise at 2600Hz
     * - Expressive comedic vocal pitch contour:
     *     Dual detuned sawtooth vocal cords (~155Hz swells to 175Hz, then cascades down in dramatic sigh to 110Hz -> 82Hz)
     *     Sub-carrier resonance at ~77Hz down to 42Hz
     *     3 resonant acoustic vocal formant filters for open "AH" vowel:
     *       F1: 820Hz (open throat)
     *       F2: 1280Hz
     *       F3: 2750Hz (vocal ring)
     * - Mild comedic microphone saturation / warm distortion
     * - 45ms slapback cockpit delay echo with 28% decay feedback
     */
    private fun synthesizeFahhhMeme(): ByteArray {
        val durationSec = 0.92
        val totalSamples = (sampleRate * durationSec).toInt()
        val floatSamples = DoubleArray(totalSamples)

        // Standard 2nd-order Biquad Bandpass Filter (constant 0 dB peak gain)
        class BiquadBP(freq: Double, q: Double) {
            private val w0 = 2.0 * PI * freq / sampleRate
            private val alpha = sin(w0) / (2.0 * q)
            private val a0 = 1.0 + alpha
            private val b0 = alpha / a0
            private val b1 = 0.0
            private val b2 = -alpha / a0
            private val a1 = (-2.0 * cos(w0)) / a0
            private val a2 = (1.0 - alpha) / a0

            private var x1 = 0.0
            private var x2 = 0.0
            private var y1 = 0.0
            private var y2 = 0.0

            fun filter(x: Double): Double {
                val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
                x2 = x1
                x1 = x
                y2 = y1
                y1 = y
                return y
            }
        }

        val noiseBP = BiquadBP(2600.0, 2.8)
        val f1 = BiquadBP(820.0, 4.5)
        val f2 = BiquadBP(1280.0, 5.5)
        val f3 = BiquadBP(2750.0, 4.0)

        var phase1 = 0.0
        var phase2 = 0.0
        var phaseSub = 0.0
        var noiseState = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate

            // 1. Stage 1: Breath "fff" friction burst (0 to 180ms)
            val white = (Math.random() * 2.0 - 1.0)
            noiseState = 0.90 * noiseState + 0.10 * white
            val filteredNoise = noiseBP.filter(noiseState)
            val noiseEnv = when {
                t < 0.04 -> (t / 0.04) * 0.28
                t < 0.18 -> 0.28 * exp(-12.0 * (t - 0.04))
                else -> 0.0
            }

            val noiseComponent = filteredNoise * noiseEnv

            // 2. Stage 2: Vocal "AHHHHHH" body (starts at ~35ms)
            var vocalComponent = 0.0
            if (t >= 0.035) {
                val vt = t - 0.035

                // Pitch contour:
                // 0 -> 0.09: 155Hz swells to 175Hz
                // 0.09 -> 0.45: drops from 175Hz to 115Hz
                // 0.45 -> 0.88: sigh drops from 115Hz to 82Hz
                val fundamentalFreq = when {
                    vt < 0.09 -> 155.0 + (175.0 - 155.0) * (vt / 0.09)
                    vt < 0.45 -> {
                        val progress = (vt - 0.09) / (0.45 - 0.09)
                        175.0 * exp(-0.419 * progress)
                    }
                    else -> {
                        val progress = ((vt - 0.45) / 0.43).coerceIn(0.0, 1.0)
                        115.0 * exp(-0.338 * progress)
                    }
                }

                val freq1 = fundamentalFreq
                val freq2 = fundamentalFreq + 2.5
                val freqSub = fundamentalFreq * 0.5

                phase1 = (phase1 + 2.0 * PI * freq1 / sampleRate) % (2.0 * PI)
                phase2 = (phase2 + 2.0 * PI * freq2 / sampleRate) % (2.0 * PI)
                phaseSub = (phaseSub + 2.0 * PI * freqSub / sampleRate) % (2.0 * PI)

                // Sawtooth generators: (phase / PI) - 1.0
                val saw1 = (phase1 / PI) - 1.0
                val saw2 = (phase2 / PI) - 1.0
                val triSub = (2.0 / PI) * asin(sin(phaseSub).coerceIn(-1.0, 1.0))

                val rawVoice = (saw1 * 0.42 + saw2 * 0.38 + triSub * 0.20)

                // Soft saturation curve for meme warmth
                val saturated = tanh(rawVoice * 1.8)

                // Formant filters
                val form1 = f1.filter(saturated)
                val form2 = f2.filter(saturated)
                val form3 = f3.filter(saturated)

                val mixedFormants = (form1 * 0.52 + form2 * 0.32 + form3 * 0.16)

                // Vocal gain envelope
                val vocalEnv = when {
                    vt < 0.08 -> (vt / 0.08).pow(1.5) * 0.72
                    vt < 0.35 -> 0.72 - 0.06 * ((vt - 0.08) / 0.27)
                    else -> 0.66 * exp(-5.2 * (vt - 0.35))
                }

                vocalComponent = mixedFormants * vocalEnv
            }

            floatSamples[i] = noiseComponent + vocalComponent
        }

        // 3. Add 45ms cockpit delay echo with 28% feedback
        val delaySamples = (sampleRate * 0.045).toInt()
        val delayFeedback = 0.28
        val finalSamples = DoubleArray(totalSamples)

        for (i in 0 until totalSamples) {
            val dry = floatSamples[i]
            val echo = if (i >= delaySamples) finalSamples[i - delaySamples] * delayFeedback else 0.0
            finalSamples[i] = dry + echo
        }

        // Convert to 16-bit PCM ShortArray
        val shorts = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            shorts[i] = (finalSamples[i].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes an EV acceleration whoosh:
     * - Fundamental frequency smoothly sweeping from 160Hz up to 460Hz
     * - Warm octave overtone at 2x frequency
     * - Soft aerodynamic air flow shimmer (pink noise)
     * - Smooth raised-cosine attack & exponential decay
     */
    private fun synthesizeEvAcceleration(): ByteArray {
        val durationSec = 0.24
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val attackSamples = (sampleRate * 0.030).toInt()
        var noiseState = 0.0

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            // Smooth exponential frequency curve
            val freq = 160.0 + 300.0 * progress.pow(1.35)

            phase1 += 2.0 * PI * freq / sampleRate
            phase2 += 2.0 * PI * (freq * 2.02) / sampleRate

            val tone1 = sin(phase1)
            val tone2 = sin(phase2) * 0.28

            // Soft lowpass filtered aerodynamic wind rush
            val white = (Math.random() * 2.0 - 1.0)
            noiseState = 0.92 * noiseState + 0.08 * white
            val wind = noiseState * 0.18 * (1.0 - progress * 0.5)

            // Envelope
            val env = if (i < attackSamples) {
                sin(0.5 * PI * i / attackSamples).pow(2)
            } else {
                exp(-4.2 * (progress - 0.12).coerceAtLeast(0.0))
            }

            val mixed = (tone1 * 0.65 + tone2 + wind) * env * 0.82
            shorts[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes an EV regenerative deceleration glide:
     * - Fundamental frequency gliding downward from 390Hz down to 135Hz
     * - Warm sub-resonant hum
     * - Silky smooth aerodynamic air release
     */
    private fun synthesizeEvRegenBrake(): ByteArray {
        val durationSec = 0.22
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)
        var phase1 = 0.0
        var phaseSub = 0.0
        val attackSamples = (sampleRate * 0.025).toInt()
        var noiseState = 0.0

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            // Downward pitch glide
            val freq = 390.0 - 255.0 * progress.pow(0.85)

            phase1 += 2.0 * PI * freq / sampleRate
            phaseSub += 2.0 * PI * (freq * 0.5) / sampleRate

            val tone1 = sin(phase1)
            val sub = sin(phaseSub) * 0.32

            val white = (Math.random() * 2.0 - 1.0)
            noiseState = 0.88 * noiseState + 0.12 * white
            val wind = noiseState * 0.14 * (1.0 - progress)

            val env = if (i < attackSamples) {
                sin(0.5 * PI * i / attackSamples).pow(2)
            } else {
                exp(-4.6 * (progress - 0.10).coerceAtLeast(0.0))
            }

            val mixed = (tone1 * 0.62 + sub + wind) * env * 0.82
            shorts[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes a luxury automotive cockpit tactile switch:
     * Ultra-short transient acoustic click with damped glass/milled switch body (zero buzz/beep).
     */
    private fun synthesizeCockpitSwitch(): ByteArray {
        val durationSec = 0.032
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // High micro-ping for crisp tactile response
            val highPing = sin(2.0 * PI * 1420.0 * t) * exp(-t / 0.0035)
            // Low chassis resonance for warm automotive tactile feel
            val lowBody = sin(2.0 * PI * 220.0 * t) * exp(-t / 0.0080)

            val mixed = (highPing * 0.6 + lowBody * 0.4) * 0.85
            shorts[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes a magnetic console latch dock:
     * Dual damped micro-pulses simulating precision mechanical latching.
     */
    private fun synthesizeMagneticLatch(): ByteArray {
        val durationSec = 0.060
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Primary pulse
            val p1 = sin(2.0 * PI * 170.0 * t) * exp(-t / 0.007)
            // Secondary rebound pulse at 13ms
            val t2 = t - 0.013
            val p2 = if (t2 >= 0) sin(2.0 * PI * 260.0 * t2) * exp(-t2 / 0.005) * 0.5 else 0.0

            val mixed = (p1 * 0.7 + p2) * 0.8
            shorts[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes a luxury EV cabin welcome chime:
     * Peaceful C5 + E5 + G5 harmonic chord with warm bell decay.
     */
    private fun synthesizeCabinWelcomeChime(): ByteArray {
        val durationSec = 0.45
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)

        val fC5 = 523.25
        val fE5 = 659.25
        val fG5 = 783.99

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec

            val env1 = exp(-4.0 * progress)
            val env2 = exp(-4.6 * progress)
            val env3 = exp(-5.0 * progress)

            val note1 = sin(2.0 * PI * fC5 * t) * env1
            val note2 = sin(2.0 * PI * fE5 * t) * env2
            val note3 = sin(2.0 * PI * fG5 * t) * env3

            val mixed = (note1 * 0.42 + note2 * 0.34 + note3 * 0.24) * 0.8
            shorts[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    /**
     * Synthesizes an EV victory/summary arpeggio:
     * Rapid futuristic ascending notes (C5 -> E5 -> G5 -> C6).
     */
    private fun synthesizeVictoryChime(): ByteArray {
        val durationSec = 0.50
        val totalSamples = (sampleRate * durationSec).toInt()
        val shorts = ShortArray(totalSamples)

        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteStarts = doubleArrayOf(0.0, 0.07, 0.14, 0.22)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            var sum = 0.0

            for (n in freqs.indices) {
                val dt = t - noteStarts[n]
                if (dt >= 0) {
                    val noteEnv = exp(-7.0 * dt)
                    sum += sin(2.0 * PI * freqs[n] * dt) * noteEnv * 0.35
                }
            }
            shorts[i] = (sum.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return shortsToBytes(shorts)
    }

    private fun shortsToBytes(shorts: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shorts.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asShortBuffer().put(shorts)
        return byteBuffer.array()
    }

    // ==========================================
    // Refined Automotive Haptics
    // ==========================================

    private enum class VibeType {
        CLICK, DOCK, ACCELERATION, REGEN, CHIME
    }

    private fun vibrateSmooth(type: VibeType) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    VibeType.CLICK -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    VibeType.DOCK -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    VibeType.ACCELERATION -> {
                        val timings = longArrayOf(0, 16, 25, 22)
                        val amps = intArrayOf(0, 140, 0, 220)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
                    }
                    VibeType.REGEN -> {
                        val timings = longArrayOf(0, 20, 20, 14)
                        val amps = intArrayOf(0, 200, 0, 120)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
                    }
                    VibeType.CHIME -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = when (type) {
                    VibeType.CLICK -> longArrayOf(0, 12)
                    VibeType.DOCK -> longArrayOf(0, 8)
                    VibeType.ACCELERATION -> longArrayOf(0, 15, 25, 20)
                    VibeType.REGEN -> longArrayOf(0, 18, 20, 12)
                    VibeType.CHIME -> longArrayOf(0, 25)
                }
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        executor.execute {
            try {
                mediaPlayerFahhh?.stop()
                mediaPlayerFahhh?.release()
                mediaPlayerFahhh = null
                mediaPlayerBruh?.stop()
                mediaPlayerBruh?.release()
                mediaPlayerBruh = null
                soundPool?.release()
                soundPool = null
                trackSwipeRight?.release()
                trackSwipeLeft?.release()
                trackClick?.release()
                trackSnapBack?.release()
                trackStartup?.release()
                trackSuccess?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
