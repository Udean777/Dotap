package com.ssajudn.dotap.data.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.ssajudn.dotap.domain.port.CustomDotImageStorage
import com.ssajudn.dotap.domain.port.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val CUSTOM_DOT_FILE_NAME = "custom_dot_icon.webp"
private const val MAX_CUSTOM_DOT_PX = 256
private const val WEBP_QUALITY = 90

internal class AndroidCustomDotImageStorage(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : CustomDotImageStorage {
    override suspend fun saveFromUri(uriString: String): String? = withContext(dispatchers.io) {
        val uri = runCatching { uriString.toUri() }.getOrNull() ?: return@withContext null
        val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return@withContext null

        val scaled = scaleDownIfNeeded(decoded)
        val outFile = File(context.filesDir, CUSTOM_DOT_FILE_NAME)
        val saved = runCatching {
            FileOutputStream(outFile).use { out ->
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                scaled.compress(format, WEBP_QUALITY, out)
            }
        }.getOrDefault(false)

        if (scaled !== decoded) {
            scaled.recycle()
        }
        decoded.recycle()

        if (saved) outFile.absolutePath else null
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDim = maxOf(bitmap.width, bitmap.height)
        if (maxDim <= MAX_CUSTOM_DOT_PX) return bitmap
        val ratio = MAX_CUSTOM_DOT_PX.toFloat() / maxDim.toFloat()
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return bitmap.scale(width, height)
    }
}
