package id.xms.xtrakernelmanager.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun ModernBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            )
    ) {


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colorScheme.background.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        // PILL BESAR FLOATING
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .fillMaxWidth(0.94f)
                .height(78.dp),
            shape = RoundedCornerShape(32.dp),
            color = colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    val pillColor by animateColorAsState(
                        targetValue = if (selected)
                            colorScheme.primary.copy(alpha = 0.18f)
                        else
                            colorScheme.surfaceColorAtElevation(2.dp),
                        label = "pillColor"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (selected)
                            colorScheme.primary
                        else
                            Color.Transparent,
                        label = "borderColor"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (selected)
                            colorScheme.onPrimaryContainer
                        else
                            colorScheme.onSurfaceVariant,
                        label = "iconColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (selected)
                            colorScheme.onPrimaryContainer
                        else
                            colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        label = "textColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.14f else 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        ),
                        label = "scale"
                    )

                    val lift by animateFloatAsState(
                        targetValue = if (selected) -8f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "lift"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onNavigate(item.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ICON PILL
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = lift
                                }
                                .size(36.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(pillColor)
                                .border(
                                    width = 1.5.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(999.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.label),
                                tint = iconColor
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = stringResource(item.label),
                            style = if (selected)
                                MaterialTheme.typography.labelMedium
                            else
                                MaterialTheme.typography.labelSmall,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val label: Int
)