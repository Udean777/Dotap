package com.ssajudn.dotap.presentation.common

import androidx.annotation.DrawableRes

data class NavigationItem(
    val title: String,
    val summary: String,
    @get:DrawableRes val iconRes: Int,
    val onClick: () -> Unit,
)
