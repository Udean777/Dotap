package com.ssajudn.dotap.domain.port

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Port interface providing coroutine dispatchers to domain and presentation layers.
 * Prevents direct references to Dispatchers.* to ensure testability.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
