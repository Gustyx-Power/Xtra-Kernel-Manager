package id.xms.xtrakernelmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Fallback LightColorScheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Fallback DarkColorScheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun XtraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to be transparent for full edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            // Set navigation bar color to match the bottom navigation bar
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Ensure content draws behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Set status bar and navigation bar icon colors based on the chosen theme
            val isLight = !darkTheme // If dynamic colors are off, this relies on isSystemInDarkTheme
                                     // If dynamic colors are on, we should ideally check the luminance of the
                                     // actual dynamic background, but this is a good starting point.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            // For navigation bar, we need to check the luminance of the surface color
            val isNavigationBarLight = colorScheme.surface.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isNavigationBarLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Make sure Typography.kt is in this package or imported
        content = content
    )
}