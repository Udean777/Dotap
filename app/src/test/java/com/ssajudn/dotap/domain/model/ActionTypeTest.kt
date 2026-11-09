package com.ssajudn.dotap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionTypeTest {
    @Test
    fun `only the panel opener and the empty action are excluded from panel slots`() {
        val excluded = ActionType.entries.filterNot { it.canBePanelSlot }

        assertEquals(setOf(ActionType.OPEN_PANEL, ActionType.NONE), excluded.toSet())
    }

    @Test
    fun `default panel slots are all valid slots`() {
        assertTrue(Defaults.DEFAULT_PANEL_SLOTS.all { it.canBePanelSlot })
    }
}
