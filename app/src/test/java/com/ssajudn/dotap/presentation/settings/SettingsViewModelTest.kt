package com.ssajudn.dotap.presentation.settings

import app.cash.turbine.test
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var repository: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        viewModel = SettingsViewModel(repository, TestDispatcherProvider())
    }

    @Test
    fun `haptics toggle persists and updates state`() = runTest {
        viewModel.onEvent(SettingsUiEvent.HapticsToggled(false))

        val state = viewModel.uiState.first { !it.settings.isHapticsEnabled }
        assertFalse(state.settings.isHapticsEnabled)
        assertFalse(repository.current.isHapticsEnabled)
    }

    @Test
    fun `open gestures emits navigation effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.OpenGesturesClicked)
            assertEquals(SettingsUiEffect.NavigateToGestures, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `open appearance emits navigation effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.OpenAppearanceClicked)
            assertEquals(SettingsUiEffect.NavigateToAppearance, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back emits back effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.BackClicked)
            assertEquals(SettingsUiEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
