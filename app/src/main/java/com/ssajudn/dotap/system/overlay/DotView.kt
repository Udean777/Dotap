package com.ssajudn.dotap.system.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.MotionEvent
import android.view.View
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotStyle

@SuppressLint("ViewConstructor")
class DotView(
    context: Context,
    private val onTouchListener: (View, MotionEvent) -> Boolean = { _, _ -> false },
    private val actions: ClickActions = ClickActions(),
) : View(context) {

    data class ClickActions(
        val onClick: () -> Unit = {},
        val onLongClick: () -> Unit = {},
    )

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(OUTER_ALPHA, BG_DARK, BG_DARK, BG_DARK)
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(DEFAULT_RING_ALPHA, RGB_MAX, RGB_MAX, RGB_MAX)
        strokeWidth = dpToPx(OUTER_RING_STROKE_DP)
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(CENTER_ALPHA, RGB_MAX, RGB_MAX, RGB_MAX)
    }

    private val exclusionRects = mutableListOf<Rect>()
    private var colorAnimator: ValueAnimator? = null
    var isMenuOpen: Boolean = false
        private set

    private var dotStyle: DotStyle = DotStyle.DEFAULT
    private var isIdlePeeking: Boolean = false
    private var isRightSide: Boolean = true
    private var loadedImagePath: String? = null
    private var customBitmap: Bitmap? = null
    private var activeBitmap: Bitmap? = null
    private val appIconBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
    }
    private val peekingCatBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.dot_cat_peeking)
    }
    private val clipPath = Path()
    private val drawRect = RectF()

    init {
        contentDescription = context.getString(R.string.dot_content_description)
        isClickable = true
        isLongClickable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun render(state: DotUiState) {
        isRightSide = state.isRightSide
        isIdlePeeking = state.dotStyle == DotStyle.APP_ICON && state.isIdle
        if (state.dotStyle == DotStyle.CUSTOM_IMAGE && loadedImagePath != state.customDotImagePath) {
            loadedImagePath = state.customDotImagePath
            customBitmap = state.customDotImagePath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
        }
        activeBitmap = selectDotBitmap(
            style = state.dotStyle,
            peeking = isIdlePeeking,
            bitmaps = Triple(peekingCatBitmap, appIconBitmap, customBitmap),
        )
        if (!state.isVisible || state.isHiddenForCapture) {
            animate().cancel()
            colorAnimator?.cancel()
            visibility = GONE
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
            updateExclusionRects(false)
        } else {
            visibility = VISIBLE
            updateAlphaAndScale(state.alpha)
            animatePeekTranslation(this, isIdlePeeking, isRightSide)
            updateExclusionRects(true)
        }
        invalidate()
    }

    private fun updateAlphaAndScale(targetAlpha: Float) {
        if (isMenuOpen) return
        if (alpha == 0f) {
            alpha = targetAlpha
            scaleX = 1f
            scaleY = 1f
            return
        }
        if (targetAlpha < 1.0f) {
            animateIdleFade(targetAlpha)
        } else if (alpha < 1.0f) {
            animateWakeupFade()
        }
    }

    fun animateMenuState(isOpen: Boolean) {
        if (isMenuOpen == isOpen) return
        isMenuOpen = isOpen
        animateMenuReaction(isOpen)
        colorAnimator?.cancel()
        colorAnimator = createRingColorAnimator(
            onColorUpdate = { color ->
                ringPaint.color = color
                invalidate()
            },
            isOpen = isOpen,
            currentColor = ringPaint.color,
        ).apply { start() }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateExclusionRects(visibility == VISIBLE)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animate().cancel()
        colorAnimator?.cancel()
        colorAnimator = null
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
        val inset = dpToPx(DRAW_INSET_DP)
        val availableSize = width.coerceAtMost(height) - (inset * 2)
        val radius = (availableSize / 2f).coerceAtLeast(0f)
        val centerX = width / 2f
        val centerY = height / 2f

        val bitmap = activeBitmap
        if (bitmap != null) {
            drawRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            clipPath.reset()
            clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
            val saveCount = canvas.save()
            if (!isIdlePeeking) {
                canvas.clipPath(clipPath)
            } else if (!isRightSide) {
                canvas.scale(-1f, 1f, centerX, centerY)
            }
            canvas.drawBitmap(bitmap, null, drawRect, null)
            canvas.restoreToCount(saveCount)
        } else {
            canvas.drawCircle(centerX, centerY, radius, outerPaint)
            canvas.drawCircle(centerX, centerY, radius * RING_RATIO, ringPaint)
            canvas.drawCircle(centerX, centerY, radius * CENTER_DOT_RATIO, centerPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> animatePressState(isPressed = true, isMenuOpen = isMenuOpen)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animatePressState(
                isPressed = false,
                isMenuOpen = isMenuOpen,
            )
        }
        val handled = onTouchListener(this, event)
        return handled || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        actions.onClick()
        return true
    }

    override fun performLongClick(): Boolean {
        super.performLongClick()
        actions.onLongClick()
        return true
    }

    companion object {
        private const val OUTER_ALPHA = 160
        private const val BG_DARK = 30
        private const val CENTER_ALPHA = 220
        private const val OUTER_RING_STROKE_DP = 1.5f
        private const val DRAW_INSET_DP = 2.0f
        private const val RING_RATIO = 0.72f
        private const val CENTER_DOT_RATIO = 0.35f
        private const val PEEK_OFFSET_RATIO = 0.25f
        private const val PEEK_ANIM_DURATION_MS = 220L
    }
}

private fun selectDotBitmap(
    style: DotStyle,
    peeking: Boolean,
    bitmaps: Triple<Bitmap?, Bitmap?, Bitmap?>,
): Bitmap? = when {
    peeking -> bitmaps.first
    style == DotStyle.APP_ICON -> bitmaps.second
    style == DotStyle.CUSTOM_IMAGE -> bitmaps.third
    else -> null
}

private const val PEEK_OFFSET_RATIO = 0.25f
private const val PEEK_ANIM_DURATION_MS = 220L

private fun animatePeekTranslation(
    view: DotView,
    isIdlePeeking: Boolean,
    isRightSide: Boolean,
) {
    val targetTx = if (isIdlePeeking) (if (isRightSide) 1f else -1f) * view.width * PEEK_OFFSET_RATIO else 0f
    if (view.translationX != targetTx) {
        view.animate().translationX(targetTx).setDuration(PEEK_ANIM_DURATION_MS).start()
    }
}
