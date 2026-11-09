package com.ssajudn.dotap.presentation.common

import androidx.annotation.StringRes
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType

@StringRes
fun ActionType.labelRes(): Int = LABELS.getValue(this)

private val LABELS: Map<ActionType, Int> = mapOf(
    ActionType.SCREENSHOT to R.string.action_screenshot,
    ActionType.HOME to R.string.action_home,
    ActionType.BACK to R.string.action_back,
    ActionType.RECENTS to R.string.action_recents,
    ActionType.NOTIFICATIONS to R.string.action_notifications,
    ActionType.QUICK_SETTINGS to R.string.action_quick_settings,
    ActionType.POWER_DIALOG to R.string.action_power_dialog,
    ActionType.LOCK_SCREEN to R.string.action_lock_screen,
    ActionType.OPEN_PANEL to R.string.action_open_panel,
    ActionType.NONE to R.string.action_none,
)
