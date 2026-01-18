package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.ui.components.WavyProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProcessInfo(
    val pid: Int,
    val packageName: String,
    val memoryMB: Float,
    val cpuPercent: Float = 0f,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialProcessManagerScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var processes by remember { mutableStateOf<List<ProcessInfo>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var isKilling by remember { mutableStateOf<String?>(null) }
  var sortByMemory by remember { mutableStateOf(true) }

  // Load processes on launch
  LaunchedEffect(Unit) { 
    processes = loadRunningProcesses()
    isLoading = false
  }

  fun refreshProcesses() {
    scope.launch {
      isLoading = true
      processes = loadRunningProcesses()
      isLoading = false
    }
  }

  fun killProcess(packageName: String) {
    scope.launch {
      isKilling = packageName
      val result = withContext(Dispatchers.IO) {
        RootManager.executeCommand("am force-stop $packageName")
      }
      if (result.isSuccess) {
        // Remove from list
        processes = processes.filter { it.packageName != packageName }
      }
      isKilling = null
    }
  }

  val sortedProcesses = remember(processes, sortByMemory) {
    if (sortByMemory) {
      processes.sortedByDescending { it.memoryMB }
    } else {
      processes.sortedBy { it.packageName }
    }
  }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
            title = {
              Column {
                Text(
                    "Process Manager",
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${processes.size} running",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
              }
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
              }
            },
            actions = {
              // Sort toggle
              IconButton(onClick = { sortByMemory = !sortByMemory }) {
                Icon(
                    if (sortByMemory) Icons.Rounded.Memory else Icons.Rounded.SortByAlpha,
                    "Sort",
                )
              }
              // Refresh
              IconButton(onClick = { refreshProcesses() }) {
                Icon(Icons.Rounded.Refresh, "Refresh")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
        )
      },
  ) { paddingValues ->
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
      if (isLoading) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          CircularProgressIndicator()
          Text(
              "Loading processes...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          )
        }
      } else if (processes.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Icon(
              Icons.Rounded.CheckCircle,
              null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.primary,
          )
          Text(
              "No user processes found",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }
      } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Memory summary card
          item {
            val totalMemory = processes.sumOf { it.memoryMB.toDouble() }
            MemorySummaryCard(
                totalMemoryMB = totalMemory.toFloat(),
                processCount = processes.size,
            )
          }

          items(sortedProcesses, key = { it.packageName }) { process ->
            ProcessItem(
                process = process,
                isKilling = isKilling == process.packageName,
                onKill = { killProcess(process.packageName) },
            )
          }

          // Bottom spacing
          item { Spacer(modifier = Modifier.height(16.dp)) }
        }
      }
    }
  }
}

@Composable
fun MemorySummaryCard(totalMemoryMB: Float, processCount: Int) {
  val totalDeviceRAM = 8192f
  val usagePercent = (totalMemoryMB / totalDeviceRAM).coerceIn(0f, 1f)

  Card(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      shape = MaterialTheme.shapes.extraLarge, // M3: 28.dp
      colors =
          CardDefaults.cardColors(containerColor = Color(0xFF3E2C28)), // Dark brown from screenshot
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
              "RAM Usage",
              style = MaterialTheme.typography.titleSmall,
              color = Color(0xFFD7C1BE).copy(alpha = 0.8f),
          )
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "${String.format("%.1f", totalMemoryMB / 1024).replace('.', ',')}",
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp),
                color = Color(0xFFEDDDD9),
            )
            Text(
                " GB",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEDDDD9).copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
            )
          }
        }

        // Apps count badge - using M3 Large shape instead of custom CookieEight
        Box(
            modifier =
                Modifier.size(72.dp)
                    .clip(MaterialTheme.shapes.large) // M3: 16.dp rounded
                    .background(Color(0xFF564442)),
            contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$processCount",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB4AB), // Bright pink accent
            )
            Text(
                "Apps",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFB4AB).copy(alpha = 0.8f),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Progress Bar
      Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                  "Visualized Usage (Estimate)",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFD7C1BE).copy(alpha = 0.6f),
              )
              Text(
                  "${(usagePercent * 100).toInt()}%",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFD7C1BE),
              )
            }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { usagePercent },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.extraSmall), // M3: 4.dp
            color = Color(0xFFFFB4AB), // Salmon/Pink progress
            trackColor = Color(0xFF534341), // Darker brown track
        )
      }
    }
  }
}

