package com.ssajudn.dotap.domain.port

interface AppLauncher {
    fun launch(packageName: String): Boolean
}
