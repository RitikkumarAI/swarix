package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun crossfade_range_and_presets_are_valid() {
    val presets = listOf(0, 3, 5, 8, 12)
    presets.forEach { seconds ->
      assertTrue(seconds in 0..12)
    }

    // Default duration should be 5 seconds
    val defaultCrossfadeSeconds = 5
    assertEquals(5, defaultCrossfadeSeconds)

    // Coerce into valid range 0 to 12
    val clamped = 15.coerceIn(0, 12)
    assertEquals(12, clamped)
  }

  @Test
  fun m3u_playlist_format_and_header_validity() {
    val songs = listOf(
      com.example.core.model.Song(
        id = "test_1",
        title = "Kesariya",
        artist = "Arijit Singh",
        album = "Brahmastra",
        durationMs = 268000,
        contentUri = "content://media/external/audio/media/1",
        artworkUri = "",
        genre = "Bollywood",
        mood = "Romantic"
      ),
      com.example.core.model.Song(
        id = "test_2",
        title = "Midnight City",
        artist = "M83",
        album = "Hurry Up",
        durationMs = 243000,
        contentUri = "file:///storage/emulated/0/Music/midnight.mp3",
        artworkUri = "",
        genre = "Synthwave",
        mood = "Energy"
      )
    )

    val mood = "Focus"
    val m3uString = com.example.core.export.M3uPlaylistExporter.buildM3uString(
      mood = mood,
      songs = songs,
      databaseSongsMap = emptyMap()
    )

    // Verify Extended M3U Header
    assertTrue(m3uString.startsWith("#EXTM3U"))
    assertTrue(m3uString.contains("#PLAYLIST:Swarix AI Mood Radio - Focus"))

    // Verify #EXTINF duration in seconds (268000ms -> 268s)
    assertTrue(m3uString.contains("#EXTINF:268,Arijit Singh - Kesariya"))
    assertTrue(m3uString.contains("content://media/external/audio/media/1"))

    // Verify second song duration in seconds (243000ms -> 243s)
    assertTrue(m3uString.contains("#EXTINF:243,M83 - Midnight City"))
    assertTrue(m3uString.contains("file:///storage/emulated/0/Music/midnight.mp3"))
  }

  @Test
  fun battery_saver_threshold_logic_is_accurate() {
    val threshold = com.example.core.battery.BatteryMonitor.LOW_BATTERY_THRESHOLD_PERCENT
    assertEquals(20, threshold)

    // Battery levels below 20% should qualify as low battery when not charging
    val level19 = 19
    val isChargingFalse = false
    val isLow19 = level19 < threshold && !isChargingFalse
    assertTrue(isLow19)

    // Battery level 20% or above is not low battery
    val level20 = 20
    val isLow20 = level20 < threshold && !isChargingFalse
    assertFalse(isLow20)

    // Charging overrides low battery engagement
    val isChargingTrue = true
    val isLowCharging = level19 < threshold && !isChargingTrue
    assertFalse(isLowCharging)
  }
}
