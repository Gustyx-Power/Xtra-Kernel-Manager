package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.DeepSleepInfo
import id.xms.xtrakernelmanager.data.model.MemoryInfo
import id.xms.xtrakernelmanager.data.model.StorageInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import java.util.Locale

// Helper function to format time duration with seconds
private fun formatTimeWithSeconds(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

// Helper function to format storage size
private fun formatStorageSize(bytes: Long): String {
    val tb = 1024L * 1024L * 1024L * 1024L
    val gb = 1024L * 1024L * 1024L
    val mb = 1024L * 1024L
    val kb = 1024L

    return when {
        bytes >= tb -> String.format(Locale.getDefault(), "%.1f TB", bytes.toDouble() / tb)
        bytes >= gb -> String.format(Locale.getDefault(), "%.1f GB", bytes.toDouble() / gb)
        bytes >= mb -> String.format(Locale.getDefault(), "%.1f MB", bytes.toDouble() / mb)
        bytes >= kb -> String.format(Locale.getDefault(), "%.1f KB", bytes.toDouble() / kb)
        else -> "$bytes B"
    }
}

@Composable
fun MergedSystemCard(
    b: BatteryInfo,
    d: DeepSleepInfo,
    rooted: Boolean,
    version: String,
    mem: MemoryInfo,
    systemInfo: SystemInfo,
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    // Main container with separated cards
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Separate Battery Card
        BatteryCard(
            batteryInfo = b,
        )

        // Separate Memory Card
        MemoryCard(
            memoryInfo = mem,
        )

        // Separate Storage Card
        StorageCard(
            storageInfo = storageInfo,
        )

        // Device Information Card
        DeviceInfoCard(
            systemInfo = systemInfo,
            rooted = rooted,
            version = version,
            storageInfo = storageInfo
        )
    }
}

@Composable
private fun BatteryCard(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery Header Section
            BatteryHeaderSection(batteryInfo = batteryInfo)

            // Battery Progress Section
            BatteryProgressSection(batteryInfo = batteryInfo)

            // Battery Stats Section
            BatteryStatsSection(batteryInfo = batteryInfo)
        }
    }
}

@Composable
private fun MemoryCard(
    memoryInfo: MemoryInfo,
    modifier: Modifier = Modifier
) {
    val usedPercentage = ((memoryInfo.used.toDouble() / memoryInfo.total.toDouble()) * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memory Header Section
            MemoryHeaderSection(memoryInfo = memoryInfo, usedPercentage = usedPercentage)

            // Memory Progress Section
            MemoryProgressSection(memoryInfo = memoryInfo, usedPercentage = usedPercentage)

            // Memory Stats Section
            MemoryStatsSection(memoryInfo = memoryInfo)
        }
    }
}

@Composable
private fun StorageCard(
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    val usedPercentage = ((storageInfo.usedSpace.toDouble() / storageInfo.totalSpace.toDouble()) * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Header Section
            StorageHeaderSection(storageInfo = storageInfo, usedPercentage = usedPercentage)

            // Storage Progress Section
            StorageProgressSection(storageInfo = storageInfo)

            // Storage Stats Section
            StorageStatsSection(storageInfo = storageInfo)
        }
    }
}

