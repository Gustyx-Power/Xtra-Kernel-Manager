package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Performance Panel Component
 * 
 * Shows real-time system performance data:
 * - CPU & GPU gauges
 * - Game duration
 * - Battery status
 * - Performance mode selector
 */
@Composable
fun PerformancePanel(
    cpuFreq: String,
    cpuLoad: Float,
    gpuFreq: String,
    gpuLoad: Float,
    fps: String,
    temperature: String,
    gameDuration: String,
    batteryPercentage: Int,
    currentPerformanceMode: String,
    onPerformanceModeChange: (String) -> Unit,
    onClearRam: () -> Unit,
    isClearingRam: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hardware Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // CPU Gauge
            HardwareGauge(
                type = HardwareGaugeType.CPU,
                value = cpuLoad,
                freqValue = cpuFreq
            )
            
            // GPU Gauge
            HardwareGauge(
                type = HardwareGaugeType.GPU,
                value = gpuLoad,
                freqValue = gpuFreq
            )
        }
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // FPS
            StatChip(
                label = "FPS",
                value = fps,
                color = Color(0xFF4CAF50)
            )
            
            // Temperature
            StatChip(
                label = "Suhu",
                value = "$temperature°C",
                color = Color(0xFFF44336)
            )
            
            // Duration
            StatChip(
                label = "Durasi",
                value = gameDuration,
                color = Color(0xFF9C27B0)
            )
            
            // Battery
            StatChip(
                label = "Baterai",
                value = "$batteryPercentage%",
                color = when {
                    batteryPercentage <= 20 -> Color(0xFFF44336)
                    batteryPercentage <= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
        
        // Divider
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        
        // Performance Mode Section
        Text(
            text = "Mode Performa",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        PerformanceModeSelector(
            currentMode = currentPerformanceMode,
            onModeChange = onPerformanceModeChange,
            accentColor = accentColor
        )
        
        // Divider
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        
        // Quick Actions
        Text(
            text = "Aksi Cepat",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        // Clear RAM Button
        ClearRamActionButton(
            isClearing = isClearingRam,
            onClick = onClearRam,
            accentColor = accentColor
        )
    }
}

/**
 * Stat Chip - Small info display
 */
@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.Gray
        )
    }
}

/**
 * Performance Mode Selector
 */
@Composable
private fun PerformanceModeSelector(
    currentMode: String,
    onModeChange: (String) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PerformanceModeChip(
            emoji = "🔋",
            label = "Hemat",
            mode = "battery",
            isSelected = currentMode == "battery",
            accentColor = Color(0xFF4CAF50),
            onClick = { onModeChange("battery") },
            modifier = Modifier.weight(1f)
        )
        
        PerformanceModeChip(
            emoji = "⚖️",
            label = "Seimbang",
            mode = "balanced",
            isSelected = currentMode == "balanced",
            accentColor = Color(0xFF2196F3),
            onClick = { onModeChange("balanced") },
            modifier = Modifier.weight(1f)
        )
        
        PerformanceModeChip(
            emoji = "⚡",
            label = "Monster",
            mode = "performance",
            isSelected = currentMode == "performance",
            accentColor = Color(0xFFFF5722),
            onClick = { onModeChange("performance") },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Performance Mode Chip
 */
@Composable
private fun PerformanceModeChip(
    emoji: String,
    label: String,
    mode: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        accentColor.copy(alpha = 0.15f)
    } else {
        Color(0xFF1A1A1A)
    }
    
    val borderColor = if (isSelected) {
        accentColor.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) accentColor else Color.White
        )
    }
}

/**
 * Clear RAM Action Button
 */
@Composable
private fun ClearRamActionButton(
    isClearing: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .clickable(enabled = !isClearing) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CleaningServices,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = if (isClearing) "Membersihkan..." else "Bersihkan RAM",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "Bebaskan memori untuk game",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        if (isClearing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = accentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Run",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
