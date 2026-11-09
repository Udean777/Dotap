package com.ssajudn.dotap.presentation.home

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var tracker: ServiceConnectionTracker
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        dispatcherProvider = TestDispatcherProvider()
        tracker = ServiceConnectionTracker()
        settingsRepository = FakeSettingsRepository()
    }

    private fun viewModel() = HomeViewModel(dispatcherProvider, tracker, settingsRepository)

    @Test
    fun onOpenSettingsClicked_emitsNavigateEffect() = runTest {
        val viewModel = viewModel()
        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.OpenSettingsClicked)
            assertEquals(HomeUiEffect.NavigateToSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onOpenAccessibilitySettingsClicked_emitsSettingsEffect() = runTest {
        val viewModel = viewModel()
        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.OpenAccessibilitySettingsClicked)
            assertEquals(HomeUiEffect.OpenAccessibilitySettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun serviceConnectionStatus_updatesUiState() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(ServiceStatus.DISABLED, awaitItem().serviceStatus)

            tracker.setConnected(true)
            assertEquals(ServiceStatus.ENABLED_CONNECTED, awaitItem().serviceStatus)

            tracker.setConnected(false)
            assertEquals(ServiceStatus.DISABLED, awaitItem().serviceStatus)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dotVisibilityToggle_persistsAndUpdatesState() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(HomeUiEvent.DotVisibilityToggled(false))

            assertFalse(awaitItem().isDotEnabled)
            assertFalse(settingsRepository.current.isDotEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dotVisibility_followsSettingsChangesFromElsewhere() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()

            settingsRepository.update { it.copy(isDotEnabled = false) }

            assertFalse(awaitItem().isDotEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
