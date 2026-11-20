package com.ssajudn.dotap.presentation.panelsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ActionType
import com.ssajudn.dotap.presentation.common.AppPickerBottomSheet
import com.ssajudn.dotap.presentation.common.AppPickerState
import com.ssajudn.dotap.presentation.common.DotapTopAppBar
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun PanelSettingsRoute(
    viewModel: PanelSettingsViewModel,
    onEffect: (PanelSettingsUiEffect) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect(onEffect)
    }
    PanelSettingsScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun PanelSettingsScreen(
    state: PanelSettingsUiState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            DotapTopAppBar(
                title = stringResource(R.string.panel_settings_title),
                onBack = { onEvent(PanelSettingsUiEvent.BackClicked) },
                actions = {
                    TextButton(onClick = { onEvent(PanelSettingsUiEvent.ResetDefaultsClicked) }) {
                        Text(text = stringResource(R.string.panel_settings_reset))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        PanelSettingsBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }

    PanelSettingsAppPicker(state = state, onEvent = onEvent)
}

@Composable
private fun PanelSettingsAppPicker(
    state: PanelSettingsUiState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
) {
    val target = state.appPickerTarget ?: return
    val selectedPkg = (target as? PanelAppPickerTarget.ExistingSlot)?.currentPackage
    AppPickerBottomSheet(
        state = AppPickerState(
            apps = state.installedApps,
            isLoading = state.isLoadingApps,
            selectedPackage = selectedPkg,
        ),
        onSelect = { onEvent(PanelSettingsUiEvent.AppPicked(it.packageName)) },
        onDismiss = { onEvent(PanelSettingsUiEvent.DismissAppPicker) },
    )
}

@Composable
private fun PanelSettingsBody(
    state: PanelSettingsUiState,
    onEvent: (PanelSettingsUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PanelPreviewCard(slots = state.slots, appLabels = state.appLabels)
        ExpressiveSectionHeader(text = stringResource(R.string.panel_active_slots_section))
        ActiveSlotsCard(state = state, onEvent = onEvent)
        ExpressiveSectionHeader(text = stringResource(R.string.panel_available_actions_section))
        AvailableActionsCard(
            actions = state.availableActions,
            canAddMore = state.canAddMore,
            onAdd = { onEvent(PanelSettingsUiEvent.AddSlot(it)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PanelSettingsScreenPreview() {
    DotapTheme {
        PanelSettingsScreen(
            state = PanelSettingsUiState(
                availableActions = listOf(ActionType.POWER_DIALOG, ActionType.LOCK_SCREEN, ActionType.LAUNCH_APP),
                isLoading = false,
            ),
            onEvent = {},
        )
    }
}
