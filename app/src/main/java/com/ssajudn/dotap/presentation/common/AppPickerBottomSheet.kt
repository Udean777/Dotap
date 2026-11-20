package com.ssajudn.dotap.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.InstalledApp

private const val SHEET_MAX_LIST_HEIGHT_DP = 360
private const val SEARCH_FIELD_CORNER_DP = 12
private const val PICKER_ICON_SIZE_DP = 36

data class AppPickerState(
    val apps: List<InstalledApp> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPackage: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(
    state: AppPickerState,
    onSelect: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        AppPickerContent(state = state, onSelect = onSelect)
    }
}

@Composable
private fun AppPickerContent(
    state: AppPickerState,
    onSelect: (InstalledApp) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(state.apps, query) { filterApps(state.apps, query) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.app_picker_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(text = stringResource(R.string.app_picker_search_hint)) },
            singleLine = true,
            shape = RoundedCornerShape(SEARCH_FIELD_CORNER_DP.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        AppPickerList(state = state, filteredApps = filteredApps, onSelect = onSelect)
    }
}

private fun filterApps(apps: List<InstalledApp>, query: String): List<InstalledApp> {
    val needle = query.trim()
    if (needle.isEmpty()) return apps
    return apps.filter {
        it.label.contains(needle, ignoreCase = true) ||
            it.packageName.contains(needle, ignoreCase = true)
    }
}

@Composable
private fun AppPickerList(
    state: AppPickerState,
    filteredApps: List<InstalledApp>,
    onSelect: (InstalledApp) -> Unit,
) {
    when {
        state.isLoading -> AppPickerLoadingIndicator()

        filteredApps.isEmpty() -> Text(
            text = stringResource(R.string.app_picker_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 24.dp),
        )

        else -> LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = SHEET_MAX_LIST_HEIGHT_DP.dp),
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppPickerRow(
                    app = app,
                    isSelected = app.packageName == state.selectedPackage,
                    onClick = { onSelect(app) },
                )
                DotapDivider()
            }
        }
    }
}

@Composable
private fun AppPickerLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppPickerRow(
    app: InstalledApp,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppShortcutIcon(
            packageName = app.packageName,
            contentDescription = null,
            modifier = Modifier.size(PICKER_ICON_SIZE_DP.dp),
        )
        AppPickerLabels(app = app, modifier = Modifier.weight(1f))
        RadioButton(selected = isSelected, onClick = onClick)
    }
}

@Composable
private fun AppPickerLabels(
    app: InstalledApp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = app.label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
