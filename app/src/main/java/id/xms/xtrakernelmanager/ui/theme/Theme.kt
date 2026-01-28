package id.xms.xtrakernelmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF8AB4F8),
        onPrimary = Color(0xFF003258),
        primaryContainer = Color(0xFF004A77),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = Color(0xFFB8C8DA),
        onSecondary = Color(0xFF23323F),
        secondaryContainer = Color(0xFF394857),
        onSecondaryContainer = Color(0xFFD4E3F6),
        tertiary = Color(0xFFD5BEE4),
        onTertiary = Color(0xFF3A2948),
        tertiaryContainer = Color(0xFF51405F),
        onTertiaryContainer = Color(0xFFF2DAFF),
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onError = Color(0xFF690005),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A1C1E),
        onBackground = Color(0xFFE2E2E6),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C7CF),
        outline = Color(0xFF8D9199),
        outlineVariant = Color(0xFF43474E),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF0061A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD1E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF535F70),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD7E3F7),
        onSecondaryContainer = Color(0xFF101C2B),
        tertiary = Color(0xFF6B5778),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2DAFF),
        onTertiaryContainer = Color(0xFF251432),
        error = Color(0xFFBA1A1A),
        errorContainer = Color(0xFFFFDAD6),
        onError = Color(0xFFFFFFFF),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFDFCFF),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFDFCFF),
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFDFE2EB),
        onSurfaceVariant = Color(0xFF43474E),
        outline = Color(0xFF73777F),
        outlineVariant = Color(0xFFC3C7CF),
    )

@Composable
fun XtraKernelManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = Color.Transparent.toArgb()
      window.navigationBarColor = Color.Transparent.toArgb()

      // Don't set appearance since we're hiding the status bar
      // The custom status bar will handle the display
      WindowCompat.setDecorFitsSystemWindows(window, false)
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
