package com.ssajudn.dotap.presentation.panel

import com.ssajudn.dotap.domain.model.ActionType

sealed interface PanelUiEvent {
    data class SlotSelected(val action: ActionType) : PanelUiEvent

    data object CloseClicked : PanelUiEvent
}
