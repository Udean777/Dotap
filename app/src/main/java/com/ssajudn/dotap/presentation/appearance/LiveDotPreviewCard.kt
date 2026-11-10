package com.ssajudn.dotap.presentation.appearance

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R

private const val PERCENT_DIVISOR = 100f
private const val PREVIEW_INNER_DOT_RATIO = 3
private const val MIN_INNER_DOT_SIZE_DP = 8
private const val PREVIEW_HEIGHT_DP = 140
private const val DOT_ANIM_DURATION_MS = 150
private const val BG_ALPHA = 0.5f
private const val INNER_DOT_ALPHA = 0.9f

@Composable
fun LiveDotPreviewCard(
    dotSizeDp: Int,
    idleOpacityPercent: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LiveDotPreviewHeader()
            LiveDotPreviewBox(dotSizeDp = dotSizeDp, idleOpacityPercent = idleOpacityPercent)
        }
    }
}

@Composable
private fun LiveDotPreviewHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.appearance_preview_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dot_preview_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LiveDotPreviewBox(
    dotSizeDp: Int,
    idleOpacityPercent: Int,
) {
    val animatedDotSize by animateDpAsState(
        targetValue = dotSizeDp.dp,
        animationSpec = tween(durationMillis = DOT_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "DotSizeAnim",
    )
    val animatedOpacity by animateFloatAsState(
        targetValue = idleOpacityPercent / PERCENT_DIVISOR,
        animationSpec = tween(durationMillis = DOT_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "DotOpacityAnim",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_HEIGHT_DP.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = BG_ALPHA),
                shape = RoundedCornerShape(16.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        PreviewDotVisual(size = animatedDotSize, opacity = animatedOpacity)
    }
}

@Composable
private fun PreviewDotVisual(
    size: Dp,
    opacity: Float,
) {
    val innerSize = (size / PREVIEW_INNER_DOT_RATIO).coerceAtLeast(MIN_INNER_DOT_SIZE_DP.dp)
    Box(
        modifier = Modifier
            .size(size)
            .alpha(opacity)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = INNER_DOT_ALPHA),
                    shape = CircleShape,
                ),
        )
    }
}
