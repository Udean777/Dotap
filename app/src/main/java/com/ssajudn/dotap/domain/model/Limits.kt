package com.ssajudn.dotap.domain.model

object Limits {
    const val MIN_DOT_SIZE_DP: Int = 32
    const val MAX_DOT_SIZE_DP: Int = 72

    const val MIN_IDLE_OPACITY_PERCENT: Int = 15
    const val MAX_IDLE_OPACITY_PERCENT: Int = 100

    const val MIN_DOUBLE_TAP_TIMEOUT_MS: Long = 150L
    const val MAX_DOUBLE_TAP_TIMEOUT_MS: Long = 600L

    const val MIN_LONG_PRESS_DURATION_MS: Long = 300L
    const val MAX_LONG_PRESS_DURATION_MS: Long = 1500L

    const val MIN_PANEL_SLOTS: Int = 3
    const val MAX_PANEL_SLOTS: Int = 8

    const val MIN_Y_RATIO: Float = 0.05f
    const val MAX_Y_RATIO: Float = 0.95f
}