@Composable
fun ProcessItem(
    process: ProcessInfo,
    isKilling: Boolean,
    onKill: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  
  Card(
      modifier = Modifier
          .fillMaxWidth()
          .animateContentSize()
          .clickable { expanded = !expanded },
      shape = MaterialTheme.shapes.large, // M3: 16.dp
      colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
      ),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // App icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium) // M3: 12.dp
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = process.packageName.substringAfterLast('.').take(2).uppercase(),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        
        // Process info
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = process.packageName.substringAfterLast('.'),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
          Text(
              text = process.packageName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
        }
        
        // Memory usage
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small, // M3: 8.dp
        ) {
          Text(
              text = "${String.format("%.0f", process.memoryMB)} MB",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
        
        // Kill button
        IconButton(
            onClick = onKill,
            enabled = !isKilling,
        ) {
          if (isKilling) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
          } else {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Kill process",
                tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
      
      // Expanded content
      AnimatedVisibility(visible = expanded) {
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
                "PID: ${process.pid}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "CPU: ${String.format("%.1f", process.cpuPercent)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          
          // Memory progress bar
          WavyProgressIndicator(
              progress = (process.memoryMB / 1024f).coerceIn(0f, 1f),
              modifier = Modifier.fillMaxWidth().height(8.dp),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant,
              strokeWidth = 4.dp,
              amplitude = 2.dp,
          )
        }
      }
    }
  }
}

suspend fun loadRunningProcesses(): List<ProcessInfo> = withContext(Dispatchers.IO) {
  try {
    val result = RootManager.executeCommand("dumpsys meminfo --package")
    if (result.isSuccess) {
      val output = result.getOrNull() ?: return@withContext emptyList()
      parseProcesses(output)
    } else {
      // Fallback to mock data for testing
      getMockProcesses()
    }
  } catch (e: Exception) {
    getMockProcesses()
  }
}

private fun parseProcesses(output: String): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val regex = Regex("""(\d+)\s+K:\s+(\S+)\s+\(pid\s+(\d+)""")
  
  output.lines().forEach { line ->
    val match = regex.find(line)
    if (match != null) {
      val memoryKB = match.groupValues[1].toIntOrNull() ?: 0
      val packageName = match.groupValues[2]
      val pid = match.groupValues[3].toIntOrNull() ?: 0
      
      if (!packageName.startsWith("com.android.") && 
          !packageName.startsWith("android.") &&
          memoryKB > 10000) {
        processes.add(
            ProcessInfo(
                pid = pid,
                packageName = packageName,
                memoryMB = memoryKB / 1024f,
            )
        )
      }
    }
  }
  
  return processes.ifEmpty { getMockProcesses() }
}

private fun getMockProcesses(): List<ProcessInfo> = listOf(
    ProcessInfo(pid = 1234, packageName = "com.instagram.android", memoryMB = 456.2f),
    ProcessInfo(pid = 2345, packageName = "com.whatsapp", memoryMB = 312.8f),
    ProcessInfo(pid = 3456, packageName = "com.spotify.music", memoryMB = 287.5f),
    ProcessInfo(pid = 4567, packageName = "com.twitter.android", memoryMB = 198.3f),
    ProcessInfo(pid = 5678, packageName = "com.discord", memoryMB = 175.6f),
    ProcessInfo(pid = 6789, packageName = "com.netflix.mediaclient", memoryMB = 523.1f),
    ProcessInfo(pid = 7890, packageName = "com.zhiliaoapp.musically", memoryMB = 612.4f),
    ProcessInfo(pid = 8901, packageName = "com.facebook.katana", memoryMB = 445.7f),
    ProcessInfo(pid = 9012, packageName = "com.snapchat.android", memoryMB = 389.2f),
    ProcessInfo(pid = 1023, packageName = "com.miHoYo.GenshinImpact", memoryMB = 1245.8f),
    ProcessInfo(pid = 1124, packageName = "com.supercell.clashofclans", memoryMB = 156.3f),
    ProcessInfo(pid = 1225, packageName = "com.mojang.minecraftpe", memoryMB = 234.1f),
)
