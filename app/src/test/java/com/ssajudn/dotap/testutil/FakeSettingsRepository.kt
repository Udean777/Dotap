package com.ssajudn.dotap.testutil

import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(
    initialSettings: DotSettings = DotSettings.Default,
) : SettingsRepository {
    private val state = MutableStateFlow(initialSettings)
    override val settings: Flow<DotSettings> = state.asStateFlow()

    val current: DotSettings get() = state.value

    override suspend fun update(transform: (DotSettings) -> DotSettings) {
        state.update(transform)
    }
}
