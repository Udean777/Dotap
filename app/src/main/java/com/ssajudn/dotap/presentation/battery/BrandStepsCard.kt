package com.ssajudn.dotap.presentation.battery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.OemBrand

private const val BRAND_ANIM_DURATION_MS = 250
private const val BRAND_SLIDE_DIVISOR = 8

internal data class StepCardActions(
    val onOpenBatterySettings: () -> Unit,
    val onOpenWebHelp: () -> Unit,
)

@Composable
internal fun BrandStepsCard(
    brand: OemBrand,
    onOpenBatterySettings: () -> Unit,
    onOpenWebHelp: () -> Unit,
) {
    val actions = StepCardActions(onOpenBatterySettings, onOpenWebHelp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        AnimatedContent(
            targetState = brand,
            transitionSpec = { brandTransitionSpec() },
            label = "BrandStepsContent",
        ) { targetBrand ->
            BrandStepsColumn(brand = targetBrand, actions = actions)
        }
    }
}

private fun brandTransitionSpec(): ContentTransform = (
    fadeIn(animationSpec = tween(BRAND_ANIM_DURATION_MS)) +
        slideInVertically(animationSpec = tween(BRAND_ANIM_DURATION_MS)) { height ->
            height / BRAND_SLIDE_DIVISOR
        }
    ).togetherWith(fadeOut(animationSpec = tween(BRAND_ANIM_DURATION_MS)))

@Composable
private fun BrandStepsColumn(
    brand: OemBrand,
    actions: StepCardActions,
) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BrandStepsHeader(brand = brand)
        Text(
            text = stringResource(brand.stepsResId()),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CardActions(actions = actions)
    }
}

@Composable
private fun BrandStepsHeader(brand: OemBrand) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_battery),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Text(
            text = stringResource(brand.labelResId()),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CardActions(actions: StepCardActions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = actions.onOpenBatterySettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.battery_open_settings_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        OutlinedButton(
            onClick = actions.onOpenWebHelp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.battery_view_online_guide),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
