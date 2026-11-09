package com.ssajudn.dotap.presentation.settings

sealed interface SettingsUiEvent {
    data class HapticsToggled(val enabled: Boolean) : SettingsUiEvent

    data object OpenGesturesClicked : SettingsUiEvent

    data object OpenAppearanceClicked : SettingsUiEvent

    data object OpenBatteryGuidanceClicked : SettingsUiEvent

    data object BackClicked : SettingsUiEvent
}
