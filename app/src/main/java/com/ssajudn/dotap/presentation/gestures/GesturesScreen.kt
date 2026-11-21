package com.ssajudn.dotap.presentation.gestures

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.presentation.common.AppPickerBottomSheet
import com.ssajudn.dotap.presentation.common.AppPickerState
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.DotapMascot
import com.ssajudn.dotap.presentation.common.DotapTopAppBar
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
import com.ssajudn.dotap.presentation.common.LabeledStepSlider
import com.ssajudn.dotap.presentation.common.ScreenMascotSize
import com.ssajudn.dotap.presentation.common.StepSliderSpec
import com.ssajudn.dotap.presentation.theme.DotapTheme

private const val DOUBLE_TAP_STEP_MS = 50
private const val LONG_PRESS_STEP_MS = 100

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
        topBar = {
            DotapTopAppBar(
                title = stringResource(R.string.gestures_title),
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        GesturesBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }

    val pickerGesture = state.activeAppPickerGesture
    if (pickerGesture != null) {
        AppPickerBottomSheet(
            state = AppPickerState(
                apps = state.installedApps,
                isLoading = state.isLoadingApps,
                selectedPackage = state.settings.appPackageFor(pickerGesture),
            ),
            onSelect = { onEvent(GesturesUiEvent.AppSelected(pickerGesture, it.packageName)) },
            onDismiss = { onEvent(GesturesUiEvent.DismissAppPicker) },
        )
    }
}

@Composable
private fun GesturesBody(
    state: GesturesUiState,
    onEvent: (GesturesUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    if (state.isLoading) return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DotapMascot(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(ScreenMascotSize),
            mascotRes = R.drawable.mascot_onboarding_gestures,
        )
        ExpressiveSectionHeader(text = stringResource(R.string.gestures_actions_section))
        GestureActionsCard(state = state, onEvent = onEvent)

        ExpressiveSectionHeader(text = stringResource(R.string.gestures_timing_section))
        TimingCard(settings = state.settings, onEvent = onEvent)
    }
}

@Composable
private fun TimingCard(
    settings: DotSettings,
    onEvent: (GesturesUiEvent) -> Unit,
) {
    DotapCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LabeledStepSlider(
                spec = doubleTapSliderSpec(settings.doubleTapTimeoutMs.toInt()),
                onValueChange = { onEvent(GesturesUiEvent.DoubleTapTimeoutChanged(it)) },
            )
            DotapDivider()
            LabeledStepSlider(
                spec = longPressSliderSpec(settings.longPressDurationMs.toInt()),
                onValueChange = { onEvent(GesturesUiEvent.LongPressDurationChanged(it)) },
            )
        }
    }
}

@Composable
private fun doubleTapSliderSpec(valueMs: Int): StepSliderSpec = StepSliderSpec(
    label = stringResource(R.string.double_tap_timeout_label),
    valueText = stringResource(R.string.timeout_value, valueMs),
    value = valueMs,
    min = Limits.MIN_DOUBLE_TAP_TIMEOUT_MS.toInt(),
    max = Limits.MAX_DOUBLE_TAP_TIMEOUT_MS.toInt(),
    step = DOUBLE_TAP_STEP_MS,
)

@Composable
private fun longPressSliderSpec(valueMs: Int): StepSliderSpec = StepSliderSpec(
    label = stringResource(R.string.long_press_duration_label),
    valueText = stringResource(R.string.timeout_value, valueMs),
    value = valueMs,
    min = Limits.MIN_LONG_PRESS_DURATION_MS.toInt(),
    max = Limits.MAX_LONG_PRESS_DURATION_MS.toInt(),
    step = LONG_PRESS_STEP_MS,
)

@Preview(showBackground = true)
@Composable
private fun GesturesScreenPreview() {
    DotapTheme {
        GesturesScreen(state = GesturesUiState(isLoading = false), onEvent = {}, onBack = {})
    }
}
