package com.ssajudn.dotap.presentation.home

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R

private const val DIALOG_CORNER_RADIUS_DP = 28
private const val BUTTON_CORNER_RADIUS_DP = 12

@Composable
fun RestrictedSettingsDialog(
    onDismiss: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(DIALOG_CORNER_RADIUS_DP.dp),
        title = { DialogTitle() },
        text = { DialogText() },
        confirmButton = { ConfirmButton(onOpenAppInfo) },
        dismissButton = { DismissButton(onDismiss) },
    )
}

@Composable
private fun DialogTitle() {
    Text(
        text = stringResource(R.string.restricted_settings_title),
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun DialogText() {
    Text(
        text = stringResource(R.string.restricted_settings_message),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ConfirmButton(onOpenAppInfo: () -> Unit) {
    Button(
        onClick = onOpenAppInfo,
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS_DP.dp),
    ) {
        Text(text = stringResource(R.string.open_app_info_button))
    }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit) {
    TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS_DP.dp),
    ) {
        Text(text = stringResource(R.string.close_dialog_button))
    }
}
