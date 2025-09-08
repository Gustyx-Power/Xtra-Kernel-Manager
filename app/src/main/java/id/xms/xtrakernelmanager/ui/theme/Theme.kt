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
            // Set navigation bar color to match the bottom navigation bar
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Ensure content draws behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)

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