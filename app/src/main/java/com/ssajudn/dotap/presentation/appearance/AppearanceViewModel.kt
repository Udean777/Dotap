package com.ssajudn.dotap.presentation.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.DotStyle
import com.ssajudn.dotap.domain.port.CustomDotImageStorage
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val settingsRepository: SettingsRepository,
    private val customDotImageStorage: CustomDotImageStorage,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val imagePickError = MutableStateFlow(false)

    val uiState: StateFlow<AppearanceUiState> = combine(
        settingsRepository.settings,
        imagePickError,
    ) { settings, hasError ->
        AppearanceUiState(
            settings = settings,
            isLoading = false,
            imagePickError = hasError,
        )
    }.stateIn(
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

                is AppearanceUiEvent.DotStyleSelected -> {
                    imagePickError.value = false
                    settingsRepository.update { it.copy(dotStyle = event.style) }
                }

                is AppearanceUiEvent.CustomImagePicked -> handleCustomImagePicked(event.uriString)
            }
        }
    }

    private suspend fun handleCustomImagePicked(uriString: String?) {
        if (uriString == null) return
        val savedPath = customDotImageStorage.saveFromUri(uriString)
        if (savedPath == null) {
            imagePickError.value = true
            return
        }
        imagePickError.value = false
        settingsRepository.update {
            it.copy(
                dotStyle = DotStyle.CUSTOM_IMAGE,
                customDotImagePath = savedPath,
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
