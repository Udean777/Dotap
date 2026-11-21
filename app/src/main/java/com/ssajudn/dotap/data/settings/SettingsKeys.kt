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
    val ACTION_SWIPE_UP = stringPreferencesKey("action_swipe_up")
    val ACTION_SWIPE_DOWN = stringPreferencesKey("action_swipe_down")
    val ACTION_SWIPE_LEFT = stringPreferencesKey("action_swipe_left")
    val ACTION_SWIPE_RIGHT = stringPreferencesKey("action_swipe_right")
    val SINGLE_TAP_APP = stringPreferencesKey("single_tap_app")
    val DOUBLE_TAP_APP = stringPreferencesKey("double_tap_app")
    val LONG_PRESS_APP = stringPreferencesKey("long_press_app")
    val PANEL_SLOTS = stringPreferencesKey("panel_slots")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val SNAP_TO_EDGE = booleanPreferencesKey("snap_to_edge")
    val DOT_SIDE = stringPreferencesKey("dot_side")
    val DOT_Y_RATIO = floatPreferencesKey("dot_y_ratio")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val DOT_STYLE = stringPreferencesKey("dot_style")
    val CUSTOM_DOT_IMAGE_PATH = stringPreferencesKey("custom_dot_image_path")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val APP_LANGUAGE = stringPreferencesKey("app_language")
}

internal const val SIDE_RIGHT = "RIGHT"
internal const val SIDE_LEFT = "LEFT"
