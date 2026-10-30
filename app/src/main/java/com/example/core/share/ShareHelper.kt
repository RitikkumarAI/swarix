package com.example.core.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.core.model.Song
import java.io.File

object ShareHelper {
    fun shareTrack(context: Context, song: Song) {
        val shareText = "🎵 Currently jamming to '${song.title}' by ${song.artist} (${song.album.ifEmpty { "Single" }}) on Swarix Music! 🎧✨\n\nExperience high-fidelity audio & AI-powered playlists."
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share Track via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun sharePlaylistSummary(context: Context, playlistTitle: String, description: String, songs: List<Song>) {
        val songListStr = songs.take(5).joinToString("\n") { "• '${it.title}' by ${it.artist}" }
        val shareText = "🔥 Check out my AI-generated playlist '$playlistTitle' on Swarix Music!\n$description\n\nFeatured Tracks:\n$songListStr\n...\n✨ Powered by Gemini AI & Swarix Music."
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share AI Playlist via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun exportPlaylistAsM3U(context: Context, playlistName: String, songs: List<Song>) {
        val m3uBuilder = StringBuilder()
        m3uBuilder.append("#EXTM3U\n")
        m3uBuilder.append("#PLAYLIST:$playlistName\n")
        for (song in songs) {
            val durationSec = 180
            m3uBuilder.append("#EXTINF:$durationSec,${song.artist} - ${song.title}\n")
            m3uBuilder.append("${song.contentUri}\n")
        }

        try {
            val fileName = "${playlistName.replace("\\s+".toRegex(), "_")}_playlist.m3u"
            val file = File(context.cacheDir, fileName)
            file.writeText(m3uBuilder.toString())

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "audio/x-mpegurl"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Export M3U Playlist")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
