package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicProfileSelector(
    currentProfile: String,
    onProfileChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Title with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = ClassicColors.OnSurface,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Performance",
                style = MaterialTheme.typography.headlineMedium,
                color = ClassicColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Profile items
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileItem(
                icon = Icons.Rounded.Eco,
                label = "Battery Saver",
                isSelected = currentProfile == "powersave",
                onClick = { onProfileChange("powersave") }
            )
            ProfileItem(
                icon = Icons.Rounded.RocketLaunch,
                label = "Balanced",
                isSelected = currentProfile == "balance",
                onClick = { onProfileChange("balance") }
            )
            ProfileItem(
                icon = Icons.Rounded.LocalFireDepartment,
                label = "Performance",
                isSelected = currentProfile == "performance",
                onClick = { onProfileChange("performance") }
            )
        }
    }
}

@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ClassicColors.Primary else ClassicColors.Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) ClassicColors.OnPrimary else ClassicColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ClassicColors.OnPrimary else ClassicColors.OnSurface
                )
            }
            
            Icon(
                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = if (isSelected) ClassicColors.OnPrimary else ClassicColors.OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
