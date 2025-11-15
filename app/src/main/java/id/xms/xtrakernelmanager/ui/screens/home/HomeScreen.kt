package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.EnhancedCard
import id.xms.xtrakernelmanager.ui.components.PillCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    val currentTheme by preferencesManager.themeMode.collectAsState(initial = 0)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Theme Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "Material 3 Dynamic",
                        1 to "Material 3 Solid",
                        2 to "Material 3 Glass"
                    ).forEach { (mode, label) ->
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    preferencesManager.setThemeMode(mode)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (currentTheme == mode)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (currentTheme == mode) 4.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = currentTheme == mode,
                                    onClick = null
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (currentTheme == mode) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PillCard(text = "Xtra Kernel Manager")
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                FilledIconButton(
                    onClick = { showThemeDialog = true },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme"
                    )
                }
            }
        }

        item {
            InfoCard(
                title = "CPU Information",
                icon = Icons.Default.Memory,
                content = {
                    Text(
                        "Clockspeed per Core",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Grid manual dengan FlowRow atau Column+Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // Buat 2 baris, masing-masing 4 core
                        val rows = cpuInfo.cores.chunked(4)
                        rows.forEach { rowCores ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowCores.forEach { core ->
                                    Column(
                                        modifier = Modifier.width(70.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (core.isOnline)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "${core.currentFreq} MHz",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "CPU${core.coreNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    InfoIconRow(Icons.Default.Dashboard, "Governor", cpuInfo.cores.firstOrNull()?.governor ?: "-")
                    InfoIconRow(Icons.Default.Thermostat, "Temperature", "${cpuInfo.temperature}°C")
                    InfoIconRow(Icons.Default.Speed, "Load", "${String.format("%.1f", cpuInfo.totalLoad)}%")
                    InfoIconRow(Icons.Default.Adb, "Online", "${cpuInfo.cores.count { it.isOnline }}/${cpuInfo.cores.size}")
                }
            )
        }


        item {
            InfoCard(
                title = "GPU Information",
                icon = Icons.Default.Videocam,
                content = {
                    // OpenGL ES version highlight
                    Text(
                        text = gpuInfo.openglVersion,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    // Grid Freq mirip CPU jika availableFreqs.size > 1
                    if (gpuInfo.availableFreqs.size > 1) {
                        Text(
                            "Frequencies",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // grid 4 kolom
                            gpuInfo.availableFreqs.chunked(4).forEachIndexed { rowIdx, rowFreqs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowFreqs.forEachIndexed { i, freq ->
                                        Column(
                                            modifier = Modifier.width(70.dp).padding(vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (freq == gpuInfo.currentFreq) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = "$freq MHz",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "GPU${rowIdx*4 + i}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (freq == gpuInfo.currentFreq)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    } else if (gpuInfo.currentFreq > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("${gpuInfo.currentFreq} MHz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("GPU", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    InfoIconRow(Icons.Default.Memory, "Vendor", gpuInfo.vendor)
                    InfoIconRow(Icons.Default.Dashboard, "Renderer", gpuInfo.renderer)
                    InfoIconRow(Icons.Default.Bolt, "Max Freq", "${gpuInfo.maxFreq} MHz")
                    InfoIconRow(Icons.Default.Bolt, "Min Freq", "${gpuInfo.minFreq} MHz")
                }
            )
        }



        item {
            InfoCard(
                title = "Battery Information",
                icon = Icons.Default.BatteryChargingFull
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,    // <-- Ganti ke Center supaya ada di tengah
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Battery Level
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = batteryInfo.level / 100f,
                            strokeWidth = 8.dp,
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text("Level")
                    }

                    Spacer(modifier = Modifier.width(32.dp))  // Jarak antara dua circle

                    // Circular Battery Health
                    val healthProgress = (batteryInfo.healthPercent.coerceIn(0f, 100f)) / 100f

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = healthProgress,
                            strokeWidth = 8.dp,
                            modifier = Modifier.size(72.dp),
                            color = if (healthProgress > 0.8f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = String.format("%.1f%%", batteryInfo.healthPercent),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text("Health")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail lainnya
                InfoIconRow(Icons.Default.Thermostat, "Temperature", "${batteryInfo.temperature}°C")
                InfoIconRow(Icons.Default.Favorite, "Health (Text)", batteryInfo.health)
                InfoIconRow(Icons.Default.Power, "Status", batteryInfo.status)
                InfoIconRow(Icons.Default.FlashOn, "Current Now", "${batteryInfo.currentNow} mA")
                InfoIconRow(Icons.Default.BatteryStd, "Voltage", "${batteryInfo.voltage} mV")
                InfoIconRow(Icons.Default.Refresh, "Cycle Count", batteryInfo.cycleCount.toString())
                InfoIconRow(Icons.Default.Memory, "Technology", batteryInfo.technology)
            }
        }

        item {
            InfoCard(
                title = "Memory & Storage",
                icon = Icons.Default.Storage
            ) {
                // RAM
                val totalRamGB = systemInfo.totalRam / (1024f * 1024f * 1024f)
                val availRamGB = systemInfo.availableRam / (1024f * 1024f * 1024f)
                val usedRamGB = totalRamGB - availRamGB
                val ramProgress = usedRamGB / totalRamGB

                Text("RAM Usage")
                LinearProgressIndicator(
                    progress = ramProgress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format("%.2f/%.2f GB (%.1f%%)", usedRamGB, totalRamGB, ramProgress * 100),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // ZRAM (tambahkan jika ada yang didapat dalam SystemInfo)
                if (systemInfo.zramSize > 0) {
                    val zramGB = systemInfo.zramSize / (1024f * 1024f * 1024f)
                    InfoIconRow(
                        Icons.Default.Memory,
                        "ZRAM",
                        String.format("%.2f GB", zramGB)
                    )
                }

                if (systemInfo.swapTotal > 0L) {
                    val swapGB = systemInfo.swapTotal / (1024f * 1024f * 1024f)
                    InfoIconRow(
                        Icons.Default.Memory,
                        "Swap",
                        String.format("%.2f GB", swapGB)
                    )
                }

                // STORAGE
                val totalStorageGB = systemInfo.totalStorage / (1024f * 1024f * 1024f)
                val availStorageGB = systemInfo.availableStorage / (1024f * 1024f * 1024f)
                val usedStorageGB = totalStorageGB - availStorageGB
                val storageProgress = usedStorageGB / totalStorageGB

                Text("Storage Usage")
                LinearProgressIndicator(
                    progress = storageProgress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format("%.2f/%.2f GB (%.1f%%)", usedStorageGB, totalStorageGB, storageProgress * 100),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }




        item {
            InfoCard(
                title = "System Information",
                icon = Icons.Default.PhoneAndroid,
                content = {
                    InfoIconRow(Icons.Default.Info, "Android", systemInfo.androidVersion)
                    InfoIconRow(Icons.Default.Dashboard, "ABI", systemInfo.abi)
                    InfoIconRow(Icons.Default.Settings, "Kernel", systemInfo.kernelVersion)
                    InfoIconRow(Icons.Default.PhoneIphone, "Device", systemInfo.deviceModel)
                    InfoIconRow(Icons.Default.Verified, "Build", systemInfo.fingerprint)
                    InfoIconRow(Icons.Default.Security, "SELinux", systemInfo.selinux)
                }
            )
        }
    }
}


@Composable
fun InfoIconRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    EnhancedCard(onClick = { isExpanded = !isExpanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                content()
            }
        }
    }
}
