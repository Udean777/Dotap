package com.ssajudn.dotap.presentation.common

data class StepSliderSpec(
    val label: String,
    val valueText: String,
    val value: Int,
    val min: Int,
    val max: Int,
    val step: Int,
) {
    val steps: Int = (max - min) / step - 1
}
