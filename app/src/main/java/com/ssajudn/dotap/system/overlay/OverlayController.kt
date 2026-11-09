package com.ssajudn.dotap.system.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.gesture.GestureEngine
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.OverlayVisibility

class OverlayController(
    private val context: Context,
    private val presenter: DotPresenter,
) : OverlayVisibility {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var dotView: DotView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var snapAnimator: ValueAnimator? = null
    var gestureEngine: GestureEngine? = null
    var accessibilityClick: (() -> Unit)? = null

    private var currentDragX = 0f
    private var currentDragY = 0f

    val view: View? get() = dotView

    fun show() {
        if (dotView != null) return

        val state = presenter.uiState.value
        val dotSizePx = context.dpToPx(state.dotSizeDp)
        val screenSize = getScreenSize(windowManager)
        val params = buildLayoutParams(state, dotSizePx, screenSize)
        layoutParams = params

        val view = createDotView(context, { gestureEngine }, { accessibilityClick?.invoke() })
        view.render(state)
        if (attachOverlayView(windowManager, view, params)) {
            dotView = view
        }
    }

    fun update(state: DotUiState) {
        val view = dotView ?: return
        val params = layoutParams ?: return
        view.render(state)

        val dotSizePx = context.dpToPx(state.dotSizeDp)
        params.width = dotSizePx
        params.height = dotSizePx

        val screenSize = getScreenSize(windowManager)
        val maxY = (screenSize.y - dotSizePx).coerceAtLeast(0)
        params.y = (maxY * state.yRatio).toInt().coerceIn(0, maxY)
        params.x = if (state.isRightSide) screenSize.x - dotSizePx else 0

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException updating overlay view", e)
        }
    }

    fun hide() {
        snapAnimator?.cancel()
        snapAnimator = null
        val view = dotView ?: return
        try {
            windowManager.removeView(view)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException removing overlay view", e)
        } finally {
            dotView = null
            layoutParams = null
        }
    }

    fun onConfigurationChanged() {
        val view = dotView
        if (view == null) {
            // A failed add is retried on the next configuration change (ARCHITECTURE §12).
            show()
            return
        }
        val params = layoutParams ?: return
        val state = presenter.uiState.value
        val dotSizePx = context.dpToPx(state.dotSizeDp)
        val screenSize = getScreenSize(windowManager)

        val maxY = (screenSize.y - dotSizePx).coerceAtLeast(0)
        params.y = (maxY * state.yRatio).toInt().coerceIn(0, maxY)
        params.x = if (state.isRightSide) screenSize.x - dotSizePx else 0

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException updating view on config changed", e)
        }
    }

    fun onDragStart() {
        val params = layoutParams ?: return
        currentDragX = params.x.toFloat()
        currentDragY = params.y.toFloat()
    }

    fun onDrag(dx: Float, dy: Float) {
        val params = layoutParams ?: return
        val view = dotView ?: return
        val screenSize = getScreenSize(windowManager)
        val dotSizePx = view.width
        val maxY = (screenSize.y - dotSizePx).coerceAtLeast(0)
        val maxX = (screenSize.x - dotSizePx).coerceAtLeast(0)

        currentDragX += dx
        currentDragY += dy
        params.x = currentDragX.toInt().coerceIn(0, maxX)
        params.y = currentDragY.toInt().coerceIn(0, maxY)

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error updating during drag", e)
        }
    }

    fun onDragEnd() {
        val params = layoutParams ?: return
        val view = dotView ?: return
        val screenSize = getScreenSize(windowManager)
        val dotSizePx = view.width
        val startX = params.x
        val isRight = (startX + dotSizePx / 2) >= screenSize.x / 2
        val targetX = if (isRight) screenSize.x - dotSizePx else 0
        val maxY = (screenSize.y - dotSizePx).coerceAtLeast(1)
        val yRatio = (params.y.toFloat() / maxY).coerceIn(Limits.MIN_Y_RATIO, Limits.MAX_Y_RATIO)

        snapAnimator?.cancel()
        val animator = ValueAnimator.ofInt(startX, targetX)
        animator.duration = SNAP_ANIMATION_DURATION_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            params.x = valueAnimator.animatedValue as Int
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error during snap animation", e)
            }
        }
        // Publish the new position only once settled, otherwise the state collector jumps the dot mid-animation.
        animator.doOnEnd {
            snapAnimator = null
            presenter.updatePosition(isRightSide = isRight, yRatio = yRatio)
        }
        snapAnimator = animator
        animator.start()
    }

    override fun hideForCapture() {
        presenter.setHiddenForCapture(true)
        dotView?.render(presenter.uiState.value)
    }

    override fun restoreAfterCapture() {
        presenter.setHiddenForCapture(false)
        dotView?.render(presenter.uiState.value)
    }

    override fun showMessage(message: MessageType) {
        val textRes = when (message) {
            MessageType.SCREENSHOT_UNAVAILABLE -> R.string.screenshot_unavailable
        }
        Toast.makeText(context, textRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "OverlayController"
        private const val SNAP_ANIMATION_DURATION_MS = 250L
    }
}

private fun createDotView(
    context: Context,
    engineProvider: () -> GestureEngine?,
    onAccessibilityClick: () -> Unit,
): DotView = DotView(
    context = context,
    onTouchListener = { _, event ->
        val engine = engineProvider()
        if (engine != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> engine.down(event.rawX, event.rawY)
                MotionEvent.ACTION_MOVE -> engine.move(event.rawX, event.rawY)
                MotionEvent.ACTION_UP -> engine.up()
                MotionEvent.ACTION_CANCEL -> engine.cancel()
            }
            true
        } else {
            false
        }
    },
    onClickAction = onAccessibilityClick,
)

private fun attachOverlayView(
    windowManager: WindowManager,
    view: View,
    params: WindowManager.LayoutParams,
): Boolean = try {
    windowManager.addView(view, params)
    true
} catch (e: WindowManager.BadTokenException) {
    Log.e("OverlayController", "BadTokenException adding overlay view", e)
    false
} catch (e: IllegalArgumentException) {
    Log.e("OverlayController", "IllegalArgumentException adding overlay view", e)
    false
}

private fun getScreenSize(windowManager: WindowManager): Point = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val bounds = windowManager.currentWindowMetrics.bounds
    Point(bounds.width(), bounds.height())
} else {
    val point = Point()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getSize(point)
    point
}

private fun buildLayoutParams(
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
        x = if (state.isRightSide) screenSize.x - dotSizePx else 0
        y = (maxY * state.yRatio).toInt().coerceIn(0, maxY)
    }
}

private fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
