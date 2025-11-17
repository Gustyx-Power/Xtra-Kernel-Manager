package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.PillCard

@SuppressLint("DefaultLocale")
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

    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillCard(text = "Xtra Kernel Manager • v${BuildConfig.VERSION_NAME}")
            }
        }

        // CPU
        item {
            CPUInfoCardNoDropdown(cpuInfo = cpuInfo)
        }

        // GPU
        item {
            InfoCard(
                title = stringResource(R.string.gpu_information),
                icon = Icons.Default.Videocam,
                defaultExpanded = false
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary atas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${gpuInfo.currentFreq} MHz",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.current_frequency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            InfoChip(
                                icon = Icons.Default.Bolt,
                                text = "Max ${gpuInfo.maxFreq} MHz"
                            )
                            InfoChip(
                                icon = Icons.Default.Memory,
                                text = gpuInfo.vendor.ifBlank { stringResource(R.string.unknown_gpu) }
                            )
                        }
                    }

                    Text(
                        text = gpuInfo.openglVersion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Frequency grid
                    if (gpuInfo.availableFreqs.size > 1) {
                        Text(
                            stringResource(R.string.frequencies),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            gpuInfo.availableFreqs.chunked(4).forEachIndexed { rowIdx, rowFreqs ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowFreqs.forEachIndexed { i, freq ->
                                        val isCurrent = freq == gpuInfo.currentFreq
                                        Column(
                                            modifier = Modifier
                                                .width(70.dp)
                                                .padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isCurrent)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = "$freq MHz",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "GPU${rowIdx * 4 + i}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isCurrent)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (gpuInfo.currentFreq > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${gpuInfo.currentFreq} MHz",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("GPU", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Detail bawah
                    InfoIconRow(Icons.Default.Memory, "Vendor", gpuInfo.vendor)
                    InfoIconRow(Icons.Default.Dashboard, "Renderer", gpuInfo.renderer)
                    InfoIconRow(Icons.Default.Bolt,
                        stringResource(R.string.max_freq), "${gpuInfo.maxFreq} MHz")
                    InfoIconRow(Icons.Default.Bolt,
                        stringResource(R.string.min_freq), "${gpuInfo.minFreq} MHz")
                }
            }
        }

        // Battery
        item {
            InfoCard(
                title = stringResource(R.string.battery_information),
                icon = Icons.Default.BatteryChargingFull,
                defaultExpanded = false
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BatteryLevelIndicator(
                        level = batteryInfo.level,
                        status = batteryInfo.status
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Health ${String.format("%.1f%%", batteryInfo.healthPercent)} • ${batteryInfo.health}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BatteryTag(
                                icon = Icons.Default.Power,
                                text = batteryInfo.status
                            )
                            BatteryTag(
                                icon = Icons.Default.Thermostat,
                                text = "${batteryInfo.temperature}°C"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoIconRow(
                            Icons.Default.FlashOn,
                            stringResource(R.string.current_now),
                            "${batteryInfo.currentNow} mA"
                        )
                        InfoIconRow(
                            Icons.Default.BatteryStd,
                            stringResource(R.string.voltage),
                            "${batteryInfo.voltage} mV"
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoIconRow(
                            Icons.Default.Refresh,
                            stringResource(R.string.cycle_count),
                            batteryInfo.cycleCount.toString()
                        )
                        InfoIconRow(
                            Icons.Default.Memory,
                            stringResource(R.string.technology),
                            batteryInfo.technology
                        )
                    }
                }
            }
        }

        // Memory & Storage
        item {
            InfoCard(
                title = stringResource(id = R.string.memory_storage),
                icon = Icons.Default.Storage,
                defaultExpanded = false
            ) {
                val totalRamGB = systemInfo.totalRam / (1024f * 1024f * 1024f)
                val availRamGB = systemInfo.availableRam / (1024f * 1024f * 1024f)
                val usedRamGB = totalRamGB - availRamGB
                val ramProgress = (usedRamGB / totalRamGB).coerceIn(0f, 1f)
                val ramPercent = (ramProgress * 100f).coerceIn(0f, 100f)

                val totalStorageGB = systemInfo.totalStorage / (1024f * 1024f * 1024f)
                val availStorageGB = systemInfo.availableStorage / (1024f * 1024f * 1024f)
                val usedStorageGB = totalStorageGB - availStorageGB
                val storageProgress = (usedStorageGB / totalStorageGB).coerceIn(0f, 1f)
                val storagePercent = (storageProgress * 100f).coerceIn(0f, 100f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UsageStatCard(
                        title = "RAM",
                        primaryValue = String.format("%.2f GB", usedRamGB),
                        secondaryValue = String.format("of %.2f GB", totalRamGB),
                        percent = ramPercent,
                        modifier = Modifier.weight(1f),
                        isWarning = ramPercent > 80f
                    )

                    UsageStatCard(
                        title = stringResource(R.string.storage),
                        primaryValue = String.format("%.2f GB", usedStorageGB),
                        secondaryValue = String.format("of %.2f GB", totalStorageGB),
                        percent = storagePercent,
                        modifier = Modifier.weight(1f),
                        isWarning = storagePercent > 85f
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ram_usage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    UsageProgressBar(
                        progress = ramProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = String.format(
                            "%.2f / %.2f GB (%.1f%%)",
                            usedRamGB,
                            totalRamGB,
                            ramPercent
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.storage_usage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    UsageProgressBar(
                        progress = storageProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = String.format(
                            "%.2f / %.2f GB (%.1f%%)",
                            usedStorageGB,
                            totalStorageGB,
                            storagePercent
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (systemInfo.zramSize > 0) {
                        val zramGB = systemInfo.zramSize / (1024f * 1024f * 1024f)
                        MemoryTagChip(
                            icon = Icons.Default.Memory,
                            label = "ZRAM",
                            value = String.format("%.2f GB", zramGB)
                        )
                    }

                    if (systemInfo.swapTotal > 0L) {
                        val swapGB = systemInfo.swapTotal / (1024f * 1024f * 1024f)
                        MemoryTagChip(
                            icon = Icons.Default.SwapHoriz,
                            label = "Swap",
                            value = String.format("%.2f GB", swapGB)
                        )
                    }
                }
            }
        }

        // System Information
        item {
            InfoCard(
                title = stringResource(R.string.system_information),
                icon = Icons.Default.PhoneAndroid,
                defaultExpanded = false
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 8.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Android ${systemInfo.androidVersion}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = systemInfo.deviceModel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = systemInfo.abi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        InfoChip(
                            icon = Icons.Default.Settings,
                            text = systemInfo.kernelVersion
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoChip(
                                icon = Icons.Default.Verified,
                                text = "Build: ${systemInfo.fingerprint.takeLast(11)}"
                            )
                            InfoChip(
                                icon = Icons.Default.Security,
                                text = "SELinux: ${systemInfo.selinux}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassmorphicCircularProgress(
    progress: Float,
    label: String,
    percentage: String,
    color: Color
) {
    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(90.dp)
                .drawBehind {
                    drawCircle(
                        color = color.copy(alpha = 0.1f),
                        radius = size.minDimension / 2f + 4.dp.toPx()
                    )
                }
        ) {
            drawCircle(
                color = color.copy(alpha = 0.15f),
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = percentage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CPUInfoCardNoDropdown(cpuInfo: CPUInfo) {
    var isExpanded by remember { mutableStateOf(true) }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = stringResource(R.string.cpu_information),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    icon = Icons.Default.Thermostat,
                    text = "${cpuInfo.temperature}°C"
                )
                InfoChip(
                    icon = Icons.Default.Speed,
                    text = stringResource(R.string.load, String.format("%.1f", cpuInfo.totalLoad))
                )
                InfoChip(
                    icon = Icons.Default.Dashboard,
                    text = cpuInfo.cores.firstOrNull()?.governor ?: stringResource(R.string.unknown)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        stringResource(R.string.clockspeed_per_core),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        val rows = cpuInfo.cores.chunked(4)
                        val maxFreq = cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0

                        rows.forEach { rowCores ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowCores.forEach { core ->
                                    val isHotCore =
                                        core.isOnline && core.currentFreq == maxFreq

                                    Column(
                                        modifier = Modifier.width(70.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = when {
                                                !core.isOnline ->
                                                    MaterialTheme.colorScheme.outline
                                                isHotCore ->
                                                    MaterialTheme.colorScheme.primary
                                                else ->
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            }
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
                                            color = if (isHotCore)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    InfoIconRow(
                        Icons.Default.Adb,
                        "Online",
                        "${cpuInfo.cores.count { it.isOnline }}/${cpuInfo.cores.size}"
                    )
                }
            }
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
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    AssistChip(
        onClick = { },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun BatteryLevelIndicator(
    level: Int,
    status: String,
    modifier: Modifier = Modifier
) {
    val clamped = level.coerceIn(0, 100)

    val fillColor = when {
        clamped <= 15 -> MaterialTheme.colorScheme.error
        clamped <= 40 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(76.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(clamped / 100f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(fillColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BatteryTag(
    icon: ImageVector,
    text: String
) {
    InfoChip(icon = icon, text = text)
}

@Composable
private fun UsageStatCard(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    percent: Float,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val pctText = String.format("%.0f%%", percent)

    val highlightColor =
        if (isWarning) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsageProgressBar(
                    progress = (percent / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f),
                    color = highlightColor
                )
                Text(
                    text = pctText,
                    style = MaterialTheme.typography.labelMedium,
                    color = highlightColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UsageProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(color)
        )
    }
}

@Composable
private fun MemoryTagChip(
    icon: ImageVector,
    label: String,
    value: String
) {
    AssistChip(
        onClick = { },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text(
                text = "$label • $value",
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    defaultExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
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
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
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
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
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