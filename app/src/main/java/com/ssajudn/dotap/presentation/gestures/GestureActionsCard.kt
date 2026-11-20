package com.ssajudn.dotap.presentation.gestures

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.presentation.common.AppShortcutIcon
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.labelRes

private val SELECTABLE_ACTIONS: List<ActionType> = ActionType.entries

private data class GestureRowSpec(
    val gesture: GestureType,
    @param:StringRes val labelRes: Int,
    val selectedAction: ActionType,
    val selectedAppPackage: String?,
    val appLabels: Map<String, String>,
    val onActionSelected: (ActionType) -> Unit,
)

@Composable
fun GestureActionsCard(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
) {
    val rows = buildGestureRowSpecs(state, onEvent)
    DotapCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            rows.forEach { spec ->
                GestureActionSection(
                    spec = spec,
                    onPickApp = { onEvent(GesturesUiEvent.OpenAppPicker(spec.gesture)) },
                )
            }
        }
    }
}

private fun buildGestureRowSpecs(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
): List<GestureRowSpec> {
    val s = state.settings
    return listOf(
        GestureRowSpec(
            gesture = GestureType.SINGLE_TAP,
            labelRes = R.string.gesture_single_tap_label,
            selectedAction = s.singleTapAction,
            selectedAppPackage = s.singleTapAppPackage,
            appLabels = state.appLabels,
            onActionSelected = { onEvent(GesturesUiEvent.SingleTapActionSelected(it)) },
        ),
        GestureRowSpec(
            gesture = GestureType.DOUBLE_TAP,
            labelRes = R.string.gesture_double_tap_label,
            selectedAction = s.doubleTapAction,
            selectedAppPackage = s.doubleTapAppPackage,
            appLabels = state.appLabels,
            onActionSelected = { onEvent(GesturesUiEvent.DoubleTapActionSelected(it)) },
        ),
        GestureRowSpec(
            gesture = GestureType.LONG_PRESS,
            labelRes = R.string.gesture_long_press_label,
            selectedAction = s.longPressAction,
            selectedAppPackage = s.longPressAppPackage,
            appLabels = state.appLabels,
            onActionSelected = { onEvent(GesturesUiEvent.LongPressActionSelected(it)) },
        ),
    )
}

@Composable
private fun GestureActionSection(
    spec: GestureRowSpec,
    onPickApp: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionDropdown(
            labelRes = spec.labelRes,
            selected = spec.selectedAction,
            onSelected = spec.onActionSelected,
        )
        if (spec.selectedAction == ActionType.LAUNCH_APP) {
            val appName = spec.selectedAppPackage?.let { spec.appLabels[it] ?: it }
            TargetAppSelector(
                appPackage = spec.selectedAppPackage,
                appName = appName,
                onClick = onPickApp,
            )
        }
    }
}

@Composable
private fun TargetAppSelector(
    appPackage: String?,
    appName: String?,
    onClick: () -> Unit,
) {
    val displayText = if (appName != null) {
        stringResource(R.string.gesture_target_app_label, appName)
    } else {
        stringResource(R.string.gesture_target_app_empty)
    }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics { role = Role.Button },
    ) {
        TargetAppSelectorRow(appPackage = appPackage, displayText = displayText)
    }
}

@Composable
private fun TargetAppSelectorRow(
    appPackage: String?,
    displayText: String,
) {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppShortcutIcon(
            packageName = appPackage,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            fallbackTint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            shape = MaterialTheme.shapes.small,
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
