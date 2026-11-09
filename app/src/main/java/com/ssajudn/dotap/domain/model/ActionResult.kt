package com.ssajudn.dotap.domain.model

sealed interface ActionResult {
    data object Success : ActionResult

    data class Unavailable(
        val reason: String,
    ) : ActionResult

    data class Failed(
        val cause: Throwable? = null,
    ) : ActionResult
}
