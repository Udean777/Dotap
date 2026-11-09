package com.ssajudn.dotap.domain.model

data class DotSettings(
    val isDotEnabled: Boolean = Defaults.DOT_ENABLED,
    val dotSizeDp: Int = Defaults.DOT_SIZE_DP,
    val idleOpacityPercent: Int = Defaults.IDLE_OPACITY_PERCENT,
    val idleDelayMs: Long = Defaults.IDLE_DELAY_MS,
    val doubleTapTimeoutMs: Long = Defaults.DOUBLE_TAP_TIMEOUT_MS,
    val longPressDurationMs: Long = Defaults.LONG_PRESS_DURATION_MS,
    val singleTapAction: ActionType = Defaults.ACTION_SINGLE_TAP,
    val doubleTapAction: ActionType = Defaults.ACTION_DOUBLE_TAP,
    val longPressAction: ActionType = Defaults.ACTION_LONG_PRESS,
    val panelSlots: List<ActionType> = Defaults.DEFAULT_PANEL_SLOTS,
    val isHapticsEnabled: Boolean = Defaults.HAPTICS_ENABLED,
    val isSnapToEdgeEnabled: Boolean = Defaults.SNAP_TO_EDGE,
    val isRightSide: Boolean = Defaults.DOT_SIDE_RIGHT,
    val yRatio: Float = Defaults.DOT_Y_RATIO,
) {
    /** Idle opacity as a 0..1 alpha; 100% is the upper limit and means no fading. */
    val idleAlpha: Float get() = idleOpacityPercent.toFloat() / Limits.MAX_IDLE_OPACITY_PERCENT

    fun withDotSize(sizeDp: Int): DotSettings = copy(
        dotSizeDp = sizeDp.coerceIn(
            Limits.MIN_DOT_SIZE_DP,
            Limits.MAX_DOT_SIZE_DP,
        ),
    )

    fun withIdleOpacity(opacityPercent: Int): DotSettings = copy(
        idleOpacityPercent = opacityPercent.coerceIn(
            Limits.MIN_IDLE_OPACITY_PERCENT,
            Limits.MAX_IDLE_OPACITY_PERCENT,
        ),
    )

    fun withDoubleTapTimeout(timeoutMs: Long): DotSettings = copy(
        doubleTapTimeoutMs = timeoutMs.coerceIn(
            Limits.MIN_DOUBLE_TAP_TIMEOUT_MS,
            Limits.MAX_DOUBLE_TAP_TIMEOUT_MS,
        ),
    )

    fun withLongPressDuration(durationMs: Long): DotSettings = copy(
        longPressDurationMs = durationMs.coerceIn(
            Limits.MIN_LONG_PRESS_DURATION_MS,
            Limits.MAX_LONG_PRESS_DURATION_MS,
        ),
    )

    fun withYRatio(ratio: Float): DotSettings = copy(
        yRatio = ratio.coerceIn(
            Limits.MIN_Y_RATIO,
            Limits.MAX_Y_RATIO,
        ),
    )

    companion object {
        val Default: DotSettings = DotSettings()
    }
}
