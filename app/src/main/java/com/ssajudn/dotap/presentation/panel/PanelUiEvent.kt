package com.ssajudn.dotap.presentation.panel

import com.ssajudn.dotap.domain.model.PanelSlot

sealed interface PanelUiEvent {
    data class SlotSelected(val slot: PanelSlot) : PanelUiEvent

    data object CloseClicked : PanelUiEvent
}
