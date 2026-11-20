package com.example.core.recommendation

import android.util.Log
import com.example.core.model.Song
import com.example.data.local.SongEntity
import com.example.data.remote.JioSaavnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class RecommendationFilter(val displayName: String, val badge: String) {
    ALL("All", "✨"),
    FAMILIAR("Familiar", "❤️"),
    DISCOVER("Discover", "🚀"),
    SIMILAR_VIBE("Similar Vibe", "🎵")
}

data class RecommendationWeights(
    val musicalSimilarity: Double = 0.25,
    val languageCompatibility: Double = 0.20,
    val genreCompatibility: Double = 0.15,
    val moodCompatibility: Double = 0.15,
    val userTasteProfile: Double = 0.15,
    val artistAlbumRelevance: Double = 0.05,
    val sessionContext: Double = 0.05
)

data class UserTasteProfile(
    val topArtists: Set<String> = emptySet(),
    val topGenres: Set<String> = emptySet(),
    val preferredLanguages: Set<String> = emptySet(),
    val favoriteMoods: Set<String> = emptySet(),
    val likedSongIds: Set<String> = emptySet(),
    val skippedSongIds: Set<String> = emptySet(),
    val completedSongIds: Set<String> = emptySet()
)

data class SonicCluster(
    val name: String,
    val language: String,
    val artists: Set<String>,
    val genres: Set<String>,
    val moods: Set<String>,
    val clashClusterNames: Set<String> = emptySet()
)

/**
 * SOREX Advanced Hybrid AI Music Recommendation Engine.
 * Combines Content-Based Filtering, Collaborative Filtering Knowledge Graph,
 * User Taste Profiles, Session Context Consistency, and Sequential Next-Track Prediction.
 */
object SmartRecommendationEngine {

    private const val TAG = "SmartRecommendation"

    var configWeights = RecommendationWeights()

