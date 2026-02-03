package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.WavyProgressIndicator
import java.util.Locale

/**
 * Material Design card displaying memory information
 * Shows RAM, ZRAM/Swap, and Internal Storage usage
 */
@Composable
fun MaterialMemoryCard(systemInfo: SystemInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id.xms.xtrakernelmanager.R.string.material_memory_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            // RAM Section
            val ramUsed = (systemInfo.totalRam - systemInfo.availableRam)
            val ramTotal = systemInfo.totalRam
            val ramProgress = if (ramTotal > 0) ramUsed.toFloat() / ramTotal.toFloat() else 0f

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.material_memory_ram),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "${formatFileSize(ramUsed)} / ${formatFileSize(ramTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }

                WavyProgressIndicator(
                    progress = ramProgress,
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp,
                )
            }

            // ZRAM / Swap Section (Show if Swap OR ZRAM exists)
            val showZram = systemInfo.swapTotal > 0 || systemInfo.zramSize > 0

            if (showZram) {
                // Prefer Swap stats if available, otherwise fallback to ZRAM capacity with 0 usage
                val swapTotal = if (systemInfo.swapTotal > 0) systemInfo.swapTotal else systemInfo.zramSize
                val swapUsed = if (systemInfo.swapTotal > 0) (systemInfo.swapTotal - systemInfo.swapFree) else systemInfo.zramUsed
                val swapProgress = if (swapTotal > 0) swapUsed.toFloat() / swapTotal.toFloat() else 0f

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = if (systemInfo.zramSize > 0) stringResource(id.xms.xtrakernelmanager.R.string.material_memory_zram) else stringResource(id.xms.xtrakernelmanager.R.string.material_memory_swap),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "${formatFileSize(swapUsed)} / ${formatFileSize(swapTotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                    }

                    WavyProgressIndicator(
                        progress = swapProgress,
                        modifier = Modifier.fillMaxWidth().height(16.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        strokeWidth = 4.dp,
                        amplitude = 4.dp,
                    )
                }
            }

            // Internal Storage Section
            val storageUsed = (systemInfo.totalStorage - systemInfo.availableStorage)
            val storageTotal = systemInfo.totalStorage
            val storageProgress = if (storageTotal > 0) storageUsed.toFloat() / storageTotal.toFloat() else 0f

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.material_memory_internal_storage),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "${formatFileSize(storageUsed)} / ${formatFileSize(storageTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }

                WavyProgressIndicator(
                    progress = storageProgress,
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp,
                )
            }
        }
    }
}

/**
 * Formats file size from bytes to human-readable format
 * Returns GB if >= 1GB, otherwise MB
 */
private fun formatFileSize(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        String.format(Locale.US, "%.0f MB", mb)
    }
}
