package com.ssajudn.dotap.presentation.panelsettings

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.testutil.FakeInstalledAppsRepository
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelSettingsViewModelTest {
    private lateinit var repository: FakeSettingsRepository
    private lateinit var appsRepository: FakeInstalledAppsRepository
    private lateinit var viewModel: PanelSettingsViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        appsRepository = FakeInstalledAppsRepository()
        viewModel = PanelSettingsViewModel(repository, appsRepository, TestDispatcherProvider())
    }

    @Test
    fun `initial state exposes default slots and remaining panel actions`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertEquals(Defaults.DEFAULT_PANEL_SLOTS, state.slots)
            assertTrue(state.canAddMore)
            assertTrue(state.canRemove)
            assertTrue(ActionType.POWER_DIALOG in state.availableActions)
            assertTrue(ActionType.LOCK_SCREEN in state.availableActions)
            assertTrue(ActionType.LAUNCH_APP in state.availableActions)
            assertFalse(ActionType.OPEN_PANEL in state.availableActions)
            assertFalse(ActionType.NONE in state.availableActions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `move slot reorders active slots and persists`() = runTest {
        viewModel.onEvent(PanelSettingsUiEvent.MoveSlot(fromIndex = 0, toIndex = 2))

        val state = viewModel.uiState.first {
            !it.isLoading && it.slots.firstOrNull()?.action == ActionType.BACK
        }
        assertEquals(
            listOf(
                ActionType.BACK,
                ActionType.RECENTS,
                ActionType.HOME,
                ActionType.SCREENSHOT,
                ActionType.NOTIFICATIONS,
                ActionType.QUICK_SETTINGS,
            ).map(::PanelSlot),
            state.slots,
        )
        assertEquals(state.slots, repository.current.panelSlots)
    }

    @Test
    fun `move slot ignores out of bounds indices`() = runTest {
        viewModel.onEvent(PanelSettingsUiEvent.MoveSlot(fromIndex = -1, toIndex = 2))
        viewModel.onEvent(PanelSettingsUiEvent.MoveSlot(fromIndex = 0, toIndex = 99))

        assertEquals(Defaults.DEFAULT_PANEL_SLOTS, repository.current.panelSlots)
    }

    @Test
    fun `add slot appends action and stops at max limit`() = runTest {
        viewModel.onEvent(PanelSettingsUiEvent.AddSlot(ActionType.POWER_DIALOG))
        viewModel.onEvent(PanelSettingsUiEvent.AddSlot(ActionType.LOCK_SCREEN))

        val fullState = viewModel.uiState.first { !it.isLoading && !it.canAddMore }
        assertEquals(Limits.MAX_PANEL_SLOTS, fullState.slots.size)
        assertTrue(ActionType.FLASHLIGHT in fullState.availableActions)

        viewModel.onEvent(PanelSettingsUiEvent.AddSlot(ActionType.FLASHLIGHT))
        assertEquals(Limits.MAX_PANEL_SLOTS, repository.current.panelSlots.size)
    }

    @Test
    fun `adding launch app opens picker and supports multiple distinct apps`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        viewModel.onEvent(PanelSettingsUiEvent.AddSlot(ActionType.LAUNCH_APP))
        val pickerState = viewModel.uiState.first { it.appPickerTarget == PanelAppPickerTarget.NewSlot }
        assertEquals(PanelAppPickerTarget.NewSlot, pickerState.appPickerTarget)

        viewModel.onEvent(PanelSettingsUiEvent.AppPicked("com.whatsapp"))
        viewModel.onEvent(PanelSettingsUiEvent.AddSlot(ActionType.LAUNCH_APP))
        viewModel.uiState.first { it.appPickerTarget == PanelAppPickerTarget.NewSlot }
        viewModel.onEvent(PanelSettingsUiEvent.AppPicked("com.android.camera2"))

        val updated = viewModel.uiState.first { it.slots.size == Defaults.DEFAULT_PANEL_SLOTS.size + 2 }
        assertTrue(PanelSlot(ActionType.LAUNCH_APP, "com.whatsapp") in updated.slots)
        assertTrue(PanelSlot(ActionType.LAUNCH_APP, "com.android.camera2") in updated.slots)
    }

    @Test
    fun `editing launch app slot updates only that slot`() = runTest {
        val initialSlots = listOf(
            PanelSlot(ActionType.HOME),
            PanelSlot(ActionType.LAUNCH_APP, "com.whatsapp"),
            PanelSlot(ActionType.BACK),
        )
        repository = FakeSettingsRepository(DotSettings.Default.copy(panelSlots = initialSlots))
        viewModel = PanelSettingsViewModel(repository, appsRepository, TestDispatcherProvider())
        viewModel.uiState.first { !it.isLoading }

        viewModel.onEvent(PanelSettingsUiEvent.EditSlotApp(index = 1))
        viewModel.uiState.first { it.appPickerTarget is PanelAppPickerTarget.ExistingSlot }
        viewModel.onEvent(PanelSettingsUiEvent.AppPicked("com.google.android.calculator"))

        val state = viewModel.uiState.first {
            it.slots[1].appPackage == "com.google.android.calculator"
        }
        assertEquals(
            listOf(
                PanelSlot(ActionType.HOME),
                PanelSlot(ActionType.LAUNCH_APP, "com.google.android.calculator"),
                PanelSlot(ActionType.BACK),
            ),
            state.slots,
        )
    }

    @Test
    fun `remove slot deletes entry and enforces min limit`() = runTest {
        val minimalSlots = listOf(
            ActionType.HOME,
            ActionType.BACK,
            ActionType.RECENTS,
            ActionType.SCREENSHOT,
        ).map(::PanelSlot)
        repository = FakeSettingsRepository(DotSettings.Default.copy(panelSlots = minimalSlots))
        viewModel = PanelSettingsViewModel(repository, appsRepository, TestDispatcherProvider())

        viewModel.onEvent(PanelSettingsUiEvent.RemoveSlot(index = 1))
        val minState = viewModel.uiState.first { !it.isLoading && !it.canRemove }
        assertEquals(
            listOf(ActionType.HOME, ActionType.RECENTS, ActionType.SCREENSHOT).map(::PanelSlot),
            minState.slots,
        )

        viewModel.onEvent(PanelSettingsUiEvent.RemoveSlot(index = 0))
        assertEquals(Limits.MIN_PANEL_SLOTS, repository.current.panelSlots.size)
    }

    @Test
    fun `reset defaults restores default panel slots`() = runTest {
        repository.update {
            it.withPanelSlots(
                listOf(ActionType.POWER_DIALOG, ActionType.LOCK_SCREEN, ActionType.BACK).map(::PanelSlot),
            )
        }

        viewModel.onEvent(PanelSettingsUiEvent.ResetDefaultsClicked)

        val state = viewModel.uiState.first { !it.isLoading && it.slots == Defaults.DEFAULT_PANEL_SLOTS }
        assertEquals(Defaults.DEFAULT_PANEL_SLOTS, state.slots)
    }

    @Test
    fun `back clicked emits navigate back effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(PanelSettingsUiEvent.BackClicked)
            assertEquals(PanelSettingsUiEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
