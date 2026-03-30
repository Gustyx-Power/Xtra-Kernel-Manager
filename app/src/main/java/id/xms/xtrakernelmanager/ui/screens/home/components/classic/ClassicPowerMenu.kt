package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicPowerMenu(onPowerAction: (PowerAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = ClassicColors.OnSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Power Menu",
                style = MaterialTheme.typography.titleLarge,
                color = ClassicColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PowerMenuItem(
                icon = Icons.Rounded.RestartAlt,
                label = "REBOOT",
                onClick = { onPowerAction(PowerAction.Reboot) },
                modifier = Modifier.weight(1f)
            )
            PowerMenuItem(
                icon = Icons.Rounded.Refresh,
                label = "FASTBOOT",
                onClick = { onPowerAction(PowerAction.Bootloader) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PowerMenuItem(
                icon = Icons.Rounded.Build,
                label = "RECOVERY",
                onClick = { onPowerAction(PowerAction.Recovery) },
                modifier = Modifier.weight(1f)
            )
            PowerMenuItem(
                icon = Icons.Rounded.PowerSettingsNew,
                label = "SHUTDOWN",
                onClick = { onPowerAction(PowerAction.PowerOff) },
                modifier = Modifier.weight(1f),
                isDestructive = true
            )
        }
    }
}

@Composable
private fun PowerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isDestructive) ClassicColors.Critical else ClassicColors.OnSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                letterSpacing = 1.5.sp
            )
        }
    }
}
