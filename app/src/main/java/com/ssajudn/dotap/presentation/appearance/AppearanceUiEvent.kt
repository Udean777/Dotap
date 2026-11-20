package com.ssajudn.dotap.presentation.appearance

import com.ssajudn.dotap.domain.model.DotStyle

sealed interface AppearanceUiEvent {
    data class DotEnabledToggled(val enabled: Boolean) : AppearanceUiEvent

    data class SnapToEdgeToggled(val enabled: Boolean) : AppearanceUiEvent

    data class DotSizeChanged(val sizeDp: Int) : AppearanceUiEvent

    data class IdleOpacityChanged(val opacityPercent: Int) : AppearanceUiEvent

    data class DotStyleSelected(val style: DotStyle) : AppearanceUiEvent

    data class CustomImagePicked(val uriString: String?) : AppearanceUiEvent
}
