package com.example.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

object HapticFeedbackHelper {

    fun triggerClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun triggerToggle(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun triggerVolumeChange(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}

@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val view = LocalView.current
    return remember(view) { HapticFeedbackHelper }
}
