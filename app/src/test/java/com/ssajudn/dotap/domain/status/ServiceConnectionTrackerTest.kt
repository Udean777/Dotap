package com.ssajudn.dotap.domain.status

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceConnectionTrackerTest {

    @Test
    fun defaultState_isDisconnected() = runTest {
        val tracker = ServiceConnectionTracker()
        tracker.isConnected.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setConnected_updatesState() = runTest {
        val tracker = ServiceConnectionTracker()
        tracker.isConnected.test {
            assertFalse(awaitItem())
            tracker.setConnected(true)
            assertTrue(awaitItem())
            tracker.setConnected(false)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
