package com.ssajudn.dotap.domain.port

import com.ssajudn.dotap.domain.model.MessageType

interface OverlayVisibility {
    fun hideForCapture()
    fun restoreAfterCapture()
    fun showMessage(message: MessageType)
}
