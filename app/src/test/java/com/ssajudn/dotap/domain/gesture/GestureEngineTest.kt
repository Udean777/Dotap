package com.ssajudn.dotap.domain.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GestureEngineTest {

    private lateinit var scheduler: FakeScheduler
    private lateinit var listener: RecordingGestureListener
    private val defaultConfig = GestureConfig(
        doubleTapTimeoutMs = 300L,
        longPressDurationMs = 500L,
        touchSlopPx = 10f,
        isDoubleTapAssigned = true,
        isLongPressAssigned = true,
    )

    @Before
    fun setUp() {
        scheduler = FakeScheduler()
        listener = RecordingGestureListener()
    }

    @Test
    fun `single tap with double tap assigned emits single tap after timeout`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()

        assertEquals(emptyList<String>(), listener.events)
        assertTrue(scheduler.hasPending("DOUBLE_TAP"))

        scheduler.trigger("DOUBLE_TAP")

        assertEquals(listOf("single_tap"), listener.events)
    }

    @Test
    fun `single tap with double tap unassigned emits single tap immediately on up`() {
        val config = defaultConfig.copy(isDoubleTapAssigned = false)
        val engine = GestureEngine(config, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()

        assertEquals(listOf("single_tap"), listener.events)
        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
    }

    @Test
    fun `double tap within timeout emits onDoubleTap and not single tap`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()
        engine.down(0f, 0f)
        engine.up()

        assertEquals(listOf("double_tap"), listener.events)
        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
    }

    @Test
    fun `long press emits onLongPress and ignores subsequent up`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        assertTrue(scheduler.hasPending("LONG_PRESS"))

        scheduler.trigger("LONG_PRESS")
        assertEquals(listOf("long_press"), listener.events)

        engine.up()
        assertEquals(listOf("long_press"), listener.events)
    }

    @Test
    fun `long press not assigned does not schedule timer`() {
        val config = defaultConfig.copy(isLongPressAssigned = false)
        val engine = GestureEngine(config, scheduler, listener)

        engine.down(0f, 0f)
        assertFalse(scheduler.hasPending("LONG_PRESS"))
    }

    @Test
    fun `drag beyond touch slop cancels long press and emits drag events without tap`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        assertTrue(scheduler.hasPending("LONG_PRESS"))

        // Move within slop does not start drag
        engine.move(5f, 0f)
        assertEquals(emptyList<String>(), listener.events)

        // Move beyond slop starts drag
        engine.move(15f, 0f)
        assertFalse(scheduler.hasPending("LONG_PRESS"))
        assertEquals(listOf("drag_start", "drag"), listener.events)

        // More drag
        engine.move(25f, 0f)
        assertEquals(listOf("drag_start", "drag", "drag"), listener.events)

        // Up finishes drag
        engine.up()
        assertEquals(listOf("drag_start", "drag", "drag", "drag_end"), listener.events)
    }

    @Test
    fun `second tap beyond touch slop emits single tap and starts a new press`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()
        engine.down(100f, 100f)

        assertEquals(listOf("single_tap"), listener.events)
        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
        assertTrue(scheduler.hasPending("LONG_PRESS"))

        engine.up()
        assertTrue(scheduler.hasPending("DOUBLE_TAP"))
    }

    @Test
    fun `drag after first tap cancels double tap and enters drag`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()
        assertEquals(emptyList<String>(), listener.events)

        // Second touch starts and drags
        engine.down(0f, 0f)
        engine.move(20f, 0f)
        engine.up()

        assertEquals(listOf("drag_start", "drag", "drag_end"), listener.events)
        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
    }

    @Test
    fun `long press on second tap fires long press and not double tap`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()
        engine.down(0f, 0f)

        assertTrue(scheduler.hasPending("LONG_PRESS"))
        scheduler.trigger("LONG_PRESS")

        assertEquals(listOf("long_press"), listener.events)

        engine.up()
        assertEquals(listOf("long_press"), listener.events)
    }

    @Test
    fun `config change during wait second tap to unassigned double tap emits single tap`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)

        engine.down(0f, 0f)
        engine.up()
        assertEquals(emptyList<String>(), listener.events)

        engine.updateConfig(defaultConfig.copy(isDoubleTapAssigned = false))
        assertEquals(listOf("single_tap"), listener.events)
        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
    }

    @Test
    fun `cancel in IDLE does nothing`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.cancel()
        assertEquals(emptyList<String>(), listener.events)
    }

    @Test
    fun `cancel in PRESSED cancels timer and emits no events`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.down(0f, 0f)
        engine.cancel()

        assertFalse(scheduler.hasPending("LONG_PRESS"))
        assertEquals(emptyList<String>(), listener.events)
    }

    @Test
    fun `cancel in WAIT_SECOND_TAP cancels timer and emits no events`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.down(0f, 0f)
        engine.up()
        engine.cancel()

        assertFalse(scheduler.hasPending("DOUBLE_TAP"))
        assertEquals(emptyList<String>(), listener.events)
    }

    @Test
    fun `cancel in SECOND_PRESSED cancels timer and emits no events`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.down(0f, 0f)
        engine.up()
        engine.down(0f, 0f)
        engine.cancel()

        assertFalse(scheduler.hasPending("LONG_PRESS"))
        assertEquals(emptyList<String>(), listener.events)
    }

    @Test
    fun `cancel in DRAGGING emits dragEnd`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.down(0f, 0f)
        engine.move(20f, 0f)
        assertEquals(listOf("drag_start", "drag"), listener.events)

        engine.cancel()
        assertEquals(listOf("drag_start", "drag", "drag_end"), listener.events)
    }

    @Test
    fun `cancel in LONG_FIRED returns to IDLE`() {
        val engine = GestureEngine(defaultConfig, scheduler, listener)
        engine.down(0f, 0f)
        scheduler.trigger("LONG_PRESS")
        engine.cancel()

        assertEquals(listOf("long_press"), listener.events)
        engine.down(0f, 0f)
        assertTrue(scheduler.hasPending("LONG_PRESS"))
    }

    private class FakeScheduler : Scheduler {
        private data class Task(val token: String, val action: () -> Unit)
        private val tasks = mutableListOf<Task>()

        override fun postDelayed(delayMs: Long, token: Any, action: () -> Unit) {
            tasks.add(Task(token.toString(), action))
        }

        override fun cancel(token: Any) {
            tasks.removeAll { it.token == token.toString() }
        }

        fun trigger(token: String) {
            val task = tasks.firstOrNull { it.token == token } ?: return
            tasks.remove(task)
            task.action()
        }

        fun hasPending(token: String): Boolean = tasks.any { it.token == token }
    }

    private class RecordingGestureListener : GestureListener {
        val events = mutableListOf<String>()

        override fun onSingleTap() {
            events.add("single_tap")
        }
        override fun onDoubleTap() {
            events.add("double_tap")
        }
        override fun onLongPress() {
            events.add("long_press")
        }
        override fun onDragStart() {
            events.add("drag_start")
        }
        override fun onDrag(dx: Float, dy: Float) {
            events.add("drag")
        }
        override fun onDragEnd() {
            events.add("drag_end")
        }
    }
}
