package com.ssajudn.dotap.presentation.settings

import com.ssajudn.dotap.domain.model.DotSettings

data class SettingsUiState(
    val settings: DotSettings = DotSettings.Default,
    val isLoading: Boolean = true,
)
