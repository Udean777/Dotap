package com.ssajudn.dotap.presentation.panelsettings

import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot

sealed interface PanelAppPickerTarget {
    data object NewSlot : PanelAppPickerTarget

    data class ExistingSlot(
        val index: Int,
        val currentPackage: String?,
    ) : PanelAppPickerTarget
}

data class PanelSettingsUiState(
    val slots: List<PanelSlot> = Defaults.DEFAULT_PANEL_SLOTS,
    val availableActions: List<ActionType> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val appLabels: Map<String, String> = emptyMap(),
    val isLoadingApps: Boolean = false,
    val appPickerTarget: PanelAppPickerTarget? = null,
    val canAddMore: Boolean = Defaults.DEFAULT_PANEL_SLOTS.size < Limits.MAX_PANEL_SLOTS,
    val canRemove: Boolean = Defaults.DEFAULT_PANEL_SLOTS.size > Limits.MIN_PANEL_SLOTS,
    val isLoading: Boolean = true,
)
