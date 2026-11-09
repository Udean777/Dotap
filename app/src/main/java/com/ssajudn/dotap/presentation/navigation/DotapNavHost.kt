package com.ssajudn.dotap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssajudn.dotap.presentation.appearance.AppearanceRoute
import com.ssajudn.dotap.presentation.appearance.AppearanceViewModel
import com.ssajudn.dotap.presentation.gestures.GesturesRoute
import com.ssajudn.dotap.presentation.gestures.GesturesViewModel
import com.ssajudn.dotap.presentation.home.HomeRoute
import com.ssajudn.dotap.presentation.home.HomeUiEffect
import com.ssajudn.dotap.presentation.home.HomeViewModel
import com.ssajudn.dotap.presentation.onboarding.OnboardingRoute
import com.ssajudn.dotap.presentation.onboarding.OnboardingViewModel
import com.ssajudn.dotap.presentation.settings.SettingsRoute
import com.ssajudn.dotap.presentation.settings.SettingsUiEffect
import com.ssajudn.dotap.presentation.settings.SettingsViewModel

@Composable
fun DotapNavHost(
    factories: ScreenViewModelFactories,
    systemActions: SystemActions,
    startDestination: String = Destinations.HOME,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        onboardingDestination(navController, factories.onboarding, systemActions.onOpenAccessibilitySettings)
        homeDestination(navController, factories.home, systemActions)
        settingsDestination(navController, factories.settings)
        gesturesDestination(navController, factories.gestures)
        appearanceDestination(navController, factories.appearance)
    }
}

private fun NavGraphBuilder.onboardingDestination(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    onOpenAccessibilitySettings: () -> Unit,
) {
    composable(Destinations.ONBOARDING) {
        OnboardingRoute(
            viewModel = viewModel<OnboardingViewModel>(factory = factory),
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onNavigateToHome = {
                navController.navigate(Destinations.HOME) {
                    popUpTo(Destinations.ONBOARDING) { inclusive = true }
                }
            },
        )
    }
}

private fun NavGraphBuilder.homeDestination(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    systemActions: SystemActions,
) {
    composable(Destinations.HOME) {
        HomeRoute(
            viewModel = viewModel(factory = factory),
            onEffect = { effect ->
                when (effect) {
                    HomeUiEffect.NavigateToSettings -> navController.navigate(Destinations.SETTINGS)
                    HomeUiEffect.OpenAccessibilitySettings -> systemActions.onOpenAccessibilitySettings()
                    HomeUiEffect.OpenAppInfo -> systemActions.onOpenAppInfo()
                }
            },
        )
    }
}

private fun NavGraphBuilder.settingsDestination(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
) {
    composable(Destinations.SETTINGS) {
        SettingsRoute(
            viewModel = viewModel<SettingsViewModel>(factory = factory),
            onEffect = { effect ->
                when (effect) {
                    SettingsUiEffect.NavigateToGestures -> navController.navigate(Destinations.GESTURES)
                    SettingsUiEffect.NavigateToAppearance -> navController.navigate(Destinations.APPEARANCE)
                    SettingsUiEffect.NavigateBack -> navController.popBackStack()
                }
            },
        )
    }
}

private fun NavGraphBuilder.gesturesDestination(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
) {
    composable(Destinations.GESTURES) {
        GesturesRoute(
            viewModel = viewModel<GesturesViewModel>(factory = factory),
            onBack = { navController.popBackStack() },
        )
    }
}

private fun NavGraphBuilder.appearanceDestination(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
) {
    composable(Destinations.APPEARANCE) {
        AppearanceRoute(
            viewModel = viewModel<AppearanceViewModel>(factory = factory),
            onBack = { navController.popBackStack() },
        )
    }
}
