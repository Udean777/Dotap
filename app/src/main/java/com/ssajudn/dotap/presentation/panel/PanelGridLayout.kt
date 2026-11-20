package com.ssajudn.dotap.presentation.panel

import com.ssajudn.dotap.domain.model.PanelSlot

internal fun resolveGridSlots(slots: List<PanelSlot>): List<List<PanelSlot?>> {
    val positions = SLOT_POSITIONS_MAP[slots.size] ?: OUTER_POSITIONS
    val grid = MutableList(PanelDimensions.GRID_COLUMNS) {
        MutableList<PanelSlot?>(PanelDimensions.GRID_COLUMNS) { null }
    }
    slots.take(positions.size).forEachIndexed { index, slot ->
        val (row, col) = positions[index]
        grid[row][col] = slot
    }
    return grid
}

private val OUTER_POSITIONS = listOf(
    0 to 0,
    0 to 1,
    0 to 2,
    1 to 0,
    1 to 2,
    2 to 0,
    2 to 1,
    2 to 2,
)

private val SLOT_POSITIONS_MAP = mapOf(
    SLOT_COUNT_THREE to listOf(0 to 1, 1 to 0, 1 to 2),
    SLOT_COUNT_FOUR to listOf(0 to 1, 1 to 0, 1 to 2, 2 to 1),
    SLOT_COUNT_FIVE to listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 2),
    SLOT_COUNT_SIX to listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 2, 2 to 1),
    SLOT_COUNT_SEVEN to listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 2, 2 to 0, 2 to 2),
    SLOT_COUNT_EIGHT to OUTER_POSITIONS,
)

private const val SLOT_COUNT_THREE = 3
private const val SLOT_COUNT_FOUR = 4
private const val SLOT_COUNT_FIVE = 5
private const val SLOT_COUNT_SIX = 6
private const val SLOT_COUNT_SEVEN = 7
private const val SLOT_COUNT_EIGHT = 8
