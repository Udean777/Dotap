package com.ssajudn.dotap.domain.status

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Live hint of whether the accessibility service is currently bound.
 * It cannot tell "enabled but not connected"; the system enabled-services list is the source of truth.
 */
class ServiceConnectionTracker {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
