package com.ssajudn.dotap.data.settings

import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsRepository(
    initialSettings: DotSettings = DotSettings(),
) : SettingsRepository {
    private val _settings = MutableStateFlow(initialSettings)
    override val settings: Flow<DotSettings> = _settings.asStateFlow()

    override suspend fun update(transform: (DotSettings) -> DotSettings) {
        _settings.update(transform)
    }
}
