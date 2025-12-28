package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.Canvas
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.ui.graphics.Color
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTuningDashboard(
    preferencesManager: PreferencesManager? = null, 
    onNavigate: (String) -> Unit = {}
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tuning",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* Import Action */ }) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = "Import Profile")
                    }
                    IconButton(onClick = { /* Export Action */ }) {
                        Icon(Icons.Rounded.Save, contentDescription = "Export Profile")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Card 
            item(span = StaggeredGridItemSpan.FullLine) {
                HeroDeviceCard()
            }

            // 2. CPU Nav 
            item {
                DashboardNavCard(
                    title = "CPU",
                    subtitle = "Clusters & Gov",
                    icon = Icons.Rounded.Memory,
                    onClick = { onNavigate("cpu_tuning") }
                )
            }

            // 3. Thermal Nav 
            item {
                DashboardNavCard(
                    title = "Thermal",
                    subtitle = "38°C • Normal",
                    icon = Icons.Rounded.Thermostat,
                    onClick = { onNavigate("thermal_tuning") }
                )
            }

            // 4. Profile Card 
            item(span = StaggeredGridItemSpan.FullLine) {
                DashboardProfileCard()
            }

            // 5. GPU Card (Expandable)
            item(span = StaggeredGridItemSpan.FullLine) {
                ExpandableGPUCard()
            }

            // 6. Memory Nav 
            item {
                DashboardNavCard(
                    title = "Memory",
                    subtitle = "ZRAM 50% • LMK",
                    icon = Icons.Rounded.SdCard,
                    onClick = { onNavigate("memory_tuning") }
                )
            }

            // 7. TCP 
            item {
                DashboardNavCard(
                    title = "Network",
                    subtitle = "Westwood • DNS",
                    icon = Icons.Rounded.Router,
                    onClick = { onNavigate("network_tuning") }
                )
            }
        }
    }
}


@Composable
fun HeroDeviceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "XIAOMI",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(100),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "TARO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = "23049PCD8G",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "marble - 18% Load",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardProfileCard() {
    var expanded by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf("Performance") }
    val profiles = listOf("Performance", "Balance", "Powersave", "Battery")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer 
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icon Bubble
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when(selectedProfile) {
                            "Performance" -> Icons.Rounded.Bolt
                            "Powersave" -> Icons.Rounded.Eco
                            "Battery" -> Icons.Rounded.BatteryFull
                            else -> Icons.Rounded.Speed // Balance
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedProfile,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Active Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }


            }

            // Expanded List
            AnimatedVisibility(visible = expanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        profiles.forEach { profile ->
                            val isSelected = profile == selectedProfile
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { 
                                        selectedProfile = profile
                                        expanded = false 
                                    }
                                    .background(if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                 Text(
                                    text = profile,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = if (isSelected) 1f else 0.8f)
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
fun ExpandableGPUCard() {
    var expanded by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0.7f) }
    var governorValue by remember { mutableStateOf("msm-adreno-tz") }
    var minFreq by remember { mutableStateOf("305 MHz") }
    var maxFreq by remember { mutableStateOf("680 MHz") }
    var rendererValue by remember { mutableStateOf("SkiaGL (Vulkan)") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer // Monet: Secondary Container
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GPU",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ADRENO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main Stats
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "220",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 40.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = " MHz",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Grid Stats (Load & Model)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Load Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "33%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Load",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Model Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Adreno (TM)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "725",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "GPU",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expanded Controls
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Governor
                    GpuControlRow(
                        label = "Governor",
                        value = governorValue,
                        icon = Icons.Rounded.Speed,
                        options = listOf("msm-adreno-tz", "performance", "powersave", "userspace"),
                        onValueChange = { governorValue = it }
                    )

                    // Min/Max Tiles
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GpuTile(
                            modifier = Modifier.weight(1f),
                            label = "Min Frequency",
                            value = minFreq,
                            options = listOf("305 MHz", "400 MHz", "500 MHz", "680 MHz"),
                            onValueChange = { minFreq = it }
                        )
                        GpuTile(
                            modifier = Modifier.weight(1f),
                            label = "Max Frequency",
                            value = maxFreq,
                            options = listOf("305 MHz", "400 MHz", "500 MHz", "680 MHz"),
                            onValueChange = { maxFreq = it }
                        )
                    }

                    // Power Slider
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Power Level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("Level ${(sliderValue * 10).toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(16.dp))
                            WavySlider(
                                value = sliderValue, 
                                onValueChange = { sliderValue = it }
                            )
                        }
                    }

                    // Renderer
                    GpuControlRow(
                        label = "Renderer",
                        value = rendererValue,
                        options = listOf("SkiaGL (Vulkan)", "SkiaGL (OpenGL)", "SkiaVK"),
                        onValueChange = { rendererValue = it }
                    )
                }
            }
        }
    }
}

@Composable
fun GpuControlRow(
    label: String, 
    value: String, 
    icon: ImageVector? = null,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                shape = RoundedCornerShape(12.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option, 
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        ),
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GpuTile(
    modifier: Modifier = Modifier, 
    label: String, 
    value: String,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.clickable { expanded = true },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                shape = RoundedCornerShape(12.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option, 
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        ),
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MaterialTuningPreview() {
    MaterialTheme {
        MaterialTuningDashboard()
    }
}

@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveAmplitude: Float = 10f,
    waveFrequency: Float = 20f,
    strokeWidth: Float = 12f
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(waveAmplitude.dp * 3)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            // Build the full wave path
            val path = Path()
            path.moveTo(0f, centerY)

            val step = 5f 
            var x = 0f
            while (x <= width) {
                val y = centerY + sin((x / width) * (PI * waveFrequency)) * waveAmplitude
                path.lineTo(x, y.toFloat())
                x += step
            }

            // Draw inactive track (full wave)
            drawPath(
                path = path,
                color = inactiveColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw active track (clipped wave)
            drawContext.canvas.save()
            drawContext.canvas.clipRect(0f, 0f, width * value, height)
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawContext.canvas.restore()
            
            // Draw thumb
            val thumbX = width * value
            val thumbY = centerY + sin((thumbX / width) * (PI * waveFrequency)) * waveAmplitude
            drawCircle(
                color = primaryColor,
                radius = strokeWidth,
                center = Offset(thumbX, thumbY.toFloat())
            )
        }
    }
}
