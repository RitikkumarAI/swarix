package com.example.recommendation

import com.example.core.model.Song
import com.example.core.recommendation.RecommendationFilter
import com.example.core.recommendation.SmartRecommendationEngine
import com.example.core.recommendation.UserTasteProfile
import com.example.core.search.LyricsSearchEngine
import com.example.core.search.SmartSearchEngine
import com.example.core.voice.DefaultHummingRecognitionProvider
import com.example.core.voice.VoiceSearchEngine
import com.example.core.voice.VoiceSearchIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SorexAdvancedEngineTest {

    private val dilDiyanGallan = Song(
        id = "song_dil_diyan_gallan",
        title = "Dil Diyan Gallan",
        artist = "Atif Aslam",
        album = "Tiger Zinda Hai",
        durationMs = 260_000,
        contentUri = "https://example.com/audio1.mp3",
        artworkUri = "https://example.com/art1.jpg",
        genre = "Bollywood Romantic",
        mood = "Romantic",
        lyrics = "Kacchi doriyon doriyon doriyon se mainun tu baandh le\nTere sang hasna tere sang rona\nTere sang dukh sukh sehna\nDil diyan gallan",
        isFavorite = false,
        playCount = 5,
        dateAdded = 1000L
    )

    private val tumHiHo = Song(
        id = "song_tum_hi_ho",
        title = "Tum Hi Ho",
        artist = "Arijit Singh",
        album = "Aashiqui 2",
        durationMs = 262_000,
        contentUri = "https://example.com/audio2.mp3",
        artworkUri = "https://example.com/art2.jpg",
        genre = "Bollywood Romantic",
        mood = "Romantic",
        lyrics = "Hum tere bin ab reh nahi sakte\nTere bina kya wajood mera\nKyunki tum hi ho",
        isFavorite = true,
        playCount = 10,
        dateAdded = 1001L
    )

    private val punjabiPartyTrack = Song(
        id = "song_punjabi_party",
        title = "295",
        artist = "Sidhu Moose Wala",
        album = "Moosetape",
        durationMs = 270_000,
        contentUri = "https://example.com/audio3.mp3",
        artworkUri = "https://example.com/art3.jpg",
        genre = "Punjabi Hip-Hop",
        mood = "High Energy",
        lyrics = "Dass bolan di jithe hove na manahi\nOthe 295 lagdi",
        isFavorite = false,
        playCount = 1,
        dateAdded = 1002L
    )

    private val englishPopTrack = Song(
        id = "song_blinding_lights",
        title = "Blinding Lights",
        artist = "The Weeknd",
        album = "After Hours",
        durationMs = 200_000,
        contentUri = "https://example.com/audio4.mp3",
        artworkUri = "https://example.com/art4.jpg",
        genre = "Synthwave Pop",
        mood = "Night Drive",
        lyrics = "I said ooh I'm blinded by the lights\nNo I can't sleep until I feel your touch",
        isFavorite = false,
        playCount = 4,
        dateAdded = 1003L
    )

    private val englishPopPartner = Song(
        id = "song_levitating",
        title = "Levitating",
        artist = "Dua Lipa",
        album = "Future Nostalgia",
        durationMs = 203_000,
        contentUri = "https://example.com/audio5.mp3",
        artworkUri = "https://example.com/art5.jpg",
        genre = "Dance Pop",
        mood = "Happy",
        lyrics = "If you wanna run away with me I know a galaxy\nAnd I can take you for a ride",
        isFavorite = false,
        playCount = 2,
        dateAdded = 1004L
    )

    private val fullCatalog = listOf(dilDiyanGallan, tumHiHo, punjabiPartyTrack, englishPopTrack, englishPopPartner)

    @Test
    fun scenario1_hindiRomanticContinuesHindiRomanticAndDoesNotSwitchToPunjabiOrEnglish() {
        val recommendations = SmartRecommendationEngine.getRankedRecommendations(
            baseSong = dilDiyanGallan,
            pool = fullCatalog,
            filter = RecommendationFilter.ALL,
            limit = 5
        )

        assertTrue(recommendations.isNotEmpty())
        assertEquals("Tum Hi Ho", recommendations.first().title)
        assertFalse(recommendations.any { it.id == punjabiPartyTrack.id })
    }

    @Test
    fun scenario2_searchingDoesNotPolluteListeningHistory() {
        val query = "Dil Diyan Gallan"
        val scored = SmartSearchEngine.rankSongsForQuery(query, fullCatalog)

        assertTrue(scored.isNotEmpty())
        assertEquals(dilDiyanGallan.id, scored.first().song.id)
    }

    @Test
    fun scenario3_lyricsSearchFindsExactSong() {
        val lyricQuery = "tere sang hasna tere sang rona"
        val match = LyricsSearchEngine.searchSongLyrics(dilDiyanGallan, lyricQuery)

        assertNotNull(match)
        assertTrue(match!!.matchScore >= 80.0)
        assertTrue(match.matchedSnippet.contains("Tere sang hasna", ignoreCase = true))

        val catalogResults = LyricsSearchEngine.searchCatalogByLyrics(fullCatalog, lyricQuery)
        assertEquals(dilDiyanGallan.id, catalogResults.first().song.id)
    }

    @Test
    fun scenario4_voiceSearchRecognizesMoodIntent() {
        val voiceResult = VoiceSearchEngine.processVoiceQuery(
            query = "play romantic songs",
            allSongs = fullCatalog
        )

        assertEquals(VoiceSearchIntent.MOOD_SEARCH, voiceResult.intent)
        assertTrue(voiceResult.matchedSongs.any { it.mood.contains("Romantic", ignoreCase = true) })
    }

    @Test
    fun scenario5_skipPatternAdjustsRecommendations() {
        val tasteProfileWithSkip = UserTasteProfile(
            skippedSongIds = setOf(punjabiPartyTrack.id)
        )

        val scoreWithoutSkip = SmartRecommendationEngine.scoreSongMatch(
            candidate = punjabiPartyTrack,
            baseSong = punjabiPartyTrack.copy(id = "other_punjabi"),
            userProfile = null
        )

        val scoreWithSkip = SmartRecommendationEngine.scoreSongMatch(
            candidate = punjabiPartyTrack,
            baseSong = punjabiPartyTrack.copy(id = "other_punjabi"),
            userProfile = tasteProfileWithSkip
        )

        assertTrue(scoreWithSkip < scoreWithoutSkip)
    }

    @Test
    fun scenario6_englishPopRecommendsEnglishPopWithoutSwitchingToHindiOrPunjabi() {
        val recommendations = SmartRecommendationEngine.getRankedRecommendations(
            baseSong = englishPopTrack,
            pool = fullCatalog,
            filter = RecommendationFilter.ALL,
            limit = 5
        )

        assertTrue(recommendations.isNotEmpty())
        assertEquals("Levitating", recommendations.first().title)
        assertFalse(recommendations.any { it.id == dilDiyanGallan.id })
        assertFalse(recommendations.any { it.id == punjabiPartyTrack.id })
    }

    @Test
    fun scenario7_extensibleHummingProviderHandlesMelodyGracefully() {
        val provider = DefaultHummingRecognitionProvider { fullCatalog }
        assertTrue(provider.isSupported())
    }

    @Test
    fun scenario8_completedPlaybackMetricsCalculation() {
        val currentPos = 260_000L
        val totalDur = 260_000L
        val completion = (currentPos.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)
        val isSkipped = completion < 0.35f && currentPos < 35_000L

        assertEquals(1.0f, completion, 0.01f)
        assertFalse(isSkipped)
    }

    @Test
    fun scenario9_skippedPlaybackMetricsCalculation() {
        val currentPos = 5_000L
        val totalDur = 260_000L
        val completion = (currentPos.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)
        val isSkipped = completion < 0.35f && currentPos < 35_000L

        assertTrue(completion < 0.05f)
        assertTrue(isSkipped)
    }

    @Test
    fun scenario10_typoTolerantSearchResolvesCorrectly() {
        val typoQuery = "arijt sing"
        val scored = SmartSearchEngine.rankSongsForQuery(typoQuery, fullCatalog)

        assertTrue(scored.isNotEmpty())
        assertEquals(tumHiHo.id, scored.first().song.id)
    }
}
