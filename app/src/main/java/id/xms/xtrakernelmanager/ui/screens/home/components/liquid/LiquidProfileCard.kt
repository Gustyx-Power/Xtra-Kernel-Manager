package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LiquidProfileCard(
    currentProfile: String,
    onNextProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color, label) = when (currentProfile) {
        "Performance" -> Triple(Icons.Rounded.Bolt, Color(0xFFEF4444), "Performance") // Red
        "Powersave", "Battery" -> Triple(Icons.Rounded.BatterySaver, Color(0xFF10B981), "Power Save") // Green
        else -> Triple(Icons.Rounded.Speed, Color(0xFF3B82F6), "Balanced") // Blue
    }

    // Material 3 colors for background and content
    val containerColor = Color.White
    val contentColor = Color.White

    LiquidSharedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Ripple handled by card or custom if needed
                ) { onNextProfile() }
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Icon
            AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    if (targetState == Icons.Rounded.Bolt) {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "ProfileIcon"
            ) { targetIcon ->
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = targetIcon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Label
            AnimatedContent(
                targetState = label,
                transitionSpec = {
                    slideInVertically { height -> height / 2 } + fadeIn() with
                            slideOutVertically { height -> -height / 2 } + fadeOut()
                },
                label = "ProfileLabel"
            ) { targetLabel ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = "Tap to Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}


