package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidSlider
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

private data class SaturationPreset(val name: String, val value: Float, val emoji: String)

@OptIn(FlowPreview::class)
@Composable
fun LiquidDisplayDetailScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit
) {
    val currentSaturation by viewModel.displaySaturation.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val applyStatus by viewModel.saturationApplyStatus.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()
    
    var sliderValue by remember(currentSaturation) { mutableFloatStateOf(currentSaturation) }

    val presets = remember {
        listOf(
            SaturationPreset("Mono", 0.5f, "⚫"),
            SaturationPreset("sRGB", 1.0f, "🎨"),
            SaturationPreset("P3", 1.1f, "🌈"),
            SaturationPreset("Vivid", 1.3f, "✨"),
            SaturationPreset("Ultra", 1.5f, "🔥"),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Header
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            onClick = onBack
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp).clickable(onClick = onBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.display_settings),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Current value badge
                Surface(
                    color = if (isLightTheme) Color(0xFFFF9500).copy(0.2f) else Color(0xFFFF9F0A).copy(0.25f),
                    shape = CircleShape
                ) {
                    Text(
                        text = String.format("%.2f", sliderValue),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Status badge
        if (applyStatus.isNotEmpty()) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = applyStatus,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                MaterialTheme.colorScheme.error
                            else -> if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A)
                        }
                    )
                }
            }
        }

        // Quick presets
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = kotlin.math.abs(sliderValue - preset.value) < 0.05f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) {
                                        if (isLightTheme) Color(0xFFFF9500).copy(0.2f)
                                        else Color(0xFFFF9F0A).copy(0.25f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f)
                                    }
                                )
                                .clickable(enabled = isRootAvailable) {
                                    sliderValue = preset.value
                                    viewModel.setDisplaySaturation(preset.value)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = preset.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(0.7f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom slider
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Custom Saturation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val backdrop = rememberLayerBackdrop()
                
                LiquidSlider(
                    value = { sliderValue },
                    onValueChange = { newValue ->
                        sliderValue = newValue
                    },
                    valueRange = 0.5f..2.0f,
                    visibilityThreshold = 0.01f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                
                // Auto-apply with debounce
                LaunchedEffect(Unit) {
                    snapshotFlow { sliderValue }
                        .debounce(800)
                        .collect { value ->
                            viewModel.setDisplaySaturation(value)
                        }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0.5",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text(
                        text = "1.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text(
                        text = "2.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }

                // Apply button
                Button(
                    onClick = { viewModel.setDisplaySaturation(sliderValue) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isRootAvailable,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apply Now")
                }
            }
        }

        // Description
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "About Display Saturation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adjust color saturation to match your preference. Lower values reduce color intensity, while higher values make colors more vivid. Requires root access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Root warning
        if (!isRootAvailable) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.display_requires_root),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
