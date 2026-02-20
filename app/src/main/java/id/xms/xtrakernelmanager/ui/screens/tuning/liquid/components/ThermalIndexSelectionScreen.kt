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
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalIndexSelectionScreen(
    viewModel: TuningViewModel,
    currentIndex: String,
    onNavigateBack: () -> Unit,
    onIndexSelected: (String) -> Unit
) {
    val thermalOptions = remember {
        listOf(
            ThermalIndexOption("Not Set", R.string.thermal_not_set, "No thermal management applied", Icons.Default.Block, "Default system behavior"),
            ThermalIndexOption("Class 0", R.string.thermal_class_0, "Balanced thermal management", Icons.Default.Speed, "Optimal balance between performance and temperature"),
            ThermalIndexOption("Extreme", R.string.thermal_extreme, "Maximum performance mode", Icons.Default.Whatshot, "High performance with increased heat generation"),
            ThermalIndexOption("Dynamic", R.string.thermal_dynamic, "Adaptive thermal control", Icons.Default.AutoMode, "Automatically adjusts based on usage patterns"),
            ThermalIndexOption("Incalls", R.string.thermal_incalls, "Optimized for voice calls", Icons.Default.Call, "Reduces heat during phone calls"),
            ThermalIndexOption("Thermal 20", R.string.thermal_20, "Custom thermal profile", Icons.Default.LocalFireDepartment, "Advanced thermal management profile")
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    title = "Thermal Index",
                    subtitle = "Choose thermal management mode",
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
                item {
                    InfoCard(
                        title = "Thermal Index System",
                        description = "Thermal index controls how your device manages heat generation and performance throttling. Choose the mode that best fits your usage pattern.",
                        icon = Icons.Default.Thermostat,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Options List with stable keys
                items(
                    count = thermalOptions.size,
                    key = { index -> thermalOptions[index].key }
                ) { index ->
                    val option = thermalOptions[index]
                    val isSelected = option.key == currentIndex
                    
                    ThermalIndexOptionCard(
                        option = option,
                        isSelected = isSelected,
                        onClick = { onIndexSelected(option.key) }
                    )
                }
                
                // Bottom spacing
                item {
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFEF4444).copy(alpha = 0.15f)
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
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
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
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF97316).copy(alpha = 0.15f)
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
                    .background(Color(0xFFF97316).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
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
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ThermalIndexOptionCard(
    option: ThermalIndexOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) 
            Color(0xFFEF4444).copy(alpha = 0.2f) 
        else 
            Color(0xFFFBBF24).copy(alpha = 0.12f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFEF4444).copy(alpha = 0.15f),
                                Color(0xFFF97316).copy(alpha = 0.1f)
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
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444),
                                        Color(0xFFEF4444).copy(alpha = 0.8f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFBBF24).copy(alpha = 0.3f),
                                        Color(0xFFFBBF24).copy(alpha = 0.2f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White
                        else Color(0xFFFBBF24),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(option.nameRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFFEF4444)
                        else Color.White
                    )
                    Text(
                        text = option.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = option.detailedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 16.sp
                    )
                }
                
                // Selection indicator
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
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
        }
    }
}

data class ThermalIndexOption(
    val key: String,
    val nameRes: Int,
    val shortDescription: String,
    val icon: ImageVector,
    val detailedDescription: String
)