package com.ssajudn.dotap.domain.repository

import com.ssajudn.dotap.domain.model.DotSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<DotSettings>

    suspend fun update(transform: (DotSettings) -> DotSettings)
}
