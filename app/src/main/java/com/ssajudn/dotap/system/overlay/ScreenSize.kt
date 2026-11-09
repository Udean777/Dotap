package com.ssajudn.dotap.system.overlay

import android.graphics.Point
import android.os.Build
import android.view.WindowManager

internal fun getScreenSize(windowManager: WindowManager): Point = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val bounds = windowManager.currentWindowMetrics.bounds
    Point(bounds.width(), bounds.height())
} else {
    val point = Point()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getSize(point)
    point
}
