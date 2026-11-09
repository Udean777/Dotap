package com.ssajudn.dotap.domain.repository

import com.ssajudn.dotap.domain.model.ServiceStatus
import kotlinx.coroutines.flow.Flow

interface ServiceStatusRepository {
    val status: Flow<ServiceStatus>
}
