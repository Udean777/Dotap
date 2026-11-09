package com.ssajudn.dotap.presentation.home

sealed interface HomeUiEffect {
    data object NavigateToSettings : HomeUiEffect

    data object OpenAccessibilitySettings : HomeUiEffect

    data object OpenAppInfo : HomeUiEffect
}
