package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.GestureType

sealed interface GesturesUiEvent {
    data class SingleTapActionSelected(val action: ActionType) : GesturesUiEvent

    data class DoubleTapActionSelected(val action: ActionType) : GesturesUiEvent

    data class LongPressActionSelected(val action: ActionType) : GesturesUiEvent

    data class DoubleTapTimeoutChanged(val millis: Int) : GesturesUiEvent

    data class LongPressDurationChanged(val millis: Int) : GesturesUiEvent

    data class OpenAppPicker(val gesture: GestureType) : GesturesUiEvent

    data class AppSelected(val gesture: GestureType, val packageName: String) : GesturesUiEvent

    data object DismissAppPicker : GesturesUiEvent
}
