package com.ssajudn.dotap.system.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

internal fun attachOverlayView(
    windowManager: WindowManager,
    view: View,
    params: WindowManager.LayoutParams,
): Boolean = try {
    windowManager.addView(view, params)
    true
} catch (e: WindowManager.BadTokenException) {
    Log.e(TAG, "BadTokenException adding overlay view", e)
    false
} catch (e: IllegalArgumentException) {
    Log.e(TAG, "IllegalArgumentException adding overlay view", e)
    false
}

internal fun buildLayoutParams(
    state: DotUiState,
    dotSizePx: Int,
    screenSize: Point,
): WindowManager.LayoutParams {
    val maxY = (screenSize.y - dotSizePx).coerceAtLeast(0)
    return WindowManager.LayoutParams(
        dotSizePx,
        dotSizePx,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = resolveX(state, dotSizePx, screenSize)
        y = (maxY * state.yRatio).toInt().coerceIn(0, maxY)
    }
}

internal fun resolveX(state: DotUiState, dotSizePx: Int, screenSize: Point): Int {
    val maxX = (screenSize.x - dotSizePx).coerceAtLeast(0)
    return when {
        state.isSnapToEdgeEnabled && state.isRightSide -> maxX
        state.isSnapToEdgeEnabled -> 0
        else -> (maxX * state.xRatio).toInt().coerceIn(0, maxX)
    }
}

internal fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

private const val TAG = "OverlayLayoutParams"
