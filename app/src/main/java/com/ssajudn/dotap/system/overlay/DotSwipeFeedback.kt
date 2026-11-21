package com.ssajudn.dotap.system.overlay

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.ssajudn.dotap.domain.model.GestureType
import kotlin.math.hypot

private const val ANTICIPATION_MAX_DP = 10f
private const val ANTICIPATION_DAMPING = 0.2f
private const val ANTICIPATION_TILT_DEG = 12f
private const val ANTICIPATION_COIL = 0.1f
private const val SETTLE_MS = 200L
private const val SETTLE_OVERSHOOT = 2.0f
private const val DASH_DISTANCE_DP = 32f
private const val DASH_TILT_DEG = 16f
private const val DASH_OUT_MS = 120L
private const val DASH_OUT_ACCELERATION = 1.5f
private const val DASH_ENTER_OFFSET = 0.7f
private const val DASH_ENTER_SCALE = 0.8f
private const val DASH_IN_MS = 300L
private const val DASH_IN_OVERSHOOT = 2.2f

/**
 * Visual feedback for swipe gestures. The dot leans toward the finger while pulled, then dashes out
 * in the swipe direction and springs back in from the opposite side. Durations follow the system
 * animator scale, so "remove animations" ends each step instantly.
 */
internal class DotSwipeFeedback(val view: View) {
    private var isDashing = false
    private val density: Float get() = view.resources.displayMetrics.density

    fun anticipate(dx: Float, dy: Float) {
        val max = ANTICIPATION_MAX_DP * density
        val tx = (dx * ANTICIPATION_DAMPING).coerceIn(-max, max)
        val ty = (dy * ANTICIPATION_DAMPING).coerceIn(-max, max)
        val progress = hypot(tx, ty) / max
        val coil = 1f - ANTICIPATION_COIL * progress.coerceAtMost(1f)
        view.translationX = tx
        view.translationY = ty
        view.rotation = tx / max * ANTICIPATION_TILT_DEG
        view.scaleX = coil
        view.scaleY = coil
    }

    fun settle() {
        view.animate()
            .translationX(0f)
            .translationY(0f)
            .rotation(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(SETTLE_MS)
            .setInterpolator(OvershootInterpolator(SETTLE_OVERSHOOT))
            .start()
    }

    fun dash(gesture: GestureType) {
        val (dirX, dirY) = gesture.swipeDirection() ?: return
        val distance = DASH_DISTANCE_DP * density
        val tilt = dirX * DASH_TILT_DEG
        isDashing = true
        view.animate().cancel()
        view.animate()
            .translationX(dirX * distance)
            .translationY(dirY * distance)
            .rotation(tilt)
            .alpha(0f)
            .setDuration(DASH_OUT_MS)
            .setInterpolator(AccelerateInterpolator(DASH_OUT_ACCELERATION))
            .withEndAction { enterFromOppositeSide(-dirX * distance, -dirY * distance, -tilt) }
            .start()
    }

    /** Stops a running dash so a new touch never starts from a half-faded dot. */
    fun cancel() {
        if (!isDashing) return
        isDashing = false
        view.animate().cancel()
        view.resetSwipeTransform()
        view.alpha = 1f
    }

    private fun enterFromOppositeSide(
        fromX: Float,
        fromY: Float,
        fromTilt: Float,
    ) {
        view.translationX = fromX * DASH_ENTER_OFFSET
        view.translationY = fromY * DASH_ENTER_OFFSET
        view.rotation = fromTilt
        view.scaleX = DASH_ENTER_SCALE
        view.scaleY = DASH_ENTER_SCALE
        view.animate()
            .translationX(0f)
            .translationY(0f)
            .rotation(0f)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(DASH_IN_MS)
            .setInterpolator(OvershootInterpolator(DASH_IN_OVERSHOOT))
            .withEndAction { isDashing = false }
            .start()
    }
}

internal fun View.resetSwipeTransform() {
    translationY = 0f
    rotation = 0f
    scaleX = 1f
    scaleY = 1f
}

private fun GestureType.swipeDirection(): Pair<Float, Float>? = when (this) {
    GestureType.SWIPE_UP -> 0f to -1f
    GestureType.SWIPE_DOWN -> 0f to 1f
    GestureType.SWIPE_LEFT -> -1f to 0f
    GestureType.SWIPE_RIGHT -> 1f to 0f
    else -> null
}

private const val SINGLE_TAP_SCALE = 0.84f
private const val DOUBLE_TAP_SCALE = 0.78f
private const val TAP_POP_UP_MS = 80L
private const val TAP_POP_DOWN_MS = 140L
private const val TAP_OVERSHOOT = 1.2f

internal fun playTapFeedback(
    view: View,
    gesture: GestureType,
) {
    val peakScale = when (gesture) {
        GestureType.SINGLE_TAP -> SINGLE_TAP_SCALE
        GestureType.DOUBLE_TAP -> DOUBLE_TAP_SCALE
        else -> return
    }
    view.animate().cancel()
    view.animate()
        .scaleX(peakScale)
        .scaleY(peakScale)
        .setDuration(TAP_POP_UP_MS)
        .withEndAction { springToFullScale(view) }
        .start()
}

private fun springToFullScale(view: View) {
    view.animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(TAP_POP_DOWN_MS)
        .setInterpolator(OvershootInterpolator(TAP_OVERSHOOT))
        .start()
}
