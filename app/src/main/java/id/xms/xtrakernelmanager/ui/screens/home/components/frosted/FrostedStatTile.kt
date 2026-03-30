package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FrostedStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    badgeText: String? = null
) {
    val isDarkTheme = true // XKM is always dark mode
    
    val glassBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.45f)
    }
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    val textSecondaryColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    val tileBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val tileBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    FrostedSharedCard(
        modifier = modifier.height(IntrinsicSize.Max),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = tileBackground,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (badgeText != null) {
                        Surface(
                            color = tileBackground,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, tileBorder)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        color = textSecondaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            fontSize = 20.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        color = textSecondaryColor
                    )
                }
            }
        }
    }
}
