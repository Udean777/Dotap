package com.ssajudn.dotap.system.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ssajudn.dotap.domain.port.FlashlightController

class AndroidFlashlightController(
    context: Context,
) : FlashlightController {

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    @Volatile
    private var isTorchEnabled: Boolean = false
    private var torchCameraId: String? = null
    private var isCallbackRegistered: Boolean = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                isTorchEnabled = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == torchCameraId) {
                isTorchEnabled = false
            }
        }
    }

    init {
        torchCameraId = findTorchCameraId()
        val manager = cameraManager
        if (manager != null && torchCameraId != null) {
            manager.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
            isCallbackRegistered = true
        }
    }

    override fun toggle(): Boolean {
        val manager = cameraManager
        val cameraId = torchCameraId ?: findTorchCameraId()?.also { torchCameraId = it }
        return if (manager != null && cameraId != null) {
            setTorchSafely(manager, cameraId, !isTorchEnabled)
        } else {
            false
        }
    }

    fun release() {
        if (isCallbackRegistered) {
            cameraManager?.unregisterTorchCallback(torchCallback)
            isCallbackRegistered = false
        }
    }

    private fun setTorchSafely(
        manager: CameraManager,
        cameraId: String,
        targetState: Boolean,
    ): Boolean = try {
        manager.setTorchMode(cameraId, targetState)
        isTorchEnabled = targetState
        true
    } catch (e: CameraAccessException) {
        Log.e(TAG, "CameraAccessException toggling torch", e)
        false
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "IllegalArgumentException toggling torch", e)
        false
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException toggling torch", e)
        false
    }

    private fun findTorchCameraId(): String? = try {
        val manager = cameraManager
        val ids = manager?.cameraIdList.orEmpty()
        if (manager == null) {
            null
        } else {
            ids.firstOrNull { id -> isBackCameraWithFlash(manager, id) }
                ?: ids.firstOrNull { id -> hasFlash(manager, id) }
        }
    } catch (e: CameraAccessException) {
        Log.e(TAG, "CameraAccessException finding torch camera", e)
        null
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException finding torch camera", e)
        null
    }

    private fun isBackCameraWithFlash(manager: CameraManager, id: String): Boolean {
        val chars = manager.getCameraCharacteristics(id)
        val facing = chars.get(CameraCharacteristics.LENS_FACING)
        val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        return facing == CameraCharacteristics.LENS_FACING_BACK && flashAvailable
    }

    private fun hasFlash(manager: CameraManager, id: String): Boolean {
        val chars = manager.getCameraCharacteristics(id)
        return chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    private companion object {
        const val TAG = "AndroidFlashlightCtrl"
    }
}
