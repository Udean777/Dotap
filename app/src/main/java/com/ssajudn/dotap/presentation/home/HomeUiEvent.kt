package com.ssajudn.dotap.presentation.home

sealed interface HomeUiEvent {
    data object OpenSettingsClicked : HomeUiEvent

    data object OpenAccessibilitySettingsClicked : HomeUiEvent
}
