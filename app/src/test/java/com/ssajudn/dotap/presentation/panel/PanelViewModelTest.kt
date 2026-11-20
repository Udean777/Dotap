package com.ssajudn.dotap.presentation.panel

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import com.ssajudn.dotap.domain.usecase.TakeScreenshotUseCase
import com.ssajudn.dotap.testutil.FakeAppLauncher
import com.ssajudn.dotap.testutil.FakeFlashlightController
import com.ssajudn.dotap.testutil.FakeInstalledAppsRepository
import com.ssajudn.dotap.testutil.FakePanelLauncher
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var installedAppsRepository: FakeInstalledAppsRepository
    private lateinit var actionPerformer: FakeActionPerformer
    private lateinit var overlayVisibility: FakeOverlayVisibility
    private lateinit var panelLauncher: FakePanelLauncher
    private lateinit var appLauncher: FakeAppLauncher
    private lateinit var executeActionUseCase: ExecuteActionUseCase
    private lateinit var testScope: TestScope
    private lateinit var viewModel: PanelViewModel

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        installedAppsRepository = FakeInstalledAppsRepository()
        actionPerformer = FakeActionPerformer()
        overlayVisibility = FakeOverlayVisibility()
        panelLauncher = FakePanelLauncher()
        appLauncher = FakeAppLauncher()
        executeActionUseCase = createExecuteUseCase()
        testScope = TestScope()
        viewModel = PanelViewModel(
            settingsRepository = settingsRepository,
            installedAppsRepository = installedAppsRepository,
            executeAction = executeActionUseCase,
            actionScope = testScope,
            dispatcherProvider = TestDispatcherProvider(),
        )
    }

    private fun createExecuteUseCase(): ExecuteActionUseCase {
        val screenshotUseCase = TakeScreenshotUseCase(overlayVisibility, actionPerformer, 0L, 0L)
        return ExecuteActionUseCase(
            actionPerformer = actionPerformer,
            takeScreenshotUseCase = screenshotUseCase,
            panelLauncher = panelLauncher,
            flashlightController = FakeFlashlightController(),
            appLauncher = appLauncher,
            overlayVisibility = overlayVisibility,
        )
    }

    @Test
    fun `uiState emits default slots initially and updates on repository change`() = runTest {
        viewModel.uiState.test {
            assertEquals(Defaults.DEFAULT_PANEL_SLOTS, awaitItem().slots)

            val customSlots = listOf(ActionType.BACK, ActionType.HOME, ActionType.RECENTS).map(::PanelSlot)
            settingsRepository.update { it.copy(panelSlots = customSlots) }

            val updated = awaitItem().let { item ->
                if (item.slots == Defaults.DEFAULT_PANEL_SLOTS) awaitItem() else item
            }
            assertEquals(customSlots, updated.slots)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SlotSelected event executes action and emits Close effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelUiEvent.SlotSelected(PanelSlot(ActionType.HOME)))
            testScope.advanceUntilIdle()

            assertEquals(listOf(ActionType.HOME), actionPerformer.performedActions)
            assertEquals(PanelUiEffect.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SlotSelected with launch app opens slot target package`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelUiEvent.SlotSelected(PanelSlot(ActionType.LAUNCH_APP, "com.whatsapp")))
            testScope.advanceUntilIdle()

            assertEquals(listOf("com.whatsapp"), appLauncher.launchedPackages)
            assertEquals(PanelUiEffect.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CloseClicked event emits Close effect without running actions`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelUiEvent.CloseClicked)
            testScope.advanceUntilIdle()

            assertTrue(actionPerformer.performedActions.isEmpty())
            assertEquals(PanelUiEffect.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SlotSelected with screenshot hides overlay, performs screenshot and emits Close effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelUiEvent.SlotSelected(PanelSlot(ActionType.SCREENSHOT)))
            testScope.advanceUntilIdle()

            assertTrue(overlayVisibility.wasHidden)
            assertTrue(overlayVisibility.wasRestored)
            assertEquals(listOf(ActionType.SCREENSHOT), actionPerformer.performedActions)
            assertEquals(PanelUiEffect.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeActionPerformer : ActionPerformer {
        val performedActions = mutableListOf<ActionType>()

        override fun perform(action: ActionType): Boolean {
            performedActions.add(action)
            return true
        }
    }

    private class FakeOverlayVisibility : OverlayVisibility {
        var wasHidden = false
        var wasRestored = false

        override fun hideForCapture() {
            wasHidden = true
        }

        override fun restoreAfterCapture() {
            wasRestored = true
        }

        override fun showMessage(message: MessageType) = Unit
    }
}
