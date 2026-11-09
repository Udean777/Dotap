package com.ssajudn.dotap.data.settings

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.Defaults
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsMapperTest {
    private val mapper = SettingsMapper()

    @Test
    fun `empty preferences produce defaults`() {
        assertEquals(DotSettings.Default, mapper.toDomain(mutablePreferencesOf()))
    }

    @Test
    fun `out of range values are clamped`() {
        val preferences = mutablePreferencesOf(
            SettingsKeys.DOT_SIZE_DP to 999,
            SettingsKeys.IDLE_OPACITY_PCT to 1,
            SettingsKeys.DOUBLE_TAP_TIMEOUT_MS to 5,
            SettingsKeys.LONG_PRESS_MS to 9_999,
            SettingsKeys.DOT_Y_RATIO to 2f,
        )

        val settings = mapper.toDomain(preferences)

        assertEquals(Limits.MAX_DOT_SIZE_DP, settings.dotSizeDp)
        assertEquals(Limits.MIN_IDLE_OPACITY_PERCENT, settings.idleOpacityPercent)
        assertEquals(Limits.MIN_DOUBLE_TAP_TIMEOUT_MS, settings.doubleTapTimeoutMs)
        assertEquals(Limits.MAX_LONG_PRESS_DURATION_MS, settings.longPressDurationMs)
        assertEquals(Limits.MAX_Y_RATIO, settings.yRatio)
    }

    @Test
    fun `unknown actions fall back to defaults`() {
        val preferences = mutablePreferencesOf(
            SettingsKeys.ACTION_SINGLE to "NOT_AN_ACTION",
            SettingsKeys.ACTION_DOUBLE to "",
            SettingsKeys.DOT_SIDE to "SOMEWHERE",
        )

        val settings = mapper.toDomain(preferences)

        assertEquals(Defaults.ACTION_SINGLE_TAP, settings.singleTapAction)
        assertEquals(Defaults.ACTION_DOUBLE_TAP, settings.doubleTapAction)
        assertEquals(true, settings.isRightSide)
    }

    @Test
    fun `panel slots are parsed when the size is valid`() {
        val preferences = mutablePreferencesOf(
            SettingsKeys.PANEL_SLOTS to "HOME,BACK,RECENTS",
        )

        assertEquals(
            listOf(ActionType.HOME, ActionType.BACK, ActionType.RECENTS),
            mapper.toDomain(preferences).panelSlots,
        )
    }

    @Test
    fun `panel slots with an invalid size fall back to defaults`() {
        val preferences = mutablePreferencesOf(
            SettingsKeys.PANEL_SLOTS to "HOME",
        )

        assertEquals(Defaults.DEFAULT_PANEL_SLOTS, mapper.toDomain(preferences).panelSlots)
    }

    @Test
    fun `write then read round trips all fields`() {
        val settings = DotSettings(
            isDotEnabled = false,
            dotSizeDp = 64,
            idleOpacityPercent = 70,
            idleDelayMs = 1_500L,
            doubleTapTimeoutMs = 450L,
            longPressDurationMs = 900L,
            singleTapAction = ActionType.BACK,
            doubleTapAction = ActionType.RECENTS,
            longPressAction = ActionType.NOTIFICATIONS,
            panelSlots = listOf(ActionType.HOME, ActionType.BACK, ActionType.RECENTS),
            isHapticsEnabled = false,
            isSnapToEdgeEnabled = false,
            isRightSide = false,
            yRatio = 0.75f,
        )
        val preferences = mutablePreferencesOf()

        mapper.write(settings, preferences)

        assertEquals(settings, mapper.toDomain(preferences))
    }
}
