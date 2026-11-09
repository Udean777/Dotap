package com.ssajudn.dotap.domain.model

/**
 * @property canBePanelSlot whether the action makes sense as a panel slot. Opening the panel from itself
 * and an empty slot would be pointless, so they are never shown there.
 */
enum class ActionType(val canBePanelSlot: Boolean = true) {
    SCREENSHOT,
    HOME,
    BACK,
    RECENTS,
    NOTIFICATIONS,
    QUICK_SETTINGS,
    POWER_DIALOG,
    LOCK_SCREEN,
    OPEN_PANEL(canBePanelSlot = false),
    NONE(canBePanelSlot = false),
}
