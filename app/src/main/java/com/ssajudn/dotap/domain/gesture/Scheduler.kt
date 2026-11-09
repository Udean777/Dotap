package com.ssajudn.dotap.domain.gesture

interface Scheduler {
    fun postDelayed(delayMs: Long, token: Any, action: () -> Unit)
    fun cancel(token: Any)
}
