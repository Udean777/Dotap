package com.ssajudn.dotap.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(settings = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SettingsUiState(),
        )

    private val _uiEffect = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SettingsUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                is SettingsUiEvent.HapticsToggled -> settingsRepository.update {
                    it.copy(isHapticsEnabled = event.enabled)
                }

                SettingsUiEvent.OpenGesturesClicked -> _uiEffect.send(SettingsUiEffect.NavigateToGestures)

                SettingsUiEvent.OpenAppearanceClicked -> _uiEffect.send(SettingsUiEffect.NavigateToAppearance)

                SettingsUiEvent.OpenBatteryGuidanceClicked -> _uiEffect.send(
                    SettingsUiEffect.NavigateToBatteryGuidance,
                )

                SettingsUiEvent.BackClicked -> _uiEffect.send(SettingsUiEffect.NavigateBack)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
