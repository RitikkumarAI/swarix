package com.example.playback

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log

class EqualizerHelper(private val context: Context) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentSessionId: Int = 0

    // Cached audio engine state to persist across track changes and session recreations
    private var isEngineEnabled: Boolean = true
    private var cachedLevels: List<Int> = listOf(6, 5, 3, 2, 1, 2, 4, 6, 8, 9)
    private var cachedBassBoost: Int = 80
    private var cachedVirtualizer: Int = 60
    private var isNormalizationEnabled: Boolean = true

    fun initSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEngineEnabled
            }
            set10BandLevels(cachedLevels)
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Failed to init Equalizer: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = isEngineEnabled
            }
            setBassBoost(cachedBassBoost)
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Failed to init BassBoost: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = isEngineEnabled
            }
            setVirtualizer(cachedVirtualizer)
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Failed to init Virtualizer: ${e.message}")
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                setTargetGain(if (isNormalizationEnabled) 1000 else 0)
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Failed to init LoudnessEnhancer: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEngineEnabled = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error toggling equalizer: ${e.message}")
        }
    }

    fun setAudioNormalizationEnabled(enabled: Boolean) {
        isNormalizationEnabled = enabled
        try {
            loudnessEnhancer?.enabled = enabled
            if (enabled) {
                loudnessEnhancer?.setTargetGain(1000)
            } else {
                loudnessEnhancer?.setTargetGain(0)
            }
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error toggling loudness enhancer: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        // levelDb typically from -10 to +10 dB; Android Equalizer uses millibels (-1000 to +1000 mB)
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (bandIndex in 0 until numBands) {
                val minMb = eq.bandLevelRange[0]
                val maxMb = eq.bandLevelRange[1]
                val targetMb = (levelDb * 100).coerceIn(minMb.toInt(), maxMb.toInt()).toShort()
                eq.setBandLevel(bandIndex.toShort(), targetMb)
            }
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error setting band level: ${e.message}")
        }
    }

    fun set10BandLevels(levels: List<Int>) {
        cachedLevels = levels
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (numBands <= 0) return
            val minMb = eq.bandLevelRange[0].toInt()
            val maxMb = eq.bandLevelRange[1].toInt()

            for (hwBand in 0 until numBands) {
                val centerFreqHz = eq.getCenterFreq(hwBand.toShort()) / 1000
                val mappedIndex = when {
                    centerFreqHz <= 45 -> 0   // 31Hz
                    centerFreqHz <= 90 -> 1   // 62Hz
                    centerFreqHz <= 180 -> 2  // 125Hz
                    centerFreqHz <= 350 -> 3  // 250Hz
                    centerFreqHz <= 750 -> 4  // 500Hz
                    centerFreqHz <= 1500 -> 5 // 1kHz
                    centerFreqHz <= 3000 -> 6 // 2kHz
                    centerFreqHz <= 6000 -> 7 // 4kHz
                    centerFreqHz <= 12000 -> 8// 8kHz
                    else -> 9                 // 16kHz
                }.coerceIn(0, levels.lastIndex)

                val targetMb = (levels[mappedIndex] * 100).coerceIn(minMb, maxMb).toShort()
                eq.setBandLevel(hwBand.toShort(), targetMb)
            }
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error applying 10-band EQ: ${e.message}")
        }
    }

    fun setBassBoost(levelPercent: Int) {
        cachedBassBoost = levelPercent
        try {
            // levelPercent 0..100, strength 0..1000
            val strength = (levelPercent * 10).coerceIn(0, 1000).toShort()
            bassBoost?.setStrength(strength)
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error setting bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(levelPercent: Int) {
        cachedVirtualizer = levelPercent
        try {
            // levelPercent 0..100, strength 0..1000
            val strength = (levelPercent * 10).coerceIn(0, 1000).toShort()
            virtualizer?.setStrength(strength)
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error setting virtualizer: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e("EqualizerHelper", "Error releasing audio effects: ${e.message}")
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
            loudnessEnhancer = null
            currentSessionId = 0
        }
    }
}
