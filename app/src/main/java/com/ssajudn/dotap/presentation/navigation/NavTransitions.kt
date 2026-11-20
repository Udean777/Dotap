package com.ssajudn.dotap.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

private const val NAV_TRANSITION_DURATION_MS = 260

fun navEnterTransition(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = NAV_TRANSITION_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
)

fun navExitTransition(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = NAV_TRANSITION_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
)

fun navPopEnterTransition(): EnterTransition = navEnterTransition()

fun navPopExitTransition(): ExitTransition = navExitTransition()
