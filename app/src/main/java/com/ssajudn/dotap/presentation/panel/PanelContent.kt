package com.ssajudn.dotap.presentation.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.presentation.common.labelRes
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun PanelRoute(
    onClose: () -> Unit,
    viewModel: PanelViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PanelUiEffect.Close -> onClose()
            }
        }
    }
    PanelScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun PanelScreen(
    state: PanelUiState,
    onEvent: (PanelUiEvent) -> Unit,
) {
    val panelTitle = stringResource(R.string.action_open_panel)
    DotapTheme {
        Surface(
            modifier = Modifier
                .size(PanelDimensions.SIDE_DP.dp)
                .semantics { paneTitle = panelTitle },
            shape = RoundedCornerShape(PanelDimensions.CORNER_RADIUS_DP.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            PanelGrid(slots = state.slots, onEvent = onEvent)
        }
    }
}

@Composable
private fun PanelGrid(
    slots: List<ActionType>,
    onEvent: (PanelUiEvent) -> Unit,
) {
    val grid = resolveGridSlots(slots)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PanelDimensions.CONTENT_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(PanelDimensions.CELL_SPACING_DP.dp),
    ) {
        for (rowIndex in 0 until PanelDimensions.GRID_COLUMNS) {
            PanelRow(
                rowIndex = rowIndex,
                rowSlots = grid[rowIndex],
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun PanelRow(
    rowIndex: Int,
    rowSlots: List<ActionType?>,
    onEvent: (PanelUiEvent) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PanelDimensions.CELL_SPACING_DP.dp),
    ) {
        for (colIndex in 0 until PanelDimensions.GRID_COLUMNS) {
            val isCenter = rowIndex == 1 && colIndex == 1
            if (isCenter) {
                PanelCloseCell(onClick = { onEvent(PanelUiEvent.CloseClicked) })
            } else {
                val action = rowSlots[colIndex]
                if (action != null) {
                    PanelActionCell(
                        action = action,
                        onClick = { onEvent(PanelUiEvent.SlotSelected(action)) },
                    )
                } else {
                    Spacer(modifier = Modifier.size(PanelDimensions.CELL_SIZE_DP.dp))
                }
            }
        }
    }
}

@Composable
private fun PanelActionCell(
    action: ActionType,
    onClick: () -> Unit,
) {
    val iconRes = action.iconRes() ?: return
    val label = stringResource(action.labelRes())
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
            .size(PanelDimensions.CELL_SIZE_DP.dp)
            .semantics { role = Role.Button },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(CELL_ICON_SIZE_DP.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PanelCloseCell(
    onClick: () -> Unit,
) {
    val label = stringResource(R.string.panel_close)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .size(PanelDimensions.CELL_SIZE_DP.dp)
            .semantics { role = Role.Button },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_panel_close),
                contentDescription = label,
                modifier = Modifier.size(CELL_ICON_SIZE_DP.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

internal fun resolveGridSlots(slots: List<ActionType>): List<List<ActionType?>> {
    val positions = SLOT_POSITIONS_MAP[slots.size] ?: OUTER_POSITIONS
    val grid = MutableList(PanelDimensions.GRID_COLUMNS) {
        MutableList<ActionType?>(PanelDimensions.GRID_COLUMNS) { null }
    }
    slots.take(positions.size).forEachIndexed { index, action ->
        val (row, col) = positions[index]
        grid[row][col] = action
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

private const val CELL_ICON_SIZE_DP = 28
private const val SLOT_COUNT_THREE = 3
private const val SLOT_COUNT_FOUR = 4
private const val SLOT_COUNT_FIVE = 5
private const val SLOT_COUNT_SIX = 6
private const val SLOT_COUNT_SEVEN = 7
private const val SLOT_COUNT_EIGHT = 8

@Preview(showBackground = true)
@Composable
private fun PanelScreenPreview() {
    PanelScreen(state = PanelUiState(), onEvent = {})
}
