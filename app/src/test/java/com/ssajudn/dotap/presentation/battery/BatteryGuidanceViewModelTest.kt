package com.ssajudn.dotap.presentation.battery

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.OemBrand
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryGuidanceViewModelTest {
    private lateinit var dispatcherProvider: TestDispatcherProvider

    @Before
    fun setUp() {
        dispatcherProvider = TestDispatcherProvider()
    }

    @Test
    fun initialUiState_setsDetectedAndSelectedBrand() = runTest {
        val viewModel = BatteryGuidanceViewModel(dispatcherProvider, "Xiaomi")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(OemBrand.XIAOMI, state.currentBrand)
            assertEquals(OemBrand.XIAOMI, state.selectedBrand)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectBrand_updatesSelectedBrandInState() = runTest {
        val viewModel = BatteryGuidanceViewModel(dispatcherProvider, "Google")
        viewModel.uiState.test {
            assertEquals(OemBrand.OTHER, awaitItem().selectedBrand)

            viewModel.onEvent(BatteryGuidanceUiEvent.SelectBrand(OemBrand.SAMSUNG))
            assertEquals(OemBrand.SAMSUNG, awaitItem().selectedBrand)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest {
        val viewModel = BatteryGuidanceViewModel(dispatcherProvider, "Samsung")
        viewModel.uiEffect.test {
            viewModel.onEvent(BatteryGuidanceUiEvent.BackClicked)
            assertEquals(BatteryGuidanceUiEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openBatterySettingsClicked_emitsSettingsEffect() = runTest {
        val viewModel = BatteryGuidanceViewModel(dispatcherProvider, "Oppo")
        viewModel.uiEffect.test {
            viewModel.onEvent(BatteryGuidanceUiEvent.OpenBatterySettingsClicked)
            assertEquals(BatteryGuidanceUiEffect.OpenBatterySettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openWebHelpClicked_emitsWebHelpEffectWithUrl() = runTest {
        val viewModel = BatteryGuidanceViewModel(dispatcherProvider, "Vivo")
        val targetUrl = "https://dontkillmyapp.com/vivo"
        viewModel.uiEffect.test {
            viewModel.onEvent(BatteryGuidanceUiEvent.OpenWebHelpClicked(targetUrl))
            assertEquals(BatteryGuidanceUiEffect.OpenWebHelp(targetUrl), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
