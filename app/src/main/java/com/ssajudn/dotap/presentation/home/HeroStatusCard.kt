package com.ssajudn.dotap.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.ServiceStatus
import com.ssajudn.dotap.presentation.common.ExpressiveIconBadge

private const val CONTAINER_ALPHA = 0.5f
private const val STATUS_ANIM_DURATION_MS = 300

@Composable
fun HeroStatusCard(
    serviceStatus: ServiceStatus,
    onEnableClicked: () -> Unit,
    onRestrictedHelpClicked: () -> Unit,
) {
    val isConnected = serviceStatus == ServiceStatus.ENABLED_CONNECTED
    val containerColor by heroContainerColor(isConnected = isConnected)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusHeader(serviceStatus = serviceStatus)
            StatusDescription(serviceStatus = serviceStatus)
            AnimatedActionButtons(
                visible = !isConnected,
                onEnableClicked = onEnableClicked,
                onRestrictedHelpClicked = onRestrictedHelpClicked,
            )
        }
    }
}

@Composable
private fun heroContainerColor(isConnected: Boolean): State<Color> {
    val target = if (isConnected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = CONTAINER_ALPHA)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    return animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = STATUS_ANIM_DURATION_MS),
        label = "HeroContainerColor",
    )
}

@Composable
private fun AnimatedActionButtons(
    visible: Boolean,
    onEnableClicked: () -> Unit,
    onRestrictedHelpClicked: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(STATUS_ANIM_DURATION_MS)) +
            fadeIn(animationSpec = tween(STATUS_ANIM_DURATION_MS)),
        exit = shrinkVertically(animationSpec = tween(STATUS_ANIM_DURATION_MS)) +
            fadeOut(animationSpec = tween(STATUS_ANIM_DURATION_MS)),
        label = "StatusActionsVisibility",
    ) {
        StatusActionButtons(
            onEnableClicked = onEnableClicked,
            onRestrictedHelpClicked = onRestrictedHelpClicked,
        )
    }
}

private data class StatusDisplay(
    val textRes: Int,
    val isConnected: Boolean,
    val iconRes: Int,
)

private fun resolveStatusDisplay(status: ServiceStatus): StatusDisplay = when (status) {
    ServiceStatus.ENABLED_CONNECTED -> StatusDisplay(
        textRes = R.string.status_enabled,
        isConnected = true,
        iconRes = R.drawable.ic_status_check,
    )

    ServiceStatus.ENABLED_NOT_CONNECTED -> StatusDisplay(
        textRes = R.string.status_enabled_not_connected,
        isConnected = false,
        iconRes = R.drawable.ic_status_warning,
    )

    ServiceStatus.DISABLED -> StatusDisplay(
        textRes = R.string.status_disabled,
        isConnected = false,
        iconRes = R.drawable.ic_status_warning,
    )
}

@Composable
private fun StatusHeader(serviceStatus: ServiceStatus) {
    StatusHeaderContent(serviceStatus = serviceStatus)
}

@Composable
private fun StatusHeaderContent(serviceStatus: ServiceStatus) {
    val info = resolveStatusDisplay(serviceStatus)
    val badgeColor = if (info.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val tintColor = if (info.isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExpressiveIconBadge(
            iconRes = info.iconRes,
            containerColor = badgeColor,
            iconTint = tintColor,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(info.textRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusDescription(serviceStatus: ServiceStatus) {
    val descRes = when (serviceStatus) {
        ServiceStatus.ENABLED_CONNECTED -> R.string.status_enabled_desc
        ServiceStatus.ENABLED_NOT_CONNECTED -> R.string.status_not_connected_desc
        ServiceStatus.DISABLED -> R.string.status_disabled_desc
    }
    Text(
        text = stringResource(descRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun StatusActionButtons(
    onEnableClicked: () -> Unit,
    onRestrictedHelpClicked: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onEnableClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.enable_service_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        OutlinedButton(
            onClick = onRestrictedHelpClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.restricted_settings_help_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
