package com.ssajudn.dotap.system.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ssajudn.dotap.domain.port.AppLauncher

class AndroidAppLauncher(
    private val context: Context,
) : AppLauncher {
    override fun launch(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            startSafely(launchIntent)
        } else {
            false
        }
    }

    private fun startSafely(intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "ActivityNotFoundException launching app", e)
        false
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException launching app", e)
        false
    }

    private companion object {
        const val TAG = "AndroidAppLauncher"
    }
}
