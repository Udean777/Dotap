package com.ssajudn.dotap.system.overlay

import com.ssajudn.dotap.domain.model.Defaults

data class DotUiState(
    val isVisible: Boolean = true,
    val isRightSide: Boolean = Defaults.DOT_SIDE_RIGHT,
    val xRatio: Float = if (isRightSide) 1f else 0f,
    val yRatio: Float = Defaults.DOT_Y_RATIO,
    val dotSizeDp: Int = Defaults.DOT_SIZE_DP,
    val alpha: Float = 1.0f,
    val isSnapToEdgeEnabled: Boolean = Defaults.SNAP_TO_EDGE,
    val isHiddenForCapture: Boolean = false,
)
