package id.xms.xtrakernelmanager.ui.components.donation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.xms.xtrakernelmanager.R

/**
 * Classic style donation dialog - Nebula Core theme
 * "The Luminous Monolith" - High-end editorial dark mode
 */
@Composable
fun ClassicDonationDialog(
    onDismiss: () -> Unit,
    onSupportClick: () -> Unit
) {
    // Nebula Core Colors
    val background = Color(0xFF080f11)
    val surfaceContainer = Color(0xFF101b1e)
    val surfaceContainerHighest = Color(0xFF19282c)
    val primaryAccent = Color(0xFFadf4ff)
    val surfaceVariant = Color(0xFF3f484a)
    
    // Pulsing animation for luminous glow
    val infiniteTransition = rememberInfiniteTransition(label = "luminousGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Ambient shadow with teal tint (40px blur, 10% opacity)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.9f
                        )
                    )
                }
                .background(
                    color = surfaceContainer,
                    shape = RoundedCornerShape(24.dp) // md corner radius
                )
                .padding(28.dp), // Spacing 4 (1.4rem ≈ 22-28dp)
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Spacing 6
        ) {
            // Icon with luminous glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        // Luminous glow effect
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryAccent.copy(alpha = glowAlpha * 0.6f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width * 0.8f
                        )
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = 0.3f),
                                primaryAccent.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = primaryAccent
                )
            }

            // Title - Editorial hierarchy
            Text(
                text = stringResource(R.string.donation_dialog_title),
                style = MaterialTheme.typography.headlineSmall, // headline-sm (1.5rem)
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            // Description - Muted variant
            Text(
                text = stringResource(R.string.donation_dialog_message),
                style = MaterialTheme.typography.bodyLarge, // body-lg (1.0rem)
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.7f), // on_surface_variant
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Support Button - Primary with luminous gradient
                Button(
                    onClick = onSupportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .drawBehind {
                            // Button glow
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryAccent.copy(alpha = glowAlpha * 0.4f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.7f
                                )
                            )
                        },
                    shape = RoundedCornerShape(28.dp), // Full rounding
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        primaryAccent,
                                        primaryAccent.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = background
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.donation_support_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = background
                            )
                        }
                    }
                }

                // Maybe Later Button - Ghost border with surface shift
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = surfaceContainerHighest.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = 0.3f), // Ghost border at 30%
                                primaryAccent.copy(alpha = 0.2f)
                            )
                        ),
                        width = 1.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryAccent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.donation_maybe_later),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
