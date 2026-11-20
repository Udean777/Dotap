package com.ssajudn.dotap.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.withContext

class PackageManagerInstalledAppsRepository(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : InstalledAppsRepository {

    override suspend fun getLaunchableApps(): List<InstalledApp> = withContext(dispatcherProvider.io) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = queryLauncherActivities(packageManager, intent)
        val ownPackage = context.packageName
        resolveInfos
            .asSequence()
            .mapNotNull { info -> toInstalledApp(info, packageManager, ownPackage) }
            .distinctBy { it.packageName }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            .toList()
    }

    private fun queryLauncherActivities(
        packageManager: PackageManager,
        intent: Intent,
    ): List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0L),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
    }

    private fun toInstalledApp(
        info: ResolveInfo,
        packageManager: PackageManager,
        ownPackage: String,
    ): InstalledApp? {
        val packageName = info.activityInfo?.packageName
        return if (packageName == null || packageName == ownPackage) {
            null
        } else {
            val label = info.loadLabel(packageManager).toString().trim()
            InstalledApp(
                packageName = packageName,
                label = label.ifEmpty { packageName },
            )
        }
    }
}
