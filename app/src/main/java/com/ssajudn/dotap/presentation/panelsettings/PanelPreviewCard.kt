package com.ssajudn.dotap.presentation.panelsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.presentation.common.AppShortcutIcon
import com.ssajudn.dotap.presentation.common.labelRes
import com.ssajudn.dotap.presentation.panel.PanelDimensions
import com.ssajudn.dotap.presentation.panel.iconRes
import com.ssajudn.dotap.presentation.panel.resolveGridSlots

private const val CARD_CORNER_DP = 24
private const val CARD_PADDING_DP = 20
private const val BADGE_CORNER_DP = 8
private const val BADGE_H_PADDING_DP = 8
private const val BADGE_V_PADDING_DP = 4
private const val PREVIEW_PANEL_CORNER_DP = 20
private const val PREVIEW_PANEL_PADDING_DP = 12
private const val PREVIEW_CELL_SPACING_DP = 8
private const val PREVIEW_CELL_SIZE_DP = 44
private const val PREVIEW_ICON_SIZE_DP = 20
private const val PREVIEW_CELL_ALPHA = 0.7f
private const val PREVIEW_TONAL_ELEVATION_DP = 4

@Composable
internal fun PanelPreviewCard(
    slots: List<PanelSlot>,
    appLabels: Map<String, String> = emptyMap(),
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_CORNER_DP.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PanelPreviewHeader(slotCount = slots.size)
            MiniPanelGrid(slots = slots, appLabels = appLabels)
        }
    }
}

@Composable
private fun PanelPreviewHeader(slotCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.panel_preview_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Surface(
            shape = RoundedCornerShape(BADGE_CORNER_DP.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = stringResource(R.string.panel_slot_count, slotCount, Limits.MAX_PANEL_SLOTS),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = BADGE_H_PADDING_DP.dp, vertical = BADGE_V_PADDING_DP.dp),
            )
        }
    }
}

@Composable
private fun MiniPanelGrid(
    slots: List<PanelSlot>,
    appLabels: Map<String, String>,
) {
    val grid = resolveGridSlots(slots)
    Surface(
        shape = RoundedCornerShape(PREVIEW_PANEL_CORNER_DP.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = PREVIEW_TONAL_ELEVATION_DP.dp,
    ) {
        Column(
            modifier = Modifier.padding(PREVIEW_PANEL_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(PREVIEW_CELL_SPACING_DP.dp),
        ) {
            for (rowIndex in 0 until PanelDimensions.GRID_COLUMNS) {
                MiniPanelRow(
                    rowSlots = grid[rowIndex],
                    isCenterRow = rowIndex == 1,
                    appLabels = appLabels,
                )
            }
        }
    }
}

@Composable
private fun MiniPanelRow(
    rowSlots: List<PanelSlot?>,
    isCenterRow: Boolean,
    appLabels: Map<String, String>,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(PREVIEW_CELL_SPACING_DP.dp)) {
        for (colIndex in 0 until PanelDimensions.GRID_COLUMNS) {
            val isCenter = isCenterRow && colIndex == 1
            if (isCenter) {
                MiniCloseCell()
            } else {
                val slot = rowSlots[colIndex]
                if (slot != null) {
                    MiniActionCell(
                        slot = slot,
                        appLabel = slot.appPackage?.let { appLabels[it] },
                    )
                } else {
                    Spacer(modifier = Modifier.size(PREVIEW_CELL_SIZE_DP.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniActionCell(
    slot: PanelSlot,
    appLabel: String?,
) {
    val iconRes = slot.action.iconRes() ?: return
    val contentLabel = appLabel ?: stringResource(slot.action.labelRes())
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PREVIEW_CELL_ALPHA),
        modifier = Modifier.size(PREVIEW_CELL_SIZE_DP.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (slot.action == ActionType.LAUNCH_APP || slot.action == ActionType.OPEN_DOTAP) {
                val pkg = if (slot.action == ActionType.OPEN_DOTAP) "com.ssajudn.dotap" else slot.appPackage
                AppShortcutIcon(
                    packageName = pkg,
                    contentDescription = contentLabel,
                    modifier = Modifier.size(PREVIEW_ICON_SIZE_DP.dp),
                )
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = contentLabel,
                    modifier = Modifier.size(PREVIEW_ICON_SIZE_DP.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MiniCloseCell() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(PREVIEW_CELL_SIZE_DP.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(R.drawable.ic_panel_close),
                contentDescription = stringResource(R.string.panel_close),
                modifier = Modifier.size(PREVIEW_ICON_SIZE_DP.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
