package com.ssajudn.dotap.presentation.appearance

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.Limits
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

    @Test
    fun `dot size change persists and clamps within limits`() = runTest {
        viewModel.onEvent(AppearanceUiEvent.DotSizeChanged(60))
        assertEquals(60, repository.current.dotSizeDp)

        viewModel.onEvent(AppearanceUiEvent.DotSizeChanged(1000))
        assertEquals(Limits.MAX_DOT_SIZE_DP, repository.current.dotSizeDp)

        viewModel.onEvent(AppearanceUiEvent.DotSizeChanged(5))
        assertEquals(Limits.MIN_DOT_SIZE_DP, repository.current.dotSizeDp)
    }

    @Test
    fun `idle opacity change persists and clamps within limits`() = runTest {
        viewModel.onEvent(AppearanceUiEvent.IdleOpacityChanged(70))
        assertEquals(70, repository.current.idleOpacityPercent)

        viewModel.onEvent(AppearanceUiEvent.IdleOpacityChanged(150))
        assertEquals(Limits.MAX_IDLE_OPACITY_PERCENT, repository.current.idleOpacityPercent)

        viewModel.onEvent(AppearanceUiEvent.IdleOpacityChanged(0))
        assertEquals(Limits.MIN_IDLE_OPACITY_PERCENT, repository.current.idleOpacityPercent)
    }
}
