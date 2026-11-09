package com.ssajudn.dotap.presentation.onboarding

sealed interface OnboardingUiEffect {
    data object OpenAccessibilitySettings : OnboardingUiEffect

    data object NavigateToHome : OnboardingUiEffect
}
