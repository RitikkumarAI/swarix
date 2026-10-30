package com.example.playback

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostic logger that hooks into ExoPlayer's AnalyticsListener to capture detailed
 * playback telemetry for onPlayerError, onPlaybackStateChanged, onMediaMetadataChanged,
 * and audio underruns. Dumps structured logs to Logcat when playback fails or when requested.
 */
class PlaybackDiagnosticLogger : AnalyticsListener {

    companion object {
        private const val TAG = "PlaybackDiagnosticLogger"
        private const val MAX_LOG_HISTORY = 50
    }

    data class DiagnosticEntry(
        val timestampMs: Long,
        val category: String,
        val details: String
    )

    private val logHistory = mutableListOf<DiagnosticEntry>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    @Synchronized
    private fun recordEvent(category: String, details: String) {
        val entry = DiagnosticEntry(System.currentTimeMillis(), category, details)
        if (logHistory.size >= MAX_LOG_HISTORY) {
            logHistory.removeAt(0)
        }
        logHistory.add(entry)
        Log.d(TAG, "[$category] $details")
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        val errorDetails = "ErrorCode=${error.errorCode} (${error.errorCodeName}): ${error.message}"
        recordEvent("PLAYER_ERROR", errorDetails)
        dumpLogsToLogcat("ON_PLAYER_ERROR (${error.errorCodeName})")
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        val stateName = when (state) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN($state)"
        }
        recordEvent("STATE_CHANGE", "State -> $stateName (RealtimePosMs=${eventTime.currentPlaybackPositionMs})")
    }

    override fun onMediaMetadataChanged(
        eventTime: AnalyticsListener.EventTime,
        mediaMetadata: MediaMetadata
    ) {
        val metaInfo = "Title='${mediaMetadata.title}', Artist='${mediaMetadata.artist}', Album='${mediaMetadata.albumTitle}'"
        recordEvent("METADATA_CHANGE", metaInfo)
    }

    override fun onAudioAttributesChanged(
        eventTime: AnalyticsListener.EventTime,
        audioAttributes: AudioAttributes
    ) {
        val attrInfo = "ContentType=${audioAttributes.contentType}, Usage=${audioAttributes.usage}, Flags=${audioAttributes.flags}"
        recordEvent("AUDIO_ATTRIBUTES", attrInfo)
    }

    override fun onAudioUnderrun(
        eventTime: AnalyticsListener.EventTime,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long
    ) {
        val underrunInfo = "BufferSize=$bufferSize, BufferSizeMs=$bufferSizeMs, ElapsedMs=$elapsedSinceLastFeedMs"
        recordEvent("AUDIO_UNDERRUN", underrunInfo)
    }

    /**
     * Dumps the accumulated diagnostic log history to Logcat with clear formatting.
     */
    @Synchronized
    fun dumpLogsToLogcat(triggerReason: String = "Manual Diagnostic Dump") {
        val sb = StringBuilder()
        sb.append("\n=================== EXOPLAYER DIAGNOSTIC LOG DUMP ===================\n")
        sb.append("Trigger Reason: ").append(triggerReason).append("\n")
        sb.append("Total Events Recorded: ").append(logHistory.size).append("\n")
        sb.append("--------------------------------------------------------------------\n")

        for (entry in logHistory) {
            val timeStr = dateFormat.format(Date(entry.timestampMs))
            sb.append(String.format("%s | %-16s | %s\n", timeStr, entry.category, entry.details))
        }

        sb.append("====================================================================")
        Log.e(TAG, sb.toString())
    }

    @Synchronized
    fun getLogHistory(): List<DiagnosticEntry> {
        return logHistory.toList()
    }
}
