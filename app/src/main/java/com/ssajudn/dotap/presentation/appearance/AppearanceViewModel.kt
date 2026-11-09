package com.ssajudn.dotap.presentation.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val settingsRepository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    val uiState: StateFlow<AppearanceUiState> = settingsRepository.settings
        .map { AppearanceUiState(settings = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = AppearanceUiState(),
        )

    fun onEvent(event: AppearanceUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                is AppearanceUiEvent.DotEnabledToggled -> settingsRepository.update {
                    it.copy(isDotEnabled = event.enabled)
                }

                is AppearanceUiEvent.SnapToEdgeToggled -> settingsRepository.update {
                    it.copy(isSnapToEdgeEnabled = event.enabled)
                }

                is AppearanceUiEvent.DotSizeChanged -> settingsRepository.update {
                    it.withDotSize(event.sizeDp)
                }

                is AppearanceUiEvent.IdleOpacityChanged -> settingsRepository.update {
                    it.withIdleOpacity(event.opacityPercent)
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
