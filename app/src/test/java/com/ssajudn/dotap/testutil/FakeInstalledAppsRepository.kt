package com.ssajudn.dotap.testutil

import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.domain.repository.InstalledAppsRepository

class FakeInstalledAppsRepository(
    var apps: List<InstalledApp> = listOf(
        InstalledApp("com.android.camera2", "Camera"),
        InstalledApp("com.whatsapp", "WhatsApp"),
        InstalledApp("com.google.android.calculator", "Calculator"),
    ),
) : InstalledAppsRepository {
    override suspend fun getLaunchableApps(): List<InstalledApp> = apps
}
