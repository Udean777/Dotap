package com.ssajudn.dotap.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.usecase.ObserveServiceStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val observeServiceStatusUseCase: ObserveServiceStatusUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeUiEffect> = _uiEffect.receiveAsFlow()

    init {
        observeServiceStatus()
        observeDotVisibility()
    }

    private fun observeServiceStatus() {
        viewModelScope.launch(dispatcherProvider.main) {
            observeServiceStatusUseCase().collect { status ->
                _uiState.update { it.copy(serviceStatus = status) }
            }
        }
    }

    private fun observeDotVisibility() {
        viewModelScope.launch(dispatcherProvider.main) {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(isDotEnabled = settings.isDotEnabled) }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            handleEvent(event)
        }
    }

    private suspend fun handleEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.OpenSettingsClicked -> _uiEffect.send(HomeUiEffect.NavigateToSettings)

            HomeUiEvent.OpenAccessibilitySettingsClicked -> {
                _uiEffect.send(HomeUiEffect.OpenAccessibilitySettings)
            }

            is HomeUiEvent.DotVisibilityToggled -> {
                settingsRepository.update { it.copy(isDotEnabled = event.enabled) }
            }

            HomeUiEvent.RestrictedSettingsHelpClicked -> {
                _uiState.update { it.copy(showRestrictedSettingsDialog = true) }
            }

            HomeUiEvent.DismissRestrictedSettingsDialog -> {
                _uiState.update { it.copy(showRestrictedSettingsDialog = false) }
            }

            HomeUiEvent.OpenAppInfoClicked -> {
                _uiState.update { it.copy(showRestrictedSettingsDialog = false) }
                _uiEffect.send(HomeUiEffect.OpenAppInfo)
            }
        }
    }
}
