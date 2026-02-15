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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        ClassicHeader(onSettingsClick)

        // Device Info
        ClassicDeviceCard(systemInfo)

        // CPU Info
        ClassicCPUCard(cpuInfo)
        
        // GPU Info
        ClassicGPUCard(gpuInfo)

        // Memory (RAM & Storage)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // RAM
            Box(modifier = Modifier.weight(1f)) {
                ClassicRamCard(systemInfo)
            }
            
            // Storage
            Box(modifier = Modifier.weight(1f)) {
                 ClassicStorageCard(systemInfo)
            }
        }

        // Battery
        ClassicBatteryCard(batteryInfo)

        // Profile Selector
        ClassicProfileSelector(currentProfile, onProfileChange)

        // Power Menu Grid
        ClassicPowerMenu(onPowerAction)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
