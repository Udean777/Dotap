package com.ssajudn.dotap.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object SettingsKeys {
    val DOT_ENABLED = booleanPreferencesKey("dot_enabled")
    val DOT_SIZE_DP = intPreferencesKey("dot_size_dp")
    val IDLE_OPACITY_PCT = intPreferencesKey("idle_opacity_pct")
    val IDLE_DELAY_MS = intPreferencesKey("idle_delay_ms")
    val DOUBLE_TAP_TIMEOUT_MS = intPreferencesKey("double_tap_timeout_ms")
    val LONG_PRESS_MS = intPreferencesKey("long_press_ms")
    val ACTION_SINGLE = stringPreferencesKey("action_single")
    val ACTION_DOUBLE = stringPreferencesKey("action_double")
    val ACTION_LONG = stringPreferencesKey("action_long")
    val PANEL_SLOTS = stringPreferencesKey("panel_slots")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val SNAP_TO_EDGE = booleanPreferencesKey("snap_to_edge")
    val DOT_SIDE = stringPreferencesKey("dot_side")
    val DOT_Y_RATIO = floatPreferencesKey("dot_y_ratio")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}

internal const val SIDE_RIGHT = "RIGHT"
internal const val SIDE_LEFT = "LEFT"
