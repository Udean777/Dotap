package com.ssajudn.dotap.data.status

import android.app.Activity
import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

internal class AccessibilityStatusWatcher(
    private val context: Context,
    private val onChange: () -> Unit,
) {
    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = onChange()
    }

    private val a11yListener = AccessibilityManager.AccessibilityStateChangeListener { onChange() }
    private var servicesListener: Any? = null
    private val lifecycleCallbacks = object : EmptyActivityLifecycleCallbacks() {
        override fun onActivityResumed(activity: Activity) = onChange()
    }

    fun start() {
        registerContentObservers()
        registerManagerListeners()
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    fun stop() {
        context.contentResolver.unregisterContentObserver(observer)
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        am?.removeAccessibilityStateChangeListener(a11yListener)
        unregisterServicesListener(am)
        (context.applicationContext as? Application)?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun registerContentObservers() {
        val resolver = context.contentResolver
        val servicesUri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val enabledUri = Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_ENABLED)
        resolver.registerContentObserver(servicesUri, false, observer)
        resolver.registerContentObserver(enabledUri, false, observer)
    }

    private fun registerManagerListeners() {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return
        am.addAccessibilityStateChangeListener(a11yListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val listener = AccessibilityManager.AccessibilityServicesStateChangeListener { onChange() }
            servicesListener = listener
            am.addAccessibilityServicesStateChangeListener(listener)
        }
    }

    private fun unregisterServicesListener(am: AccessibilityManager?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && am != null) {
            val listener = servicesListener as? AccessibilityManager.AccessibilityServicesStateChangeListener
            if (listener != null) {
                am.removeAccessibilityServicesStateChangeListener(listener)
            }
        }
    }
}

private open class EmptyActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
