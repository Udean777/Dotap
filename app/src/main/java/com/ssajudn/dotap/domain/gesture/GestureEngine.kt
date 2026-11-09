package com.ssajudn.dotap.domain.gesture

import kotlin.math.hypot

class GestureEngine(
    private var config: GestureConfig,
    private val scheduler: Scheduler,
    private val listener: GestureListener,
) {
    private enum class State {
        IDLE,
        PRESSED,
        DRAGGING,
        LONG_FIRED,
        WAIT_SECOND_TAP,
        SECOND_PRESSED,
    }

    private enum class TimerToken {
        LONG_PRESS,
        DOUBLE_TAP,
    }

    private var state = State.IDLE
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f

    fun down(x: Float, y: Float) {
        val isSecondTap = state == State.WAIT_SECOND_TAP && !exceedsSlop(x, y)
        if (state == State.WAIT_SECOND_TAP) {
            scheduler.cancel(TimerToken.DOUBLE_TAP)
            // A tap far away is a new gesture, not the second half of a double tap.
            if (!isSecondTap) listener.onSingleTap()
        } else {
            resetTimers()
        }
        startX = x
        startY = y
        lastX = x
        lastY = y
        state = if (isSecondTap) State.SECOND_PRESSED else State.PRESSED
        scheduleLongPressIfNeeded()
    }

    fun move(x: Float, y: Float) {
        when (state) {
            State.PRESSED, State.SECOND_PRESSED -> {
                if (exceedsSlop(x, y)) {
                    scheduler.cancel(TimerToken.LONG_PRESS)
                    state = State.DRAGGING
                    listener.onDragStart()
                    val dx = x - lastX
                    val dy = y - lastY
                    lastX = x
                    lastY = y
                    listener.onDrag(dx, dy)
                }
            }

            State.DRAGGING -> {
                val dx = x - lastX
                val dy = y - lastY
                lastX = x
                lastY = y
                listener.onDrag(dx, dy)
            }

            State.IDLE, State.WAIT_SECOND_TAP, State.LONG_FIRED -> Unit
        }
    }

    fun up() {
        when (state) {
            State.PRESSED -> {
                scheduler.cancel(TimerToken.LONG_PRESS)
                if (!config.isDoubleTapAssigned) {
                    state = State.IDLE
                    listener.onSingleTap()
                } else {
                    state = State.WAIT_SECOND_TAP
                    scheduler.postDelayed(config.doubleTapTimeoutMs, TimerToken.DOUBLE_TAP) {
                        onDoubleTapTimeout()
                    }
                }
            }

            State.SECOND_PRESSED -> {
                scheduler.cancel(TimerToken.LONG_PRESS)
                state = State.IDLE
                listener.onDoubleTap()
            }

            State.DRAGGING -> {
                state = State.IDLE
                listener.onDragEnd()
            }

            State.LONG_FIRED -> {
                state = State.IDLE
            }

            State.IDLE, State.WAIT_SECOND_TAP -> Unit
        }
    }

    fun cancel() {
        val wasDragging = state == State.DRAGGING
        resetTimers()
        state = State.IDLE
        if (wasDragging) {
            listener.onDragEnd()
        }
    }

    fun updateConfig(newConfig: GestureConfig) {
        config = newConfig
        if (state == State.WAIT_SECOND_TAP && !newConfig.isDoubleTapAssigned) {
            scheduler.cancel(TimerToken.DOUBLE_TAP)
            state = State.IDLE
            listener.onSingleTap()
        }
    }

    private fun exceedsSlop(x: Float, y: Float): Boolean {
        val distance = hypot((x - startX).toDouble(), (y - startY).toDouble()).toFloat()
        return distance > config.touchSlopPx
    }

    private fun scheduleLongPressIfNeeded() {
        if (config.isLongPressAssigned) {
            scheduler.postDelayed(config.longPressDurationMs, TimerToken.LONG_PRESS) {
                onLongPressTimeout()
            }
        }
    }

    private fun onDoubleTapTimeout() {
        if (state == State.WAIT_SECOND_TAP) {
            state = State.IDLE
            listener.onSingleTap()
        }
    }

    private fun onLongPressTimeout() {
        if (state == State.PRESSED || state == State.SECOND_PRESSED) {
            state = State.LONG_FIRED
            listener.onLongPress()
        }
    }

    private fun resetTimers() {
        scheduler.cancel(TimerToken.LONG_PRESS)
        scheduler.cancel(TimerToken.DOUBLE_TAP)
    }
}
