package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

// Note: This component is deprecated, use LiquidDisplayDetailScreen instead
@OptIn(FlowPreview::class)
@Composable
fun LiquidDisplaySection(viewModel: MiscViewModel) {
    val currentSaturation by viewModel.displaySaturation.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val applyStatus by viewModel.saturationApplyStatus.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()
    
    var expanded by remember { mutableStateOf(false) }
    var sliderValue by remember(currentSaturation) { mutableFloatStateOf(currentSaturation) }

    // Local presets (inline)
    val presets = remember {
        listOf(
            Triple("Mono", 0.5f, "⚫"),
            Triple("sRGB", 1.0f, "🎨"),
            Triple("P3", 1.1f, "🌈"),
            Triple("Vivid", 1.3f, "✨"),
            Triple("Ultra", 1.5f, "🔥"),
        )
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isLightTheme) Color(0xFFFF9500).copy(0.15f)
                                else Color(0xFFFF9F0A).copy(0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.display_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Saturation: ${String.format("%.2f", sliderValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            // Status badge
            AnimatedVisibility(
                visible = applyStatus.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                    MaterialTheme.colorScheme.errorContainer.copy(0.3f)
                                else -> MaterialTheme.colorScheme.primaryContainer.copy(0.3f)
                            }
                        )
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
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

                    // Quick presets
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { (name, value, emoji) ->
                            val isSelected = kotlin.math.abs(sliderValue - value) < 0.05f
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
                                        sliderValue = value
                                        viewModel.setDisplaySaturation(value)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = name,
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

                    // Slider
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Custom Saturation",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
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
                            Text("Apply Saturation")
                        }
                    }

                    // Root warning
                    if (!isRootAvailable) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(0.3f))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.display_requires_root),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
