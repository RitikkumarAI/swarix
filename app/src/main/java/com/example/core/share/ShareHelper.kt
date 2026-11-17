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

    /**
     * Generates a high-resolution aesthetic lyric card image for Instagram Stories & WhatsApp Status.
     */
    fun shareLyricStoryCard(
        context: Context,
        song: Song,
        lines: List<String>,
        theme: String = "Obsidian Gold"
    ) {
        try {
            val width = 1080
            val height = 1920
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // 1. Background Gradient based on theme
            val gradientColors = when (theme) {
                "Emerald Beats" -> intArrayOf(
                    android.graphics.Color.parseColor("#051F14"),
                    android.graphics.Color.parseColor("#0A3B25"),
                    android.graphics.Color.parseColor("#020B07")
                )
                "Neon Sunset" -> intArrayOf(
                    android.graphics.Color.parseColor("#2E0854"),
                    android.graphics.Color.parseColor("#801550"),
                    android.graphics.Color.parseColor("#0D021A")
                )
                "Sapphire Night" -> intArrayOf(
                    android.graphics.Color.parseColor("#0B1B3D"),
                    android.graphics.Color.parseColor("#1B3B6F"),
                    android.graphics.Color.parseColor("#040814")
                )
                else -> intArrayOf( // Obsidian Gold
                    android.graphics.Color.parseColor("#141008"),
                    android.graphics.Color.parseColor("#261E0E"),
                    android.graphics.Color.parseColor("#080705")
                )
            }

            val bgShader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                gradientColors, floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            val bgPaint = android.graphics.Paint().apply { shader = bgShader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Decorative Ambient Circles
            val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                val accentColor = when (theme) {
                    "Emerald Beats" -> android.graphics.Color.parseColor("#1ED760")
                    "Neon Sunset" -> android.graphics.Color.parseColor("#E040FB")
                    "Sapphire Night" -> android.graphics.Color.parseColor("#00E5FF")
                    else -> android.graphics.Color.parseColor("#D7BC83")
                }
                color = accentColor
                alpha = 35
                maskFilter = android.graphics.BlurMaskFilter(180f, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(width * 0.3f, height * 0.25f, 320f, glowPaint)
            canvas.drawCircle(width * 0.75f, height * 0.7f, 380f, glowPaint)

            // 3. Central Card Surface
            val cardRect = android.graphics.RectF(90f, 320f, width - 90f, height - 320f)
            val cardPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#161616")
                alpha = 230
            }
            val cardBorderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                alpha = 35
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)
            canvas.drawRoundRect(cardRect, 48f, 48f, cardBorderPaint)

            // 4. Header: Swarix Logo / Badge
            val appBadgePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = when (theme) {
                    "Emerald Beats" -> android.graphics.Color.parseColor("#1ED760")
                    "Neon Sunset" -> android.graphics.Color.parseColor("#FF4081")
                    "Sapphire Night" -> android.graphics.Color.parseColor("#40C4FF")
                    else -> android.graphics.Color.parseColor("#E5C07B")
                }
                textSize = 34f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                letterSpacing = 0.15f
            }
            canvas.drawText("SWARIX MUSIC  •  LIVE LYRICS", 160f, 430f, appBadgePaint)

            // 5. Track Info (Title & Artist)
            val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 58f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val displayTitle = if (song.title.length > 28) song.title.take(26) + "..." else song.title
            canvas.drawText(displayTitle, 160f, 520f, titlePaint)

            val artistPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#B3B3B3")
                textSize = 38f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            }
            canvas.drawText(song.artist, 160f, 580f, artistPaint)

            // Divider line
            val dividerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = 30
                strokeWidth = 2f
            }
            canvas.drawLine(160f, 630f, width - 160f, 630f, dividerPaint)

            // 6. Selected Lyric Lines
            val quoteMarkPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = appBadgePaint.color
                textSize = 110f
                alpha = 180
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            }
            canvas.drawText("“", 160f, 740f, quoteMarkPaint)

            val lyricPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 52f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            }

            var currentY = 820f
            val maxLines = lines.take(4)
            for (line in maxLines) {
                // Word wrapping within 760px
                val words = line.split(" ")
                var currentTextLine = ""
                for (word in words) {
                    val test = if (currentTextLine.isEmpty()) word else "$currentTextLine $word"
                    if (lyricPaint.measureText(test) < (width - 320f)) {
                        currentTextLine = test
                    } else {
                        canvas.drawText(currentTextLine, 160f, currentY, lyricPaint)
                        currentY += 72f
                        currentTextLine = word
                    }
                }
                if (currentTextLine.isNotEmpty()) {
                    canvas.drawText(currentTextLine, 160f, currentY, lyricPaint)
                    currentY += 88f
                }
            }

            canvas.drawText("”", width - 220f, currentY + 20f, quoteMarkPaint)

            // 7. Footer: "Sound for Every Story • Swarix"
            val footerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#9E9E9E")
                textSize = 32f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("🎵 Sound for Every Story  •  8K Ultra Studio Player", width / 2f, height - 370f, footerPaint)

            // 8. Save to cache and launch Share Intent
            val cacheDir = File(context.cacheDir, "lyric_cards").apply { if (!exists()) mkdirs() }
            val cardFile = File(cacheDir, "swarix_lyric_story.png")
            val outputStream = java.io.FileOutputStream(cardFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cardFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "“${maxLines.joinToString(" ")}”\n— ${song.title} by ${song.artist} on Swarix Music 🎧")
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Lyric Card to Story")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Failed to generate lyric story card", e)
        }
    }
}
