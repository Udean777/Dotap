package com.ssajudn.dotap.presentation.battery

sealed interface BatteryGuidanceUiEffect {
    data object NavigateBack : BatteryGuidanceUiEffect

    data object OpenBatterySettings : BatteryGuidanceUiEffect

    data class OpenWebHelp(val url: String) : BatteryGuidanceUiEffect
}
