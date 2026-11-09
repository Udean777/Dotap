package com.ssajudn.dotap.domain.gesture

import com.ssajudn.dotap.domain.model.Defaults

data class GestureConfig(
    val doubleTapTimeoutMs: Long = Defaults.DOUBLE_TAP_TIMEOUT_MS,
    val longPressDurationMs: Long = Defaults.LONG_PRESS_DURATION_MS,
    val touchSlopPx: Float = 0f,
    val isDoubleTapAssigned: Boolean = true,
    val isLongPressAssigned: Boolean = false,
)
