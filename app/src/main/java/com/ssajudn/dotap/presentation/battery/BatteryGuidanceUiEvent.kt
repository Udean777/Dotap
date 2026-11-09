package com.ssajudn.dotap.presentation.battery

import com.ssajudn.dotap.domain.model.OemBrand

sealed interface BatteryGuidanceUiEvent {
    data object BackClicked : BatteryGuidanceUiEvent

    data class SelectBrand(val brand: OemBrand) : BatteryGuidanceUiEvent

    data object OpenBatterySettingsClicked : BatteryGuidanceUiEvent

    data class OpenWebHelpClicked(val url: String) : BatteryGuidanceUiEvent
}
