package com.ssajudn.dotap.domain.port

import com.ssajudn.dotap.domain.model.ActionType

interface ActionPerformer {
    fun perform(action: ActionType): Boolean
}
