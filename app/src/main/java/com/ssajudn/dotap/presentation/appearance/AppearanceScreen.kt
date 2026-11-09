package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.theme.DotapTheme
import kotlin.math.roundToInt

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppearanceScreen(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.appearance_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.back_button))
                    }
                },
            )
        },
    ) { innerPadding ->
        AppearanceBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@Composable
private fun AppearanceBody(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
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
        LabeledSwitchRow(
            label = stringResource(R.string.dot_visibility_label),
            checked = state.settings.isDotEnabled,
            onCheckedChange = { onEvent(AppearanceUiEvent.DotEnabledToggled(it)) },
        )
        LabeledSwitchRow(
            label = stringResource(R.string.snap_to_edge_label),
            checked = state.settings.isSnapToEdgeEnabled,
            onCheckedChange = { onEvent(AppearanceUiEvent.SnapToEdgeToggled(it)) },
        )
        DotSizeSlider(
            sizeDp = state.settings.dotSizeDp,
            onChange = { onEvent(AppearanceUiEvent.DotSizeChanged(it)) },
        )
        IdleOpacitySlider(
            opacityPercent = state.settings.idleOpacityPercent,
            onChange = { onEvent(AppearanceUiEvent.IdleOpacityChanged(it)) },
        )
    }
}

@Composable
private fun DotSizeSlider(
    sizeDp: Int,
    onChange: (Int) -> Unit,
) {
    val label = stringResource(R.string.dot_size_label)
    val valueText = stringResource(R.string.dot_size_value, sizeDp)
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = valueText, style = MaterialTheme.typography.bodyMedium)
        Slider(
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText
            },
            value = sizeDp.toFloat(),
            onValueChange = { onChange(it.roundToInt().roundToMultiple(DOT_SIZE_STEP_DP)) },
            valueRange = Limits.MIN_DOT_SIZE_DP.toFloat()..Limits.MAX_DOT_SIZE_DP.toFloat(),
            steps = DOT_SIZE_STEPS,
        )
    }
}

@Composable
private fun IdleOpacitySlider(
    opacityPercent: Int,
    onChange: (Int) -> Unit,
) {
    val label = stringResource(R.string.idle_opacity_label)
    val valueText = stringResource(R.string.idle_opacity_value, opacityPercent)
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = valueText, style = MaterialTheme.typography.bodyMedium)
        Slider(
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText
            },
            value = opacityPercent.toFloat(),
            onValueChange = { onChange(it.roundToInt().roundToMultiple(IDLE_OPACITY_STEP_PERCENT)) },
            valueRange = Limits.MIN_IDLE_OPACITY_PERCENT.toFloat()..Limits.MAX_IDLE_OPACITY_PERCENT.toFloat(),
            steps = IDLE_OPACITY_STEPS,
        )
    }
}

private fun Int.roundToMultiple(step: Int): Int = (this.toFloat() / step).roundToInt() * step

private const val DOT_SIZE_STEP_DP = 2
private const val IDLE_OPACITY_STEP_PERCENT = 5

private const val DOT_SIZE_STEPS =
    (Limits.MAX_DOT_SIZE_DP - Limits.MIN_DOT_SIZE_DP) / DOT_SIZE_STEP_DP - 1

private const val IDLE_OPACITY_STEPS =
    (Limits.MAX_IDLE_OPACITY_PERCENT - Limits.MIN_IDLE_OPACITY_PERCENT) / IDLE_OPACITY_STEP_PERCENT - 1

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenPreview() {
    DotapTheme {
        AppearanceScreen(state = AppearanceUiState(isLoading = false), onEvent = {}, onBack = {})
    }
}
