package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.*
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun LiquidHomeScreen(
    cpuInfo: CPUInfo,
    gpuInfo: GPUInfo,
    batteryInfo: BatteryInfo,
    systemInfo: SystemInfo,
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onPowerAction: (id.xms.xtrakernelmanager.ui.model.PowerAction) -> Unit,
) {
    val dimens = id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens()
    val isCompact =
          dimens.screenSizeClass == id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass.COMPACT
    
    // Status bar data
    val statusBarData = id.xms.xtrakernelmanager.ui.components.statusbar.rememberStatusBarData()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content Scrollable Column with pure Liquid Backdrop
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(56.dp)) // Space for custom status bar (48dp + 8dp safe area)
     
            // 1. Header (Redesigned Liquid Header)
            LiquidHeader(
                onSettingsClick = onSettingsClick,
                modifier = Modifier
            )

        // 2. Liquid Device Card
        LiquidDeviceCard(systemInfo = systemInfo, modifier = Modifier.fillMaxWidth())

        // 3. CPU & GPU Tiles (Rich Tiles)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
             LiquidStatTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Rounded.Memory,
                label = "CPU",
                value = "${(cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0) / 1000} MHz",
                subValue = cpuInfo.cores.firstOrNull { it.isOnline }?.governor ?: "Unknown",
                color = id.xms.xtrakernelmanager.ui.theme.NeonGreen,
                badgeText = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%"
             )
             
             LiquidTempTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                cpuTemp = cpuInfo.temperature.toInt(),
                gpuTemp = gpuInfo.temperature.toInt(),
                pmicTemp = batteryInfo.pmicTemp.toInt(),
                thermalTemp = batteryInfo.temperature.toInt(),
                color = id.xms.xtrakernelmanager.ui.theme.NeonPurple
             )
        }

        // 4. Liquid GPU Card (Detailed)
        LiquidGPUCard(gpuInfo = gpuInfo, modifier = Modifier.fillMaxWidth())

        // 5. Liquid Battery Card (Detailed)
        LiquidBatteryCard(batteryInfo = batteryInfo, modifier = Modifier.fillMaxWidth())
        
         // 6. Memory & Storage Row
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val totalMem = systemInfo.totalRam
            val availMem = systemInfo.availableRam
            val usedMem = totalMem - availMem
            val memProgress = if (totalMem > 0) usedMem.toFloat() / totalMem.toFloat() else 0f
            val usedGb = String.format("%.1f GB", usedMem / (1024f * 1024f * 1024f))

            LiquidCircularStatsCard(
                title = "RAM",
                value = usedGb,
                progress = memProgress,
                color = id.xms.xtrakernelmanager.ui.theme.NeonBlue,
                modifier = Modifier.weight(1f)
            )

            val totalStorage = systemInfo.totalStorage
            val availStorage = systemInfo.availableStorage
            val usedStorage = totalStorage - availStorage
            val storageProgress = if (totalStorage > 0) usedStorage.toFloat() / totalStorage.toFloat() else 0f
            val usedStorageGb = String.format("%.0f GB", usedStorage / (1024f * 1024f * 1024f))

            LiquidCircularStatsCard(
                title = "ROM",
                value = usedStorageGb,
                progress = storageProgress,
                color = id.xms.xtrakernelmanager.ui.theme.NeonPurple,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 7. Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiquidPowerMenu(
                onAction = onPowerAction,
                modifier = Modifier.weight(1f).height(140.dp)
            )
             
            LiquidProfileCard(
                currentProfile = currentProfile,
                onNextProfile = {
                    val next = when (currentProfile) {
                        "Balance" -> "Performance"
                        "Performance" -> "Battery"
                        else -> "Balance"
                    }
                    onProfileChange(next)
                },
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // iOS-style Status Bar (overlay - FIXED position, tidak scroll)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            id.xms.xtrakernelmanager.ui.components.statusbar.LiquidStatusBar(
                batteryLevel = statusBarData.batteryLevel,
                isCharging = statusBarData.isCharging,
                signalStrength = statusBarData.signalStrength,
                wifiEnabled = statusBarData.wifiEnabled
            )
        }
    }
}
