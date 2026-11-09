package com.ssajudn.dotap.presentation.panel

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import com.ssajudn.dotap.domain.usecase.TakeScreenshotUseCase
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
    private lateinit var actionPerformer: FakeActionPerformer
    private lateinit var overlayVisibility: FakeOverlayVisibility
    private lateinit var panelLauncher: FakePanelLauncher
    private lateinit var executeActionUseCase: ExecuteActionUseCase
    private lateinit var testScope: TestScope
    private lateinit var viewModel: PanelViewModel

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        actionPerformer = FakeActionPerformer()
        overlayVisibility = FakeOverlayVisibility()
        panelLauncher = FakePanelLauncher()
        val screenshotUseCase = TakeScreenshotUseCase(
            overlayVisibility = overlayVisibility,
            actionPerformer = actionPerformer,
            hideDelayMs = 0L,
            restoreDelayMs = 0L,
        )
        executeActionUseCase = ExecuteActionUseCase(
            actionPerformer = actionPerformer,
            takeScreenshotUseCase = screenshotUseCase,
            panelLauncher = panelLauncher,
        )
        testScope = TestScope()
        viewModel = PanelViewModel(
            settingsRepository = settingsRepository,
            executeAction = executeActionUseCase,
            actionScope = testScope,
            dispatcherProvider = TestDispatcherProvider(),
        )
    }

    @Test
    fun `uiState emits default slots initially and updates on repository change`() = runTest {
        viewModel.uiState.test {
            assertEquals(Defaults.DEFAULT_PANEL_SLOTS, awaitItem().slots)

            val customSlots = listOf(ActionType.BACK, ActionType.HOME, ActionType.RECENTS)
            settingsRepository.update { it.copy(panelSlots = customSlots) }

            assertEquals(customSlots, awaitItem().slots)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SlotSelected event executes action and emits Close effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelUiEvent.SlotSelected(ActionType.HOME))
            testScope.advanceUntilIdle()

            assertEquals(listOf(ActionType.HOME), actionPerformer.performedActions)
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
            viewModel.onEvent(PanelUiEvent.SlotSelected(ActionType.SCREENSHOT))
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
