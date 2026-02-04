package id.xms.xtrakernelmanager.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass
import id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeviceSilhouette(
    modifier: Modifier = Modifier,
    color: Color? = null,
    showWallpaper: Boolean = false,
    customSize: androidx.compose.ui.unit.DpSize? = null,
) {
  val dimens = rememberResponsiveDimens()

  // Scale size based on screen size
  val phoneWidth: Dp
  val phoneHeight: Dp
  val cornerRadius: Dp
  val innerCornerRadius: Dp
  val cameraSize: Dp
  val cameraPadding: Dp

  if (customSize != null) {
      phoneWidth = customSize.width
      phoneHeight = customSize.height
      // Scale other dims proportionally roughly based on width relative to base 76.dp
      val scale = phoneWidth.value / 76f
      cornerRadius = 14.dp * scale
      innerCornerRadius = 10.dp * scale
      cameraSize = 7.dp * scale
      cameraPadding = 5.dp * scale
  } else {
      when (dimens.screenSizeClass) {
        ScreenSizeClass.COMPACT -> {
          phoneWidth = 64.dp
          phoneHeight = 82.dp
          cornerRadius = 12.dp
          innerCornerRadius = 8.dp
          cameraSize = 6.dp
          cameraPadding = 4.dp
        }
        ScreenSizeClass.MEDIUM -> {
          phoneWidth = 76.dp
          phoneHeight = 97.dp
          cornerRadius = 14.dp
          innerCornerRadius = 10.dp
          cameraSize = 7.dp
          cameraPadding = 5.dp
        }
        ScreenSizeClass.EXPANDED -> {
          phoneWidth = 86.dp
          phoneHeight = 110.dp
          cornerRadius = 16.dp
          innerCornerRadius = 12.dp
          cameraSize = 8.dp
          cameraPadding = 6.dp
        }
        ScreenSizeClass.LARGE -> {
          phoneWidth = 96.dp
          phoneHeight = 124.dp
          cornerRadius = 18.dp
          innerCornerRadius = 14.dp
          cameraSize = 9.dp
          cameraPadding = 7.dp
        }
      }
  }

  var wallpaperBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  val context = androidx.compose.ui.platform.LocalContext.current

  LaunchedEffect(showWallpaper) {
    if (showWallpaper) {
      // Try to load system wallpaper
      val systemWallpaper = withContext(Dispatchers.IO) { loadWallpaperWithRoot() }

      wallpaperBitmap =
          if (systemWallpaper != null) {
            systemWallpaper
          } else {
            // Fallback to xms.jpeg drawable
            withContext(Dispatchers.IO) {
              try {
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeResource(
                        context.resources,
                        id.xms.xtrakernelmanager.R.drawable.xms,
                        options,
                    )
                    ?.asImageBitmap()
              } catch (e: Exception) {
                null
              }
            }
          }
    }
  }

  val frameColor = color ?: MaterialTheme.colorScheme.onSurfaceVariant
  val bezelColor = color ?: MaterialTheme.colorScheme.surfaceVariant
  val screenTopColor =
      color?.copy(alpha = 0.6f) ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
  val screenBottomColor =
      color?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)

  Box(
      modifier =
          modifier
              .width(phoneWidth)
              .height(phoneHeight)
              .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
              .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val cornerRadiusPx = cornerRadius.toPx()
                val inset = strokeWidth / 2

                drawRect(color = bezelColor, style = androidx.compose.ui.graphics.drawscope.Fill)

                drawPath(
                    path =
                        androidx.compose.ui.graphics.Path().apply {
                          moveTo(inset, size.height)
                          lineTo(inset, cornerRadiusPx)
                          arcTo(
                              rect =
                                  androidx.compose.ui.geometry.Rect(
                                      inset,
                                      inset,
                                      (cornerRadiusPx * 2) - inset,
                                      (cornerRadiusPx * 2) - inset,
                                  ),
                              startAngleDegrees = 180f,
                              sweepAngleDegrees = 90f,
                              forceMoveTo = false,
                          )
                          lineTo(size.width - cornerRadiusPx, inset)
                          arcTo(
                              rect =
                                  androidx.compose.ui.geometry.Rect(
                                      size.width - (cornerRadiusPx * 2) + inset,
                                      inset,
                                      size.width - inset,
                                      (cornerRadiusPx * 2) - inset,
                                  ),
                              startAngleDegrees = 270f,
                              sweepAngleDegrees = 90f,
                              forceMoveTo = false,
                          )
                          lineTo(size.width - inset, size.height)
                        },
                    color = frameColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                )
              }
              .padding(3.dp)
  ) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .clip(RoundedCornerShape(topStart = innerCornerRadius, topEnd = innerCornerRadius))
                .then(
                    if (wallpaperBitmap == null) {
                      Modifier.background(
                          brush =
                              Brush.linearGradient(
                                  colors = listOf(screenTopColor, screenBottomColor),
                                  start = androidx.compose.ui.geometry.Offset.Zero,
                                  end = androidx.compose.ui.geometry.Offset.Infinite,
                              )
                      )
                    } else Modifier
                )
    ) {
      wallpaperBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
      }

      Box(
          modifier =
              Modifier.align(Alignment.TopCenter)
                  .padding(top = cameraPadding)
                  .size(cameraSize)
                  .clip(CircleShape)
                  .background(Color.Black)
      )

      Box(
          modifier =
              Modifier.align(Alignment.TopEnd)
                  .fillMaxHeight()
                  .fillMaxWidth(0.5f)
                  .background(
                      brush =
                          Brush.horizontalGradient(
                              colors =
                                  listOf(
                                      Color.Transparent,
                                      MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                  )
                          )
                  )
      )
    }
  }
}

