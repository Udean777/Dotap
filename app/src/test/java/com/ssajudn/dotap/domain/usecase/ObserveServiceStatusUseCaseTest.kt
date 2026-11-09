package com.ssajudn.dotap.domain.usecase

import app.cash.turbine.test
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.port.AccessibilityStatusSource
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveServiceStatusUseCaseTest {

    private class FakeAccessibilityStatusSource : AccessibilityStatusSource {
        val flow = MutableStateFlow(false)
        override val isServiceEnabled: Flow<Boolean> = flow
    }

    @Test
    fun serviceDisabledInSystem_returnsDisabled() = runTest {
        val source = FakeAccessibilityStatusSource()
        val tracker = ServiceConnectionTracker()
        val useCase = ObserveServiceStatusUseCase(source, tracker)

        source.flow.value = false
        tracker.setConnected(false)

        useCase().test {
            assertEquals(ServiceStatus.DISABLED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun serviceDisabledInSystemEvenIfTrackerConnected_returnsDisabled() = runTest {
        val source = FakeAccessibilityStatusSource()
        val tracker = ServiceConnectionTracker()
        val useCase = ObserveServiceStatusUseCase(source, tracker)

        source.flow.value = false
        tracker.setConnected(true)

        useCase().test {
            assertEquals(ServiceStatus.DISABLED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun serviceEnabledInSystemAndConnected_returnsEnabledConnected() = runTest {
        val source = FakeAccessibilityStatusSource()
        val tracker = ServiceConnectionTracker()
        val useCase = ObserveServiceStatusUseCase(source, tracker)

        source.flow.value = true
        tracker.setConnected(true)

        useCase().test {
            assertEquals(ServiceStatus.ENABLED_CONNECTED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun serviceEnabledInSystemButNotConnected_returnsEnabledNotConnected() = runTest {
        val source = FakeAccessibilityStatusSource()
        val tracker = ServiceConnectionTracker()
        val useCase = ObserveServiceStatusUseCase(source, tracker)

        source.flow.value = true
        tracker.setConnected(false)

        useCase().test {
            assertEquals(ServiceStatus.ENABLED_NOT_CONNECTED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun statusUpdatesReactivelyWhenStateChanges() = runTest {
        val source = FakeAccessibilityStatusSource()
        val tracker = ServiceConnectionTracker()
        val useCase = ObserveServiceStatusUseCase(source, tracker)

        source.flow.value = false
        tracker.setConnected(false)

        useCase().test {
            assertEquals(ServiceStatus.DISABLED, awaitItem())

            source.flow.value = true
            assertEquals(ServiceStatus.ENABLED_NOT_CONNECTED, awaitItem())

            tracker.setConnected(true)
            assertEquals(ServiceStatus.ENABLED_CONNECTED, awaitItem())

            source.flow.value = false
            assertEquals(ServiceStatus.DISABLED, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
