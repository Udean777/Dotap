package com.ssajudn.dotap.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeUiEffect.NavigateToSettings -> onNavigateToSettings()
                HomeUiEffect.OpenAccessibilitySettings -> onOpenAccessibilitySettings()
            }
        }
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
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val statusTextRes = if (serviceStatus == ServiceStatus.ENABLED_CONNECTED) {
                R.string.status_enabled
            } else {
                R.string.status_disabled
            }

            Text(
                text = stringResource(statusTextRes),
                style = MaterialTheme.typography.titleMedium,
            )

            if (serviceStatus != ServiceStatus.ENABLED_CONNECTED) {
                Button(
                    onClick = onEnableClicked,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.enable_service_button))
                }
            }
        }
    }
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
