package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

data class ExtractedAudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val artworkUri: String
)

class LocalMediaMetadataHelper(private val context: Context) {

    private val coversDir: File by lazy {
        File(context.cacheDir, "covers").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Extracts ID3 tags and embedded album art from a local audio Uri or path
     * using MediaMetadataRetriever.
     */
    fun extractMetadata(uri: Uri, identifier: String): ExtractedAudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Track"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() } ?: "Local Music"
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() } ?: "Local Audio"
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            var artworkUri = ""
            val pictureBytes = retriever.embeddedPicture
            if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                artworkUri = saveEmbeddedPictureToCache(identifier, pictureBytes)
            }

            ExtractedAudioMetadata(
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                durationMs = durationMs,
                artworkUri = artworkUri
            )
        } catch (e: Exception) {
            Log.e("LocalMediaMetadataHelper", "Failed to extract metadata for $uri: ${e.message}")
            ExtractedAudioMetadata(
                title = "Local Track",
                artist = "Unknown Artist",
                album = "Local Music",
                genre = "Local Audio",
                durationMs = 0L,
                artworkUri = ""
            )
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Saves raw picture bytes extracted by MediaMetadataRetriever to an app cache image file
     * and returns the file Uri string compatible with Coil.
     */
    private fun saveEmbeddedPictureToCache(identifier: String, pictureBytes: ByteArray): String {
        return try {
            val sanitizedId = identifier.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val targetFile = File(coversDir, "cover_$sanitizedId.jpg")

            if (targetFile.exists() && targetFile.length() > 0) {
                return Uri.fromFile(targetFile).toString()
            }

            val bitmap = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size)
            if (bitmap != null) {
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                }
                bitmap.recycle()
                Uri.fromFile(targetFile).toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("LocalMediaMetadataHelper", "Failed to cache album art for $identifier: ${e.message}")
            ""
        }
    }
}
