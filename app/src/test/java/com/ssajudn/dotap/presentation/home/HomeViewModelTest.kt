package com.ssajudn.dotap.presentation.home

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.port.AccessibilityStatusSource
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.domain.usecase.ObserveServiceStatusUseCase
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var source: FakeAccessibilityStatusSource
    private lateinit var tracker: ServiceConnectionTracker
    private lateinit var observeUseCase: ObserveServiceStatusUseCase
    private lateinit var settingsRepository: FakeSettingsRepository

    private class FakeAccessibilityStatusSource : AccessibilityStatusSource {
        val flow = MutableStateFlow(false)
        override val isServiceEnabled: Flow<Boolean> = flow
    }

    @Before
    fun setUp() {
        dispatcherProvider = TestDispatcherProvider()
        source = FakeAccessibilityStatusSource()
        tracker = ServiceConnectionTracker()
        observeUseCase = ObserveServiceStatusUseCase(source, tracker)
        settingsRepository = FakeSettingsRepository()
    }

    private fun viewModel() = HomeViewModel(
        dispatcherProvider = dispatcherProvider,
        observeServiceStatusUseCase = observeUseCase,
        settingsRepository = settingsRepository,
    )

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

            source.flow.value = true
            assertEquals(ServiceStatus.ENABLED_NOT_CONNECTED, awaitItem().serviceStatus)

            tracker.setConnected(true)
            assertEquals(ServiceStatus.ENABLED_CONNECTED, awaitItem().serviceStatus)

            source.flow.value = false
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

    @Test
    fun restrictedSettingsDialog_flowAndOpenAppInfo() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertFalse(awaitItem().showRestrictedSettingsDialog)

            viewModel.onEvent(HomeUiEvent.RestrictedSettingsHelpClicked)
            assertTrue(awaitItem().showRestrictedSettingsDialog)

            viewModel.onEvent(HomeUiEvent.DismissRestrictedSettingsDialog)
            assertFalse(awaitItem().showRestrictedSettingsDialog)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.OpenAppInfoClicked)
            assertEquals(HomeUiEffect.OpenAppInfo, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
