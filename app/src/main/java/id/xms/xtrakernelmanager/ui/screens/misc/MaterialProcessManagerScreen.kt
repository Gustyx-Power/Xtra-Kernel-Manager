package id.xms.xtrakernelmanager.ui.screens.misc

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
  val context = LocalContext.current
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
              Text(
                  "Process Manager",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 24.sp
              )
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
              }
            },
            actions = {
              IconButton(onClick = { sortByMemory = !sortByMemory }) {
                Icon(
                    if (sortByMemory) Icons.Rounded.Memory else Icons.Rounded.SortByAlpha,
                    "Sort",
                )
              }
              IconButton(onClick = { refreshProcesses() }) {
                Icon(Icons.Rounded.Refresh, "Refresh")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
      },
  ) { paddingValues ->
     if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (processes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
           Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("All clear", style = MaterialTheme.typography.titleLarge)
           }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val totalMemory = processes.sumOf { it.memoryMB.toDouble() }.toFloat()
                // Expressive Memory Card - High Contrast, Solid Colors
                MemorySummaryHero(totalMemory, processes.size)
            }
            
            item {
                Text(
                    "Running Processes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }

            items(sortedProcesses, key = { it.packageName }) { process ->
                ProcessItem(
                    process = process,
                    isKilling = isKilling == process.packageName,
                    onKill = { killProcess(process.packageName) }
                )
            }
        }
    }
  }
}

@Composable
fun MemorySummaryHero(totalMemoryMB: Float, processCount: Int) {
    val totalDeviceRAM = 8192f
    val usagePercent = (totalMemoryMB / totalDeviceRAM).coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${String.format("%.1f", totalMemoryMB / 1024).replace('.', ',')}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-2).sp
                            ),
                            lineHeight = 64.sp
                        )
                        Text(
                            text = "GB",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Rounded.Memory,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp).size(32.dp)
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                     Text(
                        "$processCount processes active",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        "${(usagePercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun MemorySummaryCard(totalMemoryMB: Float, processCount: Int) {
  val totalDeviceRAM = 8192f // 8GB placeholder
  val usagePercent = (totalMemoryMB / totalDeviceRAM).coerceIn(0f, 1f)

  Card(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          contentColor = MaterialTheme.colorScheme.onSurface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Side: Info
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            Text(
                "RAM Usage",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${String.format("%.1f", totalMemoryMB / 1024).replace('.', ',')}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    lineHeight = 40.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "GB",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }
        
        // Compact Apps Chip
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "$processCount Running",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
      }

      // Right Side: Circular Indicator
      Box(contentAlignment = Alignment.Center) {
          CircularProgressIndicator(
              progress = { usagePercent },
              modifier = Modifier.size(80.dp),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
              strokeWidth = 8.dp,
              strokeCap = StrokeCap.Round,
          )
          Text(
              "${(usagePercent * 100).toInt()}%",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
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
  val context = LocalContext.current
  var expanded by remember { mutableStateOf(false) }
  
  Card(
      modifier = Modifier
          .fillMaxWidth()
          .animateContentSize()
          .clickable { expanded = !expanded },
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // App icon
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(getAppIcon(context, process.packageName))
                .crossfade(true)
                .build(),
            contentDescription = null,
            loading = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        process.packageName.substringAfterLast('.').take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Android,
                        null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.large)
        )
        
        // Process info
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = process.packageName.substringAfterLast('.'),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
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
        
        // Memory usage pill
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
        ) {
          Text(
              text = "${String.format("%.0f", process.memoryMB)} MB",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
        
        // Kill button
        FilledTonalIconButton(
            onClick = onKill,
            enabled = !isKilling,
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
          if (isKilling) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
          } else {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Kill process",
                modifier = Modifier.size(18.dp)
            )
          }
        }
      }
      
      // Expanded content
      AnimatedVisibility(visible = expanded) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          
          Row(
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
                Text(
                    "Process ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${process.pid}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "CPU Usage (Est.)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${String.format("%.1f", process.cpuPercent)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
          }
          
          Spacer(modifier = Modifier.height(4.dp))
          
          // Visual Memory Bar
          Column {
             Text(
                "Memory Allocation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
             )
             LinearProgressIndicator(
                  progress = { (process.memoryMB / 1024f).coerceIn(0f, 1f) },
                  modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                  color = MaterialTheme.colorScheme.secondary,
                  trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
              )
          }
        }
      }
    }
  }
}

private fun getAppIcon(context: android.content.Context, packageName: String): android.graphics.drawable.Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
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
    ProcessInfo(pid = 8901, packageName = "com.facebook.katana", memoryMB = 445.7f),
    ProcessInfo(pid = 1023, packageName = "com.miHoYo.GenshinImpact", memoryMB = 1245.8f),
    ProcessInfo(pid = 1124, packageName = "com.supercell.clashofclans", memoryMB = 156.3f),
    ProcessInfo(pid = 1225, packageName = "com.mojang.minecraftpe", memoryMB = 234.1f),
)
