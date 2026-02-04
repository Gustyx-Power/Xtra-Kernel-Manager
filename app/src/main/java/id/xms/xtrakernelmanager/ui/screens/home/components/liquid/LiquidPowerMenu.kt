package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import id.xms.xtrakernelmanager.ui.theme.*

@Composable
fun LiquidPowerMenu(
    onAction: (PowerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidSharedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Power Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                    onClick = { onAction(PowerAction.PowerOff) }
                )
                PowerActionButton(
                    modifier = Modifier.weight(1f),
                    action = PowerAction.Reboot,
                    color = Color(0xFF3B82F6), // Blue
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
                    onClick = { onAction(PowerAction.Recovery) }
                )
                PowerActionButton(
                    modifier = Modifier.weight(1f),
                    action = PowerAction.Bootloader,
                    color = Color(0xFF10B981), // Green
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
                    color = NeonPurple,
                    onClick = { onAction(PowerAction.SystemUI) }
                )
            }
        }
    }
}

@Composable
private fun PowerActionButton(
    action: PowerAction,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
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
