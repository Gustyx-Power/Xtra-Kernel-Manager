package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.KernelInfo

@Composable
fun KernelCard(
    k: KernelInfo,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with MD3 styling
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp, 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.kernel),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(R.string.kernel_information),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Information sections with categorized layout
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Kernel Details
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Section header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Memory,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Kernel Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Kernel information items
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Version
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Version",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.version,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // Type
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Type",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = getKernelTypeByVersion(k.version),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // I/O Scheduler
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "I/O Scheduler",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.scheduler,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // System Architecture
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Section header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Computer,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "System Architecture",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Architecture information items
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // ABI
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ABI",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.abi,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // Architecture
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Architecture",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.architecture,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Security
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Section header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Security,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Security",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Security information items
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // SELinux
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SELinux",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.selinuxStatus,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = getSelinuxColor(k.selinuxStatus),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // KernelSU
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "KernelSU",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.4f)
                                        )
                                        Text(
                                            text = k.kernelSuStatus,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = getKernelSuColor(k.kernelSuStatus),
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(0.6f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Close button with MD3 style
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

                Surface(
                modifier = modifier,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section with MD3 styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.kernel),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.kernel),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "System Information",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick info grid with MD3 cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 20.dp)
            ) {
                // Process kernel version to extract clean version info
                val shortenedVersion = shortenKernelVersion(k.version)

                // Single card: Kernel Version (full width)
                CompactInfoCard(
                    label = stringResource(R.string.version, shortenedVersion),
                    value = "",
                    icon = Icons.Filled.Memory,
                    modifier = Modifier.fillMaxWidth()
                )

                // Single card: GKI Type (full width)
                CompactInfoCard(
                    label = stringResource(R.string.kernel_type, getKernelTypeByVersion(k.version)),
                    value = "",
                    icon = Icons.Filled.Computer,
                    modifier = Modifier.fillMaxWidth()
                )

                // Single card: I/O Scheduler (full width)
                CompactInfoCard(
                    label = stringResource(R.string.sched, k.scheduler),
                    value = "",
                    icon = Icons.Filled.Settings,
                    modifier = Modifier.fillMaxWidth()
                )

                // Row: ABI and Architecture
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactInfoCard(
                        label = "ABI",
                        value = k.abi,
                        icon = Icons.Filled.Computer,
                        modifier = Modifier.weight(1f)
                    )
                    CompactInfoCard(
                        label = "Architecture",
                        value = k.architecture,
                        icon = Icons.Filled.Memory,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row: SELinux and KernelSU (highlighted with colors)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactInfoCard(
                        label = "SELinux",
                        value = k.selinuxStatus,
                        icon = Icons.Filled.Shield,
                        valueColor = getSelinuxColor(k.selinuxStatus),
                        modifier = Modifier.weight(1f)
                    )
                    CompactInfoCard(
                        label = "KernelSU",
                        value = when {
                            k.kernelSuStatus.contains("Version", ignoreCase = true) -> "✓ " + k.kernelSuStatus.substringAfter("Version ").take(8)
                            k.kernelSuStatus.contains("Active", ignoreCase = true) -> {
                                val inside = k.kernelSuStatus.substringAfter("(", "").substringBefore(")")
                                if (inside.isNotBlank()) "✓ $inside" else "✓ Active"
                            }
                            k.kernelSuStatus.contains("Detected", ignoreCase = true) -> "✓ Detected"
                            else -> "✗ Not Found"
                        },
                        icon = Icons.Filled.AdminPanelSettings,
                        valueColor = getKernelSuColor(k.kernelSuStatus),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactInfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with MD3 styling using dynamic colors based on label
            val iconColor = when {
                label.contains("version", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                label.contains("type", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                label.contains("abi", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                label.contains("arch", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                label.contains("selinux", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                label.contains("kernelsu", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            
            Surface(
                modifier = Modifier.size(36.dp),
                color = iconColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (iconColor == MaterialTheme.colorScheme.errorContainer)
                            MaterialTheme.colorScheme.onErrorContainer
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Jika value tidak kosong, tampilkan label dan value terpisah
                // Jika value kosong, anggap label sudah berisi format lengkap
                if (value.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Untuk kasus format string seperti "Version: 4.19.0"
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Composable
private fun getSelinuxColor(status: String): Color {
    return when (status.lowercase()) {
        "enforcing" -> MaterialTheme.colorScheme.primary // Green tone from dynamic colors
        "permissive" -> MaterialTheme.colorScheme.tertiary // Orange tone from dynamic colors
        "disabled" -> MaterialTheme.colorScheme.error // Red tone from dynamic colors
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun getKernelSuColor(status: String): Color {
    return when {
        status.contains("Version", ignoreCase = true) -> MaterialTheme.colorScheme.primary // Green tone from dynamic colors
        status.contains("Active", ignoreCase = true) -> MaterialTheme.colorScheme.primary // Green tone from dynamic colors
        status.contains("Detected", ignoreCase = true) -> MaterialTheme.colorScheme.secondary // Blue tone from dynamic colors
        status.contains("Not Detected", ignoreCase = true) -> MaterialTheme.colorScheme.outline // Gray tone from dynamic colors
        else -> MaterialTheme.colorScheme.onSurface
    }
}

// Utility function to shorten kernel version format
private fun shortenKernelVersion(version: String): String {
    // Extract version number and kernel name
    val versionRegex = """Linux version ([\d.]+)-([^ ]+)""".toRegex()
    val matchResult = versionRegex.find(version)
    
    return if (matchResult != null) {
        val versionNumber = matchResult.groupValues[1]
        val kernelName = matchResult.groupValues[2]
        "$versionNumber-$kernelName"
    } else {
        // Fallback: try to extract version info before hash or parentheses
        val hashIndex = version.indexOf(" #")
        val parenIndex = version.indexOf(" (")
        val endIndex = when {
            hashIndex != -1 && parenIndex != -1 -> minOf(hashIndex, parenIndex)
            hashIndex != -1 -> hashIndex
            parenIndex != -1 -> parenIndex
            else -> version.length
        }
        version.substring(0, endIndex).trim()
    }
}

// Utility function to determine kernel type based on version
private fun getKernelTypeByVersion(version: String): String {
    // Debug logging
    android.util.Log.d("KernelCard", "getKernelTypeByVersion called with version: '$version'")
    
    // Extract version number
    val versionRegex = """Linux version ([\d.]+)""".toRegex()
    val matchResult = versionRegex.find(version)
    
    if (matchResult != null) {
        val versionNumber = matchResult.groupValues[1]
        android.util.Log.d("KernelCard", "Extracted version number: '$versionNumber'")
        
        // Clean the version number to remove any non-printable characters and normalize dots
        val cleanVersionNumber = versionNumber.trim()
            .replace(Regex("""[^\d.]"""), "") // Hanya biarkan angka dan titik
            .replace("..", ".") // Ganti double titik dengan satu titik (cara manual)
            .replace(Regex("""\.{2,}"""), ".") // Ganti multiple titik dengan satu titik
            .replace(Regex("""[^\x00-\x7F]"""), "") // Hapus karakter non-ASCII
            .trim('.', '.')
        
        // Jika ada multiple titik, ambil hanya bagian sebelum dan setelah titik pertama
        val finalVersionNumber = if (cleanVersionNumber.count { it == '.' } > 1) {
            val firstDotIndex = cleanVersionNumber.indexOf('.')
            val secondDotIndex = cleanVersionNumber.indexOf('.', firstDotIndex + 1)
            if (secondDotIndex != -1) {
                cleanVersionNumber.substring(0, secondDotIndex)
            } else {
                cleanVersionNumber
            }
        } else {
            cleanVersionNumber
        }
        
        // Debug: Print each character and its code
        android.util.Log.d("KernelCard", "Final cleaned version number: '$finalVersionNumber'")
        finalVersionNumber.forEachIndexed { index, char ->
            android.util.Log.d("KernelCard", "  Character $index: '$char' (code: ${char.toInt()})")
        }
        
        // Parse version as string parts for more accurate comparison
        val versionParts = finalVersionNumber.split(".").map { it.toIntOrNull() ?: 0 }
        android.util.Log.d("KernelCard", "Version parts: $versionParts")
        
        val result = when {
            versionParts.size >= 2 && (versionParts[0] < 4 || (versionParts[0] == 4 && versionParts[1] < 19)) -> {
                android.util.Log.d("KernelCard", "Returning Legacy")
                "Legacy"
            }
            versionParts.size >= 2 && (versionParts[0] > 4 || (versionParts[0] == 4 && versionParts[1] >= 19)) && 
            (versionParts[0] < 5 || (versionParts[0] == 5 && versionParts[1] <= 4)) -> {
                android.util.Log.d("KernelCard", "Returning GKI1 (4.19-5.4)")
                "GKI1"
            }
            versionParts.size >= 2 && versionParts[0] == 5 && versionParts[1] > 4 && versionParts[1] < 10 -> {
                android.util.Log.d("KernelCard", "Returning GKI1 (5.4-5.10)")
                "GKI1"
            }
            versionParts.size >= 2 && (versionParts[0] > 5 || (versionParts[0] == 5 && versionParts[1] >= 10)) -> {
                android.util.Log.d("KernelCard", "Returning GKI2")
                "GKI2"
            }
            else -> {
                android.util.Log.d("KernelCard", "Returning Unknown (else case)")
                "Unknown"
            }
        }
        
        android.util.Log.d("KernelCard", "Final result: $result")
        return result
    }
    
    android.util.Log.d("KernelCard", "No match found with primary regex")
    
    // Fallback: if we can't parse with the regex, try to extract version in a simpler way
    val simpleVersionRegex = """(\d+\.\d+)""".toRegex()
    val simpleMatch = simpleVersionRegex.find(version)
    
    if (simpleMatch != null) {
        val versionNumber = simpleMatch.groupValues[1]
        android.util.Log.d("KernelCard", "Fallback extracted version number: '$versionNumber'")
        
        // Clean the version number to remove any non-printable characters and normalize dots
        val cleanVersionNumber = versionNumber.trim()
            .replace(Regex("""[^\d.]"""), "") // Hanya biarkan angka dan titik
            .replace("..", ".") // Ganti double titik dengan satu titik (cara manual)
            .replace(Regex("""\.{2,}"""), ".") // Ganti multiple titik dengan satu titik
            .replace(Regex("""[^\x00-\x7F]"""), "") // Hapus karakter non-ASCII
            .trim('.', '.')
        
        // Jika ada multiple titik, ambil hanya bagian sebelum dan setelah titik pertama
        val finalVersionNumber = if (cleanVersionNumber.count { it == '.' } > 1) {
            val firstDotIndex = cleanVersionNumber.indexOf('.')
            val secondDotIndex = cleanVersionNumber.indexOf('.', firstDotIndex + 1)
            if (secondDotIndex != -1) {
                cleanVersionNumber.substring(0, secondDotIndex)
            } else {
                cleanVersionNumber
            }
        } else {
            cleanVersionNumber
        }
        
        // Debug: Print each character and its code
        android.util.Log.d("KernelCard", "Fallback final cleaned version number: '$finalVersionNumber'")
        finalVersionNumber.forEachIndexed { index, char ->
            android.util.Log.d("KernelCard", "  Character $index: '$char' (code: ${char.toInt()})")
        }
        
        // Parse version as string parts for more accurate comparison
        val versionParts = finalVersionNumber.split(".").map { it.toIntOrNull() ?: 0 }
        android.util.Log.d("KernelCard", "Fallback version parts: $versionParts")
        
        val result = when {
            versionParts.size >= 2 && (versionParts[0] < 4 || (versionParts[0] == 4 && versionParts[1] < 19)) -> {
                android.util.Log.d("KernelCard", "Fallback returning Legacy")
                "Legacy"
            }
            versionParts.size >= 2 && (versionParts[0] > 4 || (versionParts[0] == 4 && versionParts[1] >= 19)) && 
            (versionParts[0] < 5 || (versionParts[0] == 5 && versionParts[1] <= 4)) -> {
                android.util.Log.d("KernelCard", "Fallback returning GKI1 (4.19-5.4)")
                "GKI1"
            }
            versionParts.size >= 2 && versionParts[0] == 5 && versionParts[1] > 4 && versionParts[1] < 10 -> {
                android.util.Log.d("KernelCard", "Fallback returning GKI1 (5.4-5.10)")
                "GKI1"
            }
            versionParts.size >= 2 && (versionParts[0] > 5 || (versionParts[0] == 5 && versionParts[1] >= 10)) -> {
                android.util.Log.d("KernelCard", "Fallback returning GKI2")
                "GKI2"
            }
            else -> {
                android.util.Log.d("KernelCard", "Fallback returning Unknown (else case)")
                "Unknown"
            }
        }
        
        android.util.Log.d("KernelCard", "Fallback final result: $result")
        return result
    }
    
    android.util.Log.d("KernelCard", "No match found with fallback regex, returning Unknown")
    return "Unknown"
}