package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "swarix_prefs")

class SwarixPreferences(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val KEY_CROSSFADE = booleanPreferencesKey("crossfade_enabled")
        val KEY_CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val KEY_GAPLESS = booleanPreferencesKey("gapless_playback")
        val KEY_PLAYBACK_SPEED = stringPreferencesKey("playback_speed")
        val KEY_PAUSE_ON_HEADPHONE_DISCONNECT = booleanPreferencesKey("pause_headphone_disconnect")
        val KEY_EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val KEY_SELECTED_PRESET = stringPreferencesKey("selected_preset")
        val KEY_BASS_BOOST = intPreferencesKey("bass_boost")
        val KEY_VIRTUALIZER = intPreferencesKey("virtualizer")
        val KEY_SLEEP_TIMER = longPreferencesKey("sleep_timer_end")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_USER_MOODS = stringSetPreferencesKey("user_moods")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_REGION = stringPreferencesKey("region")
        val KEY_BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME_MODE] ?: ThemeMode.DARK.name
        try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    val audioQuality: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUDIO_QUALITY] ?: "8K Ultra Studio Master (Lossless Hi-Res)"
    }

    suspend fun setAudioQuality(quality: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUDIO_QUALITY] = quality
        }
    }

    val crossfadeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CROSSFADE] ?: true
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CROSSFADE] = enabled
        }
    }

    val crossfadeSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CROSSFADE_SECONDS] ?: 5
    }

    suspend fun setCrossfadeSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CROSSFADE_SECONDS] = seconds
        }
    }

    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAPLESS] ?: true
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GAPLESS] = enabled
        }
    }

    val equalizerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EQUALIZER_ENABLED] ?: true
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EQUALIZER_ENABLED] = enabled
        }
    }

    val selectedPreset: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_PRESET] ?: "Custom"
    }

    suspend fun setSelectedPreset(preset: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_PRESET] = preset
        }
    }

    val bassBoost: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASS_BOOST] ?: 45
    }

    suspend fun setBassBoost(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASS_BOOST] = level
        }
    }

    val virtualizer: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIRTUALIZER] ?: 30
    }

    suspend fun setVirtualizer(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VIRTUALIZER] = level
        }
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = done
        }
    }

    val userMoods: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_MOODS] ?: setOf("Chill", "Romance", "Focus")
    }

    suspend fun setUserMoods(moods: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_MOODS] = moods
        }
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    val region: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_REGION] ?: "Global"
    }

    suspend fun setRegion(region: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REGION] = region
        }
    }

    val batterySaverEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BATTERY_SAVER] ?: false
    }

    suspend fun setBatterySaverEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BATTERY_SAVER] = enabled
        }
    }

    val offlineModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_OFFLINE_MODE] ?: false
    }

    suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OFFLINE_MODE] = enabled
        }
    }
}
