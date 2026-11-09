package com.ssajudn.dotap.system.overlay

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.HapticFeedback
import com.ssajudn.dotap.domain.port.OverlayVisibility
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import com.ssajudn.dotap.domain.usecase.HandleGestureUseCase
import com.ssajudn.dotap.domain.usecase.TakeScreenshotUseCase
import com.ssajudn.dotap.testutil.FakePanelLauncher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DotPresenterTest {

    @Test
    fun defaultState_matchesDefaults() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            val state = awaitItem()
            assertTrue(state.isVisible)
            assertEquals(Defaults.DOT_SIDE_RIGHT, state.isRightSide)
            assertEquals(Defaults.DOT_Y_RATIO, state.yRatio, 0.001f)
            assertEquals(Defaults.DOT_SIZE_DP, state.dotSizeDp)
            assertEquals(1.0f, state.alpha, 0.001f)
            assertFalse(state.isHiddenForCapture)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setVisible_updatesState() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem() // initial
            presenter.setVisible(false)
            assertFalse(awaitItem().isVisible)
            presenter.setVisible(true)
            assertTrue(awaitItem().isVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setHiddenForCapture_updatesState() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem() // initial
            presenter.setHiddenForCapture(true)
            assertTrue(awaitItem().isHiddenForCapture)
            presenter.setHiddenForCapture(false)
            assertFalse(awaitItem().isHiddenForCapture)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updatePosition_clampsYRatio() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem() // initial

            presenter.updatePosition(isRightSide = false, yRatio = 0.5f)
            val state1 = awaitItem()
            assertFalse(state1.isRightSide)
            assertEquals(0.5f, state1.yRatio, 0.001f)

            // Below min
            presenter.updatePosition(isRightSide = true, yRatio = -1.0f)
            val state2 = awaitItem()
            assertTrue(state2.isRightSide)
            assertEquals(Limits.MIN_Y_RATIO, state2.yRatio, 0.001f)

            // Above max
            presenter.updatePosition(isRightSide = true, yRatio = 2.0f)
            val state3 = awaitItem()
            assertEquals(Limits.MAX_Y_RATIO, state3.yRatio, 0.001f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateAlpha_clampsWithinZeroAndOne() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem() // initial

            presenter.updateAlpha(0.6f)
            assertEquals(0.6f, awaitItem().alpha, 0.001f)

            presenter.updateAlpha(-0.5f)
            assertEquals(0f, awaitItem().alpha, 0.001f)

            presenter.updateAlpha(1.5f)
            assertEquals(1f, awaitItem().alpha, 0.001f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateDotSize_clampsWithinLimits() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem() // initial

            presenter.updateDotSize(60)
            assertEquals(60, awaitItem().dotSizeDp)

            presenter.updateDotSize(10)
            assertEquals(Limits.MIN_DOT_SIZE_DP, awaitItem().dotSizeDp)

            presenter.updateDotSize(100)
            assertEquals(Limits.MAX_DOT_SIZE_DP, awaitItem().dotSizeDp)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applySettings updates visibility, size, position and snapping`() = runTest {
        val presenter = DotPresenter()
        presenter.uiState.test {
            awaitItem()

            presenter.applySettings(
                DotSettings(
                    isDotEnabled = false,
                    dotSizeDp = 999,
                    isRightSide = false,
                    yRatio = 0.9f,
                    isSnapToEdgeEnabled = false,
                ),
            )

            val state = awaitItem()
            assertFalse(state.isVisible)
            assertEquals(Limits.MAX_DOT_SIZE_DP, state.dotSizeDp)
            assertFalse(state.isRightSide)
            assertEquals(0.9f, state.yRatio, 0.001f)
            assertFalse(state.isSnapToEdgeEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applySettings with snapping off keeps a free x position`() = runTest {
        val presenter = DotPresenter()
        presenter.updatePosition(isRightSide = false, yRatio = 0.4f, xRatio = 0.3f)
        presenter.uiState.test {
            awaitItem()

            presenter.applySettings(DotSettings(isSnapToEdgeEnabled = false))

            assertEquals(0.3f, awaitItem().xRatio, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `overlapping gestures run one action at a time`() = runTest {
        val performer = CountingActionPerformer()
        val useCase = HandleGestureUseCase(
            FakeSettingsRepository(DotSettings(doubleTapAction = ActionType.SCREENSHOT)),
            NoopHapticFeedback,
            ExecuteActionUseCase(
                performer,
                TakeScreenshotUseCase(NoopOverlayVisibility, performer, hideDelayMs = 150L, restoreDelayMs = 800L),
                FakePanelLauncher(),
            ),
        )
        val presenter = DotPresenter(handleGestureUseCase = useCase, scope = this)

        presenter.onGesture(GestureType.DOUBLE_TAP)
        presenter.onGesture(GestureType.DOUBLE_TAP)
        advanceUntilIdle()

        assertEquals(1, performer.count)

        presenter.onGesture(GestureType.DOUBLE_TAP)
        advanceUntilIdle()

        assertEquals(2, performer.count)
    }

    @Test
    fun `idle opacity fades after idle delay and returns on touch active`() = runTest {
        val presenter = DotPresenter(scope = this)
        presenter.applySettings(
            DotSettings(
                idleOpacityPercent = 40,
                idleDelayMs = 3000L,
            ),
        )

        assertEquals(1.0f, presenter.uiState.value.alpha, 0.001f)

        advanceTimeBy(3000L)
        runCurrent()
        assertEquals(0.4f, presenter.uiState.value.alpha, 0.001f)

        presenter.onTouchActive()
        assertEquals(1.0f, presenter.uiState.value.alpha, 0.001f)

        presenter.onTouchFinished()
        advanceTimeBy(2999L)
        runCurrent()
        assertEquals(1.0f, presenter.uiState.value.alpha, 0.001f)

        advanceTimeBy(1L)
        runCurrent()
        assertEquals(0.4f, presenter.uiState.value.alpha, 0.001f)
    }

    @Test
    fun `idle opacity of 100 percent never fades`() = runTest {
        val presenter = DotPresenter(scope = this)
        presenter.applySettings(
            DotSettings(
                idleOpacityPercent = 100,
                idleDelayMs = 3000L,
            ),
        )

        advanceTimeBy(5000L)
        assertEquals(1.0f, presenter.uiState.value.alpha, 0.001f)
    }

    private class CountingActionPerformer : ActionPerformer {
        var count = 0

        override fun perform(action: ActionType): Boolean {
            count++
            return true
        }
    }

    private class FakeSettingsRepository(
        initial: DotSettings,
    ) : SettingsRepository {
        private val state = MutableStateFlow(initial)
        override val settings: Flow<DotSettings> = state

        override suspend fun update(transform: (DotSettings) -> DotSettings) {
            state.value = transform(state.value)
        }
    }

    private object NoopHapticFeedback : HapticFeedback {
        override fun tick() = Unit
    }

    private object NoopOverlayVisibility : OverlayVisibility {
        override fun hideForCapture() = Unit
        override fun restoreAfterCapture() = Unit
        override fun showMessage(message: MessageType) = Unit
    }
}
