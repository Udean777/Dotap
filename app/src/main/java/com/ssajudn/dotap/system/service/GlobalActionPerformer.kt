package com.ssajudn.dotap.system.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.port.ActionPerformer

class GlobalActionPerformer(
    private val service: AccessibilityService,
) : ActionPerformer {
    override fun perform(action: ActionType): Boolean {
        val globalAction = ACTION_MAP[action] ?: return false
        // GLOBAL_ACTION_TAKE_SCREENSHOT and canTakeScreenshot exist only from Android 11.
        val isSupported = action != ActionType.SCREENSHOT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        return isSupported && try {
            service.performGlobalAction(globalAction)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException performing $action", e)
            false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException performing $action", e)
            false
        }
    }

    companion object {
        private const val TAG = "GlobalActionPerformer"

        private val ACTION_MAP = mapOf(
            ActionType.HOME to AccessibilityService.GLOBAL_ACTION_HOME,
            ActionType.BACK to AccessibilityService.GLOBAL_ACTION_BACK,
            ActionType.RECENTS to AccessibilityService.GLOBAL_ACTION_RECENTS,
            ActionType.NOTIFICATIONS to AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS,
            ActionType.QUICK_SETTINGS to AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS,
            ActionType.POWER_DIALOG to AccessibilityService.GLOBAL_ACTION_POWER_DIALOG,
            ActionType.LOCK_SCREEN to AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN,
            ActionType.SCREENSHOT to AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT,
        )
    }
}
