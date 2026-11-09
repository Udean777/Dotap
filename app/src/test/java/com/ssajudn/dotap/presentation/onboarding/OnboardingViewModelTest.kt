package com.ssajudn.dotap.presentation.onboarding

import app.cash.turbine.test
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var repository: FakeSettingsRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        viewModel = OnboardingViewModel(repository, TestDispatcherProvider())
    }

    @Test
    fun `initial state is INTRO`() {
        assertEquals(OnboardingStep.INTRO, viewModel.uiState.value.step)
        assertFalse(repository.current.isOnboardingCompleted)
    }

    @Test
    fun `GetStartedClicked advances step to DISCLOSURE without emitting effects`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.GetStartedClicked)

            assertEquals(OnboardingStep.DISCLOSURE, viewModel.uiState.value.step)
            expectNoEvents()
            assertFalse(repository.current.isOnboardingCompleted)
        }
    }

    @Test
    fun `NotNowClicked marks onboarding completed and navigates home without opening settings`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.GetStartedClicked)
            viewModel.onEvent(OnboardingUiEvent.NotNowClicked)

            assertTrue(repository.current.isOnboardingCompleted)
            assertEquals(OnboardingUiEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `settings effect is emitted only after explicit consent AgreeClicked`() = runTest {
        viewModel.uiEffect.test {
            // User on intro screen - cannot reach settings
            expectNoEvents()

            // User moves to disclosure screen - still cannot reach settings
            viewModel.onEvent(OnboardingUiEvent.GetStartedClicked)
            expectNoEvents()

            // User gives explicit affirmative consent
            viewModel.onEvent(OnboardingUiEvent.AgreeClicked)

            assertTrue(repository.current.isOnboardingCompleted)
            assertEquals(OnboardingUiEffect.OpenAccessibilitySettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
