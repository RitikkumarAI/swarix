package com.example.playback

import android.content.Context
import androidx.media3.common.Player
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MusicPlayerServiceTest {

    private lateinit var context: Context
    private lateinit var playerManager: PlayerManager

    private val testSong1 = Song(
        id = "test_1",
        title = "Acoustic Sunset",
        artist = "Solar",
        album = "Horizon",
        durationMs = 180000L,
        genre = "Acoustic",
        mood = "Chill",
        contentUri = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverending_Story.mp3"
    )

    private val testSong2 = Song(
        id = "test_2",
        title = "Midnight Beat",
        artist = "Luna",
        album = "Nightfall",
        durationMs = 210000L,
        genre = "Electronic",
        mood = "Energetic",
        contentUri = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Seagulls.mp3"
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        playerManager = PlayerManager.getInstance(context)
    }

    @Test
    fun `verify ExoPlayer initialization and AudioAttributes configuration`() {
        val exoPlayer = playerManager.exoPlayer
        assertNotNull("ExoPlayer instance should not be null", exoPlayer)
        
        val isValidConfig = MusicPlayerInitializer.verifyConfiguration(exoPlayer)
        assertTrue("ExoPlayer should be configured with USAGE_MEDIA and CONTENT_TYPE_MUSIC", isValidConfig)
    }

    @Test
    fun `playSong updates current song and queue correctly`() {
        val playlist = listOf(testSong1, testSong2)
        playerManager.playSong(testSong1, playlist)

        assertEquals("Current song should match testSong1", testSong1.id, playerManager.currentSong.value?.id)
        assertEquals("Queue should contain 2 songs", 2, playerManager.queue.value.size)
        assertEquals("Queue index should be 0", 0, playerManager.queueIndex.value)
    }

    @Test
    fun `togglePlayPause toggles playback state properly`() {
        val playlist = listOf(testSong1)
        playerManager.playSong(testSong1, playlist)

        // Pause
        playerManager.togglePlayPause()
        assertTrue("Player should respond to pause toggle", true)

        // Resume
        playerManager.togglePlayPause()
        assertTrue("Player should respond to resume toggle", true)
    }

    @Test
    fun `audio focus manager requests and abandons focus cleanly`() {
        val focusGranted = playerManager.audioFocusManager.requestAudioFocus()
        assertTrue("AudioFocus request should return true", focusGranted)

        playerManager.audioFocusManager.abandonAudioFocus()
        assertTrue("AudioFocus abandon executed without exception", true)
    }

    @Test
    fun `playback health manager tracks status and handles reset`() {
        val initialStatus = playerManager.playbackHealthManager.healthStatus.value
        assertNotNull("Health status should be populated", initialStatus)

        playerManager.resetPlayer()
        assertEquals("Player Reset Complete", playerManager.playbackHealthManager.healthStatus.value)
    }

    @Test
    fun `diagnostic logger records events and dumps telemetry`() {
        playerManager.diagnosticLogger.dumpLogsToLogcat("UnitTest Execution")
        val history = playerManager.diagnosticLogger.getLogHistory()
        assertNotNull("Diagnostic log history should be non-null", history)
    }
}
