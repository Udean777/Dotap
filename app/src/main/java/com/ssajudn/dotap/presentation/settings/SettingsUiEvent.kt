package com.ssajudn.dotap.presentation.settings

import com.ssajudn.dotap.domain.model.AppLanguage
import com.ssajudn.dotap.domain.model.ThemeMode

sealed interface SettingsUiEvent {
    data class HapticsToggled(val enabled: Boolean) : SettingsUiEvent

    data class ThemeModeSelected(val mode: ThemeMode) : SettingsUiEvent

    data class LanguageSelected(val language: AppLanguage) : SettingsUiEvent

    data object OpenGesturesClicked : SettingsUiEvent

    data object OpenAppearanceClicked : SettingsUiEvent

    data object OpenPanelSettingsClicked : SettingsUiEvent

    data object OpenBatteryGuidanceClicked : SettingsUiEvent

    data object BackClicked : SettingsUiEvent
}
