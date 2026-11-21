package com.ssajudn.dotap.presentation.panelsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.presentation.common.AppShortcutIcon
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.ExpressiveIconBadge
import com.ssajudn.dotap.presentation.common.labelRes
import com.ssajudn.dotap.presentation.panel.iconRes

private const val CARD_PADDING_DP = 16
private const val ROW_ICON_BADGE_DP = 40

private data class SlotRowState(
    val index: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val canRemove: Boolean,
    val subtitle: String? = null,
)

private data class SlotActionSpec(
    val iconRes: Int,
    val contentDescription: String,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

internal data class ActionRowIdentitySpec(
    val action: ActionType,
    val label: String,
    val subtitle: String? = null,
    val appPackage: String? = null,
)

@Composable
internal fun ActiveSlotsCard(
    state: PanelSettingsUiState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
) {
    DotapCard {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            ActiveSlotsHint(canRemove = state.canRemove)
            state.slots.forEachIndexed { index, slot ->
                if (index > 0) {
                    DotapDivider()
                }
                val subtitle = resolveSlotSubtitle(slot, state.appLabels)
                ActiveSlotRow(
                    slot = slot,
                    rowState = SlotRowState(
                        index = index,
                        isFirst = index == 0,
                        isLast = index == state.slots.lastIndex,
                        canRemove = state.canRemove,
                        subtitle = subtitle,
                    ),
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun resolveSlotSubtitle(
    slot: PanelSlot,
    appLabels: Map<String, String>,
): String? = if (slot.action == ActionType.LAUNCH_APP) {
    slot.appPackage?.let { appLabels[it] ?: it } ?: stringResource(R.string.panel_app_not_selected)
} else {
    null
}

@Composable
private fun ActiveSlotsHint(canRemove: Boolean) {
    val hintText = if (canRemove) {
        stringResource(R.string.panel_active_slots_hint)
    } else {
        stringResource(R.string.panel_min_slots_hint, Limits.MIN_PANEL_SLOTS)
    }
    Text(
        text = hintText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ActiveSlotRow(
    slot: PanelSlot,
    rowState: SlotRowState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
) {
    val label = stringResource(slot.action.labelRes())
    val clickDesc = stringResource(R.string.panel_change_app_desc, label)
    val rowModifier = if (slot.action == ActionType.LAUNCH_APP) {
        Modifier.fillMaxWidth().clickable(onClickLabel = clickDesc) {
            onEvent(PanelSettingsUiEvent.EditSlotApp(rowState.index))
        }
    } else {
        Modifier.fillMaxWidth()
    }.padding(vertical = 6.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ActionRowIdentity(
            spec = ActionRowIdentitySpec(slot.action, label, rowState.subtitle, slot.appPackage),
            modifier = Modifier.weight(1f),
        )
        ActiveSlotActions(label = label, rowState = rowState, onEvent = onEvent)
    }
}

@Composable
private fun ActiveSlotActions(
    label: String,
    rowState: SlotRowState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SlotActionButton(
            SlotActionSpec(
                iconRes = R.drawable.ic_arrow_up,
                contentDescription = stringResource(R.string.panel_move_up_desc, label),
                enabled = !rowState.isFirst,
                onClick = { onEvent(PanelSettingsUiEvent.MoveSlot(rowState.index, rowState.index - 1)) },
            ),
        )
        SlotActionButton(
            SlotActionSpec(
                iconRes = R.drawable.ic_arrow_down,
                contentDescription = stringResource(R.string.panel_move_down_desc, label),
                enabled = !rowState.isLast,
                onClick = { onEvent(PanelSettingsUiEvent.MoveSlot(rowState.index, rowState.index + 1)) },
            ),
        )
        SlotActionButton(
            SlotActionSpec(
                iconRes = R.drawable.ic_remove,
                contentDescription = stringResource(R.string.panel_remove_slot_desc, label),
                enabled = rowState.canRemove,
                onClick = { onEvent(PanelSettingsUiEvent.RemoveSlot(rowState.index)) },
            ),
        )
    }
}

@Composable
private fun SlotActionButton(spec: SlotActionSpec) {
    IconButton(onClick = spec.onClick, enabled = spec.enabled) {
        Icon(
            painter = painterResource(spec.iconRes),
            contentDescription = spec.contentDescription,
        )
    }
}

@Composable
internal fun ActionRowIdentity(
    spec: ActionRowIdentitySpec,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionRowLeadingIcon(action = spec.action, appPackage = spec.appPackage)
        Column {
            Text(text = spec.label, style = MaterialTheme.typography.titleMedium)
            spec.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ActionRowLeadingIcon(
    action: ActionType,
    appPackage: String?,
) {
    val pkg = if (action == ActionType.OPEN_DOTAP) "com.ssajudn.dotap" else appPackage
    if ((action == ActionType.LAUNCH_APP || action == ActionType.OPEN_DOTAP) && !pkg.isNullOrBlank()) {
        AppShortcutIcon(
            packageName = pkg,
            contentDescription = null,
            modifier = Modifier.size(ROW_ICON_BADGE_DP.dp),
        )
    } else {
        action.iconRes()?.let { iconRes ->
            ExpressiveIconBadge(
                iconRes = iconRes,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