@Composable
private fun BatteryHeaderSection(
    batteryInfo: BatteryInfo
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Battery Status",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Battery Status Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${batteryInfo.level}% • ${if (batteryInfo.isCharging) "Charging" else "Discharging"}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        // Battery Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MemoryHeaderSection(
    memoryInfo: MemoryInfo,
    usedPercentage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Memory Status",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Memory Status Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val totalGb = (memoryInfo.total / (1024 * 1024 * 1024))
                val zramGb = (memoryInfo.zramTotal / (1024 * 1024 * 1024))
                val swapGb = (memoryInfo.swapTotal / (1024 * 1024 * 1024))

                val memoryText = buildString {
                    append("${usedPercentage}% Used • ${totalGb}GB")
                    if (zramGb > 0) append(" + ${zramGb}GB Zram")
                    if (swapGb > 0) append(" + ${swapGb}GB Swap")
                }

                Text(
                    text = memoryText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        // Memory Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "Memory",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StorageHeaderSection(
    storageInfo: StorageInfo,
    usedPercentage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Storage Status",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Storage Status Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${usedPercentage}% Used • ${formatStorageSize(storageInfo.totalSpace)} Total",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        // Storage Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Storage",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun BatteryProgressSection(
    batteryInfo: BatteryInfo
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Charge Level",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${batteryInfo.level}%",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Battery Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val progressColor = when {
                batteryInfo.level > 70 -> MaterialTheme.colorScheme.primary
                batteryInfo.level > 30 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(batteryInfo.level / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(progressColor)
            )
        }
    }
}

