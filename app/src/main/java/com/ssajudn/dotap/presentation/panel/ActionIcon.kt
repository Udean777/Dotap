package com.ssajudn.dotap.presentation.panel

import androidx.annotation.DrawableRes
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType

/** Null for actions that are never shown in the panel (see [ActionType.canBePanelSlot]). */
@DrawableRes
internal fun ActionType.iconRes(): Int? = ICONS[this]

private val ICONS: Map<ActionType, Int> = mapOf(
    ActionType.SCREENSHOT to R.drawable.ic_panel_screenshot,
    ActionType.HOME to R.drawable.ic_panel_home,
    ActionType.BACK to R.drawable.ic_panel_back,
    ActionType.RECENTS to R.drawable.ic_panel_recents,
    ActionType.NOTIFICATIONS to R.drawable.ic_panel_notifications,
    ActionType.QUICK_SETTINGS to R.drawable.ic_panel_quick_settings,
    ActionType.POWER_DIALOG to R.drawable.ic_panel_power,
    ActionType.LOCK_SCREEN to R.drawable.ic_panel_lock,
)
