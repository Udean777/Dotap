package com.ssajudn.dotap.domain.repository

import com.ssajudn.dotap.domain.model.InstalledApp

interface InstalledAppsRepository {
    suspend fun getLaunchableApps(): List<InstalledApp>
}
