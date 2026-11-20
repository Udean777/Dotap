package com.ssajudn.dotap.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.PanelSlot
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import com.ssajudn.dotap.presentation.common.loadCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @param actionScope outlives this view model. Closing the panel clears its view model store, which
 * would cancel `viewModelScope` and abort an action that is still running, such as a screenshot.
 */
class PanelViewModel(
    settingsRepository: SettingsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val executeAction: ExecuteActionUseCase,
    private val actionScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val appLabels = MutableStateFlow<Map<String, String>>(emptyMap())

    val uiState: StateFlow<PanelUiState> = combine(
        settingsRepository.settings,
        appLabels,
    ) { settings, labels ->
        PanelUiState(slots = settings.panelSlots, appLabels = labels)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = PanelUiState(),
    )

    private val _uiEffect = Channel<PanelUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<PanelUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            appLabels.value = installedAppsRepository.loadCatalog().labels
        }
    }

    fun onEvent(event: PanelUiEvent) {
        when (event) {
            is PanelUiEvent.SlotSelected -> runAndClose(event.slot)
            PanelUiEvent.CloseClicked -> _uiEffect.trySend(PanelUiEffect.Close)
        }
    }

    private fun runAndClose(slot: PanelSlot) {
        actionScope.launch(dispatcherProvider.main) {
            executeAction(slot.action, slot.appPackage)
        }
        _uiEffect.trySend(PanelUiEffect.Close)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
