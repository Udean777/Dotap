package com.ssajudn.dotap.data.status

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.domain.port.AccessibilityStatusSource
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.ServiceStatusRepository
import com.ssajudn.dotap.domain.status.ServiceConnectionTracker
import com.ssajudn.dotap.domain.usecase.ObserveServiceStatusUseCase
import com.ssajudn.dotap.system.service.DotapAccessibilityService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AccessibilityStatusRepository(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
    private val serviceConnectionTracker: ServiceConnectionTracker,
) : AccessibilityStatusSource,
    ServiceStatusRepository {

    override val isServiceEnabled: Flow<Boolean> = callbackFlow {
        val watcher = AccessibilityStatusWatcher(context) {
            trySend(checkServiceEnabled())
        }
        watcher.start()

        val trackerJob = serviceConnectionTracker.isConnected.onEach {
            trySend(checkServiceEnabled())
        }.launchIn(this)

        trySend(checkServiceEnabled())

        awaitClose {
            watcher.stop()
            trackerJob.cancel()
        }
    }.distinctUntilChanged().flowOn(dispatcherProvider.io)

    override val status: Flow<ServiceStatus> = ObserveServiceStatusUseCase(
        accessibilityStatusSource = this,
        serviceConnectionTracker = serviceConnectionTracker,
    ).invoke()

    private fun checkServiceEnabled(): Boolean = serviceConnectionTracker.isConnected.value ||
        isEnabledInSecureSettings() ||
        isEnabledInAccessibilityManager()

    private fun isEnabledInSecureSettings(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val expectedComponent = ComponentName(context, DotapAccessibilityService::class.java)
        val targets = setOf(expectedComponent.flattenToString(), expectedComponent.flattenToShortString())

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        var found = false
        while (splitter.hasNext() && !found) {
            val item = splitter.next()
            found = targets.any { it.equals(item, ignoreCase = true) }
        }
        return found
    }

    private fun isEnabledInAccessibilityManager(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val targetClass = DotapAccessibilityService::class.java.name
        return services.any {
            it.resolveInfo?.serviceInfo?.packageName == context.packageName &&
                it.resolveInfo?.serviceInfo?.name == targetClass
        }
    }
}
