package com.ssajudn.dotap.presentation.common

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.port.DispatcherProvider
import kotlinx.coroutines.withContext

private const val ICON_CACHE_MAX_ENTRIES = 64
private const val ICON_DECODE_SIZE_DP = 48
private const val MIN_ICON_PX = 1

val LocalAppIconLoader = staticCompositionLocalOf<AppIconLoader?> { null }

class AppIconLoader(
    private val dispatcherProvider: DispatcherProvider,
) {
    private val cache = LruCache<String, ImageBitmap>(ICON_CACHE_MAX_ENTRIES)

    fun cached(packageName: String, targetPx: Int): ImageBitmap? = cache.get(cacheKey(packageName, targetPx))

    suspend fun load(
        context: Context,
        packageName: String,
        targetPx: Int,
    ): ImageBitmap? {
        val key = cacheKey(packageName, targetPx)
        cache.get(key)?.let { return it }
        val loaded = withContext(dispatcherProvider.io) {
            decodeAppIconBitmap(context, packageName, targetPx)
        }
        if (loaded != null) {
            cache.put(key, loaded)
        }
        return loaded
    }

    private fun cacheKey(packageName: String, targetPx: Int): String = "$packageName:$targetPx"
}

@Composable
fun AppShortcutIcon(
    packageName: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val bitmap = rememberAppIconBitmap(packageName)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_panel_launch_app),
            contentDescription = contentDescription,
            tint = fallbackTint,
            modifier = modifier,
        )
    }
}

@Composable
private fun rememberAppIconBitmap(packageName: String?): ImageBitmap? {
    val loader = LocalAppIconLoader.current
    if (packageName.isNullOrBlank() || loader == null || LocalInspectionMode.current) {
        return null
    }
    val context = LocalContext.current
    val targetPx = with(LocalDensity.current) {
        ICON_DECODE_SIZE_DP.dp.roundToPx().coerceAtLeast(MIN_ICON_PX)
    }
    val cached = loader.cached(packageName, targetPx)
    val iconState by produceState(initialValue = cached, loader, packageName, targetPx) {
        if (value == null) {
            value = loader.load(context, packageName, targetPx)
        }
    }
    return iconState
}

private fun decodeAppIconBitmap(
    context: Context,
    packageName: String,
    targetPx: Int,
): ImageBitmap? = try {
    val drawable = context.packageManager.getApplicationIcon(packageName)
    drawable.toBitmap(width = targetPx, height = targetPx).asImageBitmap()
} catch (_: PackageManager.NameNotFoundException) {
    null
} catch (_: Resources.NotFoundException) {
    null
}
