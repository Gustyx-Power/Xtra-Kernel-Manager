package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.WavyCircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTuningScreen(viewModel: TuningViewModel, navController: NavController) {
  // Collect States
  val ramConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())
  val availableIOSchedulers by viewModel.availableIOSchedulers.collectAsState()
  val currentIOScheduler by viewModel.currentIOScheduler.collectAsState()
  val availableAlgorithms by viewModel.availableCompressionAlgorithms.collectAsState()
  val currentAlgorithm by viewModel.currentCompressionAlgorithm.collectAsState()

  // Dialog States
  var showRamDialog by remember { mutableStateOf(false) }
  var showZramDialog by remember { mutableStateOf(false) }
  var showSwapDialog by remember { mutableStateOf(false) }
  var showIoDialog by remember { mutableStateOf(false) }
  var showAlgoDialog by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Memory", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      }
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. RAM Card (Virtual Memory)
      ExpandableRamCard(text = "Ram", ramConfig = ramConfig, onClick = { showRamDialog = true })

      // 2. Split Row: Compression Level (Left) | Swap (Right)
      var compressionExpanded by remember { mutableStateOf(false) }
      var swapExpanded by remember { mutableStateOf(false) }

      Row(
          modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Left: Compression Level
        if (!swapExpanded) {
          ExpandableSelectionCard(
              title = "Comp.\nLevel",
              options = availableAlgorithms,
              selectedOption = currentAlgorithm,
              onOptionSelected = {
                val newConfig = ramConfig.copy(compressionAlgorithm = it)
                viewModel.setRAMParameters(newConfig)
              },
              modifier = Modifier.weight(1f).fillMaxHeight(),
              expanded = compressionExpanded,
              onExpandedChange = { compressionExpanded = it },
          )
        }

        // Right: Swap
        if (!compressionExpanded) {
          ExpandableSwapCard(
              text = "Swap",
              ramConfig = ramConfig,
              onConfigChange = { viewModel.setRAMParameters(it) },
              modifier = Modifier.weight(1f).fillMaxHeight(),
              expanded = swapExpanded,
              onExpandedChange = { swapExpanded = it },
          )
        }
      }

      // 3. ZRAM Card
      ExpandableZramCard(
          text = "Zram",
          ramConfig = ramConfig,
          isSelected = true,
          onClick = { /* No-op, expanded handles config */ },
          onConfigChange = { viewModel.setRAMParameters(it) },
      )

      // 5. I/O Card
      ExpandableIOCard(
          currentScheduler = currentIOScheduler,
          availableSchedulers = availableIOSchedulers,
          onSchedulerChange = { viewModel.setIOScheduler(it) },
      )
    }

    // --- DIALOGS ---

    // RAM / Virtual Memory Dialog
    if (showRamDialog) {
      VirtualMemoryDialog(
          currentSwappiness = ramConfig.swappiness,
          currentDirtyRatio = ramConfig.dirtyRatio,
          onDismiss = { showRamDialog = false },
          onApply = { swappiness, dirtyRatio ->
            val newConfig = ramConfig.copy(swappiness = swappiness, dirtyRatio = dirtyRatio)
            viewModel.setRAMParameters(newConfig)
          },
      )
    }

    // Compression Algorithm Dialog
    if (showAlgoDialog) {
      SelectionDialog(
          title = "Compression Algorithm",
          options = availableAlgorithms,
          currentSelection = currentAlgorithm,
          onDismiss = { showAlgoDialog = false },
          onSelect = {
            val newConfig = ramConfig.copy(compressionAlgorithm = it)
            viewModel.setRAMParameters(newConfig)
          },
      )
    }

    // ZRAM Dialog
    if (showZramDialog) {
      SliderDialog(
          title = "ZRAM Size (MB)",
          currentValue = ramConfig.zramSize.toFloat(),
          valueRange = 0f..4096f, // Up to 4GB
          steps = 15,
          onDismiss = { showZramDialog = false },
          onApply = {
            val newConfig = ramConfig.copy(zramSize = it.toInt())
            viewModel.setRAMParameters(newConfig)
          },
          valueFormatter = { if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB" },
      )
    }

    // I/O Scheduler Dialog
    if (showIoDialog) {
      SelectionDialog(
          title = "I/O Scheduler",
          options = availableIOSchedulers,
          currentSelection = currentIOScheduler,
          onDismiss = { showIoDialog = false },
          onSelect = { viewModel.setIOScheduler(it) },
      )
    }

    // Swap Dialog
    if (showSwapDialog) {
      SliderDialog(
          title = "Swap Size (MB)",
          currentValue = ramConfig.swapSize.toFloat(),
          valueRange = 0f..8192f, // Up to 8GB
          steps = 31,
          onDismiss = { showSwapDialog = false },
          onApply = {
            val newConfig = ramConfig.copy(swapSize = it.toInt())
            viewModel.setRAMParameters(newConfig)
          },
          valueFormatter = { if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB" },
      )
    }
  }
}

