package com.ssajudn.dotap.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.InstalledApp
import com.ssajudn.dotap.presentation.common.AppShortcutIcon
import com.ssajudn.dotap.presentation.common.DotapEntranceContainer
import com.ssajudn.dotap.presentation.common.bounceOnPress

@Composable
fun AppSearchOverlayContent(
    apps: List<InstalledApp>,
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .padding(horizontal = 24.dp, vertical = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        DotapEntranceContainer {
            AppSearchCard(apps = apps, onAppSelected = onAppSelected)
        }
    }
}

@Composable
private fun AppSearchCard(
    apps: List<InstalledApp>,
    onAppSelected: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) apps else apps.filter { it.label.contains(trimmed, ignoreCase = true) }
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchInputField(query = query, onQueryChange = { query = it }, focusRequester = focusRequester)
            AppSearchResults(filtered = filtered, onAppSelected = onAppSelected)
        }
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.app_search_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun AppSearchResults(
    filtered: List<InstalledApp>,
    onAppSelected: (String) -> Unit,
) {
    if (filtered.isEmpty()) {
        Text(
            text = stringResource(R.string.app_search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    } else {
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                AppSearchResultRow(app = app, onClick = { onAppSelected(app.packageName) })
            }
        }
    }
}

@Composable
private fun AppSearchResultRow(
    app: InstalledApp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceOnPress(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppShortcutIcon(
            packageName = app.packageName,
            contentDescription = app.label,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
