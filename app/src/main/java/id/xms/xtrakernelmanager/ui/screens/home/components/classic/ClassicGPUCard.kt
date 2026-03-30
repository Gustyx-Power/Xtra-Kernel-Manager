package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicGPUCard(gpuInfo: GPUInfo) {
    ClassicCard(title = "", icon = Icons.Rounded.Videocam, hideHeader = true) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Icon circle at top
            Surface(
                shape = CircleShape,
                color = ClassicColors.Primary.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // GPU Load title
            Text(
                text = "GPU Load",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            
            // GPU name from system
            Text(
                text = gpuInfo.renderer.ifEmpty { "Adreno™ GPU" },
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Large percentage
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${gpuInfo.gpuLoad}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    fontWeight = FontWeight.Black,
                    color = ClassicColors.OnSurface,
                    lineHeight = 64.sp
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = ClassicColors.OnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Frequency with animated dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ClassicColors.Primary)
                )
                Text(
                    text = "${gpuInfo.currentFreq} MHz",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Primary
                )
            }
        }
    }
}