@Composable
private fun MemoryProgressSection(
    memoryInfo: MemoryInfo,
    usedPercentage: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // RAM Usage Progress Bar
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memory",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "RAM Usage",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${usedPercentage}%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val progressColor = when {
                    usedPercentage < 60 -> MaterialTheme.colorScheme.primary
                    usedPercentage < 80 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usedPercentage / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(progressColor)
                )
            }
        }

        // ZRAM Usage Progress Bar (only show if zram is available)
        if (memoryInfo.zramTotal > 0) {
            val zramUsedPercentage = ((memoryInfo.zramUsed.toDouble() / memoryInfo.zramTotal.toDouble()) * 100).toInt()

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = "Zram",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ZRAM Usage",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${zramUsedPercentage}%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(zramUsedPercentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }

        // Swap Usage Progress Bar (only show if swap is available)
        if (memoryInfo.swapTotal > 0) {
            val swapUsedPercentage = ((memoryInfo.swapUsed.toDouble() / memoryInfo.swapTotal.toDouble()) * 100).toInt()

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Swap Usage",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${swapUsedPercentage}%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(swapUsedPercentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryStatsSection(
    batteryInfo: BatteryInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery Stats Row 1 - Temperature and Voltage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Temperature
                SystemStatItem(
                    icon = Icons.Default.Thermostat,
                    label = "Temperature",
                    value = "${String.format(Locale.getDefault(), "%.1f", batteryInfo.temp)}°C",
                    modifier = Modifier.weight(1f)
                )

                // Voltage
                SystemStatItem(
                    icon = Icons.Default.ElectricBolt,
                    label = "Voltage",
                    value = run {
                        val formattedVoltage = if (batteryInfo.voltage > 0) {
                            val voltageInVolts = when {
                                batteryInfo.voltage > 1000000 -> batteryInfo.voltage / 1000000f
                                batteryInfo.voltage > 1000 -> batteryInfo.voltage / 1000f
                                else -> batteryInfo.voltage
                            }
                            String.format(Locale.getDefault(), "%.2f", voltageInVolts).trimEnd('0').trimEnd('.')
                        } else "0"
                        "${formattedVoltage}V"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Battery Stats Row 2 - Health and Cycles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Battery Health
                SystemStatItem(
                    icon = Icons.Default.HealthAndSafety,
                    label = "Health",
                    value = if (batteryInfo.healthPercentage > 0) "${batteryInfo.healthPercentage}%" else batteryInfo.health,
                    modifier = Modifier.weight(1f)
                )

                // Battery Cycles
                SystemStatItem(
                    icon = Icons.Default.Autorenew,
                    label = "Cycles",
                    value = if (batteryInfo.cycleCount > 0) "${batteryInfo.cycleCount}" else "N/A",
                    modifier = Modifier.weight(1f)
                )
            }

            // Battery Stats Row 3 - Technology and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Battery Technology
                SystemStatItem(
                    icon = Icons.Default.Science,
                    label = "Technology",
                    value = batteryInfo.technology,
                    modifier = Modifier.weight(1f)
                )

                // Battery Status
                SystemStatItem(
                    icon = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                    label = "Status",
                    value = batteryInfo.status,
                    modifier = Modifier.weight(1f)
                )
            }

            // Battery Stats Row 4 - Current Capacity and Design Capacity (combined in one row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Capacity
                SystemStatItem(
                    icon = Icons.Default.Battery6Bar,
                    label = "Current Cap",
                    value = if (batteryInfo.currentCapacity > 0) "${batteryInfo.currentCapacity}mAh" else "N/A",
                    modifier = Modifier.weight(1f)
                )

                // Design Capacity (moved to same row to utilize space efficiently)
                SystemStatItem(
                    icon = Icons.Default.BatterySaver,
                    label = "Design Cap",
                    value = if (batteryInfo.capacity > 0) "${batteryInfo.capacity}mAh" else "N/A",
                    modifier = Modifier.weight(1f)
                )
            }

            // Additional info if current is available
            if (batteryInfo.current != 0f) {
                val currentMa = batteryInfo.current / 1000
                val displayCurrent = kotlin.math.abs(currentMa)
                SystemStatItem(
                    icon = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                    label = "Current",
                    value = "${String.format(Locale.getDefault(), "%.0f", displayCurrent)}mA",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MemoryStatsSection(
    memoryInfo: MemoryInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memory Stats Row 1 - Used and Free RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Used RAM
                SystemStatItem(
                    icon = Icons.Default.Memory,
                    label = "Used RAM",
                    value = "${memoryInfo.used / (1024 * 1024)}MB",
                    modifier = Modifier.weight(1f)
                )

                // Free RAM
                SystemStatItem(
                    icon = Icons.Default.Storage,
                    label = "Free RAM",
                    value = "${memoryInfo.free / (1024 * 1024)}MB",
                    modifier = Modifier.weight(1f)
                )
            }

            // Memory Stats Row 2 - Total RAM and Usage Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total RAM
                SystemStatItem(
                    icon = Icons.Default.Widgets,
                    label = "Total RAM",
                    value = "${memoryInfo.total / (1024 * 1024)}MB",
                    modifier = Modifier.weight(1f)
                )

                // Usage Percentage
                SystemStatItem(
                    icon = Icons.Default.Analytics,
                    label = "Usage %",
                    value = "${((memoryInfo.used.toDouble() / memoryInfo.total.toDouble()) * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            // Memory Stats Row 3 - ZRAM Stats (only show if zram is available)
            if (memoryInfo.zramTotal > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ZRAM Used
                    SystemStatItem(
                        icon = Icons.Default.Compress,
                        label = "ZRAM Used",
                        value = "${memoryInfo.zramUsed / (1024 * 1024)}MB",
                        modifier = Modifier.weight(1f)
                    )

                    // ZRAM Total
                    SystemStatItem(
                        icon = Icons.Default.Compress,
                        label = "ZRAM Total",
                        value = "${memoryInfo.zramTotal / (1024 * 1024)}MB",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Memory Stats Row 4 - Swap Stats (only show if swap is available)
            if (memoryInfo.swapTotal > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Swap Used
                    SystemStatItem(
                        icon = Icons.Default.SwapHoriz,
                        label = "Swap Used",
                        value = "${memoryInfo.swapUsed / (1024 * 1024)}MB",
                        modifier = Modifier.weight(1f)
                    )

                    // Swap Total
                    SystemStatItem(
                        icon = Icons.Default.SwapHoriz,
                        label = "Swap Total",
                        value = "${memoryInfo.swapTotal / (1024 * 1024)}MB",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeviceInfoCard(
    systemInfo: SystemInfo,
    rooted: Boolean,
    version: String,
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Root Status Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (rooted) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (rooted) "Rooted" else "Not Rooted",
                                color = if (rooted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        // Version Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "v$version",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // Device Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone,
                        contentDescription = "Device",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Device Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Device Info Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.PhoneAndroid,
                            label = "Model",
                            value = systemInfo.model,
                            modifier = Modifier.weight(1f)
                        )

                        SystemStatItem(
                            icon = Icons.Default.Code,
                            label = "Codename",
                            value = systemInfo.codename,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Device Info Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.Android,
                            label = "Android",
                            value = systemInfo.androidVersion,
                            modifier = Modifier.weight(1f)
                        )

                        SystemStatItem(
                            icon = Icons.Default.Build,
                            label = "SDK",
                            value = systemInfo.sdk.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Device Info Row 3 - SoC and Fingerprint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.DeveloperBoard,
                            label = "SoC",
                            value = systemInfo.soc,
                            modifier = Modifier.weight(1f)
                        )

                        SystemStatItem(
                            icon = Icons.Default.Fingerprint,
                            label = "Fingerprint",
                            value = systemInfo.fingerprint.substringAfterLast("/").substringBefore(":"),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Device Info Row 4 - Display Resolution and Technology
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.AspectRatio,
                            label = "Resolution",
                            value = systemInfo.screenResolution,
                            modifier = Modifier.weight(1f)
                        )

                        SystemStatItem(
                            icon = Icons.Default.DisplaySettings,
                            label = "Technology",
                            value = systemInfo.displayTechnology,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Device Info Row 5 - Refresh Rate and DPI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.Speed,
                            label = "Refresh Rate",
                            value = systemInfo.refreshRate,
                            modifier = Modifier.weight(1f)
                        )

                        SystemStatItem(
                            icon = Icons.Default.PhotoSizeSelectSmall,
                            label = "DPI",
                            value = systemInfo.screenDpi,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Device Info Row 6 - GPU Renderer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SystemStatItem(
                            icon = Icons.Default.Videocam,
                            label = "GPU Renderer",
                            value = systemInfo.gpuRenderer,
                            modifier = Modifier.weight(1f)
                        )

                        // Empty placeholder to maintain layout balance
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageProgressSection(
    storageInfo: StorageInfo
) {
    val usedPercentage = if (storageInfo.totalSpace > 0) {
        ((storageInfo.usedSpace.toDouble() / storageInfo.totalSpace.toDouble()) * 100).toInt()
    } else 0

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Storage",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Internal Storage",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${formatStorageSize(storageInfo.usedSpace)} / ${formatStorageSize(storageInfo.totalSpace)}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Storage Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val progressColor = when {
                usedPercentage < 70 -> MaterialTheme.colorScheme.primary
                usedPercentage < 85 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(usedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(progressColor)
            )
        }

        // Storage Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Used: ${formatStorageSize(storageInfo.usedSpace)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Free: ${formatStorageSize(storageInfo.totalSpace - storageInfo.usedSpace)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StorageStatsSection(
    storageInfo: StorageInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Stats Row 1 - Used and Free
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Used Storage
                SystemStatItem(
                    icon = Icons.Default.Storage,
                    label = "Used Storage",
                    value = "${formatStorageSize(storageInfo.usedSpace)}",
                    modifier = Modifier.weight(1f)
                )

                // Free Storage
                SystemStatItem(
                    icon = Icons.Default.Storage,
                    label = "Free Storage",
                    value = "${formatStorageSize(storageInfo.totalSpace - storageInfo.usedSpace)}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Storage Stats Row 2 - Total and Usage Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Storage
                SystemStatItem(
                    icon = Icons.Default.Storage,
                    label = "Total Storage",
                    value = "${formatStorageSize(storageInfo.totalSpace)}",
                    modifier = Modifier.weight(1f)
                )

                // Usage Percentage
                SystemStatItem(
                    icon = Icons.Default.Analytics,
                    label = "Usage %",
                    value = "${((storageInfo.usedSpace.toDouble() / storageInfo.totalSpace.toDouble()) * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}