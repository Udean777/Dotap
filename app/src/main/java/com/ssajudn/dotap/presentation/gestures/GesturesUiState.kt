package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.DotSettings

data class GesturesUiState(
    val settings: DotSettings = DotSettings.Default,
    val isLoading: Boolean = true,
)
