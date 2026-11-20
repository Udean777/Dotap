package com.ssajudn.dotap.presentation.appearance

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider
import com.ssajudn.dotap.presentation.common.DotapTopAppBar
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
import com.ssajudn.dotap.presentation.common.LabeledSwitchRow
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        viewModel.onEvent(AppearanceUiEvent.CustomImagePicked(uri?.toString()))
    }
    AppearanceScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onPickImageClick = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}

@Composable
fun AppearanceScreen(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    onBack: () -> Unit,
    onPickImageClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            DotapTopAppBar(
                title = stringResource(R.string.appearance_title),
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AppearanceBody(
            state = state,
            onEvent = onEvent,
            onPickImageClick = onPickImageClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun AppearanceBody(
    state: AppearanceUiState,
    onEvent: (AppearanceUiEvent) -> Unit,
    onPickImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LiveDotPreviewCard(
            dotSizeDp = state.settings.dotSizeDp,
            idleOpacityPercent = state.settings.idleOpacityPercent,
        )
        ExpressiveSectionHeader(text = stringResource(R.string.appearance_style_section))
        DotStyleCard(
            state = state,
            onStyleSelected = { onEvent(AppearanceUiEvent.DotStyleSelected(it)) },
            onPickImageClick = onPickImageClick,
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
    DotapCard {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledSwitchRow(
                label = stringResource(R.string.dot_visibility_label),
                checked = settings.isDotEnabled,
                onCheckedChange = { onEvent(AppearanceUiEvent.DotEnabledToggled(it)) },
            )
            DotapDivider(modifier = Modifier.padding(vertical = 8.dp))
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
