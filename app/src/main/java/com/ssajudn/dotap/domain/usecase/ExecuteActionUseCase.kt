package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.PanelLauncher

class ExecuteActionUseCase(
    private val actionPerformer: ActionPerformer,
    private val takeScreenshotUseCase: TakeScreenshotUseCase,
    private val panelLauncher: PanelLauncher,
) {
    suspend operator fun invoke(action: ActionType): ActionResult = when (action) {
        ActionType.NONE -> ActionResult.Success

        ActionType.SCREENSHOT -> takeScreenshotUseCase()

        ActionType.OPEN_PANEL -> {
            panelLauncher.open()
            ActionResult.Success
        }

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
