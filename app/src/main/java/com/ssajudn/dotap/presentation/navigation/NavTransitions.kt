package com.ssajudn.dotap.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val NAV_ENTER_DURATION_MS = 300
private const val NAV_EXIT_DURATION_MS = 250
private const val PARALLAX_DIVISOR = 4

fun navEnterTransition(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth / PARALLAX_DIVISOR },
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
) + fadeIn(
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = LinearOutSlowInEasing,
    ),
)

fun navExitTransition(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth / PARALLAX_DIVISOR },
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
) + fadeOut(
    animationSpec = tween(
        durationMillis = NAV_EXIT_DURATION_MS,
        easing = FastOutLinearInEasing,
    ),
)

fun navPopEnterTransition(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth / PARALLAX_DIVISOR },
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
) + fadeIn(
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = LinearOutSlowInEasing,
    ),
)

fun navPopExitTransition(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth / PARALLAX_DIVISOR },
    animationSpec = tween(
        durationMillis = NAV_ENTER_DURATION_MS,
        easing = FastOutSlowInEasing,
    ),
) + fadeOut(
    animationSpec = tween(
        durationMillis = NAV_EXIT_DURATION_MS,
        easing = FastOutLinearInEasing,
    ),
)
