package com.ssajudn.dotap.testutil

import com.ssajudn.dotap.domain.port.AppLauncher

class FakeAppLauncher(
    var shouldSucceed: Boolean = true,
) : AppLauncher {
    val launchedPackages = mutableListOf<String>()

    override fun launch(packageName: String): Boolean {
        launchedPackages.add(packageName)
        return shouldSucceed
    }
}
