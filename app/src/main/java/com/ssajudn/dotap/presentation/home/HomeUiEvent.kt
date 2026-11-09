package com.ssajudn.dotap.presentation.home

sealed interface HomeUiEvent {
    data object OpenSettingsClicked : HomeUiEvent

    data object OpenAccessibilitySettingsClicked : HomeUiEvent

    data class DotVisibilityToggled(val enabled: Boolean) : HomeUiEvent

    data object RestrictedSettingsHelpClicked : HomeUiEvent

    data object DismissRestrictedSettingsDialog : HomeUiEvent

    data object OpenAppInfoClicked : HomeUiEvent
}
