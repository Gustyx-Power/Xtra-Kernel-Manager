package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

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
import androidx.compose.material.icons.rounded.Fingerprint
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun FrostedDeviceCard(systemInfo: SystemInfo, modifier: Modifier = Modifier) {
    // Detect system theme - OriginOS style
    val isDarkTheme = isSystemInDarkTheme()
    
    // OriginOS-inspired frosted glass colors
    val glassBackground = if (isDarkTheme) {
        // Dark theme: dark glass with transparency for blur effect (like screenshot 1)
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        // Light theme: lighter glass with high transparency (like screenshot 2)
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
    
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
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

    FrostedSharedCard(
        modifier = modifier.heightIn(min = 320.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            
            // Brand Logo - Top Right Corner
            if (logoRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = logoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                 Icon(
                    imageVector = Icons.Rounded.Android,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f), 
                    modifier = Modifier
                        .size(64.dp) 
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
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
                    GlassChip(text = android.os.Build.MANUFACTURER.uppercase(), color = textColor, isDarkTheme = isDarkTheme)
                    GlassChip(text = android.os.Build.BOARD.uppercase(), color = textColor, isDarkTheme = isDarkTheme)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Device Model
                Text(
                    text = systemInfo.deviceModel
                         .replace(android.os.Build.MANUFACTURER, "", ignoreCase = true)
                         .trim()
                         .ifBlank { stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_unknown_device) },
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
                    color = textColor,
                    lineHeight = 30.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = android.os.Build.DEVICE,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                    color = textSecondaryColor,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.weight(1f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row 1
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoTile(
                            icon = Icons.Rounded.Android,
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_android),
                            value = systemInfo.androidVersion,
                            color = textColor,
                            secondaryColor = textSecondaryColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1.5f).height(68.dp)
                        )
                         InfoTile(
                            icon = Icons.Rounded.DeveloperBoard,
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_kernel),
                            value = systemInfo.kernelVersion,
                            color = textColor,
                            secondaryColor = textSecondaryColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1.5f).height(68.dp)
                        )
                    }
                     // Row 2
                     Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoTile(
                            icon = Icons.Rounded.AccessTime,
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_uptime),
                            value = uptime,
                            color = textColor,
                            secondaryColor = textSecondaryColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f).height(68.dp)
                        )
                        InfoTile(
                            icon = androidx.compose.material.icons.Icons.Rounded.NightsStay, 
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_sleep),
                            value = deepSleep,
                            color = textColor,
                            secondaryColor = textSecondaryColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f).height(68.dp)
                        )
                    }
                    
                    // Row 3 - Fingerprint (Full Width)
                    InfoTile(
                        icon = androidx.compose.material.icons.Icons.Rounded.Fingerprint,
                        label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_fingerprint),
                        value = android.os.Build.FINGERPRINT,
                        color = textColor,
                        secondaryColor = textSecondaryColor,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.fillMaxWidth().height(68.dp)
                    )
                    // Row 3 (Manufacturer) - Removed as it is redundant and space consuming
                }
            }

            // 3. Futuristic Phone Mockup Layer
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd) 
                    .offset(x = 60.dp, y = 40.dp)
            ) {
                FrostedDeviceMockup(
                    size = androidx.compose.ui.unit.DpSize(140.dp, 280.dp),
                    rotation = -15f,
                    showWallpaper = true,
                    glowColor = textColor.copy(alpha = 0.6f),
                    accentColor = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GlassChip(text: String, color: Color, isDarkTheme: Boolean) {
    // OriginOS style chip
    val chipBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    val chipBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = Modifier
            .border(0.8.dp, chipBorder, CircleShape)
            .background(chipBackground, CircleShape)
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
    secondaryColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // OriginOS style info tile
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
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tileBackground)
            .border(
                width = 0.8.dp,
                color = tileBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = secondaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
