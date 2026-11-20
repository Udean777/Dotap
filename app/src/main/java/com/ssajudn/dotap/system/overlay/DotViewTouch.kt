package com.ssajudn.dotap.system.overlay

import android.view.MotionEvent
import com.ssajudn.dotap.domain.gesture.GestureEngine
import com.ssajudn.dotap.domain.model.GestureType
import kotlin.math.abs

private const val FLICK_MAX_DURATION_MS = 250L
private const val FLICK_MIN_DISTANCE_PX = 80f

private var downRawX = 0f
private var downRawY = 0f

internal fun handleTouchEvent(
    event: MotionEvent,
    presenter: DotPresenter,
    engine: GestureEngine?,
): Boolean {
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            downRawX = event.rawX
            downRawY = event.rawY
            presenter.onTouchActive()
        }

        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> presenter.onTouchFinished()
    }
    if (engine == null) return false
    when (event.action) {
        MotionEvent.ACTION_DOWN -> engine.down(event.rawX, event.rawY)

        MotionEvent.ACTION_MOVE -> engine.move(event.rawX, event.rawY)

        MotionEvent.ACTION_UP -> {
            detectFlick(event)?.let(presenter::onGesture)
            engine.up()
        }

        MotionEvent.ACTION_CANCEL -> engine.cancel()
    }
    return true
}

private fun detectFlick(event: MotionEvent): GestureType? {
    val dx = event.rawX - downRawX
    val dy = event.rawY - downRawY
    val isQuick = event.eventTime - event.downTime <= FLICK_MAX_DURATION_MS
    val isFarEnough = abs(dx) >= FLICK_MIN_DISTANCE_PX || abs(dy) >= FLICK_MIN_DISTANCE_PX
    return if (!isQuick || !isFarEnough) {
        null
    } else if (abs(dx) > abs(dy)) {
        if (dx > 0f) GestureType.SWIPE_RIGHT else GestureType.SWIPE_LEFT
    } else {
        if (dy > 0f) GestureType.SWIPE_DOWN else GestureType.SWIPE_UP
    }
}
