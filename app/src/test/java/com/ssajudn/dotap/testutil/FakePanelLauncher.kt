package com.ssajudn.dotap.testutil

import com.ssajudn.dotap.domain.port.PanelLauncher

class FakePanelLauncher : PanelLauncher {
    var openCount = 0
        private set
    var closeCount = 0
        private set

    override fun open() {
        openCount++
    }

    override fun close() {
        closeCount++
    }
}
