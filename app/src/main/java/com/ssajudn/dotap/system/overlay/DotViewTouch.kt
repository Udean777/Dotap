package com.ssajudn.dotap.system.overlay

import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.ssajudn.dotap.domain.gesture.GestureEngine

internal fun createDotView(
    context: Context,
    onTouch: (View, MotionEvent) -> Boolean,
    actions: DotView.ClickActions,
): DotView = DotView(
    context = context,
    onTouchListener = onTouch,
    actions = actions,
)

internal fun handleTouchEvent(
    event: MotionEvent,
    presenter: DotPresenter,
    engine: GestureEngine?,
): Boolean {
    when (event.action) {
        MotionEvent.ACTION_DOWN -> presenter.onTouchActive()
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> presenter.onTouchFinished()
    }
    if (engine == null) return false
    when (event.action) {
        MotionEvent.ACTION_DOWN -> engine.down(event.rawX, event.rawY)
        MotionEvent.ACTION_MOVE -> engine.move(event.rawX, event.rawY)
        MotionEvent.ACTION_UP -> engine.up()
        MotionEvent.ACTION_CANCEL -> engine.cancel()
    }
    return true
}
