package com.ssajudn.dotap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ssajudn.dotap.di.AppContainer
import com.ssajudn.dotap.domain.model.AppLanguage
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.ThemeMode
import com.ssajudn.dotap.presentation.common.LocalAppIconLoader
import com.ssajudn.dotap.presentation.navigation.Destinations
import com.ssajudn.dotap.presentation.navigation.DotapNavHost
import com.ssajudn.dotap.presentation.navigation.SystemActions
import com.ssajudn.dotap.presentation.theme.DotapTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DotapApp
        setContentRoot(app.container)
    }

    private fun setContentRoot(container: AppContainer) {
        val systemActions = SystemActions(
            onOpenAccessibilitySettings = {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onOpenAppInfo = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null),
                )
                startActivity(intent)
            },
            onOpenBatterySettings = {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            },
            onOpenWebUrl = { url ->
                startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            },
        )
        setContent {
            MainAppContent(container, systemActions)
        }
    }
}

@Composable
private fun MainAppContent(
    container: AppContainer,
    systemActions: SystemActions,
) {
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
    val current = settings ?: return
    val startDestination = if (current.isOnboardingCompleted) {
        Destinations.HOME
    } else {
        Destinations.ONBOARDING
    }
    CompositionLocalProvider(LocalAppIconLoader provides container.appIconLoader) {
        val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
            initialValue = DotSettings.Default,
        )
        val darkTheme = settings.themeMode == ThemeMode.DARK ||
            (settings.themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme())
        val navController = rememberNavController()
        val localizedContext = rememberLocalizedContext(settings.appLanguage)
        val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        SyncSystemBars(darkTheme)
        Crossfade(targetState = localizedContext, label = "languageCrossfade") { ctx ->
            LocalizedDotapContent(ctx, activityResultRegistryOwner) {
                DotapTheme(darkTheme = darkTheme) {
                    DotapNavHost(container.screenViewModelFactories, systemActions, startDestination, navController)
                }
            }
        }
    }
}

@Composable
private fun LocalizedDotapContent(
    localizedContext: Context,
    activityResultRegistryOwner: androidx.activity.result.ActivityResultRegistryOwner?,
    content: @Composable () -> Unit,
) {
    if (activityResultRegistryOwner != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedContext.resources.configuration,
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
            content = content,
        )
    } else {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedContext.resources.configuration,
            content = content,
        )
    }
}

@Composable
private fun rememberLocalizedContext(appLanguage: AppLanguage): Context {
    val baseContext = LocalContext.current
    val baseConfig = LocalConfiguration.current
    return remember(appLanguage, baseConfig) {
        val tag = appLanguage.tag
        if (tag == null) {
            baseContext
        } else {
            val config = Configuration(baseConfig).apply { setLocale(Locale.forLanguageTag(tag)) }
            baseContext.createConfigurationContext(config)
        }
    }
}

@Composable
private fun SyncSystemBars(darkTheme: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
}
