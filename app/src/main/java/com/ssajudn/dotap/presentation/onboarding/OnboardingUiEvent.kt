package com.ssajudn.dotap.presentation.onboarding

sealed interface OnboardingUiEvent {
    data object GetStartedClicked : OnboardingUiEvent

    data object AgreeClicked : OnboardingUiEvent

    data object NotNowClicked : OnboardingUiEvent
}
