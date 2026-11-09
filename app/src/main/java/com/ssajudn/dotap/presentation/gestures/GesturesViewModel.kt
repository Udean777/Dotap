package com.ssajudn.dotap.presentation.gestures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GesturesViewModel(
    private val settingsRepository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    val uiState: StateFlow<GesturesUiState> = settingsRepository.settings
        .map { GesturesUiState(settings = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = GesturesUiState(),
        )

    fun onEvent(event: GesturesUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            when (event) {
                is GesturesUiEvent.SingleTapActionSelected -> settingsRepository.update {
                    it.copy(singleTapAction = event.action)
                }

                is GesturesUiEvent.DoubleTapActionSelected -> settingsRepository.update {
                    it.copy(doubleTapAction = event.action)
                }

                is GesturesUiEvent.LongPressActionSelected -> settingsRepository.update {
                    it.copy(longPressAction = event.action)
                }

                is GesturesUiEvent.DoubleTapTimeoutChanged -> settingsRepository.update {
                    it.withDoubleTapTimeout(event.millis.toLong())
                }

                is GesturesUiEvent.LongPressDurationChanged -> settingsRepository.update {
                    it.withLongPressDuration(event.millis.toLong())
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
