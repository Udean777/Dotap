package com.ssajudn.dotap.presentation.panelsettings

import com.ssajudn.dotap.domain.model.ActionType

sealed interface PanelSettingsUiEvent {
    data class MoveSlot(val fromIndex: Int, val toIndex: Int) : PanelSettingsUiEvent

    data class AddSlot(val action: ActionType) : PanelSettingsUiEvent

    data class EditSlotApp(val index: Int) : PanelSettingsUiEvent

    data class AppPicked(val packageName: String) : PanelSettingsUiEvent

    data object DismissAppPicker : PanelSettingsUiEvent

    data class RemoveSlot(val index: Int) : PanelSettingsUiEvent

    data object ResetDefaultsClicked : PanelSettingsUiEvent

    data object BackClicked : PanelSettingsUiEvent
}
