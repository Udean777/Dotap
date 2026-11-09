package com.ssajudn.dotap.domain.usecase

import com.ssajudn.dotap.domain.model.ActionResult
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.MessageType
import com.ssajudn.dotap.domain.port.ActionPerformer
import com.ssajudn.dotap.domain.port.OverlayVisibility
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TakeScreenshotUseCaseTest {

    private lateinit var overlayVisibility: FakeOverlayVisibility
    private lateinit var actionPerformer: FakeActionPerformer
    private lateinit var useCase: TakeScreenshotUseCase

    @Before
    fun setUp() {
        overlayVisibility = FakeOverlayVisibility()
        actionPerformer = FakeActionPerformer()
        useCase = TakeScreenshotUseCase(
            overlayVisibility = overlayVisibility,
            actionPerformer = actionPerformer,
            hideDelayMs = 150L,
            restoreDelayMs = 800L,
        )
    }

    @Test
    fun `successful screenshot hides overlay delays performs and restores in order`() = runTest {
        val result = useCase()

        assertTrue(result is ActionResult.Success)
        assertEquals(listOf(ActionType.SCREENSHOT), actionPerformer.performedActions)
        assertEquals(listOf("hide", "restore"), overlayVisibility.events)
        assertFalse(overlayVisibility.isHidden)
        assertEquals(950L, currentTime)
    }

    @Test
    fun `failed screenshot restores overlay and displays message`() = runTest {
        actionPerformer.shouldSucceed = false

        val result = useCase()

        assertTrue(result is ActionResult.Unavailable)
        assertEquals(listOf("hide", "message", "restore"), overlayVisibility.events)
        assertEquals(listOf(MessageType.SCREENSHOT_UNAVAILABLE), overlayVisibility.messages)
        assertFalse(overlayVisibility.isHidden)
    }

    @Test
    fun `throwing performer restores overlay and propagates`() = runTest {
        actionPerformer.shouldThrow = IllegalStateException("Service died")

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertTrue(thrown is IllegalStateException)
        assertEquals(listOf("hide", "restore"), overlayVisibility.events)
        assertEquals(emptyList<MessageType>(), overlayVisibility.messages)
        assertFalse(overlayVisibility.isHidden)
    }

    @Test
    fun `cancellation during hide delay restores overlay`() = runTest {
        val job = launch(start = CoroutineStart.UNDISPATCHED) { useCase() }
        assertTrue(overlayVisibility.isHidden)

        job.cancelAndJoin()

        assertFalse(overlayVisibility.isHidden)
        assertEquals(listOf("hide", "restore"), overlayVisibility.events)
    }

    private class FakeOverlayVisibility : OverlayVisibility {
        val events = mutableListOf<String>()
        val messages = mutableListOf<MessageType>()
        var isHidden = false

        override fun hideForCapture() {
            events.add("hide")
            isHidden = true
        }

        override fun restoreAfterCapture() {
            events.add("restore")
            isHidden = false
        }

        override fun showMessage(message: MessageType) {
            events.add("message")
            messages.add(message)
        }
    }

    private class FakeActionPerformer : ActionPerformer {
        val performedActions = mutableListOf<ActionType>()
        var shouldSucceed = true
        var shouldThrow: Exception? = null

        override fun perform(action: ActionType): Boolean {
            shouldThrow?.let { throw it }
            performedActions.add(action)
            return shouldSucceed
        }
    }
}
