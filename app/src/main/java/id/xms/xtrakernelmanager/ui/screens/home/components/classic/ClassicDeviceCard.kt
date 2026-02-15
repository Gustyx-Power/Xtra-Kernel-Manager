package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.SystemInfo

@Composable
fun ClassicDeviceCard(systemInfo: SystemInfo) {
    ClassicCard(title = "Device Info", icon = Icons.Rounded.Smartphone) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassicInfoRow("Model", systemInfo.deviceModel)
            ClassicInfoRow("Android", systemInfo.androidVersion)
            ClassicInfoRow("Kernel", systemInfo.kernelVersion.substringBefore("-"))
        }
    }
}
