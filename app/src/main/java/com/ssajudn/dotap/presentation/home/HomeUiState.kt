package com.ssajudn.dotap.presentation.home

import com.ssajudn.dotap.domain.model.ServiceStatus

data class HomeUiState(
    val serviceStatus: ServiceStatus = ServiceStatus.DISABLED,
)
