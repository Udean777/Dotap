package com.ssajudn.dotap.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun LabeledStepSlider(
    spec: StepSliderSpec,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SliderHeader(label = spec.label, valueText = spec.valueText)
        Slider(
            modifier = Modifier.semantics {
                contentDescription = spec.label
                stateDescription = spec.valueText
            },
            value = spec.value.toFloat(),
            onValueChange = { raw ->
                val snapped = (raw / spec.step).roundToInt() * spec.step
                onValueChange(snapped.coerceIn(spec.min, spec.max))
            },
            valueRange = spec.min.toFloat()..spec.max.toFloat(),
            steps = spec.steps,
        )
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
            shape = MaterialTheme.shapes.extraSmall,
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
