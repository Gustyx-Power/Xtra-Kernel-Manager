package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.viewmodel.TuningViewModel
import id.xms.xtrakernelmanager.ui.icons.OpenGLESIcon
import kotlinx.coroutines.launch

@Composable
fun GpuControlCard(
    tuningViewModel: TuningViewModel = hiltViewModel(),
    blur: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var isExpanded by remember { mutableStateOf(false) }
    var showGovernorDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }

    // Collect GPU states from ViewModel
    val gpuGovernor by tuningViewModel.currentGpuGovernor.collectAsState()
    val availableGovernors by tuningViewModel.availableGpuGovernors.collectAsState()
    val gpuMinFreq by tuningViewModel.currentGpuMinFreq.collectAsState()
    val gpuMaxFreq by tuningViewModel.currentGpuMaxFreq.collectAsState()
    val availableGpuFrequencies by tuningViewModel.availableGpuFrequencies.collectAsState()
    val openGlesDriver by tuningViewModel.currentOpenGlesDriver.collectAsState()
    val vulkanVersion by tuningViewModel.vulkanApiVersion.collectAsState()
    val currentRenderer by tuningViewModel.currentGpuRenderer.collectAsState()
    val availableRenderers = tuningViewModel.availableGpuRenderers

    // Calculate frequency ranges for sliders
    val minFreqRange = availableGpuFrequencies.minOrNull()?.toFloat() ?: 0f
    val maxFreqRange = availableGpuFrequencies.maxOrNull()?.toFloat() ?: 1000f

    // Local state for slider values
    var sliderMinFreq by remember { mutableStateOf(gpuMinFreq.toFloat()) }
    var sliderMaxFreq by remember { mutableStateOf(gpuMaxFreq.toFloat()) }

    // Update slider values when GPU frequencies change
    LaunchedEffect(gpuMinFreq, gpuMaxFreq) {
        sliderMinFreq = gpuMinFreq.toFloat()
        sliderMaxFreq = gpuMaxFreq.toFloat()
    }

    // Load GPU data when component is first composed
    LaunchedEffect(Unit) {
        tuningViewModel.fetchGpuData()
        tuningViewModel.fetchOpenGlesDriver()
        tuningViewModel.fetchVulkanApiVersion()
        tuningViewModel.fetchCurrentGpuRenderer()
    }

    // Animation values - Simplified MD3 animation
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "GPU Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )

                    Column {
                        Text(
                            text = "GPU Control",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Graphics Processing Unit",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Quick Info Section - Layout with OpenGL ES centered in its own row
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row - Governor dan Frequency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GPUInfoChip(
                        icon = Icons.Default.Settings,
                        label = "Governor",
                        value = when {
                            gpuGovernor.isBlank() || gpuGovernor == "..." -> "Loading..."
                            gpuGovernor.contains("N/A", ignoreCase = true) -> "N/A"
                            else -> gpuGovernor
                        },
                        modifier = Modifier.weight(1f)
                    )

                    GPUInfoChip(
                        icon = Icons.Default.Speed,
                        label = "Frequency",
                        value = when {
                            gpuMinFreq <= 0 && gpuMaxFreq <= 0 -> "N/A"
                            gpuMinFreq > 0 && gpuMaxFreq > 0 -> "$gpuMinFreq-$gpuMaxFreq MHz"
                            else -> "Loading..."
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second row - OpenGL ES Card (wider and taller to show full driver version)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GPUInfoChip(
                        icon = OpenGLESIcon,
                        label = "OpenGL ES",
                        value = when {
                            openGlesDriver.isBlank() || openGlesDriver == "Loading..." -> "Loading..."
                            openGlesDriver.contains("N/A", ignoreCase = true) -> "N/A"
                            openGlesDriver.contains("Not supported", ignoreCase = true) -> "N/A"
                            else -> openGlesDriver.take(135) + if (openGlesDriver.length > 135) "..." else ""
                        },
                        modifier = Modifier.fillMaxWidth(1f), // Perlebar card menjadi 100% lebar layar
                        isWider = true // Parameter untuk membuat card lebih tinggi
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GPUInfoChip(
                        icon = Icons.Filled.VideogameAsset,
                        label = "Vulkan",
                        value = when {
                            vulkanVersion.isBlank() || vulkanVersion == "Loading..." -> "Loading..."
                            vulkanVersion.contains("Available", ignoreCase = true) -> "Available"
                            vulkanVersion.contains("Not supported", ignoreCase = true) -> "Available"
                            vulkanVersion.contains("0.0", ignoreCase = true) -> "N/A"
                            else -> "Yes"
                        },
                        modifier = Modifier.weight(1f),
                        isWider = false
                    )

                    GPUInfoChip(
                        icon = Icons.Default.Visibility,
                        label = "GPU Renderer",
                        value = when {
                            currentRenderer.isBlank() || currentRenderer == "Loading..." -> "Loading..."
                            currentRenderer.contains("N/A", ignoreCase = true) -> "N/A"
                            currentRenderer.contains("Default", ignoreCase = true) -> "Default"
                            else -> currentRenderer.take(12) + if (currentRenderer.length > 12) "..." else ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))

                    // GPU Governor Control
                    GPUControlSection(
                        title = "GPU Governor",
                        description = "Controls GPU frequency scaling behavior",
                        icon = Icons.Default.Tune
                    ) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGovernorDialog = true },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Governor",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = gpuGovernor.takeIf { it != "..." && it.isNotBlank() } ?: "Unknown",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Governor",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPU Frequency Control
                    GPUControlSection(
                        title = "GPU Frequency",
                        description = "Minimum and maximum GPU frequencies",
                        icon = Icons.Default.Speed
                    ) {
                        // Sliders for GPU frequency control
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Min Frequency Slider
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Min Frequency: ${sliderMinFreq.toInt()} MHz",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Slider(
                                    value = sliderMinFreq,
                                    onValueChange = { newValue ->
                                        sliderMinFreq = newValue
                                        // Update ViewModel with new min frequency
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuMinFrequency(newValue.toInt())
                                        }
                                    },
                                    valueRange = minFreqRange..maxFreqRange,
                                    steps = ((maxFreqRange - minFreqRange) / 10).toInt(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Max Frequency Slider
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Max Frequency: ${sliderMaxFreq.toInt()} MHz",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Slider(
                                    value = sliderMaxFreq,
                                    onValueChange = { newValue ->
                                        sliderMaxFreq = newValue
                                        // Update ViewModel with new max frequency
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuMaxFrequency(newValue.toInt())
                                        }
                                    },
                                    valueRange = minFreqRange..maxFreqRange,
                                    steps = ((maxFreqRange - minFreqRange) / 10).toInt(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.error,
                                        activeTrackColor = MaterialTheme.colorScheme.error,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPU Renderer Control
                    GPUControlSection(
                        title = "GPU Renderer",
                        description = "Select graphics rendering backend",
                        icon = Icons.Default.Visibility
                    ) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRendererDialog = true },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Renderer",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = currentRenderer.takeIf { it != "Loading..." && it.isNotBlank() } ?: "Default",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Renderer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Governor Selection Dialog
    if (showGovernorDialog) {
        AlertDialog(
            onDismissRequest = { showGovernorDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Select GPU Governor")
                }
            },
            text = {
                if (availableGovernors.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading governors...")
                    }
                } else {
                    LazyColumn {
                        items(availableGovernors) { governor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = governor == gpuGovernor,
                                        onClick = {
                                            coroutineScope.launch {
                                                tuningViewModel.setGpuGovernor(governor)
                                            }
                                            showGovernorDialog = false
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = governor == gpuGovernor,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuGovernor(governor)
                                        }
                                        showGovernorDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = governor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGovernorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Renderer Selection Dialog
    if (showRendererDialog) {
        AlertDialog(
            onDismissRequest = { showRendererDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Select GPU Renderer")
                }
            },
            text = {
                LazyColumn {
                    items(availableRenderers) { renderer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = renderer == currentRenderer,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.userSelectedGpuRenderer(renderer)
                                        }
                                        showRendererDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = renderer == currentRenderer,
                                onClick = {
                                    coroutineScope.launch {
                                        tuningViewModel.userSelectedGpuRenderer(renderer)
                                    }
                                    showRendererDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = renderer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                when (renderer) {
                                    "OpenGL" -> Text(
                                        text = "Traditional rendering",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    "Vulkan" -> Text(
                                        text = "Modern low-overhead API",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    "ANGLE" -> Text(
                                        text = "OpenGL ES on Direct3D",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    "Default" -> Text(
                                        text = "System default",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRendererDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun GPUInfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isWider: Boolean = false // Parameter untuk membuat card lebih tinggi
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isWider) 16.dp else 12.dp), // Padding lebih besar untuk card yang lebih lebar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isWider) 28.dp else 16.dp) // Icon lebih besar lagi untuk card yang lebih lebar (OpenGL ES)
            )
            Spacer(modifier = Modifier.height(if (isWider) 8.dp else 4.dp)) // Spacer lebih tinggi
            Text(
                text = label,
                fontSize = if (isWider) 12.sp else 10.sp, // Font lebih besar untuk label
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(if (isWider) 6.dp else 2.dp)) // Spacer tambahan
            Text(
                text = value,
                fontSize = if (isWider) 14.sp else 12.sp, // Font lebih besar untuk value
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = if (isWider) 3 else 2, // Lebih banyak baris untuk text yang panjang
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GPUControlSection(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
