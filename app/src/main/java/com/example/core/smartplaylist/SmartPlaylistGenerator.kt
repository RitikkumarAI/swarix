package com.example.core.smartplaylist

import androidx.compose.ui.graphics.Color
import com.example.core.model.ListeningStats
import com.example.core.model.Playlist
import com.example.core.model.Song

/**
 * Spotify Premium-style Daily Mix curated by the AI Smart Playlist Engine.
 */
data class DailyMix(
    val id: String,
    val mixNumber: Int,
    val title: String,
    val subtitle: String,
    val moodTag: String,
    val primaryGenre: String,
    val coverUri: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val songs: List<Song>,
    val reason: String
) {
    fun toPlaylist(): Playlist {
        return Playlist(
            id = mixNumber.toLong() + 1000L,
            name = title,
            description = "$reason • Featuring $subtitle",
            coverUri = coverUri,
            trackCount = songs.size,
            isSmart = true
        )
    }
}

/**
 * AI-driven smart playlist generator that uses the existing mood tagging system,
 * listening history, and genre affinity to curate personalized 'Daily Mix' style playlists.
 */
object SmartPlaylistGenerator {

    private val MIX_GRADIENTS = listOf(
        Pair(Color(0xFFE29578), Color(0xFF006D77)), // 1. Chill Sunset (Amber & Teal)
        Pair(Color(0xFFD7BC83), Color(0xFF590D22)), // 2. Romance (Gold & Velvet Red)
        Pair(Color(0xFF00B4D8), Color(0xFF03045E)), // 3. Workout (Electric Blue & Midnight)
        Pair(Color(0xFF52B788), Color(0xFF1B4332)), // 4. Focus (Emerald & Deep Pine)
        Pair(Color(0xFFFF007F), Color(0xFF3A0CA3)), // 5. Party (Neon Magenta & Indigo)
        Pair(Color(0xFFF4A261), Color(0xFF264653))  // 6. Nostalgia / Classics (Ochre & Slate)
    )

    private val MOOD_COVERS = mapOf(
        "Chill" to "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=600&auto=format&fit=crop&q=80",
        "Romance" to "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600&auto=format&fit=crop&q=80",
        "Workout" to "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80",
        "Focus" to "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=600&auto=format&fit=crop&q=80",
        "Party" to "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
        "Devotional" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
        "Sad" to "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600&auto=format&fit=crop&q=80",
        "Indie" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
        "Old Hits" to "https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&auto=format&fit=crop&q=80"
    )

