package com.ssajudn.dotap.presentation.panelsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.labelRes

private const val CARD_PADDING_DP = 16

@Composable
internal fun AvailableActionsCard(
    actions: List<ActionType>,
    canAddMore: Boolean,
    onAdd: (ActionType) -> Unit,
) {
    DotapCard {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            if (actions.isEmpty()) {
                Text(
                    text = stringResource(R.string.panel_all_actions_added),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AvailableActionsList(actions = actions, canAddMore = canAddMore, onAdd = onAdd)
            }
        }
    }
}

@Composable
private fun AvailableActionsList(
    actions: List<ActionType>,
    canAddMore: Boolean,
    onAdd: (ActionType) -> Unit,
) {
    if (!canAddMore) {
        Text(
            text = stringResource(R.string.panel_max_slots_reached, Limits.MAX_PANEL_SLOTS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
    actions.forEachIndexed { index, action ->
        if (index > 0) {
            DotapDivider()
        }
        AvailableActionRow(
            action = action,
            enabled = canAddMore,
            onAdd = { onAdd(action) },
        )
    }
}

@Composable
private fun AvailableActionRow(
    action: ActionType,
    enabled: Boolean,
    onAdd: () -> Unit,
) {
    val label = stringResource(action.labelRes())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ActionRowIdentity(
            spec = ActionRowIdentitySpec(action = action, label = label),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onAdd, enabled = enabled) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.panel_add_slot_desc, label),
            )
        }
    }
}
