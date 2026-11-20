package com.ssajudn.dotap.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.DotapTopAppBar
import com.ssajudn.dotap.presentation.common.ExpressiveIconBadge
import com.ssajudn.dotap.presentation.common.ExpressiveNavigationRow
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.common.NavigationItem
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onEffect: (SettingsUiEffect) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect(onEffect)
    }
    SettingsScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            DotapTopAppBar(
                title = stringResource(R.string.settings_title),
                onBack = { onEvent(SettingsUiEvent.BackClicked) },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        SettingsBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@Composable
private fun SettingsBody(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
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
        ExpressiveSectionHeader(text = stringResource(R.string.settings_feedback_section))
        FeedbackCard(
            isHapticsEnabled = state.settings.isHapticsEnabled,
            onHapticsToggled = { onEvent(SettingsUiEvent.HapticsToggled(it)) },
        )

        ExpressiveSectionHeader(text = stringResource(R.string.settings_behavior_section))
        BehaviorCard(items = behaviorNavigationItems(onEvent))

        ExpressiveSectionHeader(text = stringResource(R.string.settings_troubleshooting_section))
        TroubleshootingCard(
            onBatteryGuidanceClicked = { onEvent(SettingsUiEvent.OpenBatteryGuidanceClicked) },
        )
    }
}

@Composable
private fun behaviorNavigationItems(onEvent: (SettingsUiEvent) -> Unit): List<NavigationItem> = listOf(
    NavigationItem(
        title = stringResource(R.string.gestures_title),
        summary = stringResource(R.string.gestures_summary),
        iconRes = R.drawable.ic_settings_gestures,
        onClick = { onEvent(SettingsUiEvent.OpenGesturesClicked) },
    ),
    NavigationItem(
        title = stringResource(R.string.appearance_title),
        summary = stringResource(R.string.appearance_summary),
        iconRes = R.drawable.ic_settings_appearance,
        onClick = { onEvent(SettingsUiEvent.OpenAppearanceClicked) },
    ),
    NavigationItem(
        title = stringResource(R.string.panel_settings_title),
        summary = stringResource(R.string.panel_settings_summary),
        iconRes = R.drawable.ic_settings_panel,
        onClick = { onEvent(SettingsUiEvent.OpenPanelSettingsClicked) },
    ),
)

@Composable
private fun FeedbackCard(
    isHapticsEnabled: Boolean,
    onHapticsToggled: (Boolean) -> Unit,
) {
    DotapCard {
        Box(modifier = Modifier.padding(16.dp)) {
            LabeledSwitchRow(
                label = stringResource(R.string.haptics_label),
                checked = isHapticsEnabled,
                onCheckedChange = onHapticsToggled,
                leadingIcon = {
                    ExpressiveIconBadge(
                        iconRes = R.drawable.ic_settings_haptics,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
            )
        }
    }
}

@Composable
private fun BehaviorCard(items: List<NavigationItem>) {
    DotapCard {
        Column {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    DotapDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                ExpressiveNavigationRow(item = item)
            }
        }
    }
}

@Composable
private fun TroubleshootingCard(
    onBatteryGuidanceClicked: () -> Unit,
) {
    DotapCard {
        ExpressiveNavigationRow(
            item = NavigationItem(
                title = stringResource(R.string.battery_guidance_title),
                summary = stringResource(R.string.battery_guidance_summary),
                iconRes = R.drawable.ic_settings_battery,
                onClick = onBatteryGuidanceClicked,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DotapTheme {
        SettingsScreen(state = SettingsUiState(isLoading = false), onEvent = {})
    }
}
