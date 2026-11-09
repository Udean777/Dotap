package com.ssajudn.dotap.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.testutil.TestDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreSettingsRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        repository = DataStoreSettingsRepository(dataStore, TestDispatcherProvider())
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `settings start from defaults`() = runTest {
        assertEquals(DotSettings.Default, repository.settings.first())
    }

    @Test
    fun `update persists and emits the new settings`() = runTest {
        repository.update { it.copy(doubleTapAction = ActionType.BACK, isHapticsEnabled = false) }

        val settings = repository.settings.first()

        assertEquals(ActionType.BACK, settings.doubleTapAction)
        assertEquals(false, settings.isHapticsEnabled)
    }

    @Test
    fun `stored out of range values are clamped`() = runTest {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.DOT_SIZE_DP] = 500
            preferences[SettingsKeys.DOT_Y_RATIO] = 9f
        }

        val settings = repository.settings.first()

        assertEquals(Limits.MAX_DOT_SIZE_DP, settings.dotSizeDp)
        assertEquals(Limits.MAX_Y_RATIO, settings.yRatio)
    }

    @Test
    fun `update preserves fields it does not touch`() = runTest {
        repository.update { it.copy(isSnapToEdgeEnabled = false) }
        repository.update { it.copy(yRatio = 0.6f) }

        val settings = repository.settings.first()

        assertEquals(false, settings.isSnapToEdgeEnabled)
        assertEquals(0.6f, settings.yRatio)
    }
}
