package com.ssajudn.dotap.presentation.home

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var tracker: ServiceConnectionTracker

    @Before
    fun setUp() {
        dispatcherProvider = TestDispatcherProvider()
        tracker = ServiceConnectionTracker()
    }

    @Test
    fun onOpenSettingsClicked_emitsNavigateEffect() = runTest {
        val viewModel = HomeViewModel(dispatcherProvider, tracker)
        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.OpenSettingsClicked)
            assertEquals(HomeUiEffect.NavigateToSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onOpenAccessibilitySettingsClicked_emitsSettingsEffect() = runTest {
        val viewModel = HomeViewModel(dispatcherProvider, tracker)
        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.OpenAccessibilitySettingsClicked)
            assertEquals(HomeUiEffect.OpenAccessibilitySettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun serviceConnectionStatus_updatesUiState() = runTest {
        val viewModel = HomeViewModel(dispatcherProvider, tracker)
        viewModel.uiState.test {
            assertEquals(ServiceStatus.DISABLED, awaitItem().serviceStatus)

            tracker.setConnected(true)
            assertEquals(ServiceStatus.ENABLED_CONNECTED, awaitItem().serviceStatus)

            tracker.setConnected(false)
            assertEquals(ServiceStatus.DISABLED, awaitItem().serviceStatus)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
