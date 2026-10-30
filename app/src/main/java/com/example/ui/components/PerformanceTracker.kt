package com.example.ui.components

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.FrameMetrics
import androidx.annotation.RequiresApi

object PerformanceTracker {
    private const val TAG = "PerfTracker"
    private var isTracking = false

    @RequiresApi(Build.VERSION_CODES.N)
    fun startTracking(activity: Activity) {
        if (isTracking) return
        try {
            val window = activity.window ?: return
            val handler = Handler(Looper.getMainLooper())
            window.addOnFrameMetricsAvailableListener({ _, frameMetrics, _ ->
                val totalDuration = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
                // Log if frame takes more than 16ms (60fps) or 8ms (120fps)
                if (totalDuration > 16_000_000) {
                    Log.w(TAG, "Frame dropped: ${totalDuration / 1_000_000}ms")
                }
            }, handler)
            isTracking = true
            Log.d(TAG, "Frame metrics tracking started with valid main looper handler")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start frame metrics tracking safely", e)
        }
    }
}
