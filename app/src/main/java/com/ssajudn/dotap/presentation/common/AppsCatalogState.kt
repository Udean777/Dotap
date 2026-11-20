package com.ssajudn.dotap.presentation.common

import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository

data class AppsCatalogState(
    val apps: List<InstalledApp> = emptyList(),
    val labels: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
)

suspend fun InstalledAppsRepository.loadCatalog(): AppsCatalogState {
    val apps = getLaunchableApps()
    return AppsCatalogState(
        apps = apps,
        labels = apps.associate { it.packageName to it.label },
        isLoading = false,
    )
}
