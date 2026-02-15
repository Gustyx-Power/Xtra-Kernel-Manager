package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicGPUCard(gpuInfo: GPUInfo) {
    ClassicCard(title = "GPU Status", icon = Icons.Rounded.Videocam) {
            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Column {
                    Text(
                    text = "${gpuInfo.gpuLoad}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text("Load", style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                    text = "${gpuInfo.currentFreq} MHz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Primary
                )
                Text(
                    text = "Current Freq",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurfaceVariant
                )
                }
        }
    }
}
