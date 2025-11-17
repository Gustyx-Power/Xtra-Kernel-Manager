package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import kotlinx.coroutines.launch

@Composable
fun RAMControlSection(viewModel: TuningViewModel) {
    val persistedConfig by viewModel.preferencesManager
        .getRamConfig()
        .collectAsState(initial = RAMConfig())

    var swappiness by remember { mutableFloatStateOf(persistedConfig.swappiness.toFloat()) }
    var zramSize by remember { mutableFloatStateOf(persistedConfig.zramSize.toFloat()) }
    var swapSize by remember { mutableFloatStateOf(persistedConfig.swapSize.toFloat()) }
    var dirtyRatio by remember { mutableFloatStateOf(persistedConfig.dirtyRatio.toFloat()) }
    var minFreeMem by remember { mutableFloatStateOf(persistedConfig.minFreeMem.toFloat()) }

    LaunchedEffect(persistedConfig) {
        swappiness = persistedConfig.swappiness.toFloat()
        zramSize = persistedConfig.zramSize.toFloat()
        swapSize = persistedConfig.swapSize.toFloat()
        dirtyRatio = persistedConfig.dirtyRatio.toFloat()
        minFreeMem = persistedConfig.minFreeMem.toFloat()
    }

    var showZramDialog by remember { mutableStateOf(false) }
    var showSwapDialog by remember { mutableStateOf(false) }
    var showZramApplyingDialog by remember { mutableStateOf(false) }
    var showSwapApplyingDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultSuccess by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isApplyingZram by remember { mutableStateOf(false) }
    var isApplyingSwap by remember { mutableStateOf(false) }
    
    var zramLogs by remember { mutableStateOf(listOf<String>()) }
    var swapLogs by remember { mutableStateOf(listOf<String>()) }

    val scope = rememberCoroutineScope()

    fun pushConfig(
        sw: Int = swappiness.toInt(),
        zr: Int = zramSize.toInt(),
        sp: Int = swapSize.toInt(),
        dr: Int = dirtyRatio.toInt(),
        mf: Int = minFreeMem.toInt()
    ) {
        viewModel.setRAMParameters(
            RAMConfig(
                swappiness = sw,
                zramSize = zr,
                swapSize = sp,
                dirtyRatio = dr,
                minFreeMem = mf
            )
        )
    }

    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ram_control),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Adjust virtual memory parameters to balance performance, multitasking and battery life.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Swappiness
            Text(
                text = "${stringResource(R.string.swappiness)}: ${swappiness.toInt()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = swappiness,
                onValueChange = { swappiness = it },
                onValueChangeFinished = { pushConfig() },
                valueRange = 0f..200f,
                steps = 20
            )

            // Dirty ratio
            Text(
                text = "${stringResource(R.string.dirty_ratio)}: ${dirtyRatio.toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = dirtyRatio,
                onValueChange = { dirtyRatio = it },
                onValueChangeFinished = { pushConfig() },
                valueRange = 1f..50f,
                steps = 9
            )

            // Min free mem
            Text(
                text = "${stringResource(R.string.min_free_memory)}: ${minFreeMem.toInt()} KB",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = minFreeMem,
                onValueChange = { minFreeMem = it },
                onValueChangeFinished = { pushConfig() },
                valueRange = 0f..262144f,
                steps = 15
            )

            // ZRAM row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showZramDialog = true }
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.zram_size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (zramSize.toInt() > 0) "${zramSize.toInt()} MB" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isApplyingZram) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Swap row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSwapDialog = true }
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.swap_size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val swapText = if (swapSize.toInt() > 0) {
                        val gb = swapSize / 1024f
                        String.format("%.1f GB", gb)
                    } else "Disabled"
                    Text(
                        text = swapText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isApplyingSwap) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // ZRAM config dialog
    if (showZramDialog) {
        var tempZram by remember { mutableFloatStateOf(zramSize.coerceAtLeast(256f)) }

        AlertDialog(
            onDismissRequest = { if (!isApplyingZram) showZramDialog = false },
            confirmButton = {
                TextButton(
                    enabled = !isApplyingZram,
                    onClick = {
                        showZramDialog = false
                        isApplyingZram = true
                        showZramApplyingDialog = true
                        zramLogs = listOf()
                        
                        viewModel.setZRAMWithLiveLog(
                            sizeBytes = tempZram.toLong() * 1024L * 1024L,
                            onLog = { log -> zramLogs = zramLogs + log },
                            onComplete = { success ->
                                zramSize = tempZram
                                pushConfig(zr = tempZram.toInt())
                                isApplyingZram = false
                                showZramApplyingDialog = false
                                resultSuccess = success
                                resultMessage = if (success) "ZRAM applied: ${tempZram.toInt()} MB" else "Failed to apply ZRAM"
                                showResultDialog = true
                            }
                        )
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { if (!isApplyingZram) showZramDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Configure ZRAM") },
            text = {
                Column {
                    Text(
                        text = "Set compressed RAM (ZRAM) size in MB. Set to 0 to disable.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = tempZram,
                        onValueChange = { tempZram = it },
                        valueRange = 0f..4096f,
                        steps = 15
                    )
                    Text(
                        text = "${tempZram.toInt()} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    // Swap config dialog
    if (showSwapDialog) {
        var tempSwap by remember { mutableFloatStateOf(swapSize.coerceAtLeast(1024f)) }

        AlertDialog(
            onDismissRequest = { if (!isApplyingSwap) showSwapDialog = false },
            confirmButton = {
                TextButton(
                    enabled = !isApplyingSwap,
                    onClick = {
                        showSwapDialog = false
                        isApplyingSwap = true
                        showSwapApplyingDialog = true
                        swapLogs = listOf()
                        
                        viewModel.setSwapWithLiveLog(
                            sizeMb = tempSwap.toInt(),
                            onLog = { log -> swapLogs = swapLogs + log },
                            onComplete = { success ->
                                swapSize = tempSwap
                                pushConfig(sp = tempSwap.toInt())
                                isApplyingSwap = false
                                showSwapApplyingDialog = false
                                resultSuccess = success
                                val gb = tempSwap / 1024f
                                resultMessage = if (success) "Swap applied: ${String.format("%.1f GB", gb)}" else "Failed to apply swap"
                                showResultDialog = true
                            }
                        )
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { if (!isApplyingSwap) showSwapDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Configure Swap File") },
            text = {
                Column {
                    Text(
                        text = "Set swap file size in MB (max 16384 MB). Set to 0 to disable.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = tempSwap,
                        onValueChange = { tempSwap = it },
                        valueRange = 0f..16384f,
                        steps = 31
                    )
                    val gb = tempSwap / 1024f
                    Text(
                        text = if (tempSwap.toInt() == 0) "Disabled" else String.format("%.1f GB", gb),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    // ZRAM live log dialog
    if (showZramApplyingDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Applying ZRAM") },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            zramLogs.forEach { log ->
                                Text(
                                    text = "$ $log",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    // Swap live log dialog
    if (showSwapApplyingDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Applying Swap File") },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            swapLogs.forEach { log ->
                                Text(
                                    text = "$ $log",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    // Result dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    imageVector = if (resultSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (resultSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(if (resultSuccess) "Success" else "Failed") },
            text = { Text(resultMessage) }
        )
    }
}