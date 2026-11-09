package com.ssajudn.dotap.presentation.navigation

import androidx.lifecycle.ViewModelProvider

data class ScreenViewModelFactories(
    val onboarding: ViewModelProvider.Factory,
    val home: ViewModelProvider.Factory,
    val settings: ViewModelProvider.Factory,
    val gestures: ViewModelProvider.Factory,
    val appearance: ViewModelProvider.Factory,
    val batteryGuidance: ViewModelProvider.Factory,
)
