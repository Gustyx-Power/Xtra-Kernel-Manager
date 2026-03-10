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

  // OriginOS-inspired frosted glass effect
  val glassColor = if (isDark) {
      Color(0xFF000000).copy(alpha = 0.35f)
  } else {
      Color(0xFFFFFFFF).copy(alpha = 0.45f)
  }
  
  val glassBorder = if (isDark) {
      Color.White.copy(alpha = 0.15f)
  } else {
      Color.White.copy(alpha = 0.6f)
  }
  
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
                      blur(12.dp.toPx()) // Reduced blur for better performance
                      lens(
                              refractionHeight = 20.dp.toPx(), // Reduced refraction
                              refractionAmount = 28.dp.toPx(), // Reduced distortion
                              chromaticAberration = false, // Disabled for performance
                              depthEffect = false // Disabled for performance
                      )
                    },
                    onDrawSurface = {
                      drawRect(glassColor)
                      // We can render border here if needed, or via modifier.border below
                    }
            )
    // Add border on top with adaptive width
    baseModifier = baseModifier.border(
        width = if (isDark) 0.8.dp else 1.2.dp,
        color = glassBorder,
        shape = shape
    )
  } else {
    // Fallback with adaptive border
    baseModifier = baseModifier
        .background(glassColor)
        .border(
            width = if (isDark) 0.8.dp else 1.2.dp,
            color = glassBorder,
            shape = shape
        )
  }

  // Adaptive content color scheme based on theme
  val currentColorScheme = MaterialTheme.colorScheme
  val forcedContentScheme = if (isDark) {
      currentColorScheme.copy(
          onSurface = Color.White.copy(alpha = 0.95f),
          onSurfaceVariant = Color.White.copy(alpha = 0.65f),
          primary = currentColorScheme.primary
      )
  } else {
      currentColorScheme.copy(
          onSurface = Color(0xFF2C2C2C).copy(alpha = 0.85f),
          onSurfaceVariant = Color(0xFF5A5A5A).copy(alpha = 0.7f),
          primary = currentColorScheme.primary
      )
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
