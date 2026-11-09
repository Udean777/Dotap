package com.ssajudn.dotap.system.overlay

import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.usecase.HandleGestureUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DotPresenter(
    initialState: DotUiState = DotUiState(),
    private val handleGestureUseCase: HandleGestureUseCase? = null,
    private val scope: CoroutineScope? = null,
) {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<DotUiState> = _uiState.asStateFlow()

    private var isActionRunning = false

    fun onGesture(gesture: GestureType) {
        val useCase = handleGestureUseCase
        val actionScope = scope
        // Screenshot and panel actions own the overlay for a while; overlapping runs would fight over it.
        if (useCase == null || actionScope == null || isActionRunning) return
        isActionRunning = true
        actionScope.launch {
            try {
                useCase(gesture)
            } finally {
                isActionRunning = false
            }
        }
    }

    fun setVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isVisible = visible)
    }

    fun setHiddenForCapture(hidden: Boolean) {
        _uiState.value = _uiState.value.copy(isHiddenForCapture = hidden)
    }

    fun updatePosition(isRightSide: Boolean, yRatio: Float) {
        val clampedRatio = yRatio.coerceIn(Limits.MIN_Y_RATIO, Limits.MAX_Y_RATIO)
        _uiState.value = _uiState.value.copy(
            isRightSide = isRightSide,
            yRatio = clampedRatio,
        )
    }

    fun updateAlpha(alpha: Float) {
        _uiState.value = _uiState.value.copy(alpha = alpha.coerceIn(0f, 1f))
    }

    fun updateDotSize(sizeDp: Int) {
        val clampedSize = sizeDp.coerceIn(Limits.MIN_DOT_SIZE_DP, Limits.MAX_DOT_SIZE_DP)
        _uiState.value = _uiState.value.copy(dotSizeDp = clampedSize)
    }
}
