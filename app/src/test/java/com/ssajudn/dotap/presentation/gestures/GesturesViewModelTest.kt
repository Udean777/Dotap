package com.ssajudn.dotap.presentation.gestures

import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.testutil.FakeInstalledAppsRepository
import com.ssajudn.dotap.testutil.FakeSettingsRepository
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GesturesViewModelTest {
    private lateinit var repository: FakeSettingsRepository
    private lateinit var appsRepository: FakeInstalledAppsRepository
    private lateinit var viewModel: GesturesViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        appsRepository = FakeInstalledAppsRepository()
        viewModel = GesturesViewModel(repository, appsRepository, TestDispatcherProvider())
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
    fun `selecting launch app opens picker when target app is unset and stores different apps per gesture`() = runTest {
        viewModel.onEvent(GesturesUiEvent.SingleTapActionSelected(ActionType.LAUNCH_APP))
        val singlePicker = viewModel.uiState.first { it.activeAppPickerGesture == GestureType.SINGLE_TAP }
        assertEquals(GestureType.SINGLE_TAP, singlePicker.activeAppPickerGesture)

        viewModel.onEvent(GesturesUiEvent.AppSelected(GestureType.SINGLE_TAP, "com.whatsapp"))
        assertNull(viewModel.uiState.first { it.activeAppPickerGesture == null }.activeAppPickerGesture)

        viewModel.onEvent(GesturesUiEvent.LongPressActionSelected(ActionType.LAUNCH_APP))
        viewModel.uiState.first { it.activeAppPickerGesture == GestureType.LONG_PRESS }
        viewModel.onEvent(GesturesUiEvent.AppSelected(GestureType.LONG_PRESS, "com.android.camera2"))

        assertEquals("com.whatsapp", repository.current.singleTapAppPackage)
        assertEquals("com.android.camera2", repository.current.longPressAppPackage)
        assertNull(repository.current.doubleTapAppPackage)
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
