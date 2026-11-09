package com.ssajudn.dotap.presentation.appearance

import com.ssajudn.dotap.domain.model.DotSettings

data class AppearanceUiState(
    val settings: DotSettings = DotSettings.Default,
    val isLoading: Boolean = true,
)
