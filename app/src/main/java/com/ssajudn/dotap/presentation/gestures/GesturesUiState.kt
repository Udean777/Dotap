package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.InstalledApp

data class GesturesUiState(
    val settings: DotSettings = DotSettings.Default,
    val installedApps: List<InstalledApp> = emptyList(),
    val appLabels: Map<String, String> = emptyMap(),
    val isLoadingApps: Boolean = false,
    val activeAppPickerGesture: GestureType? = null,
    val isLoading: Boolean = true,
)
