package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.root.RootManager
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
  Card(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      shape = RoundedCornerShape(20.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Row(
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
            "Total Memory Usage",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
        Text(
            "${String.format("%.1f", totalMemoryMB)} MB",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
      Box(
          modifier =
              Modifier.size(48.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center,
      ) {
        Text(
            "$processCount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
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
  val memoryColor =
      when {
        process.memoryMB > 500 -> MaterialTheme.colorScheme.error
        process.memoryMB > 200 -> MaterialTheme.colorScheme.tertiary
        process.memoryMB > 100 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      }

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      // App info
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = process.packageName.substringAfterLast("."),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = process.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Memory badge
      Surface(
          color = memoryColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp),
      ) {
        Text(
            text = "${String.format("%.0f", process.memoryMB)} MB",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = memoryColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Kill button
      FilledTonalIconButton(
          onClick = onKill,
          enabled = !isKilling,
          colors =
              IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                  contentColor = MaterialTheme.colorScheme.error,
              ),
          modifier = Modifier.size(36.dp),
      ) {
        if (isKilling) {
          CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.error,
          )
        } else {
          Icon(
              Icons.Rounded.Close,
              "Kill",
              modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

private suspend fun loadRunningProcesses(): List<ProcessInfo> =
    withContext(Dispatchers.IO) {
      // TODO: Replace with real implementation once dumpsys parsing is optimized
      // For now, return dummy data for UI testing
      listOf(
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
    }
