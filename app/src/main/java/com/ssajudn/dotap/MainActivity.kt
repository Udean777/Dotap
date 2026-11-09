package com.ssajudn.dotap

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssajudn.dotap.presentation.navigation.DotapNavHost
import com.ssajudn.dotap.presentation.theme.DotapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factories = (application as DotapApp).container.screenViewModelFactories
        setContent {
            DotapTheme {
                DotapNavHost(
                    factories = factories,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }
        }
    }
}
