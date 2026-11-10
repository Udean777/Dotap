package com.ssajudn.dotap.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.presentation.theme.DotapTheme

private val MinRowHeight = 56.dp

/**
 * The whole row is the toggle target, so TalkBack reads the label together with the switch state
 * instead of announcing an unlabeled switch.
 */
@Composable
fun LabeledSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = MinRowHeight)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwitchRowContent(
            label = label,
            subtitle = subtitle,
            leadingIcon = leadingIcon,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun SwitchRowContent(
    label: String,
    subtitle: String?,
    leadingIcon: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (leadingIcon != null) {
            Box(contentAlignment = Alignment.Center) { leadingIcon() }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledSwitchRowPreview() {
    DotapTheme {
        LabeledSwitchRow(
            label = "Show floating dot",
            subtitle = "Keep dot visible on screen",
            checked = true,
            onCheckedChange = {},
        )
    }
}
