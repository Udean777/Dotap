package com.ssajudn.dotap.presentation.gestures

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
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

@Composable
fun GesturesScreen(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { GesturesTopAppBar(onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        GesturesBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GesturesTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.gestures_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExpressiveSectionHeader(text = stringResource(R.string.gestures_actions_section))
        GestureActionsCard(settings = state.settings, onEvent = onEvent)

        ExpressiveSectionHeader(text = stringResource(R.string.gestures_timing_section))
        TimingCard(settings = state.settings, onEvent = onEvent)
    }
}

@Composable
private fun TimingCard(
    settings: DotSettings,
    onEvent: (GesturesUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TimeoutSlider(
                spec = DOUBLE_TAP_SPEC,
                valueMs = settings.doubleTapTimeoutMs.toInt(),
                onChange = { onEvent(GesturesUiEvent.DoubleTapTimeoutChanged(it)) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            TimeoutSlider(
                spec = LONG_PRESS_SPEC,
                valueMs = settings.longPressDurationMs.toInt(),
                onChange = { onEvent(GesturesUiEvent.LongPressDurationChanged(it)) },
            )
        }
    }
}

@Composable
private fun TimeoutSliderHeader(label: String, valueText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TimeoutSliderHeader(label = label, valueText = valueText)
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
