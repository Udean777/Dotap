package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.port.AccessibilityStatusSource
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines the system enabled-services list with the live connection tracker into [ServiceStatus].
 */
class ObserveServiceStatusUseCase(
    private val accessibilityStatusSource: AccessibilityStatusSource,
    private val serviceConnectionTracker: ServiceConnectionTracker,
) {
    operator fun invoke(): Flow<ServiceStatus> = accessibilityStatusSource.isServiceEnabled.combine(
        serviceConnectionTracker.isConnected,
        ::resolveStatus,
    )

    private fun resolveStatus(isEnabled: Boolean, isConnected: Boolean): ServiceStatus = when {
        !isEnabled -> ServiceStatus.DISABLED
        isConnected -> ServiceStatus.ENABLED_CONNECTED
        else -> ServiceStatus.ENABLED_NOT_CONNECTED
    }
}
