package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.LabeledStepSlider
import com.ssajudn.dotap.presentation.common.StepSliderSpec

private const val DOT_SIZE_STEP_DP = 2
private const val IDLE_OPACITY_STEP_PERCENT = 5

@Composable
internal fun DimensionsCard(
    settings: DotSettings,
    onEvent: (AppearanceUiEvent) -> Unit,
) {
    DotapCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LabeledStepSlider(
                spec = dotSizeSliderSpec(settings.dotSizeDp),
                onValueChange = { onEvent(AppearanceUiEvent.DotSizeChanged(it)) },
            )
            DotapDivider()
            LabeledStepSlider(
                spec = idleOpacitySliderSpec(settings.idleOpacityPercent),
                onValueChange = { onEvent(AppearanceUiEvent.IdleOpacityChanged(it)) },
            )
        }
    }
}

@Composable
private fun dotSizeSliderSpec(dotSizeDp: Int): StepSliderSpec = StepSliderSpec(
    label = stringResource(R.string.dot_size_label),
    valueText = stringResource(R.string.dot_size_value, dotSizeDp),
    value = dotSizeDp,
    min = Limits.MIN_DOT_SIZE_DP,
    max = Limits.MAX_DOT_SIZE_DP,
    step = DOT_SIZE_STEP_DP,
)

@Composable
private fun idleOpacitySliderSpec(opacityPercent: Int): StepSliderSpec = StepSliderSpec(
    label = stringResource(R.string.idle_opacity_label),
    valueText = stringResource(R.string.idle_opacity_value, opacityPercent),
    value = opacityPercent,
    min = Limits.MIN_IDLE_OPACITY_PERCENT,
    max = Limits.MAX_IDLE_OPACITY_PERCENT,
    step = IDLE_OPACITY_STEP_PERCENT,
)
