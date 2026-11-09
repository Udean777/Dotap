package com.ssajudn.dotap.presentation.settings

sealed interface SettingsUiEffect {
    data object NavigateToGestures : SettingsUiEffect

    data object NavigateToAppearance : SettingsUiEffect

    data object NavigateBack : SettingsUiEffect
}
