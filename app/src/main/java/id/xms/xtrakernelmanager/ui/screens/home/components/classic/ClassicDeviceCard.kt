package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicDeviceCard(systemInfo: SystemInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "SYSTEM IDENTITY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ClassicColors.Primary,
            letterSpacing = 2.sp
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = systemInfo.deviceModel.split(" ").firstOrNull() ?: "Xiaomi",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                fontWeight = FontWeight.ExtraBold,
                color = ClassicColors.OnSurface,
                lineHeight = 56.sp
            )
            Text(
                text = systemInfo.deviceModel.split(" ").lastOrNull() ?: systemInfo.deviceModel,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                fontWeight = FontWeight.ExtraBold,
                color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.5f),
                lineHeight = 40.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ClassicColors.SurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ClassicColors.Primary
                    )
                    Text(
                        text = "Android ${systemInfo.androidVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ClassicColors.SurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ClassicColors.Primary
                    )
                    Text(
                        text = systemInfo.kernelVersion,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
