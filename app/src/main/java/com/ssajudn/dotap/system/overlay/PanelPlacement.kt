package com.ssajudn.dotap.system.overlay

/**
 * Pure geometry for positioning the panel window beside the dot.
 * Kept free of Android types so it can be unit-tested on the JVM.
 */
class PanelPlacement(
    private val panel: Size,
    private val screen: Size,
    private val gap: Int,
) {
    data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val centerX: Int get() = (left + right) / 2
        val centerY: Int get() = (top + bottom) / 2
    }

    data class Size(val width: Int, val height: Int)

    data class Position(val x: Int, val y: Int)

    /**
     * The panel opens on the side of the dot that faces the screen centre, vertically centred on the dot.
     * The result is clamped so the panel never leaves the screen, even when neither side has enough room.
     */
    fun positionFor(anchor: Bounds): Position {
        val opensToTheRight = anchor.centerX < screen.width / 2
        val rawX = if (opensToTheRight) anchor.right + gap else anchor.left - gap - panel.width
        val rawY = anchor.centerY - panel.height / 2
        return Position(
            x = rawX.coerceIn(0, (screen.width - panel.width).coerceAtLeast(0)),
            y = rawY.coerceIn(0, (screen.height - panel.height).coerceAtLeast(0)),
        )
    }
}
