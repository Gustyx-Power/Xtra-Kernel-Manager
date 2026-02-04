package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun LiquidProfileCard(
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidSharedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                    onClick = { onProfileChange("Battery") }
                )
                
                ProfileButton(
                    modifier = Modifier.weight(1f),
                    profile = "Balance",
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Balance",
                    color = NeonBlue,
                    isSelected = currentProfile == "Balance",
                    onClick = { onProfileChange("Balance") }
                )
                
                ProfileButton(
                    modifier = Modifier.weight(1f),
                    profile = "Performance",
                    icon = Icons.Default.Speed,
                    label = "Performance",
                    color = NeonPurple,
                    isSelected = currentProfile == "Performance",
                    onClick = { onProfileChange("Performance") }
                )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.2f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
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
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}