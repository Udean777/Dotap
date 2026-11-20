package com.ssajudn.dotap.presentation.panelsettings

sealed interface PanelSettingsUiEffect {
    data object NavigateBack : PanelSettingsUiEffect
}
