package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import kotlin.math.roundToInt

private const val DOT_SIZE_STEP_DP = 2
private const val IDLE_OPACITY_STEP_PERCENT = 5
private const val DIVIDER_ALPHA = 0.5f

private const val DOT_SIZE_STEPS =
    (Limits.MAX_DOT_SIZE_DP - Limits.MIN_DOT_SIZE_DP) / DOT_SIZE_STEP_DP - 1

private const val IDLE_OPACITY_STEPS =
    (Limits.MAX_IDLE_OPACITY_PERCENT - Limits.MIN_IDLE_OPACITY_PERCENT) / IDLE_OPACITY_STEP_PERCENT - 1

@Composable
internal fun DimensionsCard(
    settings: DotSettings,
    onEvent: (AppearanceUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DotSizeSlider(
                sizeDp = settings.dotSizeDp,
                onChange = { onEvent(AppearanceUiEvent.DotSizeChanged(it)) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA))
            IdleOpacitySlider(
                opacityPercent = settings.idleOpacityPercent,
                onChange = { onEvent(AppearanceUiEvent.IdleOpacityChanged(it)) },
            )
        }
    }
}

@Composable
private fun SliderHeader(label: String, valueText: String) {
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
private fun DotSizeSlider(
    sizeDp: Int,
    onChange: (Int) -> Unit,
) {
    val label = stringResource(R.string.dot_size_label)
    val valueText = stringResource(R.string.dot_size_value, sizeDp)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderHeader(label = label, valueText = valueText)
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderHeader(label = label, valueText = valueText)
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
