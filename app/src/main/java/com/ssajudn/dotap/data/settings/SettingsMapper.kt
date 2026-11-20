package com.ssajudn.dotap.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.DotStyle
import com.ssajudn.dotap.domain.model.Limits
import com.ssajudn.dotap.domain.model.PanelSlot

/**
 * Translates between [Preferences] and [DotSettings]. Unknown or out-of-range stored values fall back
 * to defaults and are clamped through the domain model's `with...` methods.
 */
internal class SettingsMapper {
    fun toDomain(preferences: Preferences): DotSettings {
        val d = DotSettings.Default
        return DotSettings(
            isDotEnabled = preferences.value(SettingsKeys.DOT_ENABLED, d.isDotEnabled),
            dotSizeDp = preferences.value(SettingsKeys.DOT_SIZE_DP, d.dotSizeDp),
            idleOpacityPercent = preferences.value(SettingsKeys.IDLE_OPACITY_PCT, d.idleOpacityPercent),
            idleDelayMs = preferences.longValue(SettingsKeys.IDLE_DELAY_MS, d.idleDelayMs),
            doubleTapTimeoutMs = preferences.longValue(SettingsKeys.DOUBLE_TAP_TIMEOUT_MS, d.doubleTapTimeoutMs),
            longPressDurationMs = preferences.longValue(SettingsKeys.LONG_PRESS_MS, d.longPressDurationMs),
            singleTapAction = preferences[SettingsKeys.ACTION_SINGLE].toActionType(d.singleTapAction),
            doubleTapAction = preferences[SettingsKeys.ACTION_DOUBLE].toActionType(d.doubleTapAction),
            longPressAction = preferences[SettingsKeys.ACTION_LONG].toActionType(d.longPressAction),
            swipeUpAction = preferences[SettingsKeys.ACTION_SWIPE_UP].toActionType(d.swipeUpAction),
            swipeDownAction = preferences[SettingsKeys.ACTION_SWIPE_DOWN].toActionType(d.swipeDownAction),
            swipeLeftAction = preferences[SettingsKeys.ACTION_SWIPE_LEFT].toActionType(d.swipeLeftAction),
            swipeRightAction = preferences[SettingsKeys.ACTION_SWIPE_RIGHT].toActionType(d.swipeRightAction),
            singleTapAppPackage = preferences[SettingsKeys.SINGLE_TAP_APP]?.takeIf { it.isNotBlank() },
            doubleTapAppPackage = preferences[SettingsKeys.DOUBLE_TAP_APP]?.takeIf { it.isNotBlank() },
            longPressAppPackage = preferences[SettingsKeys.LONG_PRESS_APP]?.takeIf { it.isNotBlank() },
            panelSlots = preferences[SettingsKeys.PANEL_SLOTS].toPanelSlots(d.panelSlots),
            isHapticsEnabled = preferences.value(SettingsKeys.HAPTICS_ENABLED, d.isHapticsEnabled),
            isSnapToEdgeEnabled = preferences.value(SettingsKeys.SNAP_TO_EDGE, d.isSnapToEdgeEnabled),
            isRightSide = preferences.value(SettingsKeys.DOT_SIDE, SIDE_RIGHT) != SIDE_LEFT,
            yRatio = preferences.value(SettingsKeys.DOT_Y_RATIO, d.yRatio),
            isOnboardingCompleted = preferences.value(SettingsKeys.ONBOARDING_COMPLETED, d.isOnboardingCompleted),
            dotStyle = toDotStyle(preferences[SettingsKeys.DOT_STYLE], d.dotStyle),
            customDotImagePath = preferences[SettingsKeys.CUSTOM_DOT_IMAGE_PATH]?.takeIf { it.isNotBlank() },
        ).withClampedValues()
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
        preferences[SettingsKeys.ACTION_SWIPE_UP] = settings.swipeUpAction.name
        preferences[SettingsKeys.ACTION_SWIPE_DOWN] = settings.swipeDownAction.name
        preferences[SettingsKeys.ACTION_SWIPE_LEFT] = settings.swipeLeftAction.name
        preferences[SettingsKeys.ACTION_SWIPE_RIGHT] = settings.swipeRightAction.name
        writeOptionalString(preferences, SettingsKeys.SINGLE_TAP_APP, settings.singleTapAppPackage)
        writeOptionalString(preferences, SettingsKeys.DOUBLE_TAP_APP, settings.doubleTapAppPackage)
        writeOptionalString(preferences, SettingsKeys.LONG_PRESS_APP, settings.longPressAppPackage)
        preferences[SettingsKeys.PANEL_SLOTS] = settings.panelSlots.joinToString(separator = ",") { it.serialize() }
        preferences[SettingsKeys.HAPTICS_ENABLED] = settings.isHapticsEnabled
        preferences[SettingsKeys.SNAP_TO_EDGE] = settings.isSnapToEdgeEnabled
        preferences[SettingsKeys.DOT_SIDE] = if (settings.isRightSide) SIDE_RIGHT else SIDE_LEFT
        preferences[SettingsKeys.DOT_Y_RATIO] = settings.yRatio
        preferences[SettingsKeys.ONBOARDING_COMPLETED] = settings.isOnboardingCompleted
        preferences[SettingsKeys.DOT_STYLE] = settings.dotStyle.name
        writeOptionalString(preferences, SettingsKeys.CUSTOM_DOT_IMAGE_PATH, settings.customDotImagePath)
    }

    private fun writeOptionalString(
        preferences: MutablePreferences,
        key: Preferences.Key<String>,
        value: String?,
    ) {
        val cleaned = value?.trim()?.takeIf { it.isNotEmpty() }
        if (cleaned == null) {
            preferences.remove(key)
        } else {
            preferences[key] = cleaned
        }
    }

    private fun <T> Preferences.value(key: Preferences.Key<T>, fallback: T): T = this[key] ?: fallback

    private fun Preferences.longValue(
        key: Preferences.Key<Int>,
        fallback: Long,
    ): Long = value(key, fallback.toInt()).toLong()

    private fun String?.toActionType(fallback: ActionType): ActionType {
        val stored = this
        return ActionType.entries.firstOrNull { it.name == stored } ?: fallback
    }

    private fun String?.toPanelSlots(fallback: List<PanelSlot>): List<PanelSlot> {
        val parsed = this
            ?.split(',')
            ?.mapNotNull { token -> parsePanelSlot(token) }
            ?.filter { it.action.canBePanelSlot }
            .orEmpty()
        return if (parsed.size in Limits.MIN_PANEL_SLOTS..Limits.MAX_PANEL_SLOTS) parsed else fallback
    }

    private fun parsePanelSlot(token: String): PanelSlot? {
        val parts = token.split(SLOT_APP_SEPARATOR, limit = 2)
        val actionName = parts[0].trim()
        val action = ActionType.entries.firstOrNull { it.name == actionName } ?: return null
        val appPackage = if (action == ActionType.LAUNCH_APP) {
            parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        } else {
            null
        }
        return PanelSlot(action = action, appPackage = appPackage)
    }

    private fun PanelSlot.serialize(): String = if (action == ActionType.LAUNCH_APP && !appPackage.isNullOrBlank()) {
        "${action.name}$SLOT_APP_SEPARATOR$appPackage"
    } else {
        action.name
    }

    private companion object {
        const val SLOT_APP_SEPARATOR = ':'
    }
}

private fun toDotStyle(
    stored: String?,
    fallback: DotStyle,
): DotStyle = DotStyle.entries.firstOrNull { it.name == stored } ?: fallback
