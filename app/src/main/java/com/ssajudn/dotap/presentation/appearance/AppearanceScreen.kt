package com.ssajudn.dotap.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.theme.DotapTheme

private const val DIVIDER_ALPHA = 0.5f

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppearanceScreen(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@Composable
fun AppearanceScreen(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { AppearanceTopAppBar(onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AppearanceBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.appearance_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LiveDotPreviewCard(
            dotSizeDp = state.settings.dotSizeDp,
            idleOpacityPercent = state.settings.idleOpacityPercent,
        )
        ExpressiveSectionHeader(text = stringResource(R.string.appearance_behavior_section))
        BehaviorCard(settings = state.settings, onEvent = onEvent)
        ExpressiveSectionHeader(text = stringResource(R.string.appearance_dimensions_section))
        DimensionsCard(settings = state.settings, onEvent = onEvent)
    }
}

@Composable
private fun BehaviorCard(
    settings: DotSettings,
    onEvent: (AppearanceUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledSwitchRow(
                label = stringResource(R.string.dot_visibility_label),
                checked = settings.isDotEnabled,
                onCheckedChange = { onEvent(AppearanceUiEvent.DotEnabledToggled(it)) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA),
            )
            LabeledSwitchRow(
                label = stringResource(R.string.snap_to_edge_label),
                checked = settings.isSnapToEdgeEnabled,
                onCheckedChange = { onEvent(AppearanceUiEvent.SnapToEdgeToggled(it)) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenPreview() {
    DotapTheme {
        AppearanceScreen(state = AppearanceUiState(isLoading = false), onEvent = {}, onBack = {})
    }
}
