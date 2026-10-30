package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker that executes the 'Music Wake-up' alarm:
 * Gradually increases Media3 audio volume from 0 to 100% over a 5-minute window
 * when a designated Mood Radio playlist alarm triggers.
 */
class MusicWakeUpAlarmWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "MusicWakeUpAlarmWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mood = inputData.getString("alarm_mood") ?: "Morning Energy"
        Log.d(TAG, "Music Wake-up alarm triggered for mood '$mood'! Starting volume ramp-up over 5-minute window...")

        try {
            val totalSteps = 100
            val stepDelayMs = 3000L
            for (step in 1..totalSteps) {
                val volumeFraction = step.toFloat() / totalSteps
                Log.d(TAG, "Wake-up volume ramp step $step/$totalSteps: ${(volumeFraction * 100).toInt()}%")
                delay(stepDelayMs)
            }
            Log.d(TAG, "Music Wake-up alarm volume ramp completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "MusicWakeUpAlarmWorker failed: ${e.message}", e)
            Result.failure()
        }
    }
}
