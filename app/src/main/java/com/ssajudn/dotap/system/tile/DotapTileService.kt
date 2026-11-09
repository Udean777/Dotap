package com.ssajudn.dotap.system.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.ssajudn.dotap.DotapApp
import com.ssajudn.dotap.MainActivity
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.DotSettings
import com.ssajudn.dotap.domain.model.ServiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DotapTileService : TileService() {
    private var listeningScope: CoroutineScope? = null
    private var currentStatus: ServiceStatus = ServiceStatus.DISABLED
    private var currentSettings: DotSettings? = null

    override fun onStartListening() {
        super.onStartListening()
        val app = application as? DotapApp ?: return
        val container = app.container
        val scope = CoroutineScope(SupervisorJob() + container.dispatcherProvider.main)
        listeningScope = scope

        scope.launch {
            combine(
                container.observeServiceStatusUseCase(),
                container.settingsRepository.settings,
                ::Pair,
            ).collect { (status, settings) ->
                currentStatus = status
                currentSettings = settings
                updateTileState(status, settings.isDotEnabled)
            }
        }
    }

    override fun onStopListening() {
        listeningScope?.cancel()
        listeningScope = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val app = application as? DotapApp ?: return
        if (currentStatus == ServiceStatus.ENABLED_CONNECTED) {
            val scope = listeningScope ?: CoroutineScope(
                SupervisorJob() + app.container.dispatcherProvider.main,
            )
            scope.launch {
                app.container.settingsRepository.update { current ->
                    current.copy(isDotEnabled = !current.isDotEnabled)
                }
            }
        } else {
            launchMainActivity()
        }
    }

    private fun updateTileState(status: ServiceStatus, isDotEnabled: Boolean) {
        val tile = qsTile ?: return
        when (status) {
            ServiceStatus.ENABLED_CONNECTED -> {
                tile.state = if (isDotEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                setTileSubtitle(tile, if (isDotEnabled) R.string.tile_state_active else R.string.tile_state_inactive)
            }

            ServiceStatus.ENABLED_NOT_CONNECTED,
            ServiceStatus.DISABLED,
            -> {
                tile.state = Tile.STATE_UNAVAILABLE
                setTileSubtitle(tile, R.string.tile_state_disabled)
            }
        }
        tile.updateTile()
    }

    private fun setTileSubtitle(tile: Tile, textRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(textRes)
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivity(intent)
        }
    }
}
