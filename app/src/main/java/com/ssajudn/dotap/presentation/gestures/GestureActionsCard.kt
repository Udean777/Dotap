package com.ssajudn.dotap.presentation.gestures

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.presentation.common.labelRes

private val SELECTABLE_ACTIONS: List<ActionType> = ActionType.entries

@Composable
fun GestureActionsCard(
    settings: DotSettings,
    onEvent: (GesturesUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ActionDropdown(
                labelRes = R.string.gesture_single_tap_label,
                selected = settings.singleTapAction,
                onSelected = { onEvent(GesturesUiEvent.SingleTapActionSelected(it)) },
            )
            ActionDropdown(
                labelRes = R.string.gesture_double_tap_label,
                selected = settings.doubleTapAction,
                onSelected = { onEvent(GesturesUiEvent.DoubleTapActionSelected(it)) },
            )
            ActionDropdown(
                labelRes = R.string.gesture_long_press_label,
                selected = settings.longPressAction,
                onSelected = { onEvent(GesturesUiEvent.LongPressActionSelected(it)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionDropdown(
    @StringRes labelRes: Int,
    selected: ActionType,
    onSelected: (ActionType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = stringResource(selected.labelRes()),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(labelRes)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ActionMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            onSelected = {
                onSelected(it)
                expanded = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.ActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelected: (ActionType) -> Unit,
) {
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SELECTABLE_ACTIONS.forEach { action ->
            DropdownMenuItem(
                text = { Text(text = stringResource(action.labelRes())) },
                onClick = { onSelected(action) },
            )
        }
    }
}
