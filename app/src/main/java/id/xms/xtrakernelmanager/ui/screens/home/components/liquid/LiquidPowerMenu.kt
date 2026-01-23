package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.model.PowerAction

@Composable
fun LiquidPowerMenu(
    onAction: (PowerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidSharedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PowerItem(PowerAction.PowerOff, Color(0xFFEF4444)) { onAction(it) } // Red
                PowerItem(PowerAction.Reboot, Color(0xFF3B82F6)) { onAction(it) }   // Blue
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PowerItem(PowerAction.Recovery, Color(0xFFF59E0B)) { onAction(it) } // Orange
                PowerItem(PowerAction.Bootloader, Color(0xFF10B981)) { onAction(it) } // Green
            }
        }
    }
}

@Composable
private fun PowerItem(
    action: PowerAction,
    color: Color,
    onClick: (PowerAction) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .clickable { onClick(action) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null, // Icons are self-explanatory or learnable
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
