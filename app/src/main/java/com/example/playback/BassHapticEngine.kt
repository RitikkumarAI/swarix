package com.example.playback

import android.content.Context
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot

enum class HapticIntensity(val label: String, val amplitudeMultiplier: Float, val durationMs: Long) {
    LOW("Subtle", 0.55f, 18L),
    MEDIUM("Balanced", 0.85f, 26L),
    HIGH("Powerful", 1.20f, 35L)
}

/**
 * Tactile Bass Haptic Engine that syncs physical device vibrations with the low-end
 * bass frequencies (20Hz - 160Hz) of the currently playing track.
 *
 * Implements real-time FFT hardware analysis via Android Visualizer API with dynamic
 * amplitude mapping and an acoustic beat fallback for hardware/emulator continuity.
 */
class BassHapticEngine(private val context: Context) {

    private val TAG = "BassHapticEngine"

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire Vibrator service: ${e.message}")
            null
        }
    }

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isBatterySaverSuspended = MutableStateFlow(false)
    val isBatterySaverSuspended: StateFlow<Boolean> = _isBatterySaverSuspended.asStateFlow()

    private val _intensity = MutableStateFlow(HapticIntensity.MEDIUM)
    val intensity: StateFlow<HapticIntensity> = _intensity.asStateFlow()

    fun setBatterySaverSuspension(suspended: Boolean) {
        _isBatterySaverSuspended.value = suspended
        if (suspended) {
            stopAcousticBeatEmulation()
            try {
                visualizer?.enabled = false
            } catch (e: Exception) {
                // Ignore
            }
            Log.d(TAG, "Bass haptics suspended for Battery Saver (<20% or power save)")
        } else {
            if (_isEnabled.value && isPlaying) {
                try {
                    visualizer?.enabled = true
                } catch (e: Exception) {
                    // Ignore
                }
                if (visualizer == null) {
                    startRhythmicBassSync(currentSong)
                }
            }
            Log.d(TAG, "Bass haptics resumed from Battery Saver")
        }
    }

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0

    private var isPlaying = false
    private var currentSong: Song? = null

    private var lastHapticTimeMs = 0L
    private val minHapticIntervalMs = 125L // Prevents buzzing motor fatigue, delivers distinct punchy kicks

    private var runningBassAverage = 25f
    private var beatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun initSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        releaseVisualizer()
        currentSessionId = audioSessionId

        try {
            val captureRange = Visualizer.getCaptureSizeRange()
            val captureSize = 128.coerceIn(captureRange[0], captureRange[1])

            visualizer = Visualizer(audioSessionId).apply {
                this.captureSize = captureSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        if (fft != null && _isEnabled.value && isPlaying) {
                            processFftBass(fft)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                enabled = true
            }
            Log.d(TAG, "Hardware Visualizer attached to audio session: $audioSessionId")
        } catch (e: Exception) {
            Log.w(TAG, "Visualizer initialization bypassed (using acoustic beat sync): ${e.message}")
            visualizer = null
        }
    }

    fun onPlaybackStateChanged(playing: Boolean, song: Song?) {
        this.isPlaying = playing
        this.currentSong = song

        if (playing) {
            // If hardware visualizer couldn't initialize or yields silence on this platform, start rhythm pulse
            if (visualizer == null) {
                startRhythmicBassSync(song)
            }
        } else {
            stopRhythmicBassSync()
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            stopRhythmicBassSync()
        } else if (isPlaying && visualizer == null) {
            startRhythmicBassSync(currentSong)
        }
    }

    fun setIntensity(newIntensity: HapticIntensity) {
        _intensity.value = newIntensity
    }

    /**
     * Real-time FFT processing focused on the lowest octave bins (sub-bass & kick drum range).
     */
    private fun processFftBass(fft: ByteArray) {
        if (fft.size < 8) return

        // Bin 0 is DC (n=0) and Nyquist (n=captureSize/2).
        // Bins 1, 2, 3 represent ~30Hz to ~180Hz (Bass kick and sub-bass harmonics).
        var bassSum = 0f
        var binCount = 0

        for (i in 2 until 8 step 2) {
            if (i + 1 < fft.size) {
                val real = fft[i].toFloat()
                val imag = fft[i + 1].toFloat()
                val magnitude = hypot(real, imag)
                bassSum += magnitude
                binCount++
            }
        }

        if (binCount == 0) return
        val currentBassEnergy = bassSum / binCount

        // Update running average
        runningBassAverage = (runningBassAverage * 0.85f) + (currentBassEnergy * 0.15f)

        // Threshold check: energy spike significantly higher than current local groove average
        val dynamicThreshold = (runningBassAverage * 1.35f).coerceAtLeast(18f)
        val now = System.currentTimeMillis()

        if (currentBassEnergy > dynamicThreshold && (now - lastHapticTimeMs) >= minHapticIntervalMs) {
            lastHapticTimeMs = now
            val normalizedRatio = (currentBassEnergy / (dynamicThreshold * 1.8f)).coerceIn(0.4f, 1.0f)
            triggerTactilePulse(normalizedRatio)
        }
    }

    /**
     * Fallback acoustic beat synchronizer when hardware Visualizer is restricted
     * by sandbox or on emulators.
     */
    private fun startRhythmicBassSync(song: Song?) {
        if (_isBatterySaverSuspended.value) return
        beatJob?.cancel()
        beatJob = scope.launch {
            val bpm = estimateBpmFromGenre(song?.genre ?: "")
            val intervalMs = (60_000L / bpm).coerceIn(380L, 850L)

            while (isActive && isPlaying && _isEnabled.value && !_isBatterySaverSuspended.value) {
                val now = System.currentTimeMillis()
                if (now - lastHapticTimeMs >= minHapticIntervalMs) {
                    lastHapticTimeMs = now
                    triggerTactilePulse(0.85f)
                }
                delay(intervalMs)
            }
        }
    }

    private fun stopRhythmicBassSync() {
        beatJob?.cancel()
        beatJob = null
    }

    private fun stopAcousticBeatEmulation() {
        stopRhythmicBassSync()
    }

    private fun estimateBpmFromGenre(genre: String): Int {
        val lower = genre.lowercase()
        return when {
            lower.contains("edm") || lower.contains("dance") || lower.contains("house") || lower.contains("club") -> 128
            lower.contains("techno") || lower.contains("trance") -> 132
            lower.contains("rock") || lower.contains("metal") || lower.contains("punk") -> 124
            lower.contains("pop") || lower.contains("bollywood") -> 116
            lower.contains("hip hop") || lower.contains("rap") || lower.contains("trap") -> 92
            lower.contains("acoustic") || lower.contains("chill") || lower.contains("lo-fi") -> 78
            lower.contains("ambient") || lower.contains("classical") || lower.contains("ghazal") -> 68
            else -> 110
        }
    }

    /**
     * Dispatches tactile physical vibration matching the intensity setting.
     */
    private fun triggerTactilePulse(strengthRatio: Float) {
        if (_isBatterySaverSuspended.value || !_isEnabled.value) return
        val vib = vibrator ?: return
        try {
            val currentInt = _intensity.value
            val targetDuration = currentInt.durationMs
            val baseAmplitude = (strengthRatio * currentInt.amplitudeMultiplier * 255).toInt().coerceIn(40, 255)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vib.hasAmplitudeControl()) {
                    val effect = VibrationEffect.createOneShot(targetDuration, baseAmplitude)
                    vib.vibrate(effect)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(targetDuration)
                }
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(targetDuration)
            }
        } catch (e: Exception) {
            // Ignore vibration exceptions on unsupported hardware
        }
    }

    fun release() {
        stopRhythmicBassSync()
        releaseVisualizer()
    }

    private fun releaseVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing visualizer: ${e.message}")
        } finally {
            visualizer = null
        }
    }
}
