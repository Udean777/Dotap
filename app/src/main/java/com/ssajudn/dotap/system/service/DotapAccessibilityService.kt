package com.ssajudn.dotap.system.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import com.ssajudn.dotap.DotapApp
import com.ssajudn.dotap.domain.gesture.GestureConfig
import com.ssajudn.dotap.domain.gesture.GestureEngine
import com.ssajudn.dotap.domain.gesture.GestureListener
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import com.ssajudn.dotap.domain.usecase.HandleGestureUseCase
import com.ssajudn.dotap.domain.usecase.TakeScreenshotUseCase
import com.ssajudn.dotap.system.overlay.AndroidHapticFeedback
import com.ssajudn.dotap.system.overlay.DotPresenter
import com.ssajudn.dotap.system.overlay.HandlerScheduler
import com.ssajudn.dotap.system.overlay.OverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DotapAccessibilityService : AccessibilityService() {

    private var serviceScope: CoroutineScope? = null
    private var overlayController: OverlayController? = null
    private var presenter: DotPresenter? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val app = applicationContext as? DotapApp ?: return
        val scope = CoroutineScope(SupervisorJob() + app.container.dispatcherProvider.main)
        serviceScope = scope

        app.container.serviceConnectionTracker.setConnected(true)
        val (dotPresenter, controller) = setupPresenterAndController(app, scope)
        presenter = dotPresenter
        overlayController = controller

        val touchSlopPx = ViewConfiguration.get(this).scaledTouchSlop.toFloat()
        val gestureEngine = buildGestureEngine(dotPresenter, controller, touchSlopPx)
        controller.gestureEngine = gestureEngine
        controller.accessibilityClick = { dotPresenter.onGesture(GestureType.SINGLE_TAP) }
        controller.show()

        dotPresenter.uiState.onEach { controller.update(it) }.launchIn(scope)
        val settingsRepository = app.container.settingsRepository
        settingsRepository.settings
            .onEach {
                updateEngineConfig(gestureEngine, it, touchSlopPx)
                dotPresenter.applySettings(it)
            }
            .launchIn(scope)
        controller.onPositionSettled = { isRightSide, yRatio ->
            scope.launch {
                settingsRepository.update { it.copy(isRightSide = isRightSide, yRatio = yRatio) }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        overlayController?.onConfigurationChanged()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally empty: Dotap does not observe or inspect window content/events.
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    private fun setupPresenterAndController(
        app: DotapApp,
        scope: CoroutineScope,
    ): Pair<DotPresenter, OverlayController> {
        val actionPerformer = GlobalActionPerformer(this)
        var controllerRef: OverlayController? = null

        val visibilityBridge = object : OverlayVisibility {
            override fun hideForCapture() {
                controllerRef?.hideForCapture()
            }
            override fun restoreAfterCapture() {
                controllerRef?.restoreAfterCapture()
            }
            override fun showMessage(message: MessageType) {
                controllerRef?.showMessage(message)
            }
        }

        val screenshotUseCase = TakeScreenshotUseCase(visibilityBridge, actionPerformer)
        val executeUseCase = ExecuteActionUseCase(actionPerformer, screenshotUseCase)
        val haptic = AndroidHapticFeedback { controllerRef?.view }
        val handleGesture = HandleGestureUseCase(app.container.settingsRepository, haptic, executeUseCase)

        val dotPresenter = DotPresenter(handleGestureUseCase = handleGesture, scope = scope)
        val controller = OverlayController(this, dotPresenter)
        controllerRef = controller
        return dotPresenter to controller
    }

    private fun buildGestureEngine(
        dotPresenter: DotPresenter,
        controller: OverlayController,
        touchSlopPx: Float,
    ): GestureEngine {
        val listener = object : GestureListener {
            override fun onSingleTap() = dotPresenter.onGesture(GestureType.SINGLE_TAP)
            override fun onDoubleTap() = dotPresenter.onGesture(GestureType.DOUBLE_TAP)
            override fun onLongPress() = dotPresenter.onGesture(GestureType.LONG_PRESS)
            override fun onDragStart() = controller.onDragStart()
            override fun onDrag(dx: Float, dy: Float) = controller.onDrag(dx, dy)
            override fun onDragEnd() = controller.onDragEnd()
        }
        return GestureEngine(GestureConfig(touchSlopPx = touchSlopPx), HandlerScheduler(), listener)
    }

    private fun cleanup() {
        val app = applicationContext as? DotapApp
        app?.container?.serviceConnectionTracker?.setConnected(false)
        overlayController?.hide()
        overlayController = null
        presenter = null
        serviceScope?.cancel()
        serviceScope = null
    }
}

private fun updateEngineConfig(
    engine: GestureEngine,
    settings: DotSettings,
    touchSlopPx: Float,
) {
    engine.updateConfig(
        GestureConfig(
            doubleTapTimeoutMs = settings.doubleTapTimeoutMs,
            longPressDurationMs = settings.longPressDurationMs,
            touchSlopPx = touchSlopPx,
            isDoubleTapAssigned = settings.doubleTapAction != ActionType.NONE,
            isLongPressAssigned = settings.longPressAction != ActionType.NONE,
        ),
    )
}
