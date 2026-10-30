package com.example.data.remote

import android.util.Log
import com.example.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object JioSaavnService {
    private const val TAG = "JioSaavnService"
    private const val DES_KEY = "38346591"
    private const val BASE_URL = "https://www.jiosaavn.com/api.php"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun decryptMediaUrl(encryptedUrl: String): String {
        if (encryptedUrl.isBlank()) return ""
        return try {
            val keyBytes = DES_KEY.toByteArray(Charsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = try {
                java.util.Base64.getDecoder().decode(encryptedUrl.trim())
            } catch (t: Throwable) {
                android.util.Base64.decode(encryptedUrl.trim(), android.util.Base64.DEFAULT)
            }
            val decrypted = cipher.doFinal(decoded)
            val rawUrl = String(decrypted, Charsets.UTF_8)
            rawUrl.replace("_96.mp4", "_320.mp4")
                .replace("_160.mp4", "_320.mp4")
                .replace("http://", "https://")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt media url", e)
            ""
        }
    }

    private fun cleanHtml(text: String): String {
        return text.replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    suspend fun searchSongs(query: String, limit: Int = 25): List<SongEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val songs = mutableListOf<SongEntity>()
        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "$BASE_URL?__call=search.getResults&q=$encodedQuery&_format=json&_marker=0&api_version=4&ctx=android&n=$limit&p=1"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return@withContext emptyList()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val id = item.optString("id", "")
                val title = cleanHtml(item.optString("title", "Unknown Track"))
                val rawImage = item.optString("image", "")
                val hdImage = rawImage.replace("150x150", "500x500")

                val moreInfo = item.optJSONObject("more_info") ?: continue
                val encUrl = moreInfo.optString("encrypted_media_url", "")
                val audioUrl = decryptMediaUrl(encUrl)
                if (audioUrl.isBlank()) continue

                val musicArtist = moreInfo.optString("music", "")
                val subtitle = item.optString("subtitle", "")
                val artist = cleanHtml(if (musicArtist.isNotBlank()) musicArtist else subtitle.ifBlank { "Various Artists" })
                val album = cleanHtml(moreInfo.optString("album", "Swarix Music"))
                val durationSec = moreInfo.optString("duration", "240").toLongOrNull() ?: 240L
                val durationMs = durationSec * 1000L

                songs.add(
                    SongEntity(
                        id = "online_$id",
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = durationMs,
                        contentUri = audioUrl,
                        artworkUri = hdImage,
                        genre = "Bollywood / Pop",
                        mood = "Trending",
                        lyrics = "Streaming track: $title by $artist",
                        isFavorite = false,
                        playCount = 0,
                        dateAdded = System.currentTimeMillis() - (i * 1000),
                        isLocal = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search query failed: $query", e)
        }
        songs
    }

    suspend fun getTrendingSongs(limit: Int = 25): List<SongEntity> {
        val trendingQueries = listOf("trending hindi songs", "top bollywood hits", "punjabi top hits")
        for (q in trendingQueries) {
            val results = searchSongs(q, limit)
            if (results.isNotEmpty()) {
                return results
            }
        }
        return emptyList()
    }
}
