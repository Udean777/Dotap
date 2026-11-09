package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.OverlayVisibility
import kotlinx.coroutines.delay

/**
 * Hides the dot, lets the platform settle, triggers the screenshot global action and restores the dot.
 * Exceptions from [ActionPerformer] are the system adapter's concern; the dot is restored either way.
 */
class TakeScreenshotUseCase(
    private val overlayVisibility: OverlayVisibility,
    private val actionPerformer: ActionPerformer,
    private val hideDelayMs: Long = Defaults.SCREENSHOT_HIDE_DELAY_MS,
    private val restoreDelayMs: Long = Defaults.SCREENSHOT_RESTORE_DELAY_MS,
) {
    suspend operator fun invoke(): ActionResult {
        overlayVisibility.hideForCapture()
        return try {
            delay(hideDelayMs)
            val success = actionPerformer.perform(ActionType.SCREENSHOT)
            if (success) {
                delay(restoreDelayMs)
                ActionResult.Success
            } else {
                overlayVisibility.showMessage(MessageType.SCREENSHOT_UNAVAILABLE)
                ActionResult.Unavailable("Screenshot action returned false")
            }
        } finally {
            overlayVisibility.restoreAfterCapture()
        }
    }
}
