package com.ssajudn.dotap.system.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class PanelPlacementTest {
    private val screen = PanelPlacement.Size(width = 1000, height = 2000)
    private val panel = PanelPlacement.Size(width = 400, height = 400)
    private val placement = PanelPlacement(panel = panel, screen = screen, gap = 20)

    private fun anchorAt(
        left: Int,
        top: Int,
        size: Int = 100,
    ): PanelPlacement.Bounds = PanelPlacement.Bounds(
        left = left,
        top = top,
        right = left + size,
        bottom = top + size,
    )

    @Test
    fun `dot on the left opens the panel to its right`() {
        val position = placement.positionFor(anchorAt(left = 0, top = 900))

        assertEquals(120, position.x)
    }

    @Test
    fun `dot on the right opens the panel to its left`() {
        val position = placement.positionFor(anchorAt(left = 900, top = 900))

        assertEquals(480, position.x)
    }

    @Test
    fun `panel is vertically centred on the dot`() {
        val position = placement.positionFor(anchorAt(left = 0, top = 900))

        assertEquals(750, position.y)
    }

    @Test
    fun `panel is clamped to the top edge`() {
        val position = placement.positionFor(anchorAt(left = 0, top = 0))

        assertEquals(0, position.y)
    }

    @Test
    fun `panel is clamped to the bottom edge`() {
        val position = placement.positionFor(anchorAt(left = 0, top = 1900))

        assertEquals(screen.height - panel.height, position.y)
    }

    @Test
    fun `panel stays on screen when the side has no room`() {
        val narrowScreen = PanelPlacement.Size(width = 450, height = 2000)

        val position = PanelPlacement(panel, narrowScreen, gap = 20).positionFor(anchorAt(left = 0, top = 900))

        assertEquals(narrowScreen.width - panel.width, position.x)
    }

    @Test
    fun `panel larger than the screen is pinned to the origin`() {
        val tinyScreen = PanelPlacement.Size(width = 300, height = 300)

        val position = PanelPlacement(panel, tinyScreen, gap = 20).positionFor(anchorAt(left = 100, top = 100))

        assertEquals(PanelPlacement.Position(x = 0, y = 0), position)
    }
}