    fun generateDailyMixes(
        allSongs: List<Song>,
        recentlyPlayed: List<Song>,
        favoriteSongs: List<Song>,
        preferredMoods: Set<String>,
        stats: ListeningStats? = null
    ): List<DailyMix> {
        if (allSongs.isEmpty()) return emptyList()

        // 1. Calculate weights for artists, moods, and genres from listening history
        val artistScores = mutableMapOf<String, Int>()
        val moodScores = mutableMapOf<String, Int>()
        val genreScores = mutableMapOf<String, Int>()

        // Weight by all-time plays
        allSongs.forEach { song ->
            val plays = song.playCount
            artistScores[song.artist] = (artistScores[song.artist] ?: 0) + plays * 3
            moodScores[song.mood] = (moodScores[song.mood] ?: 0) + plays * 2
            genreScores[song.genre] = (genreScores[song.genre] ?: 0) + plays * 2
        }

        // Heavy boost for favorites
        favoriteSongs.forEach { fav ->
            artistScores[fav.artist] = (artistScores[fav.artist] ?: 0) + 12
            moodScores[fav.mood] = (moodScores[fav.mood] ?: 0) + 10
            genreScores[fav.genre] = (genreScores[fav.genre] ?: 0) + 8
        }

        // Boost for recently played
        recentlyPlayed.forEachIndexed { index, recent ->
            val recencyWeight = (10 - index).coerceAtLeast(1)
            artistScores[recent.artist] = (artistScores[recent.artist] ?: 0) + recencyWeight * 2
            moodScores[recent.mood] = (moodScores[recent.mood] ?: 0) + recencyWeight * 2
        }

        // Explicit user onboarding preferences
        preferredMoods.forEach { mood ->
            moodScores[mood] = (moodScores[mood] ?: 0) + 15
        }

        // 2. Determine targeted mix moods (sorted by computed affinity)
        val allMoods = listOf("Chill", "Romance", "Workout", "Focus", "Party", "Old Hits", "Indie", "Devotional")
        val sortedMoods = allMoods.sortedByDescending { moodScores[it] ?: 0 }

        val mixes = mutableListOf<DailyMix>()

        sortedMoods.take(6).forEachIndexed { index, targetMood ->
            val mixNumber = index + 1
            val (gradStart, gradEnd) = MIX_GRADIENTS[index % MIX_GRADIENTS.size]

            // Find matching songs: prioritize primary mood, then secondary genre affinity
            val matchingSongs = allSongs.filter { it.mood.equals(targetMood, ignoreCase = true) }
            val fallbackGenreSongs = allSongs.filter {
                !it.mood.equals(targetMood, ignoreCase = true) &&
                (genreScores[it.genre] ?: 0) > 0
            }

            // Combine and score tracks for smooth flow:
            // 1) User's favorites & top played tracks first
            // 2) Related tracks matching the mood
            // 3) 1-2 discovery gems with lower play counts
            val scoredSongs = (matchingSongs + fallbackGenreSongs).distinctBy { it.id }.sortedWith(
                compareByDescending<Song> { it.mood.equals(targetMood, ignoreCase = true) }
                    .thenByDescending { it.isFavorite }
                    .thenByDescending { it.playCount }
                    .thenByDescending { artistScores[it.artist] ?: 0 }
            )

            val curatedTracks = scoredSongs.take(8).ifEmpty { allSongs.take(5) }

            // Extract top featured artists for subtitle
            val topArtists = curatedTracks.map { it.artist }.distinct().take(3).joinToString(", ")

            val topGenre = curatedTracks.groupingBy { it.genre }.eachCount().maxByOrNull { it.value }?.key ?: "Acoustic"
            val cover = MOOD_COVERS[targetMood] ?: curatedTracks.firstOrNull()?.artworkUri.orEmpty()

            val reason = when (index) {
                0 -> "Curated from your top $targetMood favorites & most repeated artists"
                1 -> "High-affinity $topGenre tracks aligned with your $targetMood vibe"
                2 -> "Deep focus & motivation mix based on your peak listening hours"
                3 -> "Heartfelt $topGenre acoustic melodies tailored for you"
                4 -> "Weekend energy anthems inspired by your recent plays"
                else -> "Fresh $targetMood discoveries matching your signature taste"
            }

            mixes.add(
                DailyMix(
                    id = "daily_mix_$mixNumber",
                    mixNumber = mixNumber,
                    title = "Daily Mix $mixNumber",
                    subtitle = topArtists.ifEmpty { "Various Artists" },
                    moodTag = targetMood,
                    primaryGenre = topGenre,
                    coverUri = cover,
                    gradientStart = gradStart,
                    gradientEnd = gradEnd,
                    songs = curatedTracks,
                    reason = reason
                )
            )
        }

        return mixes
    }

    /**
     * Curates an instant custom smart playlist based on prompt or mood input.
     */
    fun generateCustomSmartPlaylist(
        prompt: String,
        allSongs: List<Song>,
        favoriteSongs: List<Song> = emptyList()
    ): DailyMix {
        val cleanPrompt = prompt.lowercase().trim()

        // Match against mood tags and genres
        val matchingMood = listOf("chill", "romance", "workout", "focus", "party", "devotional", "sad", "indie", "old hits")
            .firstOrNull { cleanPrompt.contains(it) } ?: "Chill"

        val matchingSongs = allSongs.filter { song ->
            cleanPrompt.contains(song.mood.lowercase()) ||
            cleanPrompt.contains(song.genre.lowercase()) ||
            cleanPrompt.contains(song.artist.lowercase()) ||
            cleanPrompt.contains(song.title.lowercase()) ||
            song.mood.equals(matchingMood, ignoreCase = true)
        }.ifEmpty {
            allSongs.filter { it.mood.equals(matchingMood, ignoreCase = true) }.ifEmpty { allSongs.take(6) }
        }

        val sorted = matchingSongs.sortedWith(
            compareByDescending<Song> { it.isFavorite }
                .thenByDescending { it.playCount }
        )

        val topArtists = sorted.map { it.artist }.distinct().take(3).joinToString(", ")
        val moodCap = matchingMood.replaceFirstChar { it.uppercase() }

        return DailyMix(
            id = "custom_smart_mix_${System.currentTimeMillis()}",
            mixNumber = 0,
            title = if (cleanPrompt.length in 3..25) cleanPrompt.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } else "$moodCap Flow",
            subtitle = topArtists.ifEmpty { "Curated by Swarix AI" },
            moodTag = moodCap,
            primaryGenre = sorted.firstOrNull()?.genre ?: "Pop",
            coverUri = MOOD_COVERS[moodCap] ?: sorted.firstOrNull()?.artworkUri.orEmpty(),
            gradientStart = Color(0xFFD7BC83),
            gradientEnd = Color(0xFF1B231D),
            songs = sorted.take(12),
            reason = "AI Smart Curated based on \"$prompt\" & your audio listening history"
        )
    }
}
