package com.ssajudn.dotap.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<OnboardingUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: OnboardingUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                OnboardingUiEvent.GetStartedClicked -> {
                    _uiState.update { it.copy(step = OnboardingStep.DISCLOSURE) }
                }

                OnboardingUiEvent.AgreeClicked -> {
                    settingsRepository.update { it.copy(isOnboardingCompleted = true) }
                    _uiEffect.send(OnboardingUiEffect.OpenAccessibilitySettings)
                }

                OnboardingUiEvent.NotNowClicked -> {
                    settingsRepository.update { it.copy(isOnboardingCompleted = true) }
                    _uiEffect.send(OnboardingUiEffect.NavigateToHome)
                }
            }
        }
    }
}
