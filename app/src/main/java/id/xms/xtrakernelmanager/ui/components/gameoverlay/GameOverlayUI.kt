package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.content.Context
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.screens.misc.GameMonitorViewModel
import id.xms.xtrakernelmanager.ui.theme.ExpressiveShapes
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun GameOverlayTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme(
            primary = Color(0xFF5C6BC0),
            secondary = Color(0xFF7986CB),
            surface = Color(0xFF1E1E1E),
            surfaceContainer = Color(0xFF252525),
            background = Color(0xFF121212)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@Composable
fun GameSidebar(
    isExpanded: Boolean,
    overlayOnRight: Boolean,
    fps: String? = null,
    onToggleExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
    val contentColor = MaterialTheme.colorScheme.primary // Fixed: Consistent Monet color 
    
    // Shape: Half-rounded rect attached to side
    val shape = if (overlayOnRight) {
        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
    } else {
        RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
    }

    Surface(
        color = pillColor,
        shape = shape,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .width(52.dp) // Wider for text
            .height(36.dp) // Standard button height (compact)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null 
            ) { onToggleExpand() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Action Content (Icon OR FPS)
            if (fps != null) {
                Text(
                    text = fps,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.SportsEsports,
                    contentDescription = "Expand",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}





// --- EXPRESSIVE PANEL (Bento Style) ---
@Composable
fun GamePanelCard(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
    onCollapse: () -> Unit,
    onMoveSide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val time by produceState(initialValue = "") {
        while (true) {
            value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    val batteryLevel by viewModel.batteryPercentage.collectAsStateWithLifecycle()
    val temp by viewModel.tempValue.collectAsStateWithLifecycle()
    
    // Performance Stats
    val fps by viewModel.fpsValue.collectAsStateWithLifecycle()
    val cpuLoad by viewModel.cpuLoad.collectAsStateWithLifecycle()
    val gpuLoad by viewModel.gpuLoad.collectAsStateWithLifecycle()
    
    Card(
        shape = RoundedCornerShape(28.dp), // Slightly tighter radius
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .width(312.dp) // COMPACT: Reduced from 360dp for less width
            .wrapContentHeight()
            .padding(8.dp) // Less outer padding
    ) {
        Column(
            modifier = Modifier.padding(20.dp), // Reduced inner padding from 24dp
            verticalArrangement = Arrangement.spacedBy(16.dp) // Tighter spacing
        ) {
            // 1. EXPRESSIVE HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // No ripple
                    ) { onCollapse() }
            ) {
                // Big Bold Clock (Smaller)
                Text(
                    text = time,
                    style = MaterialTheme.typography.headlineMedium, // Reduced from displaySmall
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1).sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Status Pills (More compact)
                StatusPill(icon = Icons.Rounded.Thermostat, text = "$temp°C", isWarning = true)
                Spacer(modifier = Modifier.width(6.dp))
                StatusPill(icon = Icons.Rounded.BatteryStd, text = "$batteryLevel%")
                
                // Close button removed, click header to close
            }

            // 2. BRIGHTNESS (Compact slider)
            BrightnessControlExpressive()

            // 3. PERFORMANCE WIDGET (Bento Box)
            PerformanceBento(fps, cpuLoad, gpuLoad)

            // 4. GAME MODE SELECTOR (Text Only)
            ExpressiveModeSelector(viewModel)

            // 5. TOOLS GRID (Slidable Row)
            ToolsGridExpressive(viewModel, isFpsEnabled, onFpsToggle)
        }
    }
}

@Composable
fun StatusPill(icon: ImageVector, text: String, isWarning: Boolean = false) {
    Surface(
        color = if (isWarning) Color(0xFF2E1A1A) else Color(0xFF1A261A), // Darker muted backgrounds
        contentColor = if (isWarning) Color(0xFFEF9A9A) else Color(0xFFA5D6A7),
        shape = CircleShape,
        modifier = Modifier.height(28.dp) // Reduced height
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PerformanceBento(fps: String, cpu: Float, gpu: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp), // Reduced height from 96dp for better proportions
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // FPS Card (Left - Big)
        Card(
            modifier = Modifier.weight(1.3f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(32.dp) // Reverted to Rounded Rect
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val fpsVal = fps.toFloatOrNull()?.toInt() ?: 60
                    Text(
                        text = "$fpsVal",
                        style = MaterialTheme.typography.headlineMedium, // Reduced from displaySmall
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 32.sp
                    )
                    Text(
                        text = "FPS",
                        style = MaterialTheme.typography.labelSmall, // Reduced from labelMedium
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // Load Stats (Right - Stacked)
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoadChip(label = "CPU", value = cpu, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            LoadChip(label = "GPU", value = gpu, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LoadChip(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${value.toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun ExpressiveModeSelector(viewModel: GameMonitorViewModel) {
    val currentMode by viewModel.currentPerformanceMode.collectAsStateWithLifecycle()
    val modes = listOf(
        "powersave" to "Power Save", 
        "balanced" to "Balance", 
        "performance" to "Performance"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp) // Reduced height from 56dp
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .padding(4.dp)
    ) {
        modes.forEach { (modeKey, modeLabel) ->
            val isSelected = currentMode == modeKey
            val animatedColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(animatedColor)
                    .clickable { viewModel.setPerformanceMode(modeKey) },
                contentAlignment = Alignment.Center
            ) {
                // TEXT ONLY as requested
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BrightnessControlExpressive() {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    
    // Thick Expressive Slider
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth().height(44.dp) // Reduced height
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            // Track Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(sliderPosition)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            )
            
            // Interaction overlay (invisible slider)
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Icon Overlay
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed: Uniform tinting using theme content colors
                Icon(Icons.Rounded.BrightnessLow, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Icon(Icons.Rounded.BrightnessHigh, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// SLIDABLE TOOLS
@Composable
fun ToolsGridExpressive(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit
) {
    val dnd by viewModel.doNotDisturb.collectAsStateWithLifecycle()
    val touchGuard by viewModel.touchGuard.collectAsStateWithLifecycle()
    
    // Use LazyRow for slidable behavior
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            ToolButtonExpressive(
                icon = Icons.Rounded.Speed, // FPS Icon
                label = "FPS",
                isActive = isFpsEnabled,
            ) { onFpsToggle() }
        }
        item {
            ToolButtonExpressive(
                icon = Icons.Rounded.DoNotDisturb,
                label = "DND",
                isActive = dnd,
            ) { viewModel.setDND(!dnd) }
        }
        item {
            ToolButtonExpressive(
                icon = Icons.Rounded.RocketLaunch, // Changed from CleaningServices
                label = "Boost",
                isActive = false, 
            ) { viewModel.clearRAM() }
        }
        item {
            ToolButtonExpressive(
                icon = Icons.Rounded.TouchApp,
                label = "Gesture", // Renamed from Block
                isActive = touchGuard,
            ) { viewModel.setTouchGuard(!touchGuard) }
        }
        item {
            ToolButtonExpressive(
                icon = Icons.Rounded.Screenshot,
                label = "Screenshot", // Renamed from Shot
                isActive = false, 
            ) { /* TODO */ }
        }
        // Additional items can be added here easily
    }
}

@Composable
fun ToolButtonExpressive(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val iconColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp), // Squircle
            color = bgColor,
            modifier = Modifier.size(56.dp).aspectRatio(1f) // Reduced size from 64dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp)) // Reduced icon size
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.LightGray,
            fontSize = 11.sp
        )
    }
}
