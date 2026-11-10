package com.ssajudn.dotap.system.overlay

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator

internal const val PRESS_SCALE = 0.90f
internal const val MENU_OPEN_SCALE = 0.92f
internal const val IDLE_SCALE = 0.94f
internal const val DEFAULT_SCALE = 1.0f

internal const val PRESS_DOWN_DURATION_MS = 100L
internal const val PRESS_UP_DURATION_MS = 180L
internal const val IDLE_FADE_DURATION_MS = 350L
internal const val WAKE_UP_DURATION_MS = 160L
internal const val MENU_ANIM_DURATION_MS = 220L

internal const val DOCK_SQUASH_X = 0.88f
internal const val DOCK_SQUASH_Y = 1.00f
internal const val DOCK_SQUASH_DURATION_MS = 80L
internal const val DOCK_REBOUND_DURATION_MS = 140L

internal const val DEFAULT_RING_ALPHA = 200
internal const val ACTIVE_RING_ALPHA = 240
internal const val RGB_MAX = 255
internal const val ACCENT_RED = 74
internal const val ACCENT_GREEN = 137
internal const val ACCENT_BLUE = 220

internal fun View.animatePressState(isPressed: Boolean, isMenuOpen: Boolean) {
    if (isMenuOpen) return
    val targetScale = if (isPressed) {
        PRESS_SCALE
    } else if (alpha < 1.0f) {
        IDLE_SCALE
    } else {
        DEFAULT_SCALE
    }
    val duration = if (isPressed) PRESS_DOWN_DURATION_MS else PRESS_UP_DURATION_MS
    animate()
        .scaleX(targetScale)
        .scaleY(targetScale)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

internal fun View.animateMenuReaction(isOpen: Boolean) {
    val targetScale = if (isOpen) {
        MENU_OPEN_SCALE
    } else if (alpha < 1.0f) {
        IDLE_SCALE
    } else {
        DEFAULT_SCALE
    }
    animate()
        .scaleX(targetScale)
        .scaleY(targetScale)
        .setDuration(MENU_ANIM_DURATION_MS)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

internal fun View.animateDockSquashAndRebound() {
    animate()
        .scaleX(DOCK_SQUASH_X)
        .scaleY(DOCK_SQUASH_Y)
        .setDuration(DOCK_SQUASH_DURATION_MS)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            animate()
                .scaleX(DEFAULT_SCALE)
                .scaleY(DEFAULT_SCALE)
                .setDuration(DOCK_REBOUND_DURATION_MS)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        .start()
}

internal fun View.animateIdleFade(targetAlpha: Float) {
    animate()
        .alpha(targetAlpha)
        .scaleX(IDLE_SCALE)
        .scaleY(IDLE_SCALE)
        .setDuration(IDLE_FADE_DURATION_MS)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

internal fun View.animateWakeupFade() {
    animate()
        .alpha(1.0f)
        .scaleX(DEFAULT_SCALE)
        .scaleY(DEFAULT_SCALE)
        .setDuration(WAKE_UP_DURATION_MS)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

internal fun createRingColorAnimator(
    onColorUpdate: (Int) -> Unit,
    isOpen: Boolean,
    currentColor: Int,
): ValueAnimator {
    val targetColor = if (isOpen) {
        Color.argb(ACTIVE_RING_ALPHA, ACCENT_RED, ACCENT_GREEN, ACCENT_BLUE)
    } else {
        Color.argb(DEFAULT_RING_ALPHA, RGB_MAX, RGB_MAX, RGB_MAX)
    }
    return ValueAnimator.ofArgb(currentColor, targetColor).apply {
        duration = MENU_ANIM_DURATION_MS
        addUpdateListener { onColorUpdate(it.animatedValue as Int) }
    }
}

internal fun View.dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
