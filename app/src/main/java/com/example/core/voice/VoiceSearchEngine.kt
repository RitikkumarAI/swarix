package com.example.core.voice

import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.core.smartplaylist.SmartPlaylistGenerator

enum class VoiceSearchIntent {
    PLAY_TRACK,
    SEARCH_ARTIST,
    CURATE_PLAYLIST,
    MOOD_SEARCH,
    GENERAL_SEARCH
}

data class VoiceSearchResult(
    val spokenQuery: String,
    val intent: VoiceSearchIntent,
    val aiResponseSummary: String,
    val matchedSongs: List<Song>,
    val matchedArtists: List<String>,
    val suggestedPlaylistTitle: String? = null,
    val autoPlayCandidate: Song? = null
)

/**
 * AI Voice Search Engine for Swarix.
 * Understands natural speech commands, semantic music intents, artist queries,
 * and mood curation by leveraging Swarix AI and SmartPlaylistGenerator logic.
 */
object VoiceSearchEngine {

    fun processVoiceQuery(
        query: String,
        allSongs: List<Song>,
        playlists: List<Playlist> = emptyList()
    ): VoiceSearchResult {
        val trimmed = query.trim()
        val lower = trimmed.lowercase()

        // Strip common voice command prefixes
        val cleaned = lower
            .replace(Regex("^(play|can you play|please play|listen to|search for|find me|look up|find|show me|put on)\\s+"), "")
            .trim()

        // 1. Direct Song Exact/Fuzzy Match
        val exactSongMatch = allSongs.firstOrNull { it.title.equals(cleaned, ignoreCase = true) }
            ?: allSongs.firstOrNull { cleaned.contains(it.title.lowercase()) || it.title.lowercase().contains(cleaned) }

        // 2. Artist Match
        val artistSongs = allSongs.filter {
            it.artist.lowercase().contains(cleaned) || cleaned.contains(it.artist.lowercase())
        }

        // 3. Play Intent: User explicitly requested "play [track/artist]"
        val isExplicitPlay = lower.startsWith("play ") || lower.startsWith("put on ") || lower.startsWith("listen to ")

        if (exactSongMatch != null && (isExplicitPlay || cleaned == exactSongMatch.title.lowercase())) {
            return VoiceSearchResult(
                spokenQuery = trimmed,
                intent = VoiceSearchIntent.PLAY_TRACK,
                aiResponseSummary = "Found \"${exactSongMatch.title}\" by ${exactSongMatch.artist}. Starting 4K Hi-Res playback.",
                matchedSongs = listOf(exactSongMatch) + allSongs.filter { it.artist == exactSongMatch.artist && it.id != exactSongMatch.id },
                matchedArtists = listOf(exactSongMatch.artist),
                suggestedPlaylistTitle = "${exactSongMatch.title} & Similar Hits",
                autoPlayCandidate = exactSongMatch
            )
        }

        if (artistSongs.isNotEmpty()) {
            val artistName = artistSongs.first().artist
            return VoiceSearchResult(
                spokenQuery = trimmed,
                intent = VoiceSearchIntent.SEARCH_ARTIST,
                aiResponseSummary = "Found ${artistSongs.size} tracks by $artistName from your library.",
                matchedSongs = artistSongs,
                matchedArtists = listOf(artistName),
                suggestedPlaylistTitle = "Best of $artistName",
                autoPlayCandidate = if (isExplicitPlay) artistSongs.first() else null
            )
        }

        // 4. Playlist or Mood Semantic Curation Intent
        val (isMoodOrPlaylist, moodOrThemeTitle, moodSongs) = when {
            lower.contains("workout") || lower.contains("gym") || lower.contains("energy") -> {
                Triple(true, "High Energy Workout Mix", allSongs.filter { it.mood.contains("Workout", true) || it.genre.contains("EDM", true) })
            }
            lower.contains("drive") || lower.contains("night") -> {
                Triple(true, "Late Night Drive Acoustics", allSongs.filter { it.mood.contains("Chill", true) || it.mood.contains("Romance", true) })
            }
            lower.contains("romantic") || lower.contains("romance") || lower.contains("love") -> {
                Triple(true, "Romantic Acoustic Ballads", allSongs.filter { it.mood.contains("Romance", true) })
            }
            lower.contains("chill") || lower.contains("relax") || lower.contains("peaceful") || lower.contains("calm") -> {
                Triple(true, "Calm & Serene Soundscapes", allSongs.filter { it.mood.contains("Chill", true) || it.mood.contains("Focus", true) })
            }
            lower.contains("party") || lower.contains("dance") || lower.contains("club") -> {
                Triple(true, "Club & Party Beats", allSongs.filter { it.mood.contains("Party", true) || it.genre.contains("Dance", true) })
            }
            lower.contains("devotional") || lower.contains("spiritual") || lower.contains("bhajan") -> {
                Triple(true, "Spiritual & Sacred Chants", allSongs.filter { it.mood.contains("Devotional", true) || it.genre.contains("Devotional", true) })
            }
            lower.contains("acoustic") || lower.contains("unplugged") -> {
                Triple(true, "Pure Acoustic Sessions", allSongs.filter { it.genre.contains("Acoustic", true) || it.mood.contains("Chill", true) })
            }
            else -> Triple(false, "", emptyList())
        }

        if (isMoodOrPlaylist && moodSongs.isNotEmpty()) {
            return VoiceSearchResult(
                spokenQuery = trimmed,
                intent = VoiceSearchIntent.CURATE_PLAYLIST,
                aiResponseSummary = "Swarix AI curated \"$moodOrThemeTitle\" with ${moodSongs.size} tracks tailored to your prompt.",
                matchedSongs = moodSongs,
                matchedArtists = moodSongs.map { it.artist }.distinct(),
                suggestedPlaylistTitle = moodOrThemeTitle,
                autoPlayCandidate = if (isExplicitPlay) moodSongs.first() else null
            )
        }

        // 5. Existing Playlist Search
        val matchedPlaylist = playlists.firstOrNull { it.name.contains(cleaned, ignoreCase = true) }
        if (matchedPlaylist != null) {
            return VoiceSearchResult(
                spokenQuery = trimmed,
                intent = VoiceSearchIntent.CURATE_PLAYLIST,
                aiResponseSummary = "Found playlist \"${matchedPlaylist.name}\" in your library.",
                matchedSongs = allSongs.take(5),
                matchedArtists = emptyList(),
                suggestedPlaylistTitle = matchedPlaylist.name
            )
        }

        // 6. General Keyword Discovery
        val generalMatches = allSongs.filter { song ->
            val matchesKeywords = song.title.contains(cleaned, ignoreCase = true) ||
                    song.artist.contains(cleaned, ignoreCase = true) ||
                    song.album.contains(cleaned, ignoreCase = true) ||
                    song.genre.contains(cleaned, ignoreCase = true)

            // Basic natural language date filtering
            if (cleaned.contains("last month")) {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                matchesKeywords && song.dateAdded >= thirtyDaysAgo
            } else {
                matchesKeywords
            }
        }

        if (generalMatches.isNotEmpty()) {
            return VoiceSearchResult(
                spokenQuery = trimmed,
                intent = VoiceSearchIntent.GENERAL_SEARCH,
                aiResponseSummary = "Here are ${generalMatches.size} songs matching \"$trimmed\".",
                matchedSongs = generalMatches,
                matchedArtists = generalMatches.map { it.artist }.distinct(),
                suggestedPlaylistTitle = "Search: $trimmed",
                autoPlayCandidate = if (isExplicitPlay) generalMatches.first() else null
            )
        }

        // 7. Fallback: Smart AI Recommendations for unrecognized prompt
        val fallbackRecommendations = SmartPlaylistGenerator.generateCustomSmartPlaylist(
            prompt = trimmed,
            allSongs = allSongs,
            favoriteSongs = allSongs.filter { it.isFavorite }
        )

        return VoiceSearchResult(
            spokenQuery = trimmed,
            intent = VoiceSearchIntent.GENERAL_SEARCH,
            aiResponseSummary = "I couldn't find an exact track for \"$trimmed\", but Swarix AI curated these recommended songs for you.",
            matchedSongs = fallbackRecommendations.songs,
            matchedArtists = fallbackRecommendations.songs.map { it.artist }.distinct(),
            suggestedPlaylistTitle = "Curated for \"$trimmed\""
        )
    }
}
