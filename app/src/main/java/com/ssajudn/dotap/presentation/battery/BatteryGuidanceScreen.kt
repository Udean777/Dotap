package com.ssajudn.dotap.presentation.battery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.OemBrand
import com.ssajudn.dotap.presentation.theme.DotapTheme

@Composable
fun BatteryGuidanceRoute(
    viewModel: BatteryGuidanceViewModel,
    onEffect: (BatteryGuidanceUiEffect) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect(onEffect)
    }
    BatteryGuidanceScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun BatteryGuidanceScreen(
    state: BatteryGuidanceUiState,
    onEvent: (BatteryGuidanceUiEvent) -> Unit,
) {
    Scaffold(
        topBar = { BatteryTopAppBar(onBack = { onEvent(BatteryGuidanceUiEvent.BackClicked) }) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        BatteryGuidanceBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.battery_guidance_title),
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
private fun BatteryGuidanceBody(
    state: BatteryGuidanceUiState,
    onEvent: (BatteryGuidanceUiEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.battery_guidance_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BrandSelectorChips(
            selectedBrand = state.selectedBrand,
            currentBrand = state.currentBrand,
            onSelectBrand = { onEvent(BatteryGuidanceUiEvent.SelectBrand(it)) },
        )
        BrandStepsCard(
            brand = state.selectedBrand,
            onOpenBatterySettings = { onEvent(BatteryGuidanceUiEvent.OpenBatterySettingsClicked) },
            onOpenWebHelp = { onEvent(BatteryGuidanceUiEvent.OpenWebHelpClicked(state.selectedBrand.webHelpUrl)) },
        )
    }
}

@Composable
private fun BrandSelectorChips(
    selectedBrand: OemBrand,
    currentBrand: OemBrand,
    onSelectBrand: (OemBrand) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OemBrand.entries.forEach { brand ->
            val isCurrent = brand == currentBrand
            val suffix = if (isCurrent) " • " + stringResource(R.string.battery_detected_badge) else ""
            FilterChip(
                selected = brand == selectedBrand,
                onClick = { onSelectBrand(brand) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                label = { Text(text = stringResource(brand.labelResId()) + suffix) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BatteryGuidancePreview() {
    DotapTheme {
        BatteryGuidanceScreen(
            state = BatteryGuidanceUiState(
                currentBrand = OemBrand.XIAOMI,
                selectedBrand = OemBrand.XIAOMI,
            ),
            onEvent = {},
        )
    }
}
