package com.ssajudn.dotap.system.overlay

import android.os.Handler
import android.os.Looper
import com.ssajudn.dotap.domain.gesture.Scheduler

class HandlerScheduler(
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : Scheduler {
    override fun postDelayed(delayMs: Long, token: Any, action: () -> Unit) {
        val runnable = Runnable { action() }
        handler.postDelayed(runnable, token, delayMs)
    }

    override fun cancel(token: Any) {
        handler.removeCallbacksAndMessages(token)
    }
}
