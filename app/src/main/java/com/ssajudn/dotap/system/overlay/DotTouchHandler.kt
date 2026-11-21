package com.ssajudn.dotap.system.overlay

import android.view.MotionEvent
import android.view.View
import com.ssajudn.dotap.domain.gesture.GestureEngine
import com.ssajudn.dotap.domain.model.GestureType
import kotlin.math.abs

private const val FLICK_MAX_DURATION_MS = 250L
private const val DRAG_HOLD_THRESHOLD_MS = 180L
private const val FLICK_MIN_DISTANCE_PX = 60f

/**
 * Separates a quick swipe (animated in place, dot stays put) from a hold-then-drag that moves the dot.
 * A swipe cancels the gesture engine so it is never also counted as a tap.
 */
internal class DotTouchHandler(
    private val presenter: DotPresenter,
    private val engineProvider: () -> GestureEngine?,
) {
    private var downRawX = 0f
    private var downRawY = 0f
    private var isDragMode = false
    private var feedback: DotSwipeFeedback? = null

    fun onTouch(
        view: View,
        event: MotionEvent,
    ): Boolean {
        val swipeFeedback = feedbackFor(view)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                swipeFeedback.cancel()
                downRawX = event.rawX
                downRawY = event.rawY
                isDragMode = false
                presenter.onTouchActive()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> presenter.onTouchFinished()
        }
        val engine = engineProvider() ?: return false
        dispatch(event, engine, swipeFeedback)
        return true
    }

    private fun dispatch(
        event: MotionEvent,
        engine: GestureEngine,
        swipeFeedback: DotSwipeFeedback,
    ) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> engine.down(event.rawX, event.rawY)

            MotionEvent.ACTION_MOVE -> handleMove(event, engine, swipeFeedback)

            MotionEvent.ACTION_UP -> finishTouch(event, engine, swipeFeedback)

            MotionEvent.ACTION_CANCEL -> {
                swipeFeedback.settle()
                engine.cancel()
            }
        }
    }

    private fun handleMove(
        event: MotionEvent,
        engine: GestureEngine,
        swipeFeedback: DotSwipeFeedback,
    ) {
        if (!isDragMode && event.eventTime - event.downTime >= DRAG_HOLD_THRESHOLD_MS) {
            isDragMode = true
            swipeFeedback.settle()
        }
        if (isDragMode) {
            engine.move(event.rawX, event.rawY)
        } else {
            swipeFeedback.anticipate(event.rawX - downRawX, event.rawY - downRawY)
        }
    }

    private fun finishTouch(
        event: MotionEvent,
        engine: GestureEngine,
        swipeFeedback: DotSwipeFeedback,
    ) {
        val swipe = if (isDragMode) null else detectFlick(event)
        when {
            isDragMode -> engine.up()

            swipe == null -> {
                swipeFeedback.settle()
                engine.up()
            }

            else -> {
                swipeFeedback.dash(swipe)
                engine.cancel()
                presenter.onGesture(swipe)
            }
        }
    }

    private fun detectFlick(event: MotionEvent): GestureType? {
        val dx = event.rawX - downRawX
        val dy = event.rawY - downRawY
        val isQuick = event.eventTime - event.downTime <= FLICK_MAX_DURATION_MS
        val isFarEnough = abs(dx) >= FLICK_MIN_DISTANCE_PX || abs(dy) >= FLICK_MIN_DISTANCE_PX
        return when {
            !isQuick || !isFarEnough -> null
            abs(dx) > abs(dy) -> if (dx > 0f) GestureType.SWIPE_RIGHT else GestureType.SWIPE_LEFT
            else -> if (dy > 0f) GestureType.SWIPE_DOWN else GestureType.SWIPE_UP
        }
    }

    private fun feedbackFor(view: View): DotSwipeFeedback = feedback?.takeIf { it.view === view }
        ?: DotSwipeFeedback(view).also { feedback = it }
}
