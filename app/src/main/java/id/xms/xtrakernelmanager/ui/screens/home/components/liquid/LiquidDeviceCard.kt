package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import android.os.SystemClock
import android.text.format.DateUtils
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.theme.NeonBlue
import id.xms.xtrakernelmanager.ui.theme.NeonGreen
import id.xms.xtrakernelmanager.ui.theme.NeonPurple
import kotlinx.coroutines.delay

@Composable
fun LiquidDeviceCard(systemInfo: SystemInfo, modifier: Modifier = Modifier) {
    // Uptime calculation
    var uptime by remember { mutableStateOf(calculateUptime()) }
    var deepSleep by remember { mutableStateOf("9999%") }

    LaunchedEffect(Unit) {
        // Update loop
        while (true) {
            uptime = calculateUptime()
            
            // Format Deep Sleep
            val deepSleepMillis = systemInfo.deepSleep
            val seconds = deepSleepMillis / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            deepSleep = "${hours}h ${minutes}m"
            
            delay(60000)
        }
    }

    // Determine Brand Logo
    val manufacturer = android.os.Build.MANUFACTURER
    val logoRes = remember(manufacturer) {
        when {
            manufacturer.contains("xiaomi", ignoreCase = true) -> id.xms.xtrakernelmanager.R.drawable.mi_logo
            manufacturer.contains("redmi", ignoreCase = true) -> id.xms.xtrakernelmanager.R.drawable.redmi_logo
            manufacturer.contains("poco", ignoreCase = true) -> id.xms.xtrakernelmanager.R.drawable.poco_logo
            manufacturer.contains("oneplus", ignoreCase = true) -> id.xms.xtrakernelmanager.R.drawable.oneplus_logo
            else -> null 
        }
    }

    LiquidSharedCard(modifier = modifier.heightIn(min = 320.dp)) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            
            // Brand Logo
            if (logoRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = logoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight(0.7f) // Big watermark
                        .align(Alignment.CenterEnd)
                        .offset(x = 60.dp, y = 20.dp)
                        .rotate(-15f)
                        .alpha(0.08f), // Subtle watermark
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                 Icon(
                    imageVector = Icons.Rounded.Android,
                    contentDescription = null,
                    tint = NeonGreen.copy(alpha = 0.15f), 
                    modifier = Modifier
                        .size(280.dp) 
                        .align(Alignment.CenterEnd)
                        .offset(x = 80.dp, y = 40.dp)
                        .rotate(-25f)
                )
            }
            
            // Text & Stats
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 150.dp), // Reserve 150dp for phone
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                 // Header: Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(text = android.os.Build.MANUFACTURER.uppercase(), color = NeonGreen)
                    GlassChip(text = android.os.Build.BOARD.uppercase(), color = NeonBlue)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Device Model
                Text(
                    text = systemInfo.deviceModel
                         .replace(android.os.Build.MANUFACTURER, "", ignoreCase = true)
                         .trim()
                         .ifBlank { "Unknown Device" },
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 42.sp),
                    color = adaptiveTextColor(),
                    lineHeight = 44.sp
                )
                
                Text(
                    text = android.os.Build.DEVICE,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = adaptiveTextColor(0.5f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.weight(1f))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoTile(
                            icon = Icons.Rounded.Android,
                            label = "Android",
                            value = systemInfo.androidVersion,
                            color = NeonGreen,
                            modifier = Modifier.weight(1.5f).height(80.dp)
                        )
                         InfoTile(
                            icon = Icons.Rounded.DeveloperBoard,
                            label = "Kernel",
                            value = systemInfo.kernelVersion,
                            color = NeonPurple,
                            modifier = Modifier.weight(1.5f).height(80.dp)
                        )
                    }
                     // Row 2
                     Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoTile(
                            icon = Icons.Rounded.AccessTime,
                            label = "Uptime",
                            value = uptime,
                            color = NeonBlue,
                            modifier = Modifier.weight(1f).height(80.dp)
                        )
                        InfoTile(
                            icon = androidx.compose.material.icons.Icons.Rounded.NightsStay, 
                            label = "Sleep",
                            value = deepSleep,
                            color = Color(0xFFF48FB1), 
                            modifier = Modifier.weight(1f).height(80.dp)
                        )
                    }
                    // Row 3
                    InfoTile(
                        icon = Icons.Rounded.Android,
                        label = "Smartphone Brand",
                        value = android.os.Build.MANUFACTURER.uppercase(),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            }

            // 3. Futuristic Phone Mockup Layer
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd) 
                    .offset(x = 60.dp, y = 40.dp)
            ) {
                LiquidDeviceMockup(
                    size = androidx.compose.ui.unit.DpSize(140.dp, 280.dp),
                    rotation = -15f,
                    showWallpaper = true,
                    glowColor = NeonBlue,
                    accentColor = NeonPurple
                )
            }
        }
    }
}

@Composable
private fun GlassChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
            .background(color.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(adaptiveSurfaceColor(0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = adaptiveTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = adaptiveTextColor(0.5f)
        )
    }
}

private fun calculateUptime(): String {
    val uptimeMillis = SystemClock.elapsedRealtime()
    return if (uptimeMillis > DateUtils.DAY_IN_MILLIS) {
         "${uptimeMillis / DateUtils.DAY_IN_MILLIS}d"
    } else {
         val hours = uptimeMillis / DateUtils.HOUR_IN_MILLIS
         val minutes = (uptimeMillis % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
         "${hours}h ${minutes}m"
    }
}
