package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.HapticFeedback
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.testutil.FakeAppLauncher
import com.ssajudn.dotap.testutil.FakeFlashlightController
import com.ssajudn.dotap.testutil.FakePanelLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandleGestureUseCaseTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var hapticFeedback: RecordingHapticFeedback
    private lateinit var actionPerformer: RecordingActionPerformer
    private lateinit var appLauncher: FakeAppLauncher
    private lateinit var executeActionUseCase: ExecuteActionUseCase
    private lateinit var useCase: HandleGestureUseCase
    private val callOrder = mutableListOf<String>()

    @Before
    fun setUp() {
        callOrder.clear()
        settingsRepository = FakeSettingsRepository(
            DotSettings(
                singleTapAction = ActionType.HOME,
                doubleTapAction = ActionType.SCREENSHOT,
                longPressAction = ActionType.RECENTS,
                isHapticsEnabled = true,
            ),
        )
        hapticFeedback = RecordingHapticFeedback(callOrder)
        actionPerformer = RecordingActionPerformer(callOrder)
        appLauncher = FakeAppLauncher()
        executeActionUseCase = createExecuteUseCase()
        useCase = HandleGestureUseCase(settingsRepository, hapticFeedback, executeActionUseCase)
    }

    private fun createExecuteUseCase(): ExecuteActionUseCase {
        val overlayVisibility = object : OverlayVisibility {
            override fun hideForCapture() = Unit
            override fun restoreAfterCapture() = Unit
            override fun showMessage(message: MessageType) = Unit
        }
        val screenshotUseCase = TakeScreenshotUseCase(overlayVisibility, actionPerformer, 0L, 0L)
        return ExecuteActionUseCase(
            actionPerformer = actionPerformer,
            takeScreenshotUseCase = screenshotUseCase,
            panelLauncher = FakePanelLauncher(),
            flashlightController = FakeFlashlightController(),
            appLauncher = appLauncher,
            overlayVisibility = overlayVisibility,
        )
    }

    @Test
    fun `single tap executes single tap action with haptics in order`() = runTest {
        val result = useCase(GestureType.SINGLE_TAP)

        assertTrue(result is ActionResult.Success)
        assertEquals(1, hapticFeedback.tickCount)
        assertEquals(listOf(ActionType.HOME), actionPerformer.performedActions)
        assertEquals(listOf("haptic", "action_HOME"), callOrder)
    }

    @Test
    fun `double tap executes screenshot action`() = runTest {
        val result = useCase(GestureType.DOUBLE_TAP)

        assertTrue(result is ActionResult.Success)
        assertEquals(1, hapticFeedback.tickCount)
        assertEquals(listOf(ActionType.SCREENSHOT), actionPerformer.performedActions)
    }

    @Test
    fun `long press executes long press action`() = runTest {
        val result = useCase(GestureType.LONG_PRESS)

        assertTrue(result is ActionResult.Success)
        assertEquals(1, hapticFeedback.tickCount)
        assertEquals(listOf(ActionType.RECENTS), actionPerformer.performedActions)
    }

    @Test
    fun `haptics disabled does not tick`() = runTest {
        settingsRepository.update { it.copy(isHapticsEnabled = false) }

        val result = useCase(GestureType.SINGLE_TAP)

        assertTrue(result is ActionResult.Success)
        assertEquals(0, hapticFeedback.tickCount)
        assertEquals(listOf("action_HOME"), callOrder)
    }

    @Test
    fun `unassigned action returns success and does not tick haptics`() = runTest {
        settingsRepository.update { it.copy(singleTapAction = ActionType.NONE) }

        val result = useCase(GestureType.SINGLE_TAP)

        assertTrue(result is ActionResult.Success)
        assertEquals(0, hapticFeedback.tickCount)
        assertEquals(emptyList<ActionType>(), actionPerformer.performedActions)
    }

    @Test
    fun `launch app gesture launches its own configured package`() = runTest {
        settingsRepository.update {
            it.copy(
                singleTapAction = ActionType.LAUNCH_APP,
                doubleTapAction = ActionType.LAUNCH_APP,
                singleTapAppPackage = "com.whatsapp",
                doubleTapAppPackage = "com.android.camera2",
            )
        }

        useCase(GestureType.SINGLE_TAP)
        useCase(GestureType.DOUBLE_TAP)

        assertEquals(listOf("com.whatsapp", "com.android.camera2"), appLauncher.launchedPackages)
    }

    private class FakeSettingsRepository(
        initial: DotSettings,
    ) : SettingsRepository {
        private val state = MutableStateFlow(initial)
        override val settings: Flow<DotSettings> = state

        override suspend fun update(transform: (DotSettings) -> DotSettings) {
            state.value = transform(state.value)
        }
    }

    private class RecordingHapticFeedback(private val log: MutableList<String>) : HapticFeedback {
        var tickCount = 0
        override fun tick() {
            tickCount++
            log.add("haptic")
        }
    }

    private class RecordingActionPerformer(private val log: MutableList<String>) : ActionPerformer {
        val performedActions = mutableListOf<ActionType>()

        override fun perform(action: ActionType): Boolean {
            performedActions.add(action)
            log.add("action_$action")
            return true
        }
    }
}
