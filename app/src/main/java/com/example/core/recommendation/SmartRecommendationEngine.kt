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

data class SonicCluster(
    val name: String,
    val artists: Set<String>,
    val genres: Set<String>,
    val moods: Set<String>,
    val clashClusterNames: Set<String> = emptySet()
)

object SmartRecommendationEngine {

    private const val TAG = "SmartRecommendation"

    // Spotify & YouTube Music Sonic Knowledge Graph
    private val CLUSTERS = listOf(
        SonicCluster(
            name = "BOLLYWOOD_ROMANTIC",
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
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "HARYANVI_FOLK", "HARD_EDM")
        ),
        SonicCluster(
            name = "PUNJABI_HIPHOP_PARTY",
            artists = setOf(
                "sidhu moose wala", "diljit dosanjh", "karan aujla", "ap dhillon", "shubh",
                "badshah", "yo yo honey singh", "imran khan", "guru randhawa", "jassie gill",
                "ammy virk", "harrdy sandhu", "garry sandhu", "mankirt aulakh", "b praak",
                "jaani", "ikky", "byg byrd", "the prophec", "sukhe", "mickey singh", "bohemia",
                "gurinder gill", "gurnam bhullar", "wazir patar", "cheema y", "bally sagoo"
            ),
            genres = setOf(
                "punjabi pop", "punjabi hip-hop", "bhangra", "desi hip-hop",
                "party", "gangsta rap", "punjabi party", "punjabi"
            ),
            moods = setOf("Happy", "Party", "Workout", "High Energy"),
            clashClusterNames = setOf("LOFI_FOCUS_AMBIENT", "CLASSICAL_INSTRUMENTAL")
        ),
        SonicCluster(
            name = "BOLLYWOOD_SAD_HEARTBREAK",
            artists = setOf(
                "arijit singh", "atif aslam", "kk", "b praak", "mustafa zahid",
                "rahat fateh ali khan", "jagjit singh", "sonu nigam", "mohit chauhan",
                "papon", "ankit tiwari", "rochak kohli", "ali zafar", "lucky ali"
            ),
            genres = setOf("sad romantic", "heartbreak", "emotional", "sufi", "sad"),
            moods = setOf("Sad", "Heartbreak", "Melancholy", "Emotional"),
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "HARD_EDM")
        ),
        SonicCluster(
            name = "GLOBAL_POP_HOLLYWOOD",
            artists = setOf(
                "the weeknd", "taylor swift", "ed sheeran", "bruno mars", "post malone",
                "dua lipa", "justin bieber", "drake", "billie eilish", "ariana grande",
                "coldplay", "imagine dragons", "charlie puth", "shawn mendes", "maroon 5",
                "adele", "sam smith", "harry styles", "selena gomez", "camila cabello",
                "travis scott", "kendrick lamar", "chainsmokers", "marshmello", "david guetta"
            ),
            genres = setOf("pop", "r&b", "synthwave", "dance pop", "alternative pop", "hip-hop", "hollywood"),
            moods = setOf("Happy", "Chill", "Night Drive", "Focus"),
            clashClusterNames = setOf("HARYANVI_FOLK")
        ),
        SonicCluster(
            name = "LOFI_FOCUS_AMBIENT",
            artists = setOf(
                "lofi fruits music", "chilledcow", "kavv", "ludovico einaudi",
                "yiruma", "hans zimmer", "max richter", "ambient collective"
            ),
            genres = setOf("lo-fi", "ambient", "instrumental", "classical piano", "study focus", "focus"),
            moods = setOf("Focus", "Study", "Calm", "Meditation"),
            clashClusterNames = setOf("PUNJABI_HIPHOP_PARTY", "HARD_EDM")
        )
    )

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

        // 3. Mood fallback match
        return CLUSTERS.firstOrNull { cluster ->
            cluster.moods.any { lowerMood.contains(it.lowercase()) }
        }
    }

    /**
     * Scores a candidate song against the base playing track using Spotify & YouTube Music
     * collaborative and acoustic affinity weighting.
     */
    fun scoreSongMatch(candidate: Song, baseSong: Song, filter: RecommendationFilter): Double {
        if (candidate.id == baseSong.id) return -999.0

        val baseCluster = findMatchingCluster(baseSong)
        val candCluster = findMatchingCluster(candidate)

        // Strict clash check: never recommend clashing cultures (e.g. Haryanvi into Bollywood romantic)
        if (baseCluster != null && candCluster != null) {
            if (baseCluster.clashClusterNames.contains(candCluster.name) ||
                candCluster.clashClusterNames.contains(baseCluster.name)) {
                return -500.0
            }
        }

        var score = 0.0

        val baseArtist = baseSong.artist.lowercase().trim()
        val candArtist = candidate.artist.lowercase().trim()

        val isExactArtist = baseArtist.equals(candArtist, ignoreCase = true)
        val isCoArtist = baseArtist.contains(candArtist, ignoreCase = true) ||
                         candArtist.contains(baseArtist, ignoreCase = true)

        // 1. Artist Affinity
        if (isExactArtist) {
            score += 65.0
        } else if (isCoArtist) {
            score += 45.0
        }

        // 2. Cluster Affinity (Fans Also Like)
        if (baseCluster != null && candCluster != null && baseCluster.name == candCluster.name) {
            score += 40.0
        }

        // 3. Mood Alignment
        if (baseSong.mood.isNotBlank() && candidate.mood.isNotBlank() &&
            baseSong.mood.equals(candidate.mood, ignoreCase = true)) {
            score += 30.0
        }

        // 4. Genre Alignment
        if (baseSong.genre.isNotBlank() && candidate.genre.isNotBlank()) {
            if (baseSong.genre.equals(candidate.genre, ignoreCase = true)) {
                score += 25.0
            } else if (baseSong.genre.contains(candidate.genre, ignoreCase = true) ||
                       candidate.genre.contains(baseSong.genre, ignoreCase = true)) {
                score += 15.0
            }
        }

        // 5. Album Affinity
        if (baseSong.album.isNotBlank() && candidate.album.isNotBlank() &&
            baseSong.album.equals(candidate.album, ignoreCase = true)) {
            score += 20.0
        }

        // 6. User Affinity Modifiers based on YouTube Music Filter
        when (filter) {
            RecommendationFilter.ALL -> {
                // Balanced Spotify 70/30: reward slight familiarity but encourage variety
                if (candidate.isFavorite) score += 15.0
                score += (candidate.playCount.coerceAtMost(10) * 1.5)
            }
            RecommendationFilter.FAMILIAR -> {
                // High emphasis on same artist & user's liked tracks
                if (isExactArtist) score += 50.0
                if (candidate.isFavorite) score += 40.0
                score += (candidate.playCount.coerceAtMost(25) * 3.0)
            }
            RecommendationFilter.DISCOVER -> {
                // Reward tracks by similar artists in the cluster that user hasn't overplayed
                if (!isExactArtist && baseCluster != null && candCluster?.name == baseCluster.name) {
                    score += 55.0
                }
                if (isExactArtist) score -= 25.0
                if (candidate.playCount == 0) score += 30.0
            }
            RecommendationFilter.SIMILAR_VIBE -> {
                // Heavily weight mood & acoustic genre
                if (baseSong.mood.equals(candidate.mood, ignoreCase = true)) score += 50.0
                if (baseSong.genre.equals(candidate.genre, ignoreCase = true)) score += 40.0
            }
        }

        return score
    }

    /**
     * Filters, ranks, and returns the top recommendations from a candidate pool.
     */
    fun getRankedRecommendations(
        baseSong: Song,
        pool: List<Song>,
        filter: RecommendationFilter = RecommendationFilter.ALL,
        limit: Int = 10
    ): List<Song> {
        val scored = pool.filter { it.id != baseSong.id }
            .map { song -> Pair(song, scoreSongMatch(song, baseSong, filter)) }
            .filter { it.second > 10.0 } // Discard low-affinity / clashing tracks
            .sortedByDescending { it.second }

        val result = scored.map { it.first }.distinctBy { it.id }.take(limit)

        // If high-affinity matches are available, return them directly
        if (result.isNotEmpty()) return result

        // Fallback: strictly same artist or same cluster only (never random shuffle)
        val baseCluster = findMatchingCluster(baseSong)
        val clusterMatches = pool.filter { s ->
            s.id != baseSong.id && (
                s.artist.contains(baseSong.artist, ignoreCase = true) ||
                (baseCluster != null && findMatchingCluster(s)?.name == baseCluster.name)
            )
        }
        return if (clusterMatches.isNotEmpty()) clusterMatches.take(limit) else pool.take(limit)
    }

    /**
     * Dynamic Cloud Radio: Fetches authentic, high-fidelity online radio tracks from JioSaavn
     * tailored to the playing artist, genre, and mood.
     */
    suspend fun fetchOnlineRadio(baseSong: Song, limit: Int = 20): List<Song> = withContext(Dispatchers.IO) {
        val cluster = findMatchingCluster(baseSong)
        val queries = mutableListOf<String>()

        // 1. Direct artist radio
        val cleanArtist = baseSong.artist.split(",", "&", "feat.", "ft.").firstOrNull()?.trim() ?: baseSong.artist
        queries.add("$cleanArtist top hits")
        queries.add("$cleanArtist radio")

        // 2. Genre / cluster specific query
        when (cluster?.name) {
            "BOLLYWOOD_ROMANTIC" -> {
                queries.add("$cleanArtist romantic hits")
                queries.add("best bollywood romantic songs")
            }
            "PUNJABI_HIPHOP_PARTY" -> {
                queries.add("$cleanArtist punjabi songs")
                queries.add("punjabi party hits")
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
                        // Tag with base song's mood and genre if blank
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

        // Convert to domain songs and apply strict ranking
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
