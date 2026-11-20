package com.ssajudn.dotap.presentation.gestures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.GestureType
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.presentation.common.AppsCatalogState
import com.ssajudn.dotap.presentation.common.loadCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GesturesViewModel(
    private val settingsRepository: SettingsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val appsCatalog = MutableStateFlow(AppsCatalogState())
    private val activePickerGesture = MutableStateFlow<GestureType?>(null)

    val uiState: StateFlow<GesturesUiState> = combine(
        settingsRepository.settings,
        appsCatalog,
        activePickerGesture,
    ) { settings, catalog, pickerGesture ->
        GesturesUiState(
            settings = settings,
            installedApps = catalog.apps,
            appLabels = catalog.labels,
            isLoadingApps = catalog.isLoading,
            activeAppPickerGesture = pickerGesture,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = GesturesUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            appsCatalog.value = installedAppsRepository.loadCatalog()
        }
    }

    fun onEvent(event: GesturesUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                is GesturesUiEvent.DoubleTapTimeoutChanged -> settingsRepository.update {
                    it.withDoubleTapTimeout(event.millis.toLong())
                }

                is GesturesUiEvent.LongPressDurationChanged -> settingsRepository.update {
                    it.withLongPressDuration(event.millis.toLong())
                }

                is GesturesUiEvent.OpenAppPicker -> activePickerGesture.value = event.gesture

                is GesturesUiEvent.AppSelected -> {
                    activePickerGesture.value = null
                    settingsRepository.update { it.withGestureApp(event.gesture, event.packageName) }
                }

                GesturesUiEvent.DismissAppPicker -> activePickerGesture.value = null

                else -> handleActionEvent(event)
            }
        }
    }

    private suspend fun handleActionEvent(event: GesturesUiEvent) {
        when (event) {
            is GesturesUiEvent.SingleTapActionSelected -> selectAction(GestureType.SINGLE_TAP, event.action)
            is GesturesUiEvent.DoubleTapActionSelected -> selectAction(GestureType.DOUBLE_TAP, event.action)
            is GesturesUiEvent.LongPressActionSelected -> selectAction(GestureType.LONG_PRESS, event.action)
            is GesturesUiEvent.SwipeUpActionSelected -> selectAction(GestureType.SWIPE_UP, event.action)
            is GesturesUiEvent.SwipeDownActionSelected -> selectAction(GestureType.SWIPE_DOWN, event.action)
            is GesturesUiEvent.SwipeLeftActionSelected -> selectAction(GestureType.SWIPE_LEFT, event.action)
            is GesturesUiEvent.SwipeRightActionSelected -> selectAction(GestureType.SWIPE_RIGHT, event.action)
            else -> Unit
        }
    }

    private suspend fun selectAction(gesture: GestureType, action: ActionType) {
        val shouldOpenPicker = action == ActionType.LAUNCH_APP &&
            settingsRepository.settings.first().appPackageFor(gesture) == null
        settingsRepository.update { current -> applyGestureAction(current, gesture, action) }
        if (shouldOpenPicker) {
            activePickerGesture.value = gesture
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun applyGestureAction(
    current: com.ssajudn.dotap.domain.model.DotSettings,
    gesture: GestureType,
    action: ActionType,
): com.ssajudn.dotap.domain.model.DotSettings = when (gesture) {
    GestureType.SINGLE_TAP -> current.copy(singleTapAction = action)
    GestureType.DOUBLE_TAP -> current.copy(doubleTapAction = action)
    GestureType.LONG_PRESS -> current.copy(longPressAction = action)
    GestureType.SWIPE_UP -> current.copy(swipeUpAction = action)
    GestureType.SWIPE_DOWN -> current.copy(swipeDownAction = action)
    GestureType.SWIPE_LEFT -> current.copy(swipeLeftAction = action)
    GestureType.SWIPE_RIGHT -> current.copy(swipeRightAction = action)
}
