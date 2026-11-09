package com.ssajudn.dotap.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.Limits

/**
 * Translates between [Preferences] and [DotSettings]. Unknown or out-of-range stored values fall back
 * to defaults and are clamped through the domain model's `with...` methods.
 */
internal class SettingsMapper {
    fun toDomain(preferences: Preferences): DotSettings {
        val defaults = DotSettings.Default
        val stored = DotSettings(
            isDotEnabled = preferences.value(SettingsKeys.DOT_ENABLED, defaults.isDotEnabled),
            dotSizeDp = preferences.value(SettingsKeys.DOT_SIZE_DP, defaults.dotSizeDp),
            idleOpacityPercent = preferences.value(SettingsKeys.IDLE_OPACITY_PCT, defaults.idleOpacityPercent),
            idleDelayMs = preferences.value(SettingsKeys.IDLE_DELAY_MS, defaults.idleDelayMs.toInt()).toLong(),
            doubleTapTimeoutMs = preferences.value(
                SettingsKeys.DOUBLE_TAP_TIMEOUT_MS,
                defaults.doubleTapTimeoutMs.toInt(),
            ).toLong(),
            longPressDurationMs = preferences.value(
                SettingsKeys.LONG_PRESS_MS,
                defaults.longPressDurationMs.toInt(),
            ).toLong(),
            singleTapAction = preferences[SettingsKeys.ACTION_SINGLE].toActionType(defaults.singleTapAction),
            doubleTapAction = preferences[SettingsKeys.ACTION_DOUBLE].toActionType(defaults.doubleTapAction),
            longPressAction = preferences[SettingsKeys.ACTION_LONG].toActionType(defaults.longPressAction),
            panelSlots = preferences[SettingsKeys.PANEL_SLOTS].toPanelSlots(defaults.panelSlots),
            isHapticsEnabled = preferences.value(SettingsKeys.HAPTICS_ENABLED, defaults.isHapticsEnabled),
            isSnapToEdgeEnabled = preferences.value(SettingsKeys.SNAP_TO_EDGE, defaults.isSnapToEdgeEnabled),
            isRightSide = preferences.value(SettingsKeys.DOT_SIDE, SIDE_RIGHT) != SIDE_LEFT,
            yRatio = preferences.value(SettingsKeys.DOT_Y_RATIO, defaults.yRatio),
        )
        return stored.withClampedValues()
    }

    private fun DotSettings.withClampedValues(): DotSettings = this
        .withDotSize(dotSizeDp)
        .withIdleOpacity(idleOpacityPercent)
        .withDoubleTapTimeout(doubleTapTimeoutMs)
        .withLongPressDuration(longPressDurationMs)
        .withYRatio(yRatio)

    fun write(settings: DotSettings, preferences: MutablePreferences) {
        preferences[SettingsKeys.DOT_ENABLED] = settings.isDotEnabled
        preferences[SettingsKeys.DOT_SIZE_DP] = settings.dotSizeDp
        preferences[SettingsKeys.IDLE_OPACITY_PCT] = settings.idleOpacityPercent
        preferences[SettingsKeys.IDLE_DELAY_MS] = settings.idleDelayMs.toInt()
        preferences[SettingsKeys.DOUBLE_TAP_TIMEOUT_MS] = settings.doubleTapTimeoutMs.toInt()
        preferences[SettingsKeys.LONG_PRESS_MS] = settings.longPressDurationMs.toInt()
        preferences[SettingsKeys.ACTION_SINGLE] = settings.singleTapAction.name
        preferences[SettingsKeys.ACTION_DOUBLE] = settings.doubleTapAction.name
        preferences[SettingsKeys.ACTION_LONG] = settings.longPressAction.name
        preferences[SettingsKeys.PANEL_SLOTS] = settings.panelSlots.joinToString(separator = ",") { it.name }
        preferences[SettingsKeys.HAPTICS_ENABLED] = settings.isHapticsEnabled
        preferences[SettingsKeys.SNAP_TO_EDGE] = settings.isSnapToEdgeEnabled
        preferences[SettingsKeys.DOT_SIDE] = if (settings.isRightSide) SIDE_RIGHT else SIDE_LEFT
        preferences[SettingsKeys.DOT_Y_RATIO] = settings.yRatio
    }

    private fun <T> Preferences.value(key: Preferences.Key<T>, fallback: T): T = this[key] ?: fallback

    private fun String?.toActionType(fallback: ActionType): ActionType {
        val stored = this
        return ActionType.entries.firstOrNull { it.name == stored } ?: fallback
    }

    private fun String?.toPanelSlots(fallback: List<ActionType>): List<ActionType> {
        val parsed = this
            ?.split(',')
            ?.mapNotNull { name ->
                ActionType.entries.firstOrNull { it.name == name.trim() }
            }
            .orEmpty()
        return if (parsed.size in Limits.MIN_PANEL_SLOTS..Limits.MAX_PANEL_SLOTS) parsed else fallback
    }
}
