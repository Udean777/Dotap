package com.ssajudn.dotap.system.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.presentation.common.AppIconLoader
import com.ssajudn.dotap.presentation.common.LocalAppIconLoader
import com.ssajudn.dotap.presentation.search.AppSearchOverlayContent
import com.ssajudn.dotap.presentation.theme.DotapTheme

private const val BLUR_RADIUS_PX = 40
private const val DIM_AMOUNT = 0.45f

internal class AppSearchHost(
    private val context: Context,
    private val windowManager: WindowManager,
    private val appIconLoader: AppIconLoader,
) {
    private var view: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    val isShowing: Boolean get() = view != null

    fun show(
        apps: List<InstalledApp>,
        onAppSelected: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        if (view != null) return
        val owner = OverlayLifecycleOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setOnKeyListener { _, keyCode, event ->
                val isBack = keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
                if (isBack) onDismiss()
                isBack
            }
            setContent {
                CompositionLocalProvider(LocalAppIconLoader provides appIconLoader) {
                    DotapTheme {
                        AppSearchOverlayContent(apps, onAppSelected, onDismiss)
                    }
                }
            }
        }
        owner.start()
        val added = runCatching { windowManager.addView(composeView, createLayoutParams()) }.isSuccess
        if (added) {
            lifecycleOwner = owner
            view = composeView
        } else {
            owner.destroy()
        }
    }

    fun hide() {
        val currentView = view ?: return
        view = null
        currentView.disposeComposition()
        runCatching { windowManager.removeViewImmediate(currentView) }
        lifecycleOwner?.destroy()
        lifecycleOwner = null
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            baseFlags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        } else {
            baseFlags
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            dimAmount = DIM_AMOUNT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurBehindRadius = BLUR_RADIUS_PX
            }
        }
    }
}
