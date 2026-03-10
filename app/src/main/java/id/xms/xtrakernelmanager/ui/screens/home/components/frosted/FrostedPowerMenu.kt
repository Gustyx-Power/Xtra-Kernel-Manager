package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel

@Composable
fun FrostedPowerMenu(
    onAction: (PowerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
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
                    text = "Power Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                // First Row: Power Off, Reboot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerActionButton(
                        modifier = Modifier.weight(1f),
                        action = PowerAction.PowerOff,
                        color = Color(0xFFEF4444), // Red
                        isDarkTheme = isDarkTheme,
                        onClick = { onAction(PowerAction.PowerOff) }
                    )
                    PowerActionButton(
                        modifier = Modifier.weight(1f),
                        action = PowerAction.Reboot,
                        color = Color(0xFF3B82F6), // Blue
                        isDarkTheme = isDarkTheme,
                        onClick = { onAction(PowerAction.Reboot) }
                    )
                }
                
                // Second Row: Recovery, Bootloader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerActionButton(
                        modifier = Modifier.weight(1f),
                        action = PowerAction.Recovery,
                        color = Color(0xFFF59E0B), // Orange
                        isDarkTheme = isDarkTheme,
                        onClick = { onAction(PowerAction.Recovery) }
                    )
                    PowerActionButton(
                        modifier = Modifier.weight(1f),
                        action = PowerAction.Bootloader,
                        color = Color(0xFF10B981), // Green
                        isDarkTheme = isDarkTheme,
                        onClick = { onAction(PowerAction.Bootloader) }
                    )
                }
                
                // Third Row: System UI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PowerActionButton(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        action = PowerAction.SystemUI,
                        color = Color(0xFF8B5CF6), // Purple
                        isDarkTheme = isDarkTheme,
                        onClick = { onAction(PowerAction.SystemUI) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerActionButton(
    action: PowerAction,
    color: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val buttonBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
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
                imageVector = action.icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = action.getLocalizedLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
