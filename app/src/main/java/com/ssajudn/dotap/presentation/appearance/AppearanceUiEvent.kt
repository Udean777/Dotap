package com.ssajudn.dotap.presentation.appearance

sealed interface AppearanceUiEvent {
    data class DotEnabledToggled(val enabled: Boolean) : AppearanceUiEvent

    data class SnapToEdgeToggled(val enabled: Boolean) : AppearanceUiEvent

    data class DotSizeChanged(val sizeDp: Int) : AppearanceUiEvent

    data class IdleOpacityChanged(val opacityPercent: Int) : AppearanceUiEvent
}
