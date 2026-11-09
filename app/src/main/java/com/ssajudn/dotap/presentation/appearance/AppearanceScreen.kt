package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppearanceScreen(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.appearance_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.back_button))
                    }
                },
            )
        },
    ) { innerPadding ->
        AppearanceBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@Composable
private fun AppearanceBody(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabeledSwitchRow(
            label = stringResource(R.string.dot_visibility_label),
            checked = state.settings.isDotEnabled,
            onCheckedChange = { onEvent(AppearanceUiEvent.DotEnabledToggled(it)) },
        )
        LabeledSwitchRow(
            label = stringResource(R.string.snap_to_edge_label),
            checked = state.settings.isSnapToEdgeEnabled,
            onCheckedChange = { onEvent(AppearanceUiEvent.SnapToEdgeToggled(it)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenPreview() {
    DotapTheme {
        AppearanceScreen(state = AppearanceUiState(isLoading = false), onEvent = {}, onBack = {})
    }
}
