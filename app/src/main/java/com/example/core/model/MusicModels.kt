package com.example.core.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val contentUri: String,
    val artworkUri: String = "",
    val genre: String = "Pop",
    val mood: String = "Chill",
    val lyrics: String = "",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val isLocal: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverUri: String = "",
    val trackCount: Int = 0,
    val isSmart: Boolean = false
)

data class Artist(
    val name: String,
    val songCount: Int = 0,
    val artworkUri: String = ""
)

data class Album(
    val title: String,
    val artist: String,
    val songCount: Int = 0,
    val artworkUri: String = "",
    val year: String = ""
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val presetName: String = "Custom",
    val bandLevels: List<Int> = listOf(3, 2, 1, 0, 1, 2, 4, 3, 2, 2), // 10 bands: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
    val bassBoost: Int = 45, // 0 to 100
    val virtualizer: Int = 30, // 0 to 100
    val isAutoGenreEnabled: Boolean = true,
    val detectedGenre: String = "Bollywood Romance"
)

data class FriendActivity(
    val id: String,
    val friendName: String,
    val avatarUrl: String,
    val songTitle: String,
    val artistName: String,
    val albumArtUrl: String,
    val statusText: String,
    val isListeningNow: Boolean = true,
    val songId: String = ""
)

data class GenreStats(
    val genre: String,
    val percentage: Int,
    val trackCount: Int
)

data class ListeningStats(
    val totalMinutesListened: Int,
    val totalTracksPlayed: Int,
    val topArtists: List<Pair<String, Int>>,
    val topGenres: List<GenreStats>,
    val streakDays: Int = 7,
    val topSongs: List<Song> = emptyList(),
    val soundPersonality: String = "Nocturnal Audiophile",
    val personalityDescription: String = "You seek deep, resonant melodies when the world goes quiet. Late-night sessions define your sonic identity.",
    val topMood: String = "Romance & Chill",
    val weeklyActivityHours: List<Pair<String, Float>> = listOf(
        "Mon" to 1.8f,
        "Tue" to 2.4f,
        "Wed" to 3.1f,
        "Thu" to 2.0f,
        "Fri" to 4.5f,
        "Sat" to 5.2f,
        "Sun" to 3.8f
    ),
    val listeningGoalMinutes: Int = 2000,
    val peakListeningTime: String = "10:30 PM - 1:00 AM",
    val artistLoyaltyPercentage: Int = 42
)

