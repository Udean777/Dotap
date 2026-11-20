package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotStyle
import com.ssajudn.dotap.presentation.common.DotapCard

@Composable
internal fun DotStyleCard(
    state: AppearanceUiState,
    onStyleSelected: (DotStyle) -> Unit,
    onPickImageClick: () -> Unit,
) {
    DotapCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DotStyleChipsRow(
                selectedStyle = state.settings.dotStyle,
                onStyleSelected = onStyleSelected,
            )
            if (state.settings.dotStyle == DotStyle.CUSTOM_IMAGE) {
                OutlinedButton(
                    onClick = onPickImageClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.dot_style_pick_image))
                }
            }
            if (state.imagePickError) {
                Text(
                    text = stringResource(R.string.dot_style_image_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DotStyleChipsRow(
    selectedStyle: DotStyle,
    onStyleSelected: (DotStyle) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DotStyleChip(
            label = stringResource(R.string.dot_style_default),
            selected = selectedStyle == DotStyle.DEFAULT,
            onClick = { onStyleSelected(DotStyle.DEFAULT) },
            modifier = Modifier.weight(1f),
        )
        DotStyleChip(
            label = stringResource(R.string.dot_style_app_icon),
            selected = selectedStyle == DotStyle.APP_ICON,
            onClick = { onStyleSelected(DotStyle.APP_ICON) },
            modifier = Modifier.weight(1f),
        )
        DotStyleChip(
            label = stringResource(R.string.dot_style_custom_image),
            selected = selectedStyle == DotStyle.CUSTOM_IMAGE,
            onClick = { onStyleSelected(DotStyle.CUSTOM_IMAGE) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DotStyleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, maxLines = 1) },
        modifier = modifier,
    )
}
