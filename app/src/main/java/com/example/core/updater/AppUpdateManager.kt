package com.example.core.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val latestVersion: String,
    val releaseTitle: String,
    val changelog: String,
    val apkDownloadUrl: String,
    val hasUpdate: Boolean
)

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val RELEASES_API_URL = "https://api.github.com/repos/RitikkumarAI/swarix/releases/latest"
    private const val FALLBACK_APK_URL = "https://github.com/RitikkumarAI/swarix/releases/download/v1.1/Swarix.apk"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(currentVersionName: String): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RELEASES_API_URL)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "Swarix-Android-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "latest").removePrefix("v")
            val releaseTitle = json.optString("name", "New Update")
            val changelog = json.optString("body", "Bug fixes & new features.")

            var downloadUrl = FALLBACK_APK_URL
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true) && !name.contains("debug", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", FALLBACK_APK_URL)
                        break
                    }
                }
            }

            val hasUpdate = isVersionNewer(tagName, currentVersionName)
            AppUpdateInfo(
                latestVersion = tagName,
                releaseTitle = releaseTitle,
                changelog = changelog,
                apkDownloadUrl = downloadUrl,
                hasUpdate = hasUpdate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates", e)
            null
        }
    }

    private fun isVersionNewer(remote: String, local: String): Boolean {
        if (remote.equals("latest", ignoreCase = true)) return false
        val rParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val lParts = local.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(rParts.size, lParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("User-Agent", "Swarix-Android-App")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body ?: return@withContext null
            val contentLength = body.contentLength()

            val targetFile = File(context.cacheDir, "swarix_update.apk")
            if (targetFile.exists()) {
                targetFile.delete()
            }

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val progress = totalRead.toFloat() / contentLength.toFloat()
                            withContext(Dispatchers.Main) {
                                onProgress(progress.coerceIn(0f, 1f))
                            }
                        }
                    }
                    output.flush()
                }
            }
            targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update APK", e)
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
        }
    }
}

