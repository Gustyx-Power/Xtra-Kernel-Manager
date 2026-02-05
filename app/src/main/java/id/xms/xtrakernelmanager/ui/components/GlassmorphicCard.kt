package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.utils.LayerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.drawBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.blur
import id.xms.xtrakernelmanager.ui.components.utils.colorControls
import id.xms.xtrakernelmanager.ui.components.utils.lens
import id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens

val LocalBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
fun GlassmorphicCard(
        modifier: Modifier = Modifier,
        // backdrop param removed, use LocalBackdrop.current
        onClick: (() -> Unit)? = null,
        enabled: Boolean = true,
        shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(32.dp),
        contentPadding: PaddingValues? = null,
        content: @Composable ColumnScope.() -> Unit,
) {
  val backdrop = LocalBackdrop.current
  val dimens = rememberResponsiveDimens()
  val finalPadding = contentPadding ?: PaddingValues(dimens.cardPadding)
  val isDark = isSystemInDarkTheme()

  // Force Light Mode style glass effect (White tint + higher opacity) as requested
  val glassColor = Color.White.copy(alpha = 0.4f)
  val glassBorder = Color.White.copy(alpha = 0.4f)
  val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

  // Base modifier without shadow to avoid artifacts behind transparent content
  var baseModifier =
          modifier.fillMaxWidth()
                  .clip(shape)

  // Apply Backdrop effect if available, else usage manual fallback
  if (backdrop != null) {
    baseModifier =
            baseModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                      colorControls(
                              saturation = 0.7f, // High vibrancy
                              brightness = 0.15f // Thicker glass look (darker interior)
                      )
                      blur(20.dp.toPx()) // Balanced blur
                      lens(
                              refractionHeight = 32.dp.toPx(), // Deep refraction (Thickness)
                              refractionAmount = 48.dp.toPx(), // Strong distortion
                              chromaticAberration = true,
                              depthEffect = true
                      )
                    },
                    onDrawSurface = {
                      drawRect(glassColor)
                      // We can render border here if needed, or via modifier.border below
                    }
            )
    // Add border on top
    baseModifier = baseModifier.border(1.dp, glassBorder, shape)
  } else {
    // Fallback
    baseModifier = baseModifier.background(glassColor).border(1.dp, glassBorder, shape)
  }

  // Override internal theme to force dark text on the white glass card
  val currentColorScheme = MaterialTheme.colorScheme
  val forcedContentScheme = if (isDark) {
      currentColorScheme.copy(
          onSurface = Color.Black,
          onSurfaceVariant = Color.DarkGray, // or Color(0xFF444444)
          primary = currentColorScheme.primary // Keep primary color
      )
  } else {
      currentColorScheme
  }

  if (onClick != null) {
    Box(modifier = baseModifier.clickable(enabled = enabled, onClick = onClick)) {
      MaterialTheme(colorScheme = forcedContentScheme) {
          Column(
                  modifier = Modifier.padding(finalPadding),
                  verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
          ) { content() }
      }
    }
  } else {
    Box(modifier = baseModifier) {
      MaterialTheme(colorScheme = forcedContentScheme) {
          Column(
                  modifier = Modifier.padding(finalPadding),
                  verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
          ) { content() }
      }
    }
  }
}

@Composable
fun EnhancedCard(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        enabled: Boolean = true,
        content: @Composable ColumnScope.() -> Unit,
) {
  GlassmorphicCard(modifier = modifier, onClick = onClick, enabled = enabled, content = content)
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
    )
  }
}
