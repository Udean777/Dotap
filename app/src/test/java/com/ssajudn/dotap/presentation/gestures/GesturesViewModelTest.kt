package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.ActionType
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
class GesturesViewModelTest {
    private lateinit var repository: FakeSettingsRepository
    private lateinit var viewModel: GesturesViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        viewModel = GesturesViewModel(repository, TestDispatcherProvider())
    }

    @Test
    fun `selecting actions persists each gesture`() = runTest {
        viewModel.onEvent(GesturesUiEvent.SingleTapActionSelected(ActionType.BACK))
        viewModel.onEvent(GesturesUiEvent.DoubleTapActionSelected(ActionType.RECENTS))
        viewModel.onEvent(GesturesUiEvent.LongPressActionSelected(ActionType.NOTIFICATIONS))

        val settings = repository.current
        assertEquals(ActionType.BACK, settings.singleTapAction)
        assertEquals(ActionType.RECENTS, settings.doubleTapAction)
        assertEquals(ActionType.NOTIFICATIONS, settings.longPressAction)
    }

    @Test
    fun `timeout changes are clamped by the domain model`() = runTest {
        viewModel.onEvent(GesturesUiEvent.DoubleTapTimeoutChanged(9_999))
        viewModel.onEvent(GesturesUiEvent.LongPressDurationChanged(1))

        assertEquals(Limits.MAX_DOUBLE_TAP_TIMEOUT_MS, repository.current.doubleTapTimeoutMs)
        assertEquals(Limits.MIN_LONG_PRESS_DURATION_MS, repository.current.longPressDurationMs)
    }

    @Test
    fun `state reflects stored settings`() = runTest {
        viewModel.onEvent(GesturesUiEvent.DoubleTapTimeoutChanged(450))

        val state = viewModel.uiState.first { it.settings.doubleTapTimeoutMs == 450L }
        assertEquals(450L, state.settings.doubleTapTimeoutMs)
    }
}
