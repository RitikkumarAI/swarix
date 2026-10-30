package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val artworkUri: String,
    val genre: String,
    val mood: String,
    val lyrics: String,
    val isFavorite: Boolean,
    val playCount: Int,
    val dateAdded: Long,
    val isLocal: Boolean
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val coverUri: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String,
    val orderPosition: Int = 0
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey val name: String,
    val bandLevelsCsv: String,
    val bassBoost: Int,
    val virtualizer: Int,
    val isCustom: Boolean = false
)
@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
