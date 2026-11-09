package com.ssajudn.dotap.system.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.view.View
import com.ssajudn.dotap.R

@SuppressLint("ViewConstructor")
class DotView(
    context: Context,
    private val onTouchListener: (View, MotionEvent) -> Boolean = { _, _ -> false },
    private val onClickAction: () -> Unit = {},
) : View(context) {

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(160, 30, 30, 30)
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(200, 255, 255, 255)
        strokeWidth = dpToPx(OUTER_RING_STROKE_DP)
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(220, 255, 255, 255)
    }

    private val exclusionRects = mutableListOf<Rect>()

    init {
        contentDescription = context.getString(R.string.dot_content_description)
        isClickable = true
        isFocusable = false
    }

    fun render(state: DotUiState) {
        animate().cancel()
        if (!state.isVisible || state.isHiddenForCapture) {
            visibility = GONE
            alpha = 0f
            updateExclusionRects(false)
        } else {
            visibility = VISIBLE
            if (state.alpha >= 1f || alpha == 0f) {
                alpha = state.alpha
            } else {
                animate().alpha(state.alpha).setDuration(FADE_DURATION_MS).start()
            }
            updateExclusionRects(true)
        }
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateExclusionRects(visibility == VISIBLE)
        }
    }

    private fun updateExclusionRects(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exclusionRects.clear()
            if (enabled && width > 0 && height > 0) {
                exclusionRects.add(Rect(0, 0, width, height))
            }
            systemGestureExclusionRects = exclusionRects
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width.coerceAtMost(height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        canvas.drawCircle(centerX, centerY, radius, outerPaint)
        canvas.drawCircle(centerX, centerY, radius * RING_RATIO, ringPaint)
        canvas.drawCircle(centerX, centerY, radius * CENTER_DOT_RATIO, centerPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = onTouchListener(this, event)
        return handled || super.onTouchEvent(event)
    }

    // TalkBack activation must trigger the single-tap action, which raw touch handling alone never reaches.
    override fun performClick(): Boolean {
        super.performClick()
        onClickAction()
        return true
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

    companion object {
        private const val FADE_DURATION_MS = 250L
        private const val OUTER_RING_STROKE_DP = 1.5f
        private const val RING_RATIO = 0.72f
        private const val CENTER_DOT_RATIO = 0.35f
    }
}
