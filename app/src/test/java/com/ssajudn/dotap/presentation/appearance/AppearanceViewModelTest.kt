package com.ssajudn.dotap.presentation.appearance

import app.cash.turbine.test
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {
    private lateinit var repository: FakeSettingsRepository
    private lateinit var viewModel: AppearanceViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        viewModel = AppearanceViewModel(repository, TestDispatcherProvider())
    }

    @Test
    fun `dot visibility toggle persists`() = runTest {
        viewModel.onEvent(AppearanceUiEvent.DotEnabledToggled(false))
        assertEquals(false, repository.current.isDotEnabled)
    }

    @Test
    fun `snap toggle persists and updates state`() = runTest {
        viewModel.onEvent(AppearanceUiEvent.SnapToEdgeToggled(false))

        val state = viewModel.uiState.first { !it.settings.isSnapToEdgeEnabled }
        assertEquals(false, state.settings.isSnapToEdgeEnabled)
        assertEquals(false, repository.current.isSnapToEdgeEnabled)
    }
}
