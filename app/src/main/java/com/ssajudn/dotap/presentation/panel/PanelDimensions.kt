package com.ssajudn.dotap.presentation.panel

/**
 * Fixed panel metrics. The overlay window must know its size before it is attached to be able
 * to place it beside the dot, so the Compose layout and the window share these constants.
 */
internal object PanelDimensions {
    const val GRID_COLUMNS = 3
    const val CELL_SIZE_DP = 64
    const val CELL_SPACING_DP = 8
    const val CONTENT_PADDING_DP = 12
    const val ANCHOR_GAP_DP = 8
    const val CORNER_RADIUS_DP = 28

    const val SIDE_DP =
        GRID_COLUMNS * CELL_SIZE_DP +
            (GRID_COLUMNS - 1) * CELL_SPACING_DP +
            2 * CONTENT_PADDING_DP
}
