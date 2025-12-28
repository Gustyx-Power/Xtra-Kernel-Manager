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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer // Distinctive color
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
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
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Perform",
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
    }
}

@Composable
fun ExpandableGPUCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ADRENO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
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
                        lineHeight = 40.sp
                    )
                    Text(
                        text = " MHz",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                            fontWeight = FontWeight.Medium
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "725",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium
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
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Governor
                    GpuControlRow(
                        label = "Governor",
                        value = "msm-adreno-tz",
                        icon = Icons.Rounded.Speed,
                        options = listOf("msm-adreno-tz", "performance", "powersave", "userspace"),
                        onValueChange = { /* TODO */ }
                    )

                    // Min/Max Tiles
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GpuTile(
                            modifier = Modifier.weight(1f),
                            label = "Min Frequency",
                            value = "305 MHz",
                            options = listOf("305 MHz", "400 MHz", "500 MHz", "680 MHz"),
                            onValueChange = { /* TODO */ }
                        )
                        GpuTile(
                            modifier = Modifier.weight(1f),
                            label = "Max Frequency",
                            value = "680 MHz",
                            options = listOf("305 MHz", "400 MHz", "500 MHz", "680 MHz"),
                            onValueChange = { /* TODO */ }
                        )
                    }

                    // Power Slider
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Power Level", style = MaterialTheme.typography.bodySmall)
                                Text("Level 5", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(16.dp))
                            WavySlider(
                                value = 0.7f, 
                                onValueChange = {}
                            )
                        }
                    }

                    // Renderer
                    GpuControlRow(
                        label = "Renderer",
                        value = "SkiaGL (Vulkan)",
                        icon = Icons.Rounded.Brush,
                        options = listOf("SkiaGL (Vulkan)", "SkiaGL (OpenGL)", "SkiaVK"),
                        onValueChange = { /* TODO */ }
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
    icon: ImageVector,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
