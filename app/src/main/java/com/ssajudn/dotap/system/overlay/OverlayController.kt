package com.ssajudn.dotap.system.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.gesture.GestureEngine
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
    var accessibilityLongClick: (() -> Unit)? = null

    /** Called when the dot settles after a drag so the service can persist the position. */
    var onPositionSettled: ((isRightSide: Boolean, yRatio: Float) -> Unit)? = null

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

        val view = createDotView(
            context = context,
            onTouch = { _, event -> handleTouchEvent(event, presenter, gestureEngine) },
            actions = DotView.ClickActions(
                onClick = { accessibilityClick?.invoke() },
                onLongClick = { accessibilityLongClick?.invoke() },
            ),
        )
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
        params.x = resolveX(state, dotSizePx, screenSize)

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
        params.x = resolveX(state, dotSizePx, screenSize)

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
            Log.e(TAG, "IllegalArgumentException updating view on drag", e)
        }
    }

    fun onDragEnd() {
        val view = dotView ?: return
        val params = layoutParams ?: return
        val metrics = measureDrag(view, params, windowManager)

        if (presenter.uiState.value.isSnapToEdgeEnabled) {
            val animator = createSnapAnimator(metrics, windowManager) {
                presenter.updatePosition(metrics.isRight, metrics.yRatio)
                onPositionSettled?.invoke(metrics.isRight, metrics.yRatio)
            }
            snapAnimator = animator
            animator.start()
        } else {
            presenter.updatePosition(
                isRightSide = metrics.isRight,
                yRatio = metrics.yRatio,
                xRatio = metrics.xRatio,
            )
            onPositionSettled?.invoke(metrics.isRight, metrics.yRatio)
        }
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

    private companion object {
        const val TAG = "OverlayController"
    }
}
