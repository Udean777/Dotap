package com.ssajudn.dotap.domain.model

object Defaults {
    const val DOT_ENABLED: Boolean = true
    const val DOT_SIZE_DP: Int = 48
    const val IDLE_OPACITY_PERCENT: Int = 40
    const val IDLE_DELAY_MS: Long = 3000L
    const val DOUBLE_TAP_TIMEOUT_MS: Long = 300L
    const val LONG_PRESS_DURATION_MS: Long = 500L
    const val SCREENSHOT_HIDE_DELAY_MS: Long = 150L
    const val SCREENSHOT_RESTORE_DELAY_MS: Long = 800L
    const val HAPTICS_ENABLED: Boolean = true
    const val SNAP_TO_EDGE: Boolean = true
    const val DOT_SIDE_RIGHT: Boolean = true
    const val DOT_Y_RATIO: Float = 0.4f

    val ACTION_SINGLE_TAP: ActionType = ActionType.HOME
    val ACTION_DOUBLE_TAP: ActionType = ActionType.SCREENSHOT
    val ACTION_LONG_PRESS: ActionType = ActionType.NONE

    val DEFAULT_PANEL_SLOTS: List<ActionType> =
        listOf(
            ActionType.HOME,
            ActionType.BACK,
            ActionType.RECENTS,
            ActionType.SCREENSHOT,
            ActionType.NOTIFICATIONS,
            ActionType.QUICK_SETTINGS,
        )
}
