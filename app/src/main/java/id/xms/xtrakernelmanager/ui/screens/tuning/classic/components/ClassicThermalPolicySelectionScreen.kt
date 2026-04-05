package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicThermalPolicySelectionScreen(
    viewModel: TuningViewModel,
    currentPolicy: String,
    onNavigateBack: () -> Unit,
    onPolicySelected: (String) -> Unit
) {
    val allPolicies = remember { ThermalPolicyPresets.getAllPolicies() }
    
    // Stabilize currentPolicy to prevent glitches during state updates
    val stableCurrentPolicy by remember { derivedStateOf { currentPolicy } }

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ClassicColors.Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Thermal Policy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Choose thermal behavior profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
        ) {
            // Header Info Card
            item(key = "info_card") {
                ClassicInfoCard(
                    title = "Thermal Policy System",
                    description = "Thermal policies define temperature thresholds for different performance states. Each policy has specific emergency, warning, restore, and critical temperature limits.",
                    icon = Icons.Default.Psychology,
                    color = ClassicColors.Primary
                )
            }
            
            // Policies List with stable keys
            items(
                count = allPolicies.size,
                key = { index -> allPolicies[index].name }
            ) { index ->
                val policy = allPolicies[index]
                // Use stable state for isSelected to prevent glitches
                val isSelected = remember(stableCurrentPolicy, policy.name) {
                    policy.name == stableCurrentPolicy
                }
                
                ClassicThermalPolicyCard(
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

@Composable
private fun ClassicInfoCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
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
                    .background(color.copy(alpha = 0.2f)),
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
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ClassicThermalPolicyCard(
    policy: id.xms.xtrakernelmanager.data.model.ThermalPolicy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Stabilize isSelected state to prevent unnecessary recompositions
    val stableIsSelected by remember { derivedStateOf { isSelected } }
    
    val scale by animateFloatAsState(
        targetValue = if (stableIsSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "thermal_policy_card_scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (stableIsSelected) 
            ClassicColors.Primary.copy(alpha = 0.15f)
        else 
            ClassicColors.Surface,
        border = if (stableIsSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, ClassicColors.Primary)
        else null,
        shadowElevation = if (stableIsSelected) 4.dp else 1.dp
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
                            .background(
                                if (stableIsSelected) ClassicColors.Primary
                                else ClassicColors.Primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (stableIsSelected) ClassicColors.Background
                            else ClassicColors.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = policy.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
                
                // Selection indicator
                AnimatedVisibility(
                    visible = stableIsSelected,
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
                            .background(ClassicColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ClassicColors.Background,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Temperature Thresholds
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (stableIsSelected) 
                    ClassicColors.Primary.copy(alpha = 0.1f)
                else 
                    ClassicColors.Background
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
                        color = ClassicColors.OnSurface
                    )
                    
                    // Temperature rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ClassicThermalThresholdChip(
                            label = "Emergency",
                            temperature = "${policy.emergencyThreshold}°C",
                            color = ClassicColors.Error,
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f)
                        )
                        ClassicThermalThresholdChip(
                            label = "Warning",
                            temperature = "${policy.warningThreshold}°C",
                            color = ClassicColors.Moderate,
                            icon = Icons.Default.Info,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ClassicThermalThresholdChip(
                            label = "Restore",
                            temperature = "${policy.restoreThreshold}°C",
                            color = ClassicColors.Secondary,
                            icon = Icons.Default.Restore,
                            modifier = Modifier.weight(1f)
                        )
                        ClassicThermalThresholdChip(
                            label = "Critical",
                            temperature = "${policy.criticalThreshold}°C",
                            color = ClassicColors.Error,
                            icon = Icons.Default.Error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicThermalThresholdChip(
    label: String,
    temperature: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(color) { color.copy(alpha = 0.15f) }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
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
                color = ClassicColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
