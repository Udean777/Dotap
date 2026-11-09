package com.ssajudn.dotap.presentation.panel

sealed interface PanelUiEffect {
    data object Close : PanelUiEffect
}
