package com.ssajudn.dotap.system.overlay

import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.usecase.HandleGestureUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var idleJob: Job? = null
    private var idleDelayMs: Long = Defaults.IDLE_DELAY_MS
    private var targetIdleAlpha: Float =
        Defaults.IDLE_OPACITY_PERCENT.toFloat() / Limits.MAX_IDLE_OPACITY_PERCENT
    private var isTouchActive: Boolean = false

    init {
        restartIdleTimer()
    }

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

    fun onTouchActive() {
        isTouchActive = true
        idleJob?.cancel()
        idleJob = null
        if (_uiState.value.alpha != FULL_ALPHA) {
            _uiState.value = _uiState.value.copy(alpha = FULL_ALPHA)
        }
    }

    fun onTouchFinished() {
        isTouchActive = false
        restartIdleTimer()
    }

    fun setVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isVisible = visible)
    }

    fun setHiddenForCapture(hidden: Boolean) {
        _uiState.value = _uiState.value.copy(isHiddenForCapture = hidden)
    }

    fun updatePosition(isRightSide: Boolean, yRatio: Float, xRatio: Float = if (isRightSide) 1f else 0f) {
        val clampedRatio = yRatio.coerceIn(Limits.MIN_Y_RATIO, Limits.MAX_Y_RATIO)
        _uiState.value = _uiState.value.copy(
            isRightSide = isRightSide,
            yRatio = clampedRatio,
            xRatio = xRatio.coerceIn(0f, 1f),
        )
    }

    fun applySettings(settings: DotSettings) {
        val current = _uiState.value
        idleDelayMs = settings.idleDelayMs
        targetIdleAlpha = settings.idleAlpha
        _uiState.value = current.copy(
            isVisible = settings.isDotEnabled,
            dotSizeDp = settings.dotSizeDp.coerceIn(Limits.MIN_DOT_SIZE_DP, Limits.MAX_DOT_SIZE_DP),
            isRightSide = settings.isRightSide,
            yRatio = settings.yRatio.coerceIn(Limits.MIN_Y_RATIO, Limits.MAX_Y_RATIO),
            isSnapToEdgeEnabled = settings.isSnapToEdgeEnabled,
            // With snapping on the side decides the position; with it off keep where the user left the dot.
            xRatio = if (settings.isSnapToEdgeEnabled) {
                if (settings.isRightSide) 1f else 0f
            } else {
                current.xRatio
            },
        )
        if (!isTouchActive) {
            restartIdleTimer()
        }
    }

    fun updateAlpha(alpha: Float) {
        _uiState.value = _uiState.value.copy(alpha = alpha.coerceIn(0f, 1f))
    }

    fun updateDotSize(sizeDp: Int) {
        val clampedSize = sizeDp.coerceIn(Limits.MIN_DOT_SIZE_DP, Limits.MAX_DOT_SIZE_DP)
        _uiState.value = _uiState.value.copy(dotSizeDp = clampedSize)
    }

    private fun restartIdleTimer() {
        idleJob?.cancel()
        if (targetIdleAlpha >= FULL_ALPHA) {
            if (_uiState.value.alpha != FULL_ALPHA) {
                _uiState.value = _uiState.value.copy(alpha = FULL_ALPHA)
            }
            return
        }
        val coroutineScope = scope ?: return
        idleJob = coroutineScope.launch {
            delay(idleDelayMs)
            _uiState.value = _uiState.value.copy(alpha = targetIdleAlpha)
        }
    }

    private companion object {
        const val FULL_ALPHA = 1.0f
    }
}
