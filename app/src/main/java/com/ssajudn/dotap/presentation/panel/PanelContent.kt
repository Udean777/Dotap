package com.ssajudn.dotap.presentation.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.presentation.common.AppShortcutIcon
import com.ssajudn.dotap.presentation.common.labelRes
import com.ssajudn.dotap.presentation.theme.DotapTheme
import kotlinx.coroutines.delay

private const val PANEL_ENTER_SCALE = 0.85f
private const val PANEL_EXIT_SCALE = 0.88f
private const val PANEL_ENTER_DURATION_MS = 220
private const val PANEL_FADE_DURATION_MS = 180
private const val PANEL_EXIT_DURATION_MS = 160L
private const val TONAL_ELEVATION_DP = 6
private const val SHADOW_ELEVATION_DP = 8
private const val LAUNCH_APP_ICON_SIZE_DP = 24
private const val CELL_ICON_SIZE_DP = 28
private const val CELL_BG_ALPHA = 0.7f

@Composable
fun PanelRoute(
    onClose: () -> Unit,
    viewModel: PanelViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PanelUiEffect.Close -> {
                    isVisible = false
                    delay(PANEL_EXIT_DURATION_MS)
                    onClose()
                }
            }
        }
    }
    PanelScreen(
        state = state,
        isVisible = isVisible,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun PanelScreen(
    state: PanelUiState,
    isVisible: Boolean = true,
    onEvent: (PanelUiEvent) -> Unit,
) {
    val panelTitle = stringResource(R.string.action_open_panel)
    DotapTheme {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = PANEL_ENTER_SCALE,
                animationSpec = tween(PANEL_ENTER_DURATION_MS, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(PANEL_FADE_DURATION_MS)),
            exit = scaleOut(
                targetScale = PANEL_EXIT_SCALE,
                animationSpec = tween(PANEL_EXIT_DURATION_MS.toInt(), easing = FastOutLinearInEasing),
            ) + fadeOut(animationSpec = tween(PANEL_EXIT_DURATION_MS.toInt())),
            label = "PanelVisibility",
        ) {
            Surface(
                modifier = Modifier
                    .size(PanelDimensions.SIDE_DP.dp)
                    .semantics { paneTitle = panelTitle },
                shape = RoundedCornerShape(PanelDimensions.CORNER_RADIUS_DP.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = TONAL_ELEVATION_DP.dp,
                shadowElevation = SHADOW_ELEVATION_DP.dp,
            ) {
                PanelGrid(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun PanelGrid(
    state: PanelUiState,
    onEvent: (PanelUiEvent) -> Unit,
) {
    val grid = resolveGridSlots(state.slots)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PanelDimensions.CONTENT_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(PanelDimensions.CELL_SPACING_DP.dp),
    ) {
        for (rowIndex in 0 until PanelDimensions.GRID_COLUMNS) {
            PanelRow(
                rowState = PanelRowState(
                    rowSlots = grid[rowIndex],
                    isCenterRow = rowIndex == 1,
                    appLabels = state.appLabels,
                ),
                onEvent = onEvent,
            )
        }
    }
}

private data class PanelRowState(
    val rowSlots: List<PanelSlot?>,
    val isCenterRow: Boolean,
    val appLabels: Map<String, String>,
)

@Composable
private fun PanelRow(
    rowState: PanelRowState,
    onEvent: (PanelUiEvent) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(PanelDimensions.CELL_SPACING_DP.dp)) {
        for (colIndex in 0 until PanelDimensions.GRID_COLUMNS) {
            val isCenter = rowState.isCenterRow && colIndex == 1
            if (isCenter) {
                PanelCloseCell(onClick = { onEvent(PanelUiEvent.CloseClicked) })
            } else {
                val slot = rowState.rowSlots[colIndex]
                if (slot != null) {
                    PanelActionCell(
                        slot = slot,
                        appLabel = slot.appPackage?.let { rowState.appLabels[it] },
                        onClick = { onEvent(PanelUiEvent.SlotSelected(slot)) },
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
    slot: PanelSlot,
    appLabel: String?,
    onClick: () -> Unit,
) {
    val iconRes = slot.action.iconRes() ?: return
    val contentLabel = appLabel ?: stringResource(slot.action.labelRes())
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CELL_BG_ALPHA),
        modifier = Modifier.size(PanelDimensions.CELL_SIZE_DP.dp).semantics { role = Role.Button },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        ) {
            when (slot.action) {
                ActionType.LAUNCH_APP -> LaunchAppCellContent(slot.appPackage, appLabel, contentLabel)

                ActionType.OPEN_DOTAP -> LaunchAppCellContent("com.ssajudn.dotap", contentLabel, contentLabel)

                else -> Icon(
                    painter = painterResource(iconRes),
                    contentDescription = contentLabel,
                    modifier = Modifier.size(CELL_ICON_SIZE_DP.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LaunchAppCellContent(
    appPackage: String?,
    appLabel: String?,
    contentLabel: String,
) {
    if (appLabel != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AppShortcutIcon(
                packageName = appPackage,
                contentDescription = appLabel,
                modifier = Modifier.size(LAUNCH_APP_ICON_SIZE_DP.dp),
            )
            Text(
                text = appLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        AppShortcutIcon(
            packageName = appPackage,
            contentDescription = contentLabel,
            modifier = Modifier.size(CELL_ICON_SIZE_DP.dp),
        )
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

@Preview(showBackground = true)
@Composable
private fun PanelScreenPreview() {
    PanelScreen(state = PanelUiState(), onEvent = {})
}
