package com.ssajudn.dotap.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onEffect: (HomeUiEffect) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect(onEffect)
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.home_title)) },
            )
        },
    ) { innerPadding ->
        HomeBody(
            state = state,
            onEvent = onEvent,
            innerPadding = innerPadding,
        )
    }

    if (state.showRestrictedSettingsDialog) {
        RestrictedSettingsDialog(
            onDismiss = { onEvent(HomeUiEvent.DismissRestrictedSettingsDialog) },
            onOpenAppInfo = { onEvent(HomeUiEvent.OpenAppInfoClicked) },
        )
    }
}

@Composable
private fun HomeBody(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusCard(
            serviceStatus = state.serviceStatus,
            onEnableClicked = { onEvent(HomeUiEvent.OpenAccessibilitySettingsClicked) },
            onRestrictedHelpClicked = { onEvent(HomeUiEvent.RestrictedSettingsHelpClicked) },
        )

        LabeledSwitchRow(
            label = stringResource(R.string.dot_visibility_label),
            checked = state.isDotEnabled,
            onCheckedChange = { onEvent(HomeUiEvent.DotVisibilityToggled(it)) },
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onEvent(HomeUiEvent.OpenSettingsClicked) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.settings_button))
        }
    }
}

@Composable
private fun StatusCard(
    serviceStatus: ServiceStatus,
    onEnableClicked: () -> Unit,
    onRestrictedHelpClicked: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val statusTextRes = when (serviceStatus) {
                ServiceStatus.ENABLED_CONNECTED -> R.string.status_enabled
                ServiceStatus.ENABLED_NOT_CONNECTED -> R.string.status_enabled_not_connected
                ServiceStatus.DISABLED -> R.string.status_disabled
            }
            Text(
                text = stringResource(statusTextRes),
                style = MaterialTheme.typography.titleMedium,
            )
            if (serviceStatus != ServiceStatus.ENABLED_CONNECTED) {
                StatusActionButtons(
                    onEnableClicked = onEnableClicked,
                    onRestrictedHelpClicked = onRestrictedHelpClicked,
                )
            }
        }
    }
}

@Composable
private fun StatusActionButtons(
    onEnableClicked: () -> Unit,
    onRestrictedHelpClicked: () -> Unit,
) {
    Button(
        onClick = onEnableClicked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.enable_service_button))
    }
    TextButton(
        onClick = onRestrictedHelpClicked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.restricted_settings_help_button))
    }
}

@Composable
private fun RestrictedSettingsDialog(
    onDismiss: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.restricted_settings_title)) },
        text = { Text(text = stringResource(R.string.restricted_settings_message)) },
        confirmButton = {
            Button(onClick = onOpenAppInfo) {
                Text(text = stringResource(R.string.open_app_info_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close_dialog_button))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DotapTheme {
        HomeScreen(
            state = HomeUiState(serviceStatus = ServiceStatus.DISABLED),
            onEvent = {},
        )
    }
}
