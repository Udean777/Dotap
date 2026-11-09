package com.ssajudn.dotap
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.di.AppContainer
import com.ssajudn.dotap.presentation.navigation.Destinations
import com.ssajudn.dotap.presentation.navigation.DotapNavHost
import com.ssajudn.dotap.presentation.navigation.SystemActions
import com.ssajudn.dotap.presentation.theme.DotapTheme

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
    DotapTheme {
        DotapNavHost(
            factories = container.screenViewModelFactories,
            systemActions = systemActions,
            startDestination = startDestination,
        )
    }
}
