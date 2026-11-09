package com.ssajudn.dotap.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.usecase.ExecuteActionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @param actionScope outlives this view model. Closing the panel clears its view model store, which
 * would cancel `viewModelScope` and abort an action that is still running, such as a screenshot.
 */
class PanelViewModel(
    settingsRepository: SettingsRepository,
    private val executeAction: ExecuteActionUseCase,
    private val actionScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    val uiState: StateFlow<PanelUiState> = settingsRepository.settings
        .map { PanelUiState(slots = it.panelSlots) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PanelUiState(),
        )

    private val _uiEffect = Channel<PanelUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<PanelUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: PanelUiEvent) {
        when (event) {
            is PanelUiEvent.SlotSelected -> runAndClose(event.action)
            PanelUiEvent.CloseClicked -> _uiEffect.trySend(PanelUiEffect.Close)
        }
    }

    private fun runAndClose(action: ActionType) {
        actionScope.launch(dispatcherProvider.main) { executeAction(action) }
        _uiEffect.trySend(PanelUiEffect.Close)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
