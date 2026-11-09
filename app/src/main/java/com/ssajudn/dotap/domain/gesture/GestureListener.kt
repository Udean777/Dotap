package com.ssajudn.dotap.domain.gesture

interface GestureListener {
    fun onSingleTap()
    fun onDoubleTap()
    fun onLongPress()
    fun onDragStart()
    fun onDrag(dx: Float, dy: Float)
    fun onDragEnd()
}
