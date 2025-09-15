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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
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
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Configure GPU governor, frequency, and renderer settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGovernorDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
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
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary,
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
                        icon = Icons.Default.Monitor
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRendererDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
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
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "Vulkan" -> Text(
                                        text = "Modern low-overhead API",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "ANGLE" -> Text(
                                        text = "OpenGL ES on Direct3D",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "Default" -> Text(
                                        text = "System default",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