    // SOREX Multi-Language & Multi-Genre Sonic Knowledge Graph
    val CLUSTERS = listOf(
        SonicCluster(
            name = "BOLLYWOOD_ROMANTIC",
            language = "Hindi",
            artists = setOf(
                "arijit singh", "pritam", "atif aslam", "shreya ghoshal", "kk", "mohit chauhan",
                "jubin nautiyal", "sachin-jigar", "mithoon", "vishal-shekhar", "jasleen royal",
                "b praak", "armaan malik", "darshan raval", "asees kaur", "tulsi kumar",
                "palak muchhal", "ankit tiwari", "rahat fateh ali khan", "sonu nigam",
                "javed ali", "shankar-ehsaan-loy", "amit trivedi", "neha kakkar", "shilpa rao",
                "monali thakur", "sunidhi chauhan", "sachet-parampara", "rochak kohli", "tanishk bagchi"
            ),
            genres = setOf(
                "bollywood romantic", "romantic acoustic", "love", "soulful",
                "bollywood melodies", "bollywood pop", "romantic", "bollywood"
            ),
            moods = setOf("Love", "Romantic", "Chill", "Emotional"),
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "GLOBAL_POP_HOLLYWOOD", "HARYANVI_FOLK", "HARD_EDM")
        ),
        SonicCluster(
            name = "BOLLYWOOD_SAD_HEARTBREAK",
            language = "Hindi",
            artists = setOf(
                "arijit singh", "atif aslam", "kk", "b praak", "mustafa zahid",
                "rahat fateh ali khan", "jagjit singh", "sonu nigam", "mohit chauhan",
                "papon", "ankit tiwari", "rochak kohli", "ali zafar", "lucky ali"
            ),
            genres = setOf("sad romantic", "heartbreak", "emotional", "sufi", "sad"),
            moods = setOf("Sad", "Heartbreak", "Melancholy", "Emotional"),
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "HARD_EDM", "GLOBAL_POP_HOLLYWOOD")
        ),
        SonicCluster(
            name = "PUNJABI_ROMANTIC_ACOUSTIC",
            language = "Punjabi",
            artists = setOf(
                "b praak", "ammy virk", "gurnam bhullar", "diljit dosanjh", "jassie gill",
                "harrdy sandhu", "jaani", "maninder buttar", "akhil", "sunanda sharma", "prabh gill"
            ),
            genres = setOf("punjabi romantic", "punjabi acoustic", "punjabi sad", "punjabi love"),
            moods = setOf("Love", "Romantic", "Sad", "Emotional"),
            clashClusterNames = setOf("GLOBAL_POP_HOLLYWOOD", "HARD_EDM")
        ),
        SonicCluster(
            name = "PUNJABI_HIPHOP_PARTY",
            language = "Punjabi",
            artists = setOf(
                "sidhu moose wala", "diljit dosanjh", "karan aujla", "ap dhillon", "shubh",
                "badshah", "yo yo honey singh", "imran khan", "guru randhawa", "jassie gill",
                "ammy virk", "harrdy sandhu", "garry sandhu", "mankirt aulakh",
                "ikky", "byg byrd", "the prophec", "sukhe", "mickey singh", "bohemia",
                "gurinder gill", "wazir patar", "cheema y", "bally sagoo"
            ),
            genres = setOf(
                "punjabi pop", "punjabi hip-hop", "bhangra", "desi hip-hop",
                "party", "gangsta rap", "punjabi party", "punjabi"
            ),
            moods = setOf("Happy", "Party", "Workout", "High Energy"),
            clashClusterNames = setOf("BOLLYWOOD_SAD_HEARTBREAK", "LOFI_FOCUS_AMBIENT", "CLASSICAL_INSTRUMENTAL")
        ),
        SonicCluster(
            name = "GLOBAL_POP_HOLLYWOOD",
            language = "English",
            artists = setOf(
                "the weeknd", "taylor swift", "ed sheeran", "bruno mars", "post malone",
                "dua lipa", "justin bieber", "drake", "billie eilish", "ariana grande",
                "coldplay", "imagine dragons", "charlie puth", "shawn mendes", "maroon 5",
                "adele", "sam smith", "harry styles", "selena gomez", "camila cabello",
                "travis scott", "kendrick lamar", "chainsmokers", "marshmello", "david guetta"
            ),
            genres = setOf("pop", "r&b", "synthwave", "dance pop", "alternative pop", "hip-hop", "hollywood"),
            moods = setOf("Happy", "Chill", "Night Drive", "Focus"),
            clashClusterNames = setOf("BOLLYWOOD_ROMANTIC", "HARYANVI_FOLK")
        ),
        SonicCluster(
            name = "LOFI_FOCUS_AMBIENT",
            language = "Instrumental",
            artists = setOf(
                "lofi fruits music", "chilledcow", "kavv", "ludovico einaudi",
                "yiruma", "hans zimmer", "max richter", "ambient collective"
            ),
            genres = setOf("lo-fi", "ambient", "instrumental", "classical piano", "study focus", "focus"),
            moods = setOf("Focus", "Study", "Calm", "Meditation"),
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "HARD_EDM")
        )
    )

    /**
     * Determines primary language of a song through artist metadata, genre, and acoustic tags.
     */
    fun inferLanguage(song: Song): String {
        val lowerArtist = song.artist.lowercase()
        val lowerGenre = song.genre.lowercase()
        val lowerTitle = song.title.lowercase()

        // 1. Direct cluster match
        val cluster = findMatchingCluster(song)
        if (cluster != null) return cluster.language

        // 2. Genre detection
        if (lowerGenre.contains("punjabi") || lowerGenre.contains("bhangra")) return "Punjabi"
        if (lowerGenre.contains("hindi") || lowerGenre.contains("bollywood")) return "Hindi"
        if (lowerGenre.contains("english") || lowerGenre.contains("pop") || lowerGenre.contains("hollywood")) return "English"

        // 3. Known artist mapping
        if (listOf("arijit", "pritam", "shreya", "atif", "sonu", "jubin", "kk", "neha").any { lowerArtist.contains(it) }) return "Hindi"
        if (listOf("sidhu", "diljit", "karan aujla", "ap dhillon", "shubh", "ammy").any { lowerArtist.contains(it) }) return "Punjabi"
        if (listOf("weeknd", "taylor", "sheeran", "bruno", "drake", "bieber").any { lowerArtist.contains(it) }) return "English"

        return "Hindi" // Default baseline
    }

    fun findMatchingCluster(song: Song): SonicCluster? {
        val lowerArtist = song.artist.lowercase()
        val lowerGenre = song.genre.lowercase()
        val lowerMood = song.mood.lowercase()

        // 1. Direct artist match
        CLUSTERS.firstOrNull { cluster ->
            cluster.artists.any { lowerArtist.contains(it) || it.contains(lowerArtist) }
        }?.let { return it }

        // 2. Direct genre match
        CLUSTERS.firstOrNull { cluster ->
            cluster.genres.any { lowerGenre.contains(it) || it.contains(lowerGenre) }
        }?.let { return it }

        // 3. Mood match with language alignment
        return CLUSTERS.firstOrNull { cluster ->
            cluster.moods.any { lowerMood.contains(it.lowercase()) }
        }
    }

    /**
     * Comprehensive multi-signal ranking function implementing Spotify & YouTube Music
     * recommendation mathematics with configurable scoring weights.
     */
    fun scoreSongMatch(
        candidate: Song,
        baseSong: Song,
        filter: RecommendationFilter = RecommendationFilter.ALL,
        userProfile: UserTasteProfile? = null,
        sessionHistory: List<Song> = emptyList()
    ): Double {
        if (candidate.id == baseSong.id) return -999.0

        val baseLanguage = inferLanguage(baseSong)
        val candLanguage = inferLanguage(candidate)

        val baseCluster = findMatchingCluster(baseSong)
        val candCluster = findMatchingCluster(candidate)

        // Strict clash check: clashing sonic clusters or abrupt culture change
        if (baseCluster != null && candCluster != null) {
            if (baseCluster.clashClusterNames.contains(candCluster.name) ||
                candCluster.clashClusterNames.contains(baseCluster.name)) {
                return -500.0
            }
        }

        // Language Clash Penalty: Hindi Romantic must never jump into English Rock or Punjabi Party
        val isLanguageSame = baseLanguage.equals(candLanguage, ignoreCase = true)
        if (!isLanguageSame && baseLanguage != "Instrumental" && candLanguage != "Instrumental") {
            // Strong negative penalty prevents abrupt cross-lingual hopping unless explicit user preference
            if (userProfile == null || !userProfile.preferredLanguages.contains(candLanguage)) {
                return -350.0
            }
        }

        var musicalSimilarityScore = 0.0
        var languageCompatScore = 0.0
        var genreCompatScore = 0.0
        var moodCompatScore = 0.0
        var userPreferenceScore = 0.0
        var artistAlbumScore = 0.0
        var contextSessionScore = 0.0

        val baseArtist = baseSong.artist.lowercase().trim()
        val candArtist = candidate.artist.lowercase().trim()
        val isExactArtist = baseArtist.equals(candArtist, ignoreCase = true)
        val isCoArtist = baseArtist.contains(candArtist, ignoreCase = true) || candArtist.contains(baseArtist, ignoreCase = true)

        // 1. Musical Similarity (Weight: 25%)
        if (baseCluster != null && candCluster != null && baseCluster.name == candCluster.name) {
            musicalSimilarityScore += 70.0
        }
        val durationDiff = kotlin.math.abs(baseSong.durationMs - candidate.durationMs)
        if (durationDiff < 60_000) musicalSimilarityScore += 30.0

        // 2. Language Compatibility (Weight: 20%)
        if (isLanguageSame) {
            languageCompatScore += 100.0
        } else if (candLanguage == "Instrumental") {
            languageCompatScore += 70.0
        }

        // 3. Genre Compatibility (Weight: 15%)
        if (baseSong.genre.isNotBlank() && candidate.genre.isNotBlank()) {
            if (baseSong.genre.equals(candidate.genre, ignoreCase = true)) {
                genreCompatScore += 100.0
            } else if (baseSong.genre.contains(candidate.genre, ignoreCase = true) ||
                       candidate.genre.contains(baseSong.genre, ignoreCase = true)) {
                genreCompatScore += 65.0
            }
        }

        // 4. Mood Compatibility (Weight: 15%)
        if (baseSong.mood.isNotBlank() && candidate.mood.isNotBlank()) {
            if (baseSong.mood.equals(candidate.mood, ignoreCase = true)) {
                moodCompatScore += 100.0
            } else if (isCompatibleMood(baseSong.mood, candidate.mood)) {
                moodCompatScore += 60.0
            } else {
                moodCompatScore -= 80.0 // Clashing mood penalty (e.g. Sad vs Party)
            }
        }

        // 5. User Listening Preferences & Taste Profile (Weight: 15%)
        if (userProfile != null) {
            if (userProfile.topArtists.contains(candArtist)) userPreferenceScore += 40.0
            if (userProfile.favoriteMoods.contains(candidate.mood)) userPreferenceScore += 30.0
            if (userProfile.topGenres.contains(candidate.genre)) userPreferenceScore += 30.0
        } else {
            if (candidate.isFavorite) userPreferenceScore += 40.0
            userPreferenceScore += (candidate.playCount.coerceAtMost(15) * 4.0)
        }

        // 6. Artist and Album Relevance (Weight: 5%)
        if (isExactArtist) {
            artistAlbumScore += 70.0
        } else if (isCoArtist) {
            artistAlbumScore += 45.0
        }
        if (baseSong.album.isNotBlank() && baseSong.album.equals(candidate.album, ignoreCase = true)) {
            artistAlbumScore += 30.0
        }

        // 7. Listening Context / Session Continuity (Weight: 5%)
        if (sessionHistory.isNotEmpty()) {
            val lastTrack = sessionHistory.last()
            if (inferLanguage(lastTrack) == candLanguage) contextSessionScore += 50.0
            if (lastTrack.mood.equals(candidate.mood, ignoreCase = true)) contextSessionScore += 50.0
        }

        // Apply configurable scoring weights
        val w = configWeights
        var finalScore = (
            (musicalSimilarityScore * w.musicalSimilarity) +
            (languageCompatScore * w.languageCompatibility) +
            (genreCompatScore * w.genreCompatibility) +
            (moodCompatScore * w.moodCompatibility) +
            (userPreferenceScore * w.userTasteProfile) +
            (artistAlbumScore * w.artistAlbumRelevance) +
            (contextSessionScore * w.sessionContext)
        )

        // Behavioral Adjustments
        if (userProfile != null) {
            if (userProfile.skippedSongIds.contains(candidate.id)) {
                finalScore -= 30.0 // Skipped penalty
            }
            if (userProfile.likedSongIds.contains(candidate.id) || candidate.isFavorite) {
                finalScore += 35.0 // Liked boost
            }
            if (userProfile.completedSongIds.contains(candidate.id)) {
                finalScore += 20.0 // Completion boost
            }
        }

        // Session Repetition Penalty: do not repeat songs recently played in current session
        if (sessionHistory.takeLast(10).any { it.id == candidate.id }) {
            finalScore -= 100.0
        }

        // Quick filter pills modulation
        when (filter) {
            RecommendationFilter.ALL -> { /* standard */ }
            RecommendationFilter.FAMILIAR -> {
                if (isExactArtist) finalScore += 30.0
                if (candidate.isFavorite) finalScore += 25.0
            }
            RecommendationFilter.DISCOVER -> {
                if (!isExactArtist) finalScore += 35.0
                if (candidate.playCount == 0) finalScore += 25.0
            }
            RecommendationFilter.SIMILAR_VIBE -> {
                if (baseSong.mood.equals(candidate.mood, ignoreCase = true)) finalScore += 35.0
                if (isLanguageSame) finalScore += 25.0
            }
        }

        return finalScore
    }

    private fun isCompatibleMood(mood1: String, mood2: String): Boolean {
        val m1 = mood1.lowercase()
        val m2 = mood2.lowercase()
        if (m1 == m2) return true
        if ((m1.contains("love") || m1.contains("romantic")) && (m2.contains("love") || m2.contains("romantic") || m2.contains("chill"))) return true
        if ((m1.contains("party") || m1.contains("dance")) && (m2.contains("party") || m2.contains("workout") || m2.contains("happy"))) return true
        if (m1.contains("sad") && (m2.contains("sad") || m2.contains("heartbreak") || m2.contains("emotional"))) return true
        return false
    }

    /**
     * Filters, ranks, and returns the top recommendations from a candidate pool.
     */
    fun getRankedRecommendations(
        baseSong: Song,
        pool: List<Song>,
        filter: RecommendationFilter = RecommendationFilter.ALL,
        limit: Int = 10,
        userProfile: UserTasteProfile? = null,
        sessionHistory: List<Song> = emptyList()
    ): List<Song> {
        val scored = pool.filter { it.id != baseSong.id }
            .map { song -> Pair(song, scoreSongMatch(song, baseSong, filter, userProfile, sessionHistory)) }
            .filter { it.second > 10.0 }
            .sortedByDescending { it.second }

        val result = scored.map { it.first }.distinctBy { it.id }.take(limit)
        if (result.isNotEmpty()) return result

        // Fallback: strictly same language and compatible cluster only
        val baseLang = inferLanguage(baseSong)
        val baseCluster = findMatchingCluster(baseSong)
        val clusterMatches = pool.filter { s ->
            s.id != baseSong.id && inferLanguage(s) == baseLang && (
                s.artist.contains(baseSong.artist, ignoreCase = true) ||
                (baseCluster != null && findMatchingCluster(s)?.name == baseCluster.name)
            )
        }
        return if (clusterMatches.isNotEmpty()) clusterMatches.take(limit) else pool.take(limit)
    }

    /**
     * Sequential Next-Track Prediction:
     * Predicts the single most optimal next song for continuous autoplay without jarring transitions.
     */
    fun predictNextTrack(
        currentSong: Song,
        pool: List<Song>,
        sessionHistory: List<Song> = emptyList(),
        userProfile: UserTasteProfile? = null
    ): Song? {
        val candidates = pool.filter { cand ->
            cand.id != currentSong.id && sessionHistory.takeLast(8).none { it.id == cand.id }
        }
        val ranked = getRankedRecommendations(
            baseSong = currentSong,
            pool = candidates,
            filter = RecommendationFilter.ALL,
            limit = 5,
            userProfile = userProfile,
            sessionHistory = sessionHistory
        )
        return ranked.firstOrNull()
    }

    /**
     * Dynamic Cloud Radio: Fetches authentic online radio tracks from JioSaavn
     * tailored to the playing artist, genre, language, and mood.
     */
    suspend fun fetchOnlineRadio(baseSong: Song, limit: Int = 20): List<Song> = withContext(Dispatchers.IO) {
        val cluster = findMatchingCluster(baseSong)
        val queries = mutableListOf<String>()

        val cleanArtist = baseSong.artist.split(",", "&", "feat.", "ft.").firstOrNull()?.trim() ?: baseSong.artist
        queries.add("$cleanArtist top hits")
        queries.add("$cleanArtist radio")

        when (cluster?.name) {
            "BOLLYWOOD_ROMANTIC" -> {
                queries.add("$cleanArtist romantic hits")
                queries.add("best bollywood romantic songs")
            }
            "PUNJABI_HIPHOP_PARTY" -> {
                queries.add("$cleanArtist punjabi songs")
                queries.add("punjabi party hits")
            }
            "PUNJABI_ROMANTIC_ACOUSTIC" -> {
                queries.add("$cleanArtist punjabi romantic acoustic")
            }
            "BOLLYWOOD_SAD_HEARTBREAK" -> {
                queries.add("$cleanArtist sad songs")
                queries.add("bollywood heartbreak songs")
            }
            "GLOBAL_POP_HOLLYWOOD" -> {
                queries.add("$cleanArtist pop radio")
                queries.add("global pop hits")
            }
            "LOFI_FOCUS_AMBIENT" -> {
                queries.add("lofi study focus beats")
                queries.add("instrumental focus music")
            }
            else -> {
                queries.add("${baseSong.genre} songs")
            }
        }

        val fetchedEntities = mutableListOf<SongEntity>()
        for (q in queries) {
            if (fetchedEntities.size >= limit) break
            try {
                val results = JioSaavnService.searchSongs(q, limit = limit)
                for (res in results) {
                    if (fetchedEntities.none { it.id == res.id || it.title.equals(res.title, ignoreCase = true) }) {
                        val updated = res.copy(
                            mood = if (res.mood.isBlank() || res.mood == "Trending") baseSong.mood else res.mood,
                            genre = if (res.genre.isBlank() || res.genre == "Bollywood / Pop") baseSong.genre else res.genre
                        )
                        fetchedEntities.add(updated)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch radio query '$q': ${e.message}")
            }
        }

        val domainSongs = fetchedEntities.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                durationMs = entity.durationMs,
                contentUri = entity.contentUri,
                artworkUri = entity.artworkUri,
                genre = entity.genre,
                mood = entity.mood,
                lyrics = entity.lyrics,
                isFavorite = entity.isFavorite,
                playCount = entity.playCount,
                dateAdded = entity.dateAdded,
                isLocal = entity.isLocal
            )
        }

        getRankedRecommendations(baseSong, domainSongs, RecommendationFilter.ALL, limit)
    }
}
