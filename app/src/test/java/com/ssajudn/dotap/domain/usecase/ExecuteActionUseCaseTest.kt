package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.testutil.FakeAppLauncher
import com.ssajudn.dotap.testutil.FakeFlashlightController
import com.ssajudn.dotap.testutil.FakePanelLauncher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExecuteActionUseCaseTest {

    private lateinit var actionPerformer: FakeActionPerformer
    private lateinit var overlayVisibility: FakeOverlayVisibility
    private lateinit var panelLauncher: FakePanelLauncher
    private lateinit var flashlightController: FakeFlashlightController
    private lateinit var appLauncher: FakeAppLauncher
    private lateinit var takeScreenshotUseCase: TakeScreenshotUseCase
    private lateinit var useCase: ExecuteActionUseCase

    @Before
    fun setUp() {
        actionPerformer = FakeActionPerformer()
        overlayVisibility = FakeOverlayVisibility()
        panelLauncher = FakePanelLauncher()
        flashlightController = FakeFlashlightController()
        appLauncher = FakeAppLauncher()
        takeScreenshotUseCase = TakeScreenshotUseCase(
            overlayVisibility = overlayVisibility,
            actionPerformer = actionPerformer,
            hideDelayMs = 0L,
            restoreDelayMs = 0L,
        )
        useCase = ExecuteActionUseCase(
            actionPerformer = actionPerformer,
            takeScreenshotUseCase = takeScreenshotUseCase,
            panelLauncher = panelLauncher,
            flashlightController = flashlightController,
            appLauncher = appLauncher,
            overlayVisibility = overlayVisibility,
        )
    }

    @Test
    fun `none action returns success without performing`() = runTest {
        val result = useCase(ActionType.NONE)
        assertTrue(result is ActionResult.Success)
        assertEquals(emptyList<ActionType>(), actionPerformer.performedActions)
    }

    @Test
    fun `screenshot action delegates to TakeScreenshotUseCase`() = runTest {
        val result = useCase(ActionType.SCREENSHOT)
        assertTrue(result is ActionResult.Success)
        assertEquals(listOf(ActionType.SCREENSHOT), actionPerformer.performedActions)
    }

    @Test
    fun `open panel action opens the panel without a global action`() = runTest {
        val result = useCase(ActionType.OPEN_PANEL)
        assertTrue(result is ActionResult.Success)
        assertEquals(1, panelLauncher.openCount)
        assertEquals(emptyList<ActionType>(), actionPerformer.performedActions)
    }

    @Test
    fun `flashlight action toggles flashlight controller`() = runTest {
        val result = useCase(ActionType.FLASHLIGHT)
        assertTrue(result is ActionResult.Success)
        assertEquals(1, flashlightController.toggleCount)
        assertEquals(emptyList<ActionType>(), actionPerformer.performedActions)
        assertEquals(emptyList<MessageType>(), overlayVisibility.messages)
    }

    @Test
    fun `failed flashlight action returns unavailable and shows message`() = runTest {
        flashlightController.shouldSucceed = false
        val result = useCase(ActionType.FLASHLIGHT)
        assertTrue(result is ActionResult.Unavailable)
        assertEquals(1, flashlightController.toggleCount)
        assertEquals(listOf(MessageType.FLASHLIGHT_UNAVAILABLE), overlayVisibility.messages)
    }

    @Test
    fun `launch app action launches configured package`() = runTest {
        val result = useCase(ActionType.LAUNCH_APP, "com.whatsapp")
        assertTrue(result is ActionResult.Success)
        assertEquals(listOf("com.whatsapp"), appLauncher.launchedPackages)
        assertEquals(emptyList<MessageType>(), overlayVisibility.messages)
    }

    @Test
    fun `launch app action without package returns unavailable and shows message`() = runTest {
        val result = useCase(ActionType.LAUNCH_APP, null)
        assertTrue(result is ActionResult.Unavailable)
        assertEquals(emptyList<String>(), appLauncher.launchedPackages)
        assertEquals(listOf(MessageType.APP_NOT_CONFIGURED), overlayVisibility.messages)
    }

    @Test
    fun `launch app action failing returns unavailable and shows message`() = runTest {
        appLauncher.shouldSucceed = false
        val result = useCase(ActionType.LAUNCH_APP, "com.missing.app")
        assertTrue(result is ActionResult.Unavailable)
        assertEquals(listOf("com.missing.app"), appLauncher.launchedPackages)
        assertEquals(listOf(MessageType.APP_LAUNCH_FAILED), overlayVisibility.messages)
    }

    @Test
    fun `global actions call actionPerformer with correct action`() = runTest {
        val globalActions = listOf(
            ActionType.HOME,
            ActionType.BACK,
            ActionType.RECENTS,
            ActionType.NOTIFICATIONS,
            ActionType.QUICK_SETTINGS,
            ActionType.POWER_DIALOG,
            ActionType.LOCK_SCREEN,
        )

        for (action in globalActions) {
            val result = useCase(action)
            assertTrue(result is ActionResult.Success)
        }

        assertEquals(globalActions, actionPerformer.performedActions)
    }

    @Test
    fun `failed global action returns unavailable`() = runTest {
        actionPerformer.shouldSucceed = false
        val result = useCase(ActionType.HOME)
        assertTrue(result is ActionResult.Unavailable)
    }

    private class FakeActionPerformer : ActionPerformer {
        val performedActions = mutableListOf<ActionType>()
        var shouldSucceed = true

        override fun perform(action: ActionType): Boolean {
            performedActions.add(action)
            return shouldSucceed
        }
    }

    private class FakeOverlayVisibility : OverlayVisibility {
        val messages = mutableListOf<MessageType>()

        override fun hideForCapture() = Unit
        override fun restoreAfterCapture() = Unit
        override fun showMessage(message: MessageType) {
            messages.add(message)
        }
    }
}
