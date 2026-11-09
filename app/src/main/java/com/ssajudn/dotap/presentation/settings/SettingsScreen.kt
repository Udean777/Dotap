package com.ssajudn.dotap.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = { onEvent(SettingsUiEvent.BackClicked) }) {
                        Text(text = stringResource(R.string.back_button))
                    }
                },
            )
        },
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
            .padding(innerPadding),
    ) {
        SectionHeader(text = stringResource(R.string.settings_feedback_section))
        LabeledSwitchRow(
            label = stringResource(R.string.haptics_label),
            checked = state.settings.isHapticsEnabled,
            onCheckedChange = { onEvent(SettingsUiEvent.HapticsToggled(it)) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        HorizontalDivider()
        SectionHeader(text = stringResource(R.string.settings_behavior_section))
        NavigationRow(
            title = stringResource(R.string.gestures_title),
            summary = stringResource(R.string.gestures_summary),
            onClick = { onEvent(SettingsUiEvent.OpenGesturesClicked) },
        )
        NavigationRow(
            title = stringResource(R.string.appearance_title),
            summary = stringResource(R.string.appearance_summary),
            onClick = { onEvent(SettingsUiEvent.OpenAppearanceClicked) },
        )
    }
}

@Composable
private fun NavigationRow(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = summary) },
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { heading() },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DotapTheme {
        SettingsScreen(state = SettingsUiState(isLoading = false), onEvent = {})
    }
}
