package com.ssajudn.dotap.system.overlay

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.ssajudn.dotap.domain.model.Limits

internal data class DragMetrics(
    val view: View,
    val params: WindowManager.LayoutParams,
    val startX: Int,
    val targetX: Int,
    val isRight: Boolean,
    val xRatio: Float,
    val yRatio: Float,
)

internal fun measureDrag(
    view: View,
    params: WindowManager.LayoutParams,
    windowManager: WindowManager,
): DragMetrics {
    val screenSize = getScreenSize(windowManager)
    val dotSizePx = view.width
    val startX = params.x
    val maxX = (screenSize.x - dotSizePx).coerceAtLeast(1)
    val maxY = (screenSize.y - dotSizePx).coerceAtLeast(1)
    val isRight = (startX + dotSizePx / 2) >= screenSize.x / 2
    return DragMetrics(
        view = view,
        params = params,
        startX = startX,
        isRight = isRight,
        xRatio = (startX.toFloat() / maxX).coerceIn(0f, 1f),
        yRatio = (params.y.toFloat() / maxY).coerceIn(Limits.MIN_Y_RATIO, Limits.MAX_Y_RATIO),
        targetX = if (isRight) screenSize.x - dotSizePx else 0,
    )
}

internal fun createSnapAnimator(
    metrics: DragMetrics,
    windowManager: WindowManager,
    onSettled: () -> Unit,
): ValueAnimator {
    val animator = ValueAnimator.ofInt(metrics.startX, metrics.targetX)
    animator.duration = SNAP_ANIMATION_DURATION_MS
    animator.interpolator = DecelerateInterpolator()
    animator.addUpdateListener { valueAnimator ->
        metrics.params.x = valueAnimator.animatedValue as Int
        try {
            windowManager.updateViewLayout(metrics.view, metrics.params)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error during snap animation", e)
        }
    }
    // Publish the new position only once settled, otherwise the state collector jumps the dot mid-animation.
    animator.doOnEnd { onSettled() }
    return animator
}

private const val TAG = "OverlayDrag"
private const val SNAP_ANIMATION_DURATION_MS = 250L
