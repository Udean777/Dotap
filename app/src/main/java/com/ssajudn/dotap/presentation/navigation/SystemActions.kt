package com.ssajudn.dotap.presentation.navigation

data class SystemActions(
    val onOpenAccessibilitySettings: () -> Unit,
    val onOpenAppInfo: () -> Unit,
)
