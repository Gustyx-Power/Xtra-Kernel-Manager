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
        Color(0xFF6B9EFF).copy(alpha = 0.85f)
    } else {
        Color(0xFF3B82F6).copy(alpha = 0.85f)
    }
    
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.6f)
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
        modifier = modifier.height(280.dp),
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
                .clip(RoundedCornerShape(24.dp))
        ) {
            
            if (logoRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = logoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) 
                    .offset(x = 35.dp, y = 50.dp)
            ) {
                FrostedDeviceMockup(
                    size = androidx.compose.ui.unit.DpSize(120.dp, 240.dp),
                    rotation = 0f,
                    showWallpaper = true,
                    glowColor = textColor.copy(alpha = 0.6f),
                    accentColor = textColor.copy(alpha = 0.8f)
                )
            }
            
            // Text & Stats
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Brand & Board Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(
                        text = android.os.Build.MANUFACTURER.uppercase(), 
                        color = textSecondaryColor, 
                        isDarkTheme = isDarkTheme
                    )
                    GlassChip(
                        text = android.os.Build.BOARD.uppercase(), 
                        color = textSecondaryColor, 
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Device Model
                Column(
                    modifier = Modifier.fillMaxWidth(0.55f)
                ) {
                    Text(
                        text = systemInfo.deviceModel
                             .replace(android.os.Build.MANUFACTURER, "", ignoreCase = true)
                             .trim()
                             .ifBlank { stringResource(id.xms.xtrakernelmanager.R.string.frosted_device_unknown_device) },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 28.sp
                        ),
                        color = textColor,
                        lineHeight = 30.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // SoC Name
                    if (systemInfo.socName.isNotEmpty() && systemInfo.socName != "Unknown") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = systemInfo.socName.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = textSecondaryColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val totalRamGB = (systemInfo.totalRam / (1024f * 1024f * 1024f))
                        val usedRamGB = ((systemInfo.totalRam - systemInfo.availableRam) / (1024f * 1024f * 1024f))
                        val zramSizeGB = (systemInfo.zramSize / (1024f * 1024f * 1024f))
                        val ramPercentage = if (systemInfo.totalRam > 0) {
                            ((systemInfo.totalRam - systemInfo.availableRam).toFloat() / systemInfo.totalRam.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        
                        val ramLabel = if (zramSizeGB >= 0.1f) {
                            String.format("%.1f GB + %.1f GB", totalRamGB, zramSizeGB)
                        } else {
                            String.format("%.1f GB", totalRamGB)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RAM:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = textColor.copy(alpha = 0.6f)
                            )
                            Text(
                                text = ramLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = textColor
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(textColor.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ramPercentage)
                                    .fillMaxHeight()
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val totalStorageGiB = (systemInfo.totalStorage / (1024f * 1024f * 1024f))
                        val usedStorageGiB = ((systemInfo.totalStorage - systemInfo.availableStorage) / (1024f * 1024f * 1024f))
                        val storagePercentage = if (systemInfo.totalStorage > 0) {
                            ((systemInfo.totalStorage - systemInfo.availableStorage).toFloat() / systemInfo.totalStorage.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        
                        val marketingTotal = when {
                            totalStorageGiB >= 220f -> 256 
                            totalStorageGiB >= 110f -> 128
                            totalStorageGiB >= 55f -> 64
                            totalStorageGiB >= 27f -> 32
                            else -> totalStorageGiB.toInt()
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ROM:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = textColor.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("%.2f GB / %d GB", usedStorageGiB, marketingTotal),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = textColor
                            )
                        }
                        
                        // ROM Progress Bar (shows actual usage)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(textColor.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(storagePercentage)
                                    .fillMaxHeight()
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    // Android Version Badge
                    GlassChip(
                        text = "Android ${systemInfo.androidVersion}", 
                        color = textColor, 
                        isDarkTheme = isDarkTheme
                    )
                }
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
