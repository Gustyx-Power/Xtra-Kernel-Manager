package id.xms.xtrakernelmanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Responsive Dimensions System for XKM
 * Automatically adjusts UI dimensions based on screen size
 */

enum class ScreenSizeClass {
    COMPACT,   // < 360dp (720p phones, small devices)
    MEDIUM,    // 360dp - 600dp (standard phones)
    EXPANDED   // > 600dp (tablets, foldables)
}

data class ResponsiveDimens(
    val screenSizeClass: ScreenSizeClass,
    
    // Padding
    val screenHorizontalPadding: Dp,
    val cardPadding: Dp,
    val cardPaddingSmall: Dp,
    val itemPadding: Dp,
    
    // Spacing
    val spacingLarge: Dp,
    val spacingMedium: Dp,
    val spacingSmall: Dp,
    val spacingTiny: Dp,
    
    // Corner Radius
    val cornerRadiusLarge: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusSmall: Dp,
    
    // Icon Sizes
    val iconSizeLarge: Dp,
    val iconSizeMedium: Dp,
    val iconSizeSmall: Dp,
    
    // Component Sizes
    val buttonHeight: Dp,
    val chipHeight: Dp,
    val avatarSizeLarge: Dp,
    val avatarSizeMedium: Dp,
    val avatarSizeSmall: Dp,
    
    // Typography Scale Factor
    val fontScale: Float,
)

// Compact: 720p phones, small screens (< 360dp)
private val CompactDimens = ResponsiveDimens(
    screenSizeClass = ScreenSizeClass.COMPACT,
    
    screenHorizontalPadding = 12.dp,
    cardPadding = 12.dp,
    cardPaddingSmall = 8.dp,
    itemPadding = 8.dp,
    
    spacingLarge = 12.dp,
    spacingMedium = 8.dp,
    spacingSmall = 4.dp,
    spacingTiny = 2.dp,
    
    cornerRadiusLarge = 16.dp,
    cornerRadiusMedium = 12.dp,
    cornerRadiusSmall = 8.dp,
    
    iconSizeLarge = 20.dp,
    iconSizeMedium = 18.dp,
    iconSizeSmall = 14.dp,
    
    buttonHeight = 40.dp,
    chipHeight = 28.dp,
    avatarSizeLarge = 56.dp,
    avatarSizeMedium = 40.dp,
    avatarSizeSmall = 28.dp,
    
    fontScale = 0.9f,
)

// Medium: Standard phones (360dp - 600dp)
private val MediumDimens = ResponsiveDimens(
    screenSizeClass = ScreenSizeClass.MEDIUM,
    
    screenHorizontalPadding = 16.dp,
    cardPadding = 16.dp,
    cardPaddingSmall = 12.dp,
    itemPadding = 12.dp,
    
    spacingLarge = 16.dp,
    spacingMedium = 12.dp,
    spacingSmall = 8.dp,
    spacingTiny = 4.dp,
    
    cornerRadiusLarge = 24.dp,
    cornerRadiusMedium = 16.dp,
    cornerRadiusSmall = 12.dp,
    
    iconSizeLarge = 24.dp,
    iconSizeMedium = 20.dp,
    iconSizeSmall = 16.dp,
    
    buttonHeight = 48.dp,
    chipHeight = 32.dp,
    avatarSizeLarge = 72.dp,
    avatarSizeMedium = 48.dp,
    avatarSizeSmall = 32.dp,
    
    fontScale = 1.0f,
)

// Expanded: Tablets, foldables (> 600dp)
private val ExpandedDimens = ResponsiveDimens(
    screenSizeClass = ScreenSizeClass.EXPANDED,
    
    screenHorizontalPadding = 24.dp,
    cardPadding = 24.dp,
    cardPaddingSmall = 16.dp,
    itemPadding = 16.dp,
    
    spacingLarge = 24.dp,
    spacingMedium = 16.dp,
    spacingSmall = 12.dp,
    spacingTiny = 8.dp,
    
    cornerRadiusLarge = 32.dp,
    cornerRadiusMedium = 24.dp,
    cornerRadiusSmall = 16.dp,
    
    iconSizeLarge = 28.dp,
    iconSizeMedium = 24.dp,
    iconSizeSmall = 20.dp,
    
    buttonHeight = 56.dp,
    chipHeight = 36.dp,
    avatarSizeLarge = 80.dp,
    avatarSizeMedium = 56.dp,
    avatarSizeSmall = 40.dp,
    
    fontScale = 1.0f,
)

@Composable
fun rememberResponsiveDimens(): ResponsiveDimens {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    return remember(screenWidthDp) {
        when {
            screenWidthDp < 360 -> CompactDimens
            screenWidthDp < 600 -> MediumDimens
            else -> ExpandedDimens
        }
    }
}

// Extension functions for scaled font sizes
fun ResponsiveDimens.scaledSp(baseSp: Float): TextUnit = (baseSp * fontScale).sp

// Quick access composable for common use
object Dimens {
    @Composable
    fun get(): ResponsiveDimens = rememberResponsiveDimens()
}
