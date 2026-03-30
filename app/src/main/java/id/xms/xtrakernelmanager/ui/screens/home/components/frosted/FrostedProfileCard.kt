package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.*

@Composable
fun FrostedProfileCard(
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
    
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    FrostedSharedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = borderColor,
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Performance Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileButton(
                        modifier = Modifier.weight(1f),
                        profile = "Battery",
                        icon = Icons.Default.Battery0Bar,
                        label = "Battery",
                        color = NeonGreen,
                        isSelected = currentProfile == "Battery",
                        isDarkTheme = isDarkTheme,
                        onClick = { onProfileChange("Battery") }
                    )
                    
                    ProfileButton(
                        modifier = Modifier.weight(1f),
                        profile = "Balance",
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Balance",
                        color = NeonBlue,
                        isSelected = currentProfile == "Balance",
                        isDarkTheme = isDarkTheme,
                        onClick = { onProfileChange("Balance") }
                    )
                    
                    ProfileButton(
                        modifier = Modifier.weight(1f),
                        profile = "Performance",
                        icon = Icons.Default.Speed,
                        label = "Performance",
                        color = NeonPurple,
                        isSelected = currentProfile == "Performance",
                        isDarkTheme = isDarkTheme,
                        onClick = { onProfileChange("Performance") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileButton(
    profile: String,
    icon: ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBackground = if (isSelected) {
        color.copy(alpha = 0.3f)
    } else {
        if (isDarkTheme) {
            Color(0xFF000000).copy(alpha = 0.3f)
        } else {
            Color.White.copy(alpha = 0.5f)
        }
    }
    
    val buttonBorder = if (isSelected) {
        color.copy(alpha = 0.5f)
    } else {
        if (isDarkTheme) {
            Color.White.copy(alpha = 0.15f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }
    }
    
    val textColor = if (isSelected) {
        color
    } else {
        if (isDarkTheme) {
            Color.White.copy(alpha = 0.7f)
        } else {
            Color(0xFF2C2C2C).copy(alpha = 0.7f)
        }
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBackground)
            .border(0.8.dp, buttonBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}