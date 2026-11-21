package com.ssajudn.dotap.di

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssajudn.dotap.data.apps.PackageManagerInstalledAppsRepository
import com.ssajudn.dotap.data.settings.AndroidCustomDotImageStorage
import com.ssajudn.dotap.data.settings.DataStoreSettingsRepository
import com.ssajudn.dotap.data.settings.settingsDataStore
import com.ssajudn.dotap.data.status.AccessibilityStatusRepository
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.domain.usecase.ObserveServiceStatusUseCase
import com.ssajudn.dotap.presentation.appearance.AppearanceViewModel
import com.ssajudn.dotap.presentation.battery.BatteryGuidanceViewModel
import com.ssajudn.dotap.presentation.common.AppIconLoader
import com.ssajudn.dotap.presentation.gestures.GesturesViewModel
import com.ssajudn.dotap.presentation.home.HomeViewModel
import com.ssajudn.dotap.presentation.navigation.ScreenViewModelFactories
import com.ssajudn.dotap.presentation.onboarding.OnboardingViewModel
import com.ssajudn.dotap.presentation.panelsettings.PanelSettingsViewModel
import com.ssajudn.dotap.presentation.settings.SettingsViewModel

/**
 * Dependency container for application-level components.
 * Follows manual dependency injection rules without third-party frameworks.
 */
class AppContainer(
    val appContext: Context,
) {
    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    val serviceConnectionTracker: ServiceConnectionTracker by lazy {
        ServiceConnectionTracker()
    }

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(appContext.settingsDataStore, dispatcherProvider)
    }

    val installedAppsRepository: InstalledAppsRepository by lazy {
        PackageManagerInstalledAppsRepository(appContext, dispatcherProvider)
    }

    val appIconLoader: AppIconLoader by lazy {
        AppIconLoader(dispatcherProvider)
    }

    private val accessibilityStatusRepository: AccessibilityStatusRepository by lazy {
        AccessibilityStatusRepository(
            context = appContext,
            dispatcherProvider = dispatcherProvider,
            serviceConnectionTracker = serviceConnectionTracker,
        )
    }

    val observeServiceStatusUseCase: ObserveServiceStatusUseCase by lazy {
        ObserveServiceStatusUseCase(
            accessibilityStatusSource = accessibilityStatusRepository,
            serviceConnectionTracker = serviceConnectionTracker,
        )
    }

    private val onboardingViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                OnboardingViewModel(
                    settingsRepository = settingsRepository,
                    dispatcherProvider = dispatcherProvider,
                )
            }
        }

    private val homeViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                HomeViewModel(
                    dispatcherProvider = dispatcherProvider,
                    observeServiceStatusUseCase = observeServiceStatusUseCase,
                    settingsRepository = settingsRepository,
                )
            }
        }

    private val settingsViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    dispatcherProvider = dispatcherProvider,
                )
            }
        }

    private val gesturesViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                GesturesViewModel(
                    settingsRepository = settingsRepository,
                    installedAppsRepository = installedAppsRepository,
                    dispatcherProvider = dispatcherProvider,
                )
            }
        }

    private val appearanceViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                AppearanceViewModel(
                    settingsRepository = settingsRepository,
                    customDotImageStorage = AndroidCustomDotImageStorage(appContext, dispatcherProvider),
                    dispatcherProvider = dispatcherProvider,
                )
            }
        }

    private val panelSettingsViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                PanelSettingsViewModel(
                    settingsRepository = settingsRepository,
                    installedAppsRepository = installedAppsRepository,
                    dispatcherProvider = dispatcherProvider,
                )
            }
        }

    private val batteryGuidanceViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                BatteryGuidanceViewModel(
                    dispatcherProvider = dispatcherProvider,
                    deviceManufacturer = Build.MANUFACTURER,
                )
            }
        }

    val screenViewModelFactories: ScreenViewModelFactories = ScreenViewModelFactories(
        onboarding = onboardingViewModelFactory,
        home = homeViewModelFactory,
        settings = settingsViewModelFactory,
        gestures = gesturesViewModelFactory,
        appearance = appearanceViewModelFactory,
        panelSettings = panelSettingsViewModelFactory,
        batteryGuidance = batteryGuidanceViewModelFactory,
    )
}
