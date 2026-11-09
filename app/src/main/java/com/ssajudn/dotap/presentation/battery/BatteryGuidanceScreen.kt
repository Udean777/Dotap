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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryGuidanceScreen(
    state: BatteryGuidanceUiState,
    onEvent: (BatteryGuidanceUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.battery_guidance_title)) },
                navigationIcon = {
                    TextButton(onClick = { onEvent(BatteryGuidanceUiEvent.BackClicked) }) {
                        Text(text = stringResource(R.string.back_button))
                    }
                },
            )
        },
    ) { innerPadding ->
        BatteryGuidanceBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OemBrand.entries.forEach { brand ->
            val isCurrent = brand == currentBrand
            val suffix = if (isCurrent) " • " + stringResource(R.string.battery_detected_badge) else ""
            FilterChip(
                selected = brand == selectedBrand,
                onClick = { onSelectBrand(brand) },
                label = { Text(text = stringResource(brand.labelResId()) + suffix) },
            )
        }
    }
}

@Composable
private fun BrandStepsCard(
    brand: OemBrand,
    onOpenBatterySettings: () -> Unit,
    onOpenWebHelp: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(brand.labelResId()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(brand.stepsResId()),
                style = MaterialTheme.typography.bodyMedium,
            )
            CardActions(
                onOpenBatterySettings = onOpenBatterySettings,
                onOpenWebHelp = onOpenWebHelp,
            )
        }
    }
}

@Composable
private fun CardActions(
    onOpenBatterySettings: () -> Unit,
    onOpenWebHelp: () -> Unit,
) {
    Button(
        onClick = onOpenBatterySettings,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.battery_open_settings_button))
    }
    OutlinedButton(
        onClick = onOpenWebHelp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.battery_view_online_guide))
    }
}

private fun OemBrand.labelResId(): Int = when (this) {
    OemBrand.XIAOMI -> R.string.oem_brand_xiaomi
    OemBrand.SAMSUNG -> R.string.oem_brand_samsung
    OemBrand.OPPO -> R.string.oem_brand_oppo
    OemBrand.VIVO -> R.string.oem_brand_vivo
    OemBrand.HUAWEI -> R.string.oem_brand_huawei
    OemBrand.OTHER -> R.string.oem_brand_other
}

private fun OemBrand.stepsResId(): Int = when (this) {
    OemBrand.XIAOMI -> R.string.battery_steps_xiaomi
    OemBrand.SAMSUNG -> R.string.battery_steps_samsung
    OemBrand.OPPO -> R.string.battery_steps_oppo
    OemBrand.VIVO -> R.string.battery_steps_vivo
    OemBrand.HUAWEI -> R.string.battery_steps_huawei
    OemBrand.OTHER -> R.string.battery_steps_other
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
