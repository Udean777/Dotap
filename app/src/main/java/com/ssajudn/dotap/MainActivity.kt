package com.ssajudn.dotap

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ssajudn.dotap.presentation.home.HomeRoute
import com.ssajudn.dotap.presentation.home.HomeViewModel
import com.ssajudn.dotap.presentation.theme.DotapTheme

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels {
        (application as DotapApp).container.homeViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DotapTheme {
                HomeRoute(
                    viewModel = homeViewModel,
                    onNavigateToSettings = {
                        // Will navigate to settings screen in M3
                    },
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }
        }
    }
}
