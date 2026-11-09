package com.ssajudn.dotap.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.port.DispatcherProvider
import com.ssajudn.dotap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val dispatcherProvider: DispatcherProvider,
) : SettingsRepository {
    private val mapper = SettingsMapper()

    override val settings: Flow<DotSettings> = dataStore.data
        .catch { error ->
            // A corrupted file must not crash the service; fall back to defaults.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(mapper::toDomain)
        .distinctUntilChanged()
        .flowOn(dispatcherProvider.io)

    override suspend fun update(transform: (DotSettings) -> DotSettings) {
        dataStore.edit { preferences ->
            mapper.write(transform(mapper.toDomain(preferences)), preferences)
        }
    }
}
