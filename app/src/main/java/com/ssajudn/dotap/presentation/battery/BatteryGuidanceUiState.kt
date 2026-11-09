package com.ssajudn.dotap.presentation.battery

import com.ssajudn.dotap.domain.model.OemBrand

data class BatteryGuidanceUiState(
    val currentBrand: OemBrand = OemBrand.OTHER,
    val selectedBrand: OemBrand = OemBrand.OTHER,
)
