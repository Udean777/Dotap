package com.ssajudn.dotap.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.presentation.common.ExpressiveIconBadge
import com.ssajudn.dotap.presentation.common.ExpressiveSectionHeader
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
    HomeScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Scaffold(
        topBar = { HomeTopAppBar() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        HomeBody(state = state, onEvent = onEvent, innerPadding = innerPadding)
    }

    if (state.showRestrictedSettingsDialog) {
        RestrictedSettingsDialog(
            onDismiss = { onEvent(HomeUiEvent.DismissRestrictedSettingsDialog) },
            onOpenAppInfo = { onEvent(HomeUiEvent.OpenAppInfoClicked) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeroStatusCard(
            serviceStatus = state.serviceStatus,
            onEnableClicked = { onEvent(HomeUiEvent.OpenAccessibilitySettingsClicked) },
            onRestrictedHelpClicked = { onEvent(HomeUiEvent.RestrictedSettingsHelpClicked) },
        )
        ExpressiveSectionHeader(text = stringResource(R.string.home_section_controls))
        ControlsCard(
            isDotEnabled = state.isDotEnabled,
            onToggle = { onEvent(HomeUiEvent.DotVisibilityToggled(it)) },
        )
        ExpressiveSectionHeader(text = stringResource(R.string.home_section_customize))
        SettingsNavigationCard(onClick = { onEvent(HomeUiEvent.OpenSettingsClicked) })
    }
}

@Composable
private fun ControlsCard(
    isDotEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            LabeledSwitchRow(
                label = stringResource(R.string.dot_visibility_label),
                subtitle = stringResource(R.string.appearance_summary),
                checked = isDotEnabled,
                onCheckedChange = onToggle,
                leadingIcon = {
                    ExpressiveIconBadge(
                        iconRes = R.drawable.ic_tile_dot,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
            )
        }
    }
}

@Composable
private fun SettingsNavigationCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SettingsNavContent(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsNavContent(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        ExpressiveIconBadge(
            iconRes = R.drawable.ic_settings_appearance,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.gestures_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenConnectedPreview() {
    DotapTheme {
        HomeScreen(
            state = HomeUiState(serviceStatus = ServiceStatus.ENABLED_CONNECTED),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenDisabledPreview() {
    DotapTheme {
        HomeScreen(
            state = HomeUiState(serviceStatus = ServiceStatus.DISABLED),
            onEvent = {},
        )
    }
}
