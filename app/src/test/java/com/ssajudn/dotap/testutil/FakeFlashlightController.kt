package com.ssajudn.dotap.testutil

import com.ssajudn.dotap.domain.port.FlashlightController

class FakeFlashlightController(
    var shouldSucceed: Boolean = true,
) : FlashlightController {
    var toggleCount: Int = 0
        private set

    override fun toggle(): Boolean {
        toggleCount++
        return shouldSucceed
    }
}
