package com.ssajudn.dotap.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssajudn.dotap.data.settings.InMemorySettingsRepository
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.presentation.home.HomeViewModel

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
        InMemorySettingsRepository()
    }

    val homeViewModelFactory: ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                HomeViewModel(
                    dispatcherProvider = dispatcherProvider,
                    serviceConnectionTracker = serviceConnectionTracker,
                )
            }
        }
}
