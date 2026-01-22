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
import id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass
import id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens

/**
 * Material You Info Card - Always visible, no dropdown Uses Material You theme colors for clean,
 * cohesive design NOW WITH RESPONSIVE DIMENSIONS
 */
@Composable
fun PlayfulInfoCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = rememberResponsiveDimens()
    val isCompact = dimens.screenSizeClass == ScreenSizeClass.COMPACT

    // Subtle pulse animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
    val iconScale by
            infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(2000, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "iconScale"
            )

    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
            modifier = modifier.fillMaxWidth(),
    ) {
        Column(
                modifier = Modifier.padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
        ) {
            // Header with themed icon
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
            ) {
                // Icon with Material You container
                Surface(
                        shape = RoundedCornerShape(dimens.cornerRadiusSmall),
                        color = accentColor.copy(alpha = 0.1f),
                        modifier = Modifier.scale(iconScale)
                ) {
                    Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier =
                                    Modifier.padding(dimens.spacingSmall)
                                            .size(dimens.iconSizeMedium),
                            tint = accentColor
                    )
                }
                Text(
                        text = title,
                        style =
                                if (isCompact) MaterialTheme.typography.titleMedium
                                else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
    val dimens = rememberResponsiveDimens()

    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(dimens.cornerRadiusSmall),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
                modifier =
                        Modifier.padding(
                                horizontal = dimens.cardPaddingSmall,
                                vertical = dimens.spacingSmall
                        ),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Progress bar with label - Material You styled */
@Composable
fun ProgressBarWithLabel(
        label: String,
        progress: Float,
        usedText: String,
        totalText: String,
        color: Color,
        modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val isCompact = dimens.screenSizeClass == ScreenSizeClass.COMPACT

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = label,
                    style =
                            if (isCompact) MaterialTheme.typography.labelMedium
                            else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(dimens.cornerRadiusSmall)
            ) {
                Text(
                        text = "$usedText / $totalText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                                Modifier.padding(
                                        horizontal = dimens.spacingSmall,
                                        vertical = dimens.spacingTiny
                                )
                )
            }
        }
        LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier =
                        Modifier.fillMaxWidth()
                                .height(if (isCompact) 6.dp else 10.dp)
                                .clip(RoundedCornerShape(if (isCompact) 3.dp else 5.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

/** Core frequency item for CPU grid */
@Composable
fun CoreFreqItem(
        coreNumber: Int,
        frequency: Int,
        isOnline: Boolean,
        isHot: Boolean,
        modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val isCompact = dimens.screenSizeClass == ScreenSizeClass.COMPACT

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

    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(dimens.cornerRadiusSmall),
            color = bgColor
    ) {
        Column(
                modifier =
                        Modifier.padding(
                                horizontal = dimens.spacingSmall,
                                vertical = dimens.spacingSmall
                        ),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = "CPU$coreNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = if (isOnline) "${frequency}MHz" else "OFF",
                    style =
                            if (isCompact) MaterialTheme.typography.labelMedium
                            else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
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
        modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val isCompact = dimens.screenSizeClass == ScreenSizeClass.COMPACT

    Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
    ) {
        Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(if (isCompact) 24.dp else 32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.iconSizeSmall),
                        tint = color
                )
            }
        }
        Column {
            Text(
                    text = value,
                    style =
                            if (isCompact) MaterialTheme.typography.titleSmall
                            else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
