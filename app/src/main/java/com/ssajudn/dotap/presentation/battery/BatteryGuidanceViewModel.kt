package com.ssajudn.dotap.presentation.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.dotap.domain.model.OemBrand
import com.ssajudn.dotap.domain.port.DispatcherProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BatteryGuidanceViewModel(
    private val dispatcherProvider: DispatcherProvider,
    deviceManufacturer: String,
) : ViewModel() {
    private val detectedBrand = OemBrand.fromManufacturer(deviceManufacturer)
    private val _uiState = MutableStateFlow(
        BatteryGuidanceUiState(
            currentBrand = detectedBrand,
            selectedBrand = detectedBrand,
        ),
    )
    val uiState: StateFlow<BatteryGuidanceUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<BatteryGuidanceUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<BatteryGuidanceUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: BatteryGuidanceUiEvent) {
        viewModelScope.launch(dispatcherProvider.main) {
            handleEvent(event)
        }
    }

    private suspend fun handleEvent(event: BatteryGuidanceUiEvent) {
        when (event) {
            BatteryGuidanceUiEvent.BackClicked -> {
                _uiEffect.send(BatteryGuidanceUiEffect.NavigateBack)
            }

            is BatteryGuidanceUiEvent.SelectBrand -> {
                _uiState.update { it.copy(selectedBrand = event.brand) }
            }

            BatteryGuidanceUiEvent.OpenBatterySettingsClicked -> {
                _uiEffect.send(BatteryGuidanceUiEffect.OpenBatterySettings)
            }

            is BatteryGuidanceUiEvent.OpenWebHelpClicked -> {
                _uiEffect.send(BatteryGuidanceUiEffect.OpenWebHelp(event.url))
            }
        }
    }
}
