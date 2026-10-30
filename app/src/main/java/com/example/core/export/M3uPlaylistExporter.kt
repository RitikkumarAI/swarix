package com.example.core.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.core.model.Song
import com.example.data.local.MusicDao
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Standardized M3U / M3U8 Playlist Exporter.
 * Maps AI-generated 'Mood Radio' playlists to industry-standard Extended M3U playlist format,
 * querying Room database for track duration and content URIs, and triggers Android native
 * share/export intents for cross-platform interoperability.
 */
object M3uPlaylistExporter {

    private const val TAG = "M3uPlaylistExporter"

    data class ExportResult(
        val file: File,
        val contentUri: Uri,
        val trackCount: Int,
        val formattedSize: String
    )

    /**
     * Builds standard Extended M3U formatted text for the playlist.
     */
    fun buildM3uString(
        mood: String,
        songs: List<Song>,
        databaseSongsMap: Map<String, Song> = emptyMap()
    ): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#PLAYLIST:Swarix AI Mood Radio - ").append(mood).append("\n")
        sb.append("#EXTENC:UTF-8\n")

        for (song in songs) {
            val dbSong = databaseSongsMap[song.id] ?: song
            val durationSec = (dbSong.durationMs / 1000).coerceAtLeast(1)
            val artist = dbSong.artist.ifBlank { "Unknown Artist" }.replace(",", "-")
            val title = dbSong.title.ifBlank { "Untitled" }

            // #EXTINF:<seconds>,<artist> - <title>
            sb.append("#EXTINF:").append(durationSec).append(",").append(artist).append(" - ").append(title).append("\n")

            // Track location path / URI
            val trackUri = dbSong.contentUri.ifBlank { "content://swarix/tracks/${dbSong.id}" }
            sb.append(trackUri).append("\n")
        }

        return sb.toString()
    }

    /**
     * Exports the Mood Radio playlist to a .m3u8 file on disk and launches the
     * system share/export sheet using FileProvider.
     */
    suspend fun exportAndShareMoodPlaylist(
        context: Context,
        mood: String,
        songs: List<Song>,
        musicDao: MusicDao? = null
    ): Result<ExportResult> {
        return try {
            if (songs.isEmpty()) {
                return Result.failure(IllegalArgumentException("Cannot export empty playlist"))
            }

            // Map library tracks via Room database if available
            val dbSongsMap = mutableMapOf<String, Song>()
            if (musicDao != null) {
                try {
                    for (song in songs) {
                        val entity = musicDao.getSongById(song.id)
                        if (entity != null) {
                            dbSongsMap[song.id] = Song(
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
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed mapping Room entities for export: ${e.message}")
                }
            }

            val m3uContent = buildM3uString(mood, songs, dbSongsMap)

            val exportDir = File(context.cacheDir, "playlists")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val sanitizedMood = mood.trim().replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "Swarix_MoodRadio_${sanitizedMood}.m3u8"
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(m3uContent)
                    writer.flush()
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/x-mpegurl"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Swarix AI Mood Radio: $mood Playlist")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Exported AI-generated '$mood' Mood Radio playlist from Swarix Music (${songs.size} tracks)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export '$mood' M3U Playlist").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            val formattedSize = "${file.length() / 1024 + 1} KB"
            Log.d(TAG, "Successfully exported M3U8 playlist to: ${file.absolutePath} ($formattedSize)")

            Result.success(ExportResult(file, contentUri, songs.size, formattedSize))
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting M3U playlist", e)
            Result.failure(e)
        }
    }
}
