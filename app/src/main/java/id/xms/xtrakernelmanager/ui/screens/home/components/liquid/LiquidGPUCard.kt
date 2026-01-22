package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.theme.NeonPurple

@Composable
fun LiquidGPUCard(gpuInfo: GPUInfo, modifier: Modifier = Modifier) {
    LiquidSharedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    val gpuBadge = remember(gpuInfo.renderer) {
                        when {
                            gpuInfo.renderer.contains("Adreno", true) -> "Adreno"
                            gpuInfo.renderer.contains("Mali", true) -> "Mali"
                            gpuInfo.renderer.contains("PowerVR", true) -> "PowerVR"
                            gpuInfo.renderer.contains("NVIDIA", true) -> "NVIDIA"
                            gpuInfo.renderer != "Unknown" -> gpuInfo.renderer.take(12)
                            else -> gpuInfo.vendor.ifEmpty { "GPU" }
                        }
                    }
                    Text(
                        text = gpuBadge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "${gpuInfo.currentFreq} MHz",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonPurple
                )
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inner Card 1: Load
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "${gpuInfo.gpuLoad}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Load",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Inner Card 2: GPU Name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                     Column {
                        val cleanGpuName = remember(gpuInfo.renderer) {
                             when {
                                gpuInfo.renderer.contains("Adreno", ignoreCase = true) -> {
                                  val match = Regex("Adreno.*?(\\d{3})").find(gpuInfo.renderer)
                                  match?.let { "Adreno ${it.groupValues[1]}" } ?: gpuInfo.renderer
                                }
                                else -> gpuInfo.renderer
                             }
                        }
                        Text(
                            text = cleanGpuName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                             lineHeight = 1.2.em
                        )
                        Text(
                            text = "GPU",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