@Composable
fun MemorySettingCard(
    text: String,
    modifier: Modifier = Modifier,
    iconAlignment: Alignment = Alignment.CenterEnd,
    isHeaderLike: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
  val containerColor = Color(0xFFE0E0E0)
  val iconColor = Color(0xFF6750A4)
  val shape = RoundedCornerShape(4.dp)
  val border = if (isSelected) BorderStroke(2.dp, Color(0xFF2196F3)) else null

  Surface(
      onClick = onClick,
      modifier = modifier.fillMaxWidth().height(100.dp),
      color = containerColor,
      shape = shape,
      border = border,
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
      val textAlignment =
          when {
            isHeaderLike -> Alignment.Center
            iconAlignment == Alignment.CenterStart -> Alignment.CenterEnd
            else -> Alignment.CenterStart
          }

      Text(
          text = text,
          style =
              MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Normal,
                  fontSize = 24.sp,
              ),
          color = Color.Black,
          modifier = Modifier.align(textAlignment),
      )

      if (!isHeaderLike) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(48.dp).align(iconAlignment),
        )
      }
    }
  }
}

@Composable
fun SliderDialog(
    title: String,
    currentValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onDismiss: () -> Unit,
    onApply: (Float) -> Unit,
    valueFormatter: (Float) -> String,
) {
  var tempValue by remember { mutableFloatStateOf(currentValue) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(title) },
      text = {
        Column {
          Text(
              text = valueFormatter(tempValue),
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          Spacer(Modifier.height(16.dp))
          Slider(
              value = tempValue,
              onValueChange = { tempValue = it },
              valueRange = valueRange,
              steps = steps,
          )
        }
      },
      confirmButton = {
        TextButton(
            onClick = {
              onApply(tempValue)
              onDismiss()
            }
        ) {
          Text("Apply")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    currentSelection: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(title) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          options.forEach { option ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable {
                          onSelect(option)
                          onDismiss()
                        }
                        .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = (option == currentSelection), onClick = null)
              Spacer(Modifier.width(8.dp))
              Text(text = option)
            }
          }
        }
      },
      confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun VirtualMemoryDialog(
    currentSwappiness: Int,
    currentDirtyRatio: Int,
    onDismiss: () -> Unit,
    onApply: (Int, Int) -> Unit,
) {
  var swappiness by remember { mutableFloatStateOf(currentSwappiness.toFloat()) }
  var dirtyRatio by remember { mutableFloatStateOf(currentDirtyRatio.toFloat()) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Virtual Memory") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text("Swappiness: ${swappiness.toInt()}")
          Slider(
              value = swappiness,
              onValueChange = { swappiness = it },
              valueRange = 0f..200f,
              steps = 19,
          )
          Spacer(Modifier.height(16.dp))
          Text("Dirty Ratio: ${dirtyRatio.toInt()}%")
          Slider(
              value = dirtyRatio,
              onValueChange = { dirtyRatio = it },
              valueRange = 0f..100f,
              steps = 19,
          )
        }
      },
      confirmButton = {
        TextButton(
            onClick = {
              onApply(swappiness.toInt(), dirtyRatio.toInt())
              onDismiss()
            }
        ) {
          Text("Apply")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun ExpandableRamCard(
    text: String,
    modifier: Modifier = Modifier,
    ramConfig: RAMConfig,
    onClick: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = shape,
        onClick = onClick,
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge, // Adjusted
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Box(contentAlignment = Alignment.Center) {
                    WavyCircularProgressIndicator(
                        progress = 0.75f,
                        modifier = Modifier.size(100.dp),
                        color = MaterialTheme.colorScheme.primary, // MD3 Primary
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 8.dp,
                        amplitude = 0.12f,
                        frequency = 10,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "5.8GB",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // Adjusted
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Normal",
                            style = MaterialTheme.typography.bodySmall, // Adjusted
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Always Expanded Content
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "App Consumption",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Wrapped in Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppUsageItem("Genshin Impact", "2.1 GB")
                        AppUsageItem("Chrome", "850 MB")
                        AppUsageItem("System UI", "450 MB")
                        AppUsageItem("Google Play Services", "320 MB")
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ExpandableZramCard(
    text: String,
    ramConfig: RAMConfig,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onConfigChange: (RAMConfig) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
        color = containerColor,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Wavy Indicator (Now on Left)
                Box(contentAlignment = Alignment.Center) {
                    WavyCircularProgressIndicator(
                        progress = 0.40f,
                        modifier = Modifier.size(100.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 8.dp,
                        amplitude = 0.12f,
                        frequency = 10,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ramConfig.zramSize / 1024}GB",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "ZRAM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Title Text (Now on Right)
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Expanded Content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Stats Section
                Text(
                    text = "Compression Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppUsageItem("Total Data", "4.2 GB")
                        AppUsageItem("Compressed", "1.1 GB")
                        AppUsageItem("Ratio", "3.8x")
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Configuration Section
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ZRAM Size Slider
                        Column {
                            Text(
                                text = "ZRAM Size: ${if (ramConfig.zramSize == 0) "Disabled" else "${ramConfig.zramSize} MB"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Slider(
                                value = ramConfig.zramSize.toFloat(),
                                onValueChange = { onConfigChange(ramConfig.copy(zramSize = it.toInt())) },
                                valueRange = 0f..4096f,
                                steps = 15,
                            )
                        }

                        // Swappiness Slider
                        Column {
                            Text(
                                text = "Swappiness: ${ramConfig.swappiness}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Slider(
                                value = ramConfig.swappiness.toFloat(),
                                onValueChange = { onConfigChange(ramConfig.copy(swappiness = it.toInt())) },
                                valueRange = 0f..100f,
                                steps = 19,
                            )
                        }

                        // Dirty Ratio Slider
                        Column {
                            Text(
                                text = "Dirty Ratio: ${ramConfig.dirtyRatio}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Slider(
                                value = ramConfig.dirtyRatio.toFloat(),
                                onValueChange = { onConfigChange(ramConfig.copy(dirtyRatio = it.toInt())) },
                                valueRange = 0f..100f,
                                steps = 19,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSelectionCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize().clickable { onExpandedChange(!expanded) },
        color = containerColor,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge, // Adjusted
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Current Selection Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = selectedOption,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            // Expanded List
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onOptionSelected(option)
                                    onExpandedChange(false)
                                }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                                )
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSwapCard(
    text: String,
    ramConfig: RAMConfig,
    onConfigChange: (RAMConfig) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize().clickable { onExpandedChange(!expanded) },
        color = containerColor,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge, // Adjusted
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterStart),
                )

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp).align(Alignment.CenterEnd),
                )
            }

            // Expanded Content (Slider)
            if (expanded) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Size: ${if (ramConfig.swapSize == 0) "Disabled" else "${ramConfig.swapSize} MB"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Slider(
                    value = ramConfig.swapSize.toFloat(),
                    onValueChange = { onConfigChange(ramConfig.copy(swapSize = it.toInt())) },
                    valueRange = 0f..8192f, // 8GB
                    steps = 31,
                )
            }
        }
    }
}

@Composable
fun ExpandableIOCard(
    currentScheduler: String,
    availableSchedulers: List<String>,
    onSchedulerChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    // Mock devices for UI structure
    val devices = listOf("sda", "sdb", "sdc", "sdd")

    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
        color = containerColor,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "I/O Scheduler",
                    style = MaterialTheme.typography.titleLarge, // Adjusted
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // Use a different icon or simple status
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    devices.forEach { device ->
                        DeviceIOSection(
                            deviceName = device.uppercase(),
                            currentScheduler = currentScheduler,
                            availableSchedulers = availableSchedulers,
                            onSchedulerChange = onSchedulerChange
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceIOSection(
    deviceName: String,
    currentScheduler: String,
    availableSchedulers: List<String>,
    onSchedulerChange: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var applyOnBoot by remember { mutableStateOf(false) } // Represents "Set on Boot"

    Column(modifier = Modifier.fillMaxWidth()) {
        // Device Header with Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676)) // Keep Android Green signature
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Set on Boot Switch
            Switch(
                checked = applyOnBoot,
                onCheckedChange = { applyOnBoot = it },
                modifier = Modifier.scale(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Scheduler Selection (Dropdown) - Always Enabled
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentScheduler,
                onValueChange = {},
                readOnly = true,
                label = { Text("Scheduler", fontSize = 12.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .height(56.dp)
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                availableSchedulers.forEach { scheduler ->
                    DropdownMenuItem(
                        text = { Text(text = scheduler) },
                        onClick = {
                            onSchedulerChange(scheduler)
                            dropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppUsageItem(name: String, size: String) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black.copy(alpha = 0.8f),
    )
    Text(
        text = size,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF6750A4),
    )
  }
}
