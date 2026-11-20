package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.port.HapticFeedback
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class HandleGestureUseCase(
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val executeActionUseCase: ExecuteActionUseCase,
) {
    suspend operator fun invoke(gesture: GestureType): ActionResult {
        val settings = settingsRepository.settings.first()
        val action = actionForGesture(gesture, settings)
        if (action == ActionType.NONE) {
            return ActionResult.Success
        }
        if (settings.isHapticsEnabled) {
            hapticFeedback.tick()
        }
        return executeActionUseCase(action, settings.appPackageFor(gesture))
    }
}

private fun actionForGesture(
    gesture: GestureType,
    settings: com.ssajudn.dotap.domain.model.DotSettings,
): ActionType = when (gesture) {
    GestureType.SINGLE_TAP -> settings.singleTapAction
    GestureType.DOUBLE_TAP -> settings.doubleTapAction
    GestureType.LONG_PRESS -> settings.longPressAction
    GestureType.SWIPE_UP -> settings.swipeUpAction
    GestureType.SWIPE_DOWN -> settings.swipeDownAction
    GestureType.SWIPE_LEFT -> settings.swipeLeftAction
    GestureType.SWIPE_RIGHT -> settings.swipeRightAction
}
