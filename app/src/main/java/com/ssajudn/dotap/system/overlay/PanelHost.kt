package com.ssajudn.dotap.system.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.ssajudn.dotap.domain.port.PanelLauncher
import com.ssajudn.dotap.presentation.panel.PanelDimensions

/**
 * Shows the quick-action panel as an accessibility overlay window and owns its lifecycle.
 *
 * [open] and [close] are idempotent so rapid repeated taps cannot stack windows or leak views.
 * Both must be called on the main thread.
 */
class PanelHost(
    private val context: Context,
    private val anchorProvider: () -> View?,
    private val content: @Composable (onClose: () -> Unit) -> Unit,
) : PanelLauncher {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var shownPanel: ShownPanel? = null

    override fun open() {
        if (shownPanel != null) return
        val anchor = anchorProvider()?.screenBounds() ?: return

        val owner = OverlayLifecycleOwner()
        val view = createComposeView(owner)
        owner.start()

        if (attach(view, buildLayoutParams(anchor))) {
            view.requestFocus()
            shownPanel = ShownPanel(view, owner)
        } else {
            owner.destroy()
        }
    }

    override fun close() {
        val panel = shownPanel ?: return
        shownPanel = null
        try {
            windowManager.removeView(panel.view)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException removing panel view", e)
        } finally {
            panel.owner.destroy()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createComposeView(owner: OverlayLifecycleOwner): ComposeView = ComposeView(context).apply {
        owner.attachTo(this)
        isFocusable = true
        isFocusableInTouchMode = true
        setContent { content(::close) }
        // The window is not touch-modal, so taps outside it arrive as ACTION_OUTSIDE and dismiss the panel.
        setOnTouchListener { _, event ->
            val isOutside = event.action == MotionEvent.ACTION_OUTSIDE
            if (isOutside) close()
            isOutside
        }
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                close()
                true
            } else {
                false
            }
        }
    }

    private fun attach(view: ComposeView, params: WindowManager.LayoutParams): Boolean = try {
        windowManager.addView(view, params)
        true
    } catch (e: WindowManager.BadTokenException) {
        Log.e(TAG, "BadTokenException adding panel view", e)
        false
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "IllegalArgumentException adding panel view", e)
        false
    }

    private fun buildLayoutParams(anchor: PanelPlacement.Bounds): WindowManager.LayoutParams {
        val density = context.resources.displayMetrics.density
        val sidePx = (PanelDimensions.SIDE_DP * density).toInt()
        val screen = getScreenSize(windowManager)
        val placement = PanelPlacement(
            panel = PanelPlacement.Size(width = sidePx, height = sidePx),
            screen = PanelPlacement.Size(width = screen.x, height = screen.y),
            gap = (PanelDimensions.ANCHOR_GAP_DP * density).toInt(),
        )
        val position = placement.positionFor(anchor)
        return WindowManager.LayoutParams(
            sidePx,
            sidePx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = position.x
            y = position.y
        }
    }

    private class ShownPanel(
        val view: ComposeView,
        val owner: OverlayLifecycleOwner,
    )

    // Both overlays use TYPE_ACCESSIBILITY_OVERLAY with FLAG_LAYOUT_NO_LIMITS, so the view's
    // on-screen origin equals its window position.
    private fun View.screenBounds(): PanelPlacement.Bounds {
        val origin = IntArray(2)
        getLocationOnScreen(origin)
        return PanelPlacement.Bounds(
            left = origin[0],
            top = origin[1],
            right = origin[0] + width,
            bottom = origin[1] + height,
        )
    }

    private companion object {
        const val TAG = "PanelHost"
    }
}
