package com.ssajudn.dotap.presentation.panel

import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults

data class PanelUiState(
    val slots: List<ActionType> = Defaults.DEFAULT_PANEL_SLOTS,
)
