package id.xms.xtrakernelmanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive Dimensions System for XKM Uses screen width in dp AND density to properly scale UI
 *
 * Detection based on actual screen width:
 * - COMPACT: <= 392dp (covers 720p/HD phones with high density)
 * - MEDIUM: 393dp - 600dp (standard 1080p/FHD phones)
 * - EXPANDED: > 600dp (tablets, foldables)
 */
enum class ScreenSizeClass {
  COMPACT, // Small phones, 720p, high density devices
  MEDIUM, // Standard phones 1080p
  EXPANDED, // Tablets, foldables
}

data class ResponsiveDimens(
    val screenSizeClass: ScreenSizeClass,
    val screenWidthDp: Int,
    val scaleFactor: Float,

    // Padding - scaled based on screen width
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

@Composable
fun rememberResponsiveDimens(): ResponsiveDimens {
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val screenWidthDp = configuration.screenWidthDp
  val screenHeightDp = configuration.screenHeightDp
  val densityDpi = configuration.densityDpi

  return remember(screenWidthDp, densityDpi) {
    // Calculate scale factor based on screen width
    // Reference: 400dp = 1.0f scale (typical modern phone)
    val scaleFactor = (screenWidthDp / 400f).coerceIn(0.7f, 1.3f)

    // Determine screen class
    val screenClass =
        when {
          screenWidthDp <= 392 -> ScreenSizeClass.COMPACT
          screenWidthDp <= 600 -> ScreenSizeClass.MEDIUM
          else -> ScreenSizeClass.EXPANDED
        }

    // Create dimensions scaled by factor
    createScaledDimens(screenClass, screenWidthDp, scaleFactor)
  }
}

private fun createScaledDimens(
    screenClass: ScreenSizeClass,
    screenWidthDp: Int,
    scaleFactor: Float,
): ResponsiveDimens {
  // Base values that will be scaled
  return when (screenClass) {
    ScreenSizeClass.COMPACT ->
        ResponsiveDimens(
            screenSizeClass = screenClass,
            screenWidthDp = screenWidthDp,
            scaleFactor = scaleFactor,

            // Very tight padding for small screens
            screenHorizontalPadding = (10f * scaleFactor).dp,
            cardPadding = (10f * scaleFactor).dp,
            cardPaddingSmall = (6f * scaleFactor).dp,
            itemPadding = (6f * scaleFactor).dp,
            spacingLarge = (10f * scaleFactor).dp,
            spacingMedium = (6f * scaleFactor).dp,
            spacingSmall = (4f * scaleFactor).dp,
            spacingTiny = (2f * scaleFactor).dp,
            cornerRadiusLarge = (14f * scaleFactor).dp,
            cornerRadiusMedium = (10f * scaleFactor).dp,
            cornerRadiusSmall = (6f * scaleFactor).dp,
            iconSizeLarge = (18f * scaleFactor).dp,
            iconSizeMedium = (16f * scaleFactor).dp,
            iconSizeSmall = (12f * scaleFactor).dp,
            buttonHeight = (36f * scaleFactor).dp,
            chipHeight = (24f * scaleFactor).dp,
            avatarSizeLarge = (48f * scaleFactor).dp,
            avatarSizeMedium = (36f * scaleFactor).dp,
            avatarSizeSmall = (24f * scaleFactor).dp,
            fontScale = 0.85f * scaleFactor,
        )

    ScreenSizeClass.MEDIUM ->
        ResponsiveDimens(
            screenSizeClass = screenClass,
            screenWidthDp = screenWidthDp,
            scaleFactor = scaleFactor,
            screenHorizontalPadding = (16f * scaleFactor).dp,
            cardPadding = (14f * scaleFactor).dp,
            cardPaddingSmall = (10f * scaleFactor).dp,
            itemPadding = (10f * scaleFactor).dp,
            spacingLarge = (14f * scaleFactor).dp,
            spacingMedium = (10f * scaleFactor).dp,
            spacingSmall = (6f * scaleFactor).dp,
            spacingTiny = (3f * scaleFactor).dp,
            cornerRadiusLarge = (20f * scaleFactor).dp,
            cornerRadiusMedium = (14f * scaleFactor).dp,
            cornerRadiusSmall = (10f * scaleFactor).dp,
            iconSizeLarge = (22f * scaleFactor).dp,
            iconSizeMedium = (18f * scaleFactor).dp,
            iconSizeSmall = (14f * scaleFactor).dp,
            buttonHeight = (44f * scaleFactor).dp,
            chipHeight = (28f * scaleFactor).dp,
            avatarSizeLarge = (64f * scaleFactor).dp,
            avatarSizeMedium = (44f * scaleFactor).dp,
            avatarSizeSmall = (28f * scaleFactor).dp,
            fontScale = 0.95f * scaleFactor,
        )

    ScreenSizeClass.EXPANDED ->
        ResponsiveDimens(
            screenSizeClass = screenClass,
            screenWidthDp = screenWidthDp,
            scaleFactor = scaleFactor,
            screenHorizontalPadding = (24f * scaleFactor).dp,
            cardPadding = (20f * scaleFactor).dp,
            cardPaddingSmall = (14f * scaleFactor).dp,
            itemPadding = (14f * scaleFactor).dp,
            spacingLarge = (20f * scaleFactor).dp,
            spacingMedium = (14f * scaleFactor).dp,
            spacingSmall = (10f * scaleFactor).dp,
            spacingTiny = (6f * scaleFactor).dp,
            cornerRadiusLarge = (28f * scaleFactor).dp,
            cornerRadiusMedium = (20f * scaleFactor).dp,
            cornerRadiusSmall = (14f * scaleFactor).dp,
            iconSizeLarge = (26f * scaleFactor).dp,
            iconSizeMedium = (22f * scaleFactor).dp,
            iconSizeSmall = (18f * scaleFactor).dp,
            buttonHeight = (52f * scaleFactor).dp,
            chipHeight = (34f * scaleFactor).dp,
            avatarSizeLarge = (76f * scaleFactor).dp,
            avatarSizeMedium = (52f * scaleFactor).dp,
            avatarSizeSmall = (36f * scaleFactor).dp,
            fontScale = 1.0f,
        )
  }
}

// Extension to calculate percentage of screen width
@Composable
fun screenWidthFraction(fraction: Float): Dp {
  val configuration = LocalConfiguration.current
  return (configuration.screenWidthDp * fraction).dp
}

// Extension functions for scaled font sizes
fun ResponsiveDimens.scaledSp(baseSp: Float): TextUnit = (baseSp * fontScale).sp

// Quick access composable for common use
object Dimens {
  @Composable fun get(): ResponsiveDimens = rememberResponsiveDimens()
}
