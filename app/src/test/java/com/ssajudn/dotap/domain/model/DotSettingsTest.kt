package com.ssajudn.dotap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DotSettingsTest {
    @Test
    fun defaultSettings_hasSensibleDefaults() {
        val settings = DotSettings.Default

        assertTrue(settings.isDotEnabled)
        assertEquals(Defaults.DOT_SIZE_DP, settings.dotSizeDp)
        assertEquals(Defaults.IDLE_OPACITY_PERCENT, settings.idleOpacityPercent)
        assertEquals(Defaults.DOUBLE_TAP_TIMEOUT_MS, settings.doubleTapTimeoutMs)
        assertEquals(Defaults.LONG_PRESS_DURATION_MS, settings.longPressDurationMs)
        assertEquals(ActionType.OPEN_PANEL, settings.singleTapAction)
        assertEquals(ActionType.SCREENSHOT, settings.doubleTapAction)
        assertEquals(ActionType.NONE, settings.longPressAction)
    }

    @Test
    fun idleAlpha_convertsPercentToFraction() {
        assertEquals(0.4f, DotSettings(idleOpacityPercent = 40).idleAlpha, 0.001f)
        assertEquals(1f, DotSettings(idleOpacityPercent = Limits.MAX_IDLE_OPACITY_PERCENT).idleAlpha, 0.001f)
    }

    @Test
    fun withDotSize_clampsWithinLimits() {
        val settings = DotSettings.Default

        val belowMin = settings.withDotSize(Limits.MIN_DOT_SIZE_DP - 10)
        assertEquals(Limits.MIN_DOT_SIZE_DP, belowMin.dotSizeDp)

        val aboveMax = settings.withDotSize(Limits.MAX_DOT_SIZE_DP + 10)
        assertEquals(Limits.MAX_DOT_SIZE_DP, aboveMax.dotSizeDp)

        val valid = settings.withDotSize(50)
        assertEquals(50, valid.dotSizeDp)
    }

    @Test
    fun withIdleOpacity_clampsWithinLimits() {
        val settings = DotSettings.Default

        val belowMin = settings.withIdleOpacity(Limits.MIN_IDLE_OPACITY_PERCENT - 5)
        assertEquals(Limits.MIN_IDLE_OPACITY_PERCENT, belowMin.idleOpacityPercent)

        val aboveMax = settings.withIdleOpacity(Limits.MAX_IDLE_OPACITY_PERCENT + 20)
        assertEquals(Limits.MAX_IDLE_OPACITY_PERCENT, aboveMax.idleOpacityPercent)

        val valid = settings.withIdleOpacity(60)
        assertEquals(60, valid.idleOpacityPercent)
    }

    @Test
    fun withDoubleTapTimeout_clampsWithinLimits() {
        val settings = DotSettings.Default

        val belowMin = settings.withDoubleTapTimeout(Limits.MIN_DOUBLE_TAP_TIMEOUT_MS - 50L)
        assertEquals(Limits.MIN_DOUBLE_TAP_TIMEOUT_MS, belowMin.doubleTapTimeoutMs)

        val aboveMax = settings.withDoubleTapTimeout(Limits.MAX_DOUBLE_TAP_TIMEOUT_MS + 200L)
        assertEquals(Limits.MAX_DOUBLE_TAP_TIMEOUT_MS, aboveMax.doubleTapTimeoutMs)

        val valid = settings.withDoubleTapTimeout(400L)
        assertEquals(400L, valid.doubleTapTimeoutMs)
    }

    @Test
    fun withLongPressDuration_clampsWithinLimits() {
        val settings = DotSettings.Default

        val belowMin = settings.withLongPressDuration(Limits.MIN_LONG_PRESS_DURATION_MS - 100L)
        assertEquals(Limits.MIN_LONG_PRESS_DURATION_MS, belowMin.longPressDurationMs)

        val aboveMax = settings.withLongPressDuration(Limits.MAX_LONG_PRESS_DURATION_MS + 500L)
        assertEquals(Limits.MAX_LONG_PRESS_DURATION_MS, aboveMax.longPressDurationMs)

        val valid = settings.withLongPressDuration(600L)
        assertEquals(600L, valid.longPressDurationMs)
    }

    @Test
    fun withYRatio_clampsWithinLimits() {
        val settings = DotSettings.Default

        val belowMin = settings.withYRatio(Limits.MIN_Y_RATIO - 0.1f)
        assertEquals(Limits.MIN_Y_RATIO, belowMin.yRatio, 0.001f)

        val aboveMax = settings.withYRatio(Limits.MAX_Y_RATIO + 0.1f)
        assertEquals(Limits.MAX_Y_RATIO, aboveMax.yRatio, 0.001f)

        val valid = settings.withYRatio(0.5f)
        assertEquals(0.5f, valid.yRatio, 0.001f)
    }
}
