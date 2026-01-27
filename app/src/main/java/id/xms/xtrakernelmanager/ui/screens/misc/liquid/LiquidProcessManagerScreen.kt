package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.shared.ProcessInfo
import id.xms.xtrakernelmanager.ui.screens.misc.shared.loadRunningProcesses
import id.xms.xtrakernelmanager.ui.screens.misc.shared.getAppIconSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LiquidProcessManager"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidProcessManagerScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var processes by remember { mutableStateOf<List<ProcessInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isKilling by remember { mutableStateOf<String?>(null) }
    var sortByMemory by remember { mutableStateOf(true) }

    // Load processes on launch
    LaunchedEffect(Unit) {
        Log.d(TAG, "Loading processes on launch...")
        try {
            processes = loadRunningProcesses()
            Log.d(TAG, "Loaded ${processes.size} processes")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading processes", e)
        } finally {
            isLoading = false
        }
    }

    fun refreshProcesses() {
        scope.launch {
            Log.d(TAG, "Refreshing processes...")
            isLoading = true
            try {
                processes = loadRunningProcesses()
                Log.d(TAG, "Refreshed ${processes.size} processes")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing processes", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun killProcess(packageName: String) {
        scope.launch {
            Log.d(TAG, "Killing process: $packageName")
            isKilling = packageName
            try {
                val result = withContext(Dispatchers.IO) { 
                    RootManager.executeCommand("am force-stop $packageName") 
                }
                if (result.isSuccess) {
                    processes = processes.filter { it.packageName != packageName }
                    Log.d(TAG, "Successfully killed: $packageName")
                } else {
                    Log.w(TAG, "Failed to kill: $packageName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error killing process: $packageName", e)
            } finally {
                isKilling = null
            }
        }
    }

    val sortedProcesses = remember(processes, sortByMemory) {
        if (sortByMemory) {
            processes.sortedByDescending { it.memoryMB }
        } else {
            processes.sortedBy { it.packageName }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Custom Top Bar
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                LiquidProcessManagerTopBar(
                    onBack = onBack,
                    sortByMemory = sortByMemory,
                    onSortToggle = { sortByMemory = !sortByMemory },
                    onRefresh = { refreshProcesses() }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Loading processes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (processes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            null,
                            Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "All clear",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No running processes found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val totalMemory = processes.sumOf { it.memoryMB.toDouble() }.toFloat()
                        LiquidMemorySummaryCard(totalMemory, processes.size)
                    }

                    item {
                        Text(
                            "Running Processes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(sortedProcesses, key = { it.pid }) { process ->
                        Log.d(TAG, "LazyColumn composing item: ${process.packageName}")
                        LiquidProcessItem(
                            process = process,
                            isKilling = isKilling == process.packageName,
                            onKill = { killProcess(process.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidProcessManagerTopBar(
    onBack: () -> Unit,
    sortByMemory: Boolean,
    onSortToggle: () -> Unit,
    onRefresh: () -> Unit
) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp).clickable(onClick = onBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "Processes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Badge
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Monitor",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp).clickable(onClick = onSortToggle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (sortByMemory) Icons.Rounded.Memory else Icons.Rounded.SortByAlpha,
                            contentDescription = "Sort",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp).clickable(onClick = onRefresh)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidMemorySummaryCard(totalMemoryMB: Float, processCount: Int) {
    val totalDeviceRAM = 8192f
    val usagePercent = (totalMemoryMB / totalDeviceRAM).coerceIn(0f, 1f)

    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "RAM Usage",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", totalMemoryMB / 1024),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " GB",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Memory,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$processCount processes active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "${(usagePercent * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun LiquidProcessItem(
    process: ProcessInfo,
    isKilling: Boolean,
    onKill: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Log.d(TAG, "=== Rendering LiquidProcessItem START ===")
    Log.d(TAG, "Package: ${process.packageName}, PID: ${process.pid}, Memory: ${process.memoryMB}MB")

    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            onClick = { 
                Log.d(TAG, "Card clicked: ${process.packageName}")
                expanded = !expanded 
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // App icon - Load asynchronously to prevent crashes
                    var appIcon by remember(process.packageName) { 
                        Log.d(TAG, "Initializing icon state for: ${process.packageName}")
                        mutableStateOf<android.graphics.drawable.Drawable?>(null) 
                    }
                    var iconLoadError by remember(process.packageName) { mutableStateOf(false) }
                    
                    LaunchedEffect(process.packageName) {
                        Log.d(TAG, "LaunchedEffect: Starting icon load for ${process.packageName}")
                        withContext(Dispatchers.IO) {
                            try {
                                Log.d(TAG, "IO Thread: Loading icon for ${process.packageName}")
                                val icon = context.packageManager.getApplicationIcon(process.packageName)
                                Log.d(TAG, "IO Thread: Icon loaded successfully for ${process.packageName}")
                                appIcon = icon
                                iconLoadError = false
                            } catch (e: Exception) {
                                Log.e(TAG, "IO Thread: Error loading icon for ${process.packageName}", e)
                                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}, Message: ${e.message}")
                                appIcon = null
                                iconLoadError = true
                            }
                        }
                        Log.d(TAG, "LaunchedEffect: Finished icon load for ${process.packageName}")
                    }
                    
                    Log.d(TAG, "Rendering SubcomposeAsyncImage for ${process.packageName}")
                    SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(appIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                process.packageName.substringAfterLast('.').take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Android,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                // Process info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = process.packageName.substringAfterLast('.'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = process.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Memory badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${String.format("%.0f", process.memoryMB)} MB",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Kill button
                IconButton(
                    onClick = onKill,
                    enabled = !isKilling,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    if (isKilling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Kill process",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Process ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "${process.pid}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "CPU Usage (Est.)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "${String.format("%.1f", process.cpuPercent)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Memory bar
                    Column {
                        Text(
                            "Memory Allocation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = { (process.memoryMB / 1024f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
    
    Log.d(TAG, "=== Rendering LiquidProcessItem END: ${process.packageName} ===")
}
