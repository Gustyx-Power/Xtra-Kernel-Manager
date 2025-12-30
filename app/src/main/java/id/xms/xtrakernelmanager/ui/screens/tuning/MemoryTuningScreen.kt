package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.xms.xtrakernelmanager.data.model.RAMConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTuningScreen(
    viewModel: TuningViewModel,
    navController: NavController
) {
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
                title = {
                    Text(
                        text = "Memory",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. RAM Card (Virtual Memory)
            MemorySettingCard(
                text = "Ram",
                iconAlignment = Alignment.CenterEnd,
                onClick = { showRamDialog = true }
            )

            // 2. Compression Level Card (Algorithm)
            MemorySettingCard(
                text = "compresion level",
                isHeaderLike = true,
                onClick = { showAlgoDialog = true }
            )

            // 3. ZRAM Card
            MemorySettingCard(
                text = "zram",
                iconAlignment = Alignment.CenterStart,
                isSelected = true, // Highlighted as in design
                onClick = { showZramDialog = true }
            )

            // 4. I/O Card
            MemorySettingCard(
                text = "i/0",
                isHeaderLike = true,
                onClick = { showIoDialog = true }
            )

            // 5. Swap Card
            MemorySettingCard(
                text = "swap",
                iconAlignment = Alignment.CenterEnd,
                onClick = { showSwapDialog = true }
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
                }
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
                }
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
                valueFormatter = { if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB" }
            )
        }

        // I/O Scheduler Dialog
        if (showIoDialog) {
            SelectionDialog(
                title = "I/O Scheduler",
                options = availableIOSchedulers,
                currentSelection = currentIOScheduler,
                onDismiss = { showIoDialog = false },
                onSelect = { viewModel.setIOScheduler(it) }
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
                valueFormatter = { if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB" }
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
    onClick: () -> Unit
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
        border = border
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            val textAlignment = when {
                isHeaderLike -> Alignment.Center
                iconAlignment == Alignment.CenterStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }

            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp
                ),
                color = Color.Black,
                modifier = Modifier.align(textAlignment)
            )

            if (!isHeaderLike) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(48.dp).align(iconAlignment)
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
    valueFormatter: (Float) -> String
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
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Slider(
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    valueRange = valueRange,
                    steps = steps
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(tempValue)
                onDismiss()
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    currentSelection: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentSelection),
                            onClick = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun VirtualMemoryDialog(
    currentSwappiness: Int,
    currentDirtyRatio: Int,
    onDismiss: () -> Unit,
    onApply: (Int, Int) -> Unit
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
                    steps = 19
                )
                Spacer(Modifier.height(16.dp))
                Text("Dirty Ratio: ${dirtyRatio.toInt()}%")
                Slider(
                    value = dirtyRatio,
                    onValueChange = { dirtyRatio = it },
                    valueRange = 0f..100f,
                    steps = 19
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(swappiness.toInt(), dirtyRatio.toInt())
                onDismiss()
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
