package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.*
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicHomeScreen(
    cpuInfo: CPUInfo,
    gpuInfo: GPUInfo,
    batteryInfo: BatteryInfo,
    systemInfo: SystemInfo,
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onPowerAction: (PowerAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 100.dp), // Extra padding untuk bottom bar
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Header
        ClassicHeader(onSettingsClick)

        // Device Info - Compact
        ClassicDeviceCard(systemInfo)

        // CPU Status - Large prominent card
        ClassicCPUCard(cpuInfo)
        
        // GPU Load - Compact card
        ClassicGPUCard(gpuInfo)

        // RAM Usage - Compact with progress bar
        ClassicRamCard(systemInfo)

        // Storage - Compact with progress bar  
        ClassicStorageCard(systemInfo)

        // Battery - Large prominent card with charging status
        ClassicBatteryCard(batteryInfo)

        // Profile Selector - Compact chips
        ClassicProfileSelector(currentProfile, onProfileChange)

        // Power Menu - Bottom buttons
        ClassicPowerMenu(onPowerAction)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
