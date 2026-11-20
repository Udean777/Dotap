package com.ssajudn.dotap.presentation.panel

import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.PanelSlot

data class PanelUiState(
    val slots: List<PanelSlot> = Defaults.DEFAULT_PANEL_SLOTS,
    val appLabels: Map<String, String> = emptyMap(),
)
