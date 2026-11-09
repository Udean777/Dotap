package com.ssajudn.dotap.domain.port

import kotlinx.coroutines.flow.Flow

/**
 * Port providing whether the accessibility service is currently enabled in Android system settings.
 */
interface AccessibilityStatusSource {
    val isServiceEnabled: Flow<Boolean>
}
