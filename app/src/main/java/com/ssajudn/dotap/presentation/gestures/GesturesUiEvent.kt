package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.ActionType

sealed interface GesturesUiEvent {
    data class SingleTapActionSelected(val action: ActionType) : GesturesUiEvent

    data class DoubleTapActionSelected(val action: ActionType) : GesturesUiEvent

    data class LongPressActionSelected(val action: ActionType) : GesturesUiEvent

    data class DoubleTapTimeoutChanged(val millis: Int) : GesturesUiEvent

    data class LongPressDurationChanged(val millis: Int) : GesturesUiEvent
}
