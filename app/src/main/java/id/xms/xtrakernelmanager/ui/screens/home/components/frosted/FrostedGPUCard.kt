package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import id.xms.xtrakernelmanager.data.model.GPUInfo

@Composable
fun FrostedGPUCard(gpuInfo: GPUInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = true // XKM is always dark mode
    
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
    
    val textSecondaryColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    val tileBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val tileBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    FrostedSharedCard(
        modifier = modifier.height(IntrinsicSize.Max),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_gpu_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Surface(
                        color = tileBackground,
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
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "${gpuInfo.currentFreq} MHz",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_gpu_frequency),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondaryColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.large)
                            .background(tileBackground)
                            .border(0.8.dp, tileBorder, MaterialTheme.shapes.large)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "${gpuInfo.gpuLoad}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_gpu_load),
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryColor
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.large)
                            .background(tileBackground)
                            .border(0.8.dp, tileBorder, MaterialTheme.shapes.large)
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
                                color = textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                 lineHeight = 1.2.em
                            )
                            Text(
                                text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_gpu_name),
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
