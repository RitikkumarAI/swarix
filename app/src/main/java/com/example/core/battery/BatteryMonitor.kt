package com.example.core.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Monitors device battery level, charging status, and system power-save mode.
 * Automatically engages Battery Saver when battery falls below 20%, suppressing
 * tactile bass haptic feedback and throttling background AI metadata enrichment.
 */
class BatteryMonitor(private val context: Context) {

    companion object {
        private const val TAG = "BatteryMonitor"
        const val LOW_BATTERY_THRESHOLD_PERCENT = 20
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _isLowBattery = MutableStateFlow(false)
    val isLowBattery: StateFlow<Boolean> = _isLowBattery.asStateFlow()

    private val _isPowerSaveMode = MutableStateFlow(false)
    val isPowerSaveMode: StateFlow<Boolean> = _isPowerSaveMode.asStateFlow()

    private val _isBatterySaverEngaged = MutableStateFlow(false)
    val isBatterySaverEngaged: StateFlow<Boolean> = _isBatterySaverEngaged.asStateFlow()

    private var manualBatterySaverSetting = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateBatteryState(it) }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        stickyIntent?.let { updateBatteryState(it) }
    }

    fun setManualBatterySaver(enabled: Boolean) {
        manualBatterySaverSetting = enabled
        recomputeEngagement()
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) {
            (level * 100f / scale).toInt().coerceIn(0, 100)
        } else {
            100
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val powerSave = powerManager?.isPowerSaveMode ?: false

        val low = percent < LOW_BATTERY_THRESHOLD_PERCENT && !charging

        _batteryLevel.value = percent
        _isCharging.value = charging
        _isLowBattery.value = low
        _isPowerSaveMode.value = powerSave

        recomputeEngagement()
    }

    private fun recomputeEngagement() {
        val engaged = manualBatterySaverSetting || _isLowBattery.value || _isPowerSaveMode.value
        val previous = _isBatterySaverEngaged.value
        _isBatterySaverEngaged.value = engaged

        if (previous != engaged) {
            Log.d(TAG, "Battery saver engaged state changed: $engaged (level: ${_batteryLevel.value}%, manual: $manualBatterySaverSetting, low: ${_isLowBattery.value})")
        }
    }

    fun release() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
