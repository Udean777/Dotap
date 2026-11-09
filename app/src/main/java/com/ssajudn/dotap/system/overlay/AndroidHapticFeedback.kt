package com.ssajudn.dotap.system.overlay

import android.view.HapticFeedbackConstants
import android.view.View
import com.ssajudn.dotap.domain.port.HapticFeedback

class AndroidHapticFeedback(
    private val viewProvider: () -> View?,
) : HapticFeedback {
    override fun tick() {
        val view = viewProvider() ?: return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
