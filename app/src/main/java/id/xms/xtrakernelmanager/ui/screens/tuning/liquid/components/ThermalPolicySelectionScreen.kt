package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalPolicySelectionScreen(
    viewModel: TuningViewModel,
    currentPolicy: String,
    onNavigateBack: () -> Unit,
    onPolicySelected: (String) -> Unit
) {
    val allPolicies = remember { ThermalPolicyPresets.getAllPolicies() }

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    title = "Thermal Policy",
                    subtitle = "Choose thermal behavior profile",
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(20.dp)
            ) {
                // Header Info Card
                item(key = "info_card") {
                    InfoCard(
                        title = "Thermal Policy System",
                        description = "Thermal policies define temperature thresholds for different performance states. Each policy has specific emergency, warning, restore, and critical temperature limits.",
                        icon = Icons.Default.Psychology,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Policies List with stable keys
                items(
                    count = allPolicies.size,
                    key = { index -> allPolicies[index].name }
                ) { index ->
                    val policy = allPolicies[index]
                    val isSelected = policy.name == currentPolicy
                    
                    ThermalPolicyCard(
                        policy = policy,
                        isSelected = isSelected,
                        onClick = { onPolicySelected(policy.name) }
                    )
                }
                
                // Bottom spacing
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernTopBar(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Placeholder for balance
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ThermalPolicyCard(
    policy: id.xms.xtrakernelmanager.data.model.ThermalPolicy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Stable animation state
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "policy_card_scale"
    )
    
    // Stable colors
    val containerColor = remember(isSelected) {
        if (isSelected) {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0x4D6750A4), // primaryContainer with alpha
                    Color(0x1A6750A4)  // primary with alpha
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent
                )
            )
        }
    }
    
    val iconBackgroundBrush = remember(isSelected) {
        if (isSelected) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF6750A4), // primary
                    Color(0xCC6750A4)  // primary with alpha
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE7E0EC), // surfaceVariant
                    Color(0xCCE7E0EC)  // surfaceVariant with alpha
                )
            )
        }
    }
    
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(iconBackgroundBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Text(
                            text = policy.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFF1D1B20)
                        )
                    }
                    
                    // Selection indicator with stable animation
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(),
                        exit = scaleOut(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6750A4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Temperature Thresholds
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            Color(0x4D6750A4)
                        else 
                            Color(0x4DE7E0EC)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Temperature Thresholds",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        
                        // Temperature rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ThermalThresholdChip(
                                label = "Emergency",
                                temperature = "${policy.emergencyThreshold}°C",
                                color = Color(0xFFBA1A1A),
                                icon = Icons.Default.Warning,
                                modifier = Modifier.weight(1f)
                            )
                            ThermalThresholdChip(
                                label = "Warning",
                                temperature = "${policy.warningThreshold}°C",
                                color = Color(0xFF7D5260),
                                icon = Icons.Default.Info,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ThermalThresholdChip(
                                label = "Restore",
                                temperature = "${policy.restoreThreshold}°C",
                                color = Color(0xFF006A6B),
                                icon = Icons.Default.Restore,
                                modifier = Modifier.weight(1f)
                            )
                            ThermalThresholdChip(
                                label = "Critical",
                                temperature = "${policy.criticalThreshold}°C",
                                color = Color(0xFFBA1A1A),
                                icon = Icons.Default.Error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThermalThresholdChip(
    label: String,
    temperature: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    // Stable background color
    val backgroundColor = remember(color) { color.copy(alpha = 0.1f) }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = temperature,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1D1B20),
                fontWeight = FontWeight.Bold
            )
        }
    }
}