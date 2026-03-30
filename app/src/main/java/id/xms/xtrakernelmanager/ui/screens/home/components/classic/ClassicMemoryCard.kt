package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicRamCard(systemInfo: SystemInfo) {
    val usedRam = systemInfo.totalRam - systemInfo.availableRam
    val totalRam = systemInfo.totalRam
    val percent = if (totalRam > 0) (usedRam * 100 / totalRam).toInt() else 0
    
    ClassicCard(title = "RAM Usage", icon = Icons.Rounded.Memory) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RAM Usage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
            }
            
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = ClassicColors.Primary,
                trackColor = ClassicColors.SurfaceVariant,
            )
            
            Text(
                text = "${usedRam / 1024 / 1024} MB / ${totalRam / 1024 / 1024} MB",
                style = MaterialTheme.typography.labelMedium,
                color = ClassicColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
fun ClassicStorageCard(systemInfo: SystemInfo) {
    val usedStorage = systemInfo.totalStorage - systemInfo.availableStorage
    val totalStorage = systemInfo.totalStorage
    val percent = if (totalStorage > 0) (usedStorage * 100 / totalStorage).toInt() else 0
    
    ClassicCard(title = "System Storage", icon = Icons.Rounded.Storage) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Storage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
            }
            
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = ClassicColors.Accent,
                trackColor = ClassicColors.SurfaceVariant,
            )
            
            Text(
                text = "${usedStorage / 1024 / 1024 / 1024} GB / ${totalStorage / 1024 / 1024 / 1024} GB",
                style = MaterialTheme.typography.labelMedium,
                color = ClassicColors.OnSurfaceVariant
            )
        }
    }
}
