package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.AppLauncher
import com.ssajudn.dotap.domain.port.FlashlightController
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.port.PanelLauncher

class ExecuteActionUseCase(
    private val actionPerformer: ActionPerformer,
    private val takeScreenshotUseCase: TakeScreenshotUseCase,
    private val panelLauncher: PanelLauncher,
    private val flashlightController: FlashlightController,
    private val appLauncher: AppLauncher,
    private val overlayVisibility: OverlayVisibility,
) {
    suspend operator fun invoke(
        action: ActionType,
        appPackage: String? = null,
    ): ActionResult = when (action) {
        ActionType.NONE -> ActionResult.Success

        ActionType.SCREENSHOT -> takeScreenshotUseCase()

        ActionType.OPEN_PANEL -> {
            panelLauncher.open()
            ActionResult.Success
        }

        ActionType.SEARCH_APPS -> {
            panelLauncher.openAppSearch()
            ActionResult.Success
        }

        ActionType.FLASHLIGHT -> toggleFlashlight()

        ActionType.LAUNCH_APP -> launchApp(appPackage)

        ActionType.HOME,
        ActionType.BACK,
        ActionType.RECENTS,
        ActionType.NOTIFICATIONS,
        ActionType.QUICK_SETTINGS,
        ActionType.POWER_DIALOG,
        ActionType.LOCK_SCREEN,
        -> performGlobalAction(action)
    }

    private fun toggleFlashlight(): ActionResult = if (flashlightController.toggle()) {
        ActionResult.Success
    } else {
        overlayVisibility.showMessage(MessageType.FLASHLIGHT_UNAVAILABLE)
        ActionResult.Unavailable("Flashlight unavailable")
    }

    private fun launchApp(appPackage: String?): ActionResult {
        val targetPackage = appPackage?.trim().orEmpty()
        return if (targetPackage.isEmpty()) {
            overlayVisibility.showMessage(MessageType.APP_NOT_CONFIGURED)
            ActionResult.Unavailable("No target app configured")
        } else if (appLauncher.launch(targetPackage)) {
            ActionResult.Success
        } else {
            overlayVisibility.showMessage(MessageType.APP_LAUNCH_FAILED)
            ActionResult.Unavailable("Failed to launch $targetPackage")
        }
    }

    private fun performGlobalAction(action: ActionType): ActionResult = if (actionPerformer.perform(action)) {
        ActionResult.Success
    } else {
        ActionResult.Unavailable("Action $action returned false")
    }
}