private val WALLPAPER_PATHS =
    listOf(
        "/data/system/users/0/wallpaper_screenshot",
        "/data/system/users/0/wallpaper_screenshot.png",
        "/data/system/users/0/wallpaper_screenshot.jpg",
        "/data/system/users/0/wallpaper",
        "/data/system/users/0/wallpaper.png",
        "/data/system/users/0/wallpaper.jpg",
        "/data/system/users/0/wallpaper_lock",
        "/data/system/users/0/wallpaper_orig",
        "/data/system/users/0/lock_wallpaper",
        "/data/system/users/0/wallpaper/wallpaper",
        "/data/system/users/0/home_wallpaper",
    )

private fun loadWallpaperWithRoot(): ImageBitmap? {
  return try {
    val tempFile = File.createTempFile("wallpaper", ".tmp")
    tempFile.deleteOnExit()

    val discoveredPath = discoverWallpaperFile()
    val pathsToTry = mutableListOf<String>()
    discoveredPath?.let { pathsToTry.add(it) }
    pathsToTry.addAll(WALLPAPER_PATHS)

    for (path in pathsToTry) {
      val result = Shell.cmd("cp '$path' '${tempFile.absolutePath}'").exec()

      if (result.isSuccess && tempFile.exists() && tempFile.length() > 0) {
        Shell.cmd("chmod 644 '${tempFile.absolutePath}'").exec()
        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath, options)

        if (bitmap != null) {
          tempFile.delete()
          return bitmap.asImageBitmap()
        }
      }
    }

    tempFile.delete()
    null
  } catch (e: Exception) {
    null
  }
}

private fun discoverWallpaperFile(): String? {
  return try {
    val result = Shell.cmd("ls -la /data/system/users/0/ | grep -i wallpaper").exec()

    if (result.isSuccess && result.out.isNotEmpty()) {
      for (line in result.out) {
        val parts = line.trim().split(Regex("\\s+"))
        if (parts.size >= 9) {
          val filename = parts.last()
          if (!line.startsWith("d") && !filename.endsWith(".xml")) {
            val fullPath = "/data/system/users/0/$filename"
            val sizeResult = Shell.cmd("stat -c%s '$fullPath' 2>/dev/null || echo 0").exec()
            val size = sizeResult.out.firstOrNull()?.toLongOrNull() ?: 0
            if (size > 1000) return fullPath
          }
        }
      }
    }
    null
  } catch (e: Exception) {
    null
  }
}
