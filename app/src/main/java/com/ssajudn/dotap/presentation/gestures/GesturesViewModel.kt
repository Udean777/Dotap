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
                is GesturesUiEvent.SingleTapActionSelected ->
                    selectAction(GestureType.SINGLE_TAP, event.action)

                is GesturesUiEvent.DoubleTapActionSelected ->
                    selectAction(GestureType.DOUBLE_TAP, event.action)

                is GesturesUiEvent.LongPressActionSelected ->
                    selectAction(GestureType.LONG_PRESS, event.action)

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
            }
        }
    }

    private suspend fun selectAction(gesture: GestureType, action: ActionType) {
        val shouldOpenPicker = action == ActionType.LAUNCH_APP &&
            settingsRepository.settings.first().appPackageFor(gesture) == null
        settingsRepository.update { current ->
            when (gesture) {
                GestureType.SINGLE_TAP -> current.copy(singleTapAction = action)
                GestureType.DOUBLE_TAP -> current.copy(doubleTapAction = action)
                GestureType.LONG_PRESS -> current.copy(longPressAction = action)
            }
        }
        if (shouldOpenPicker) {
            activePickerGesture.value = gesture
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
