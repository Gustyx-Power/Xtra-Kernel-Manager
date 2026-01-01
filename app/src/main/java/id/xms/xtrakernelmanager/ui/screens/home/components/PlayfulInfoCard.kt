package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Material You Info Card - Always visible, no dropdown Uses Material You theme colors for clean,
 * cohesive design
 */
@Composable
fun PlayfulInfoCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
  // Subtle pulse animation for the icon
  val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
  val iconScale by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.05f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(2000, easing = EaseInOutSine),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "iconScale",
      )

  Card(
      modifier = modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp), // More rounded for modern look
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer, // Modern MD3 container
          ),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      // Header with themed icon
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp), // More spacing
      ) {
        // Icon with Material You container
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = accentColor.copy(alpha = 0.1f),
            modifier = Modifier.scale(iconScale),
        ) {
          Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.padding(12.dp).size(24.dp),
              tint = accentColor,
          )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }

      // Content - always visible
      content()
    }
  }
}

/** Stat Chip - Material You styled */
@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Surface(
      modifier = modifier,
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh, // Distinct from card bg
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          text = value,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** Progress bar with label - Material You styled */
/** Progress bar with label - Material You styled */
@Composable
fun ProgressBarWithLabel(
    label: String,
    progress: Float,
    usedText: String,
    totalText: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Surface(
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
          shape = RoundedCornerShape(8.dp),
      ) {
        Text(
            text = "$usedText / $totalText",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier =
            Modifier.fillMaxWidth()
                .height(10.dp) // Slightly thicker
                .clip(RoundedCornerShape(5.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
  }
}

/** Core frequency item for CPU grid - Legacy version (kept for compatibility) */
@Composable
fun CoreFreqItem(
    coreNumber: Int,
    frequency: Int,
    isOnline: Boolean,
    isHot: Boolean,
    modifier: Modifier = Modifier,
) {
  val bgColor =
      when {
        !isOnline -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        isHot -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
      }

  val textColor =
      when {
        !isOnline -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isHot -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
      }

  Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = bgColor) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          text = "CPU$coreNumber",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
          text = if (isOnline) "${frequency}MHz" else "OFF",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = textColor,
      )
    }
  }
}

/** Mini stat item with icon - Material You styled */
@Composable
fun MiniStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.size(32.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color,
        )
      }
    }
    Column {
      Text(
          text = value,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
