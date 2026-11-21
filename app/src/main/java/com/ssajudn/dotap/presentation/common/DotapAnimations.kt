package com.ssajudn.dotap.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

object DotapAnimations {
    const val PRESS_SCALE = 0.96f
    const val ENTRANCE_SCALE = 0.94f
    const val ENTRANCE_DURATION_MS = 220

    fun bounceSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

fun Modifier.bounceOnPress(interactionSource: InteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DotapAnimations.PRESS_SCALE else 1f,
        animationSpec = DotapAnimations.bounceSpring(),
        label = "bounceOnPress",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.bounceOnPress(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    bounceOnPress(interactionSource)
}

@Composable
fun DotapEntranceContainer(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(DotapAnimations.ENTRANCE_DURATION_MS)) +
            scaleIn(
                initialScale = DotapAnimations.ENTRANCE_SCALE,
                animationSpec = tween(DotapAnimations.ENTRANCE_DURATION_MS),
            ),
    ) {
        content()
    }
}
