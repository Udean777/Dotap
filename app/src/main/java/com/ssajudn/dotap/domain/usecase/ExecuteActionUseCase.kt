package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.port.ActionPerformer

class ExecuteActionUseCase(
    private val actionPerformer: ActionPerformer,
    private val takeScreenshotUseCase: TakeScreenshotUseCase,
) {
    suspend operator fun invoke(action: ActionType): ActionResult = when (action) {
        ActionType.NONE -> ActionResult.Success

        ActionType.SCREENSHOT -> takeScreenshotUseCase()

        // The panel ships in M4; until then the action must not pretend to succeed.
        ActionType.OPEN_PANEL -> ActionResult.Unavailable("Panel is not available yet")

        ActionType.HOME,
        ActionType.BACK,
        ActionType.RECENTS,
        ActionType.NOTIFICATIONS,
        ActionType.QUICK_SETTINGS,
        ActionType.POWER_DIALOG,
        ActionType.LOCK_SCREEN,
        -> {
            val success = actionPerformer.perform(action)
            if (success) {
                ActionResult.Success
            } else {
                ActionResult.Unavailable("Action $action returned false")
            }
        }
    }
}
