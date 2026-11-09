package com.ssajudn.dotap.presentation.gestures

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.presentation.common.labelRes
import com.ssajudn.dotap.presentation.theme.DotapTheme
import kotlin.math.roundToInt

@Composable
fun GesturesRoute(
    viewModel: GesturesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GesturesScreen(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturesScreen(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.gestures_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.back_button))
                    }
                },
            )
        },
    ) { innerPadding ->
        GesturesBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@Composable
private fun GesturesBody(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GestureActions(settings = state.settings, onEvent = onEvent)
        GestureTimings(settings = state.settings, onEvent = onEvent)
    }
}

@Composable
private fun GestureActions(
    settings: DotSettings,
    onEvent: (GesturesUiEvent) -> Unit,
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

@Composable
private fun GestureTimings(
    settings: DotSettings,
    onEvent: (GesturesUiEvent) -> Unit,
) {
    TimeoutSlider(
        spec = DOUBLE_TAP_SPEC,
        valueMs = settings.doubleTapTimeoutMs.toInt(),
        onChange = { onEvent(GesturesUiEvent.DoubleTapTimeoutChanged(it)) },
    )
    TimeoutSlider(
        spec = LONG_PRESS_SPEC,
        valueMs = settings.longPressDurationMs.toInt(),
        onChange = { onEvent(GesturesUiEvent.LongPressDurationChanged(it)) },
    )
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

@Composable
private fun TimeoutSlider(
    spec: SliderSpec,
    valueMs: Int,
    onChange: (Int) -> Unit,
) {
    val label = stringResource(spec.labelRes)
    val valueText = stringResource(R.string.timeout_value, valueMs)
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = valueText, style = MaterialTheme.typography.bodyMedium)
        Slider(
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText
            },
            value = valueMs.toFloat(),
            onValueChange = { onChange(it.roundToInt().roundToMultiple(spec.stepMs)) },
            valueRange = spec.minMs.toFloat()..spec.maxMs.toFloat(),
            steps = spec.steps,
        )
    }
}

private data class SliderSpec(
    val labelRes: Int,
    val minMs: Int,
    val maxMs: Int,
    val stepMs: Int,
) {
    val steps: Int = (maxMs - minMs) / stepMs - 1
}

private fun Int.roundToMultiple(step: Int): Int = (this.toFloat() / step).roundToInt() * step

private val SELECTABLE_ACTIONS: List<ActionType> = ActionType.entries

private val DOUBLE_TAP_SPEC = SliderSpec(
    labelRes = R.string.double_tap_timeout_label,
    minMs = Limits.MIN_DOUBLE_TAP_TIMEOUT_MS.toInt(),
    maxMs = Limits.MAX_DOUBLE_TAP_TIMEOUT_MS.toInt(),
    stepMs = DOUBLE_TAP_STEP_MS,
)

private val LONG_PRESS_SPEC = SliderSpec(
    labelRes = R.string.long_press_duration_label,
    minMs = Limits.MIN_LONG_PRESS_DURATION_MS.toInt(),
    maxMs = Limits.MAX_LONG_PRESS_DURATION_MS.toInt(),
    stepMs = LONG_PRESS_STEP_MS,
)

private const val DOUBLE_TAP_STEP_MS = 50
private const val LONG_PRESS_STEP_MS = 100

@Preview(showBackground = true)
@Composable
private fun GesturesScreenPreview() {
    DotapTheme {
        GesturesScreen(state = GesturesUiState(isLoading = false), onEvent = {}, onBack = {})
    }
}
