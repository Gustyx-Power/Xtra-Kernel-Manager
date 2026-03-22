package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

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
            ThermalIndexOption("Not Set", R.string.thermal_not_set, R.string.thermal_not_set_long_desc, Icons.Default.Block, R.string.thermal_not_set_detail),
            ThermalIndexOption("Class 0", R.string.thermal_class_0, R.string.thermal_class_0_long_desc, Icons.Default.Speed, R.string.thermal_class_0_detail),
            ThermalIndexOption("Extreme", R.string.thermal_extreme, R.string.thermal_extreme_long_desc, Icons.Default.Whatshot, R.string.thermal_extreme_detail),
            ThermalIndexOption("Dynamic", R.string.thermal_dynamic, R.string.thermal_dynamic_long_desc, Icons.Default.AutoMode, R.string.thermal_dynamic_detail),
            ThermalIndexOption("Incalls", R.string.thermal_incalls, R.string.thermal_incalls_long_desc, Icons.Default.Call, R.string.thermal_incalls_detail),
            ThermalIndexOption("Thermal 20", R.string.thermal_20, R.string.thermal_20_long_desc, Icons.Default.LocalFireDepartment, R.string.thermal_20_detail)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    title = stringResource(R.string.thermal_index),
                    subtitle = stringResource(R.string.thermal_choose_management_mode),
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
                        title = stringResource(R.string.thermal_index_system),
                        description = stringResource(R.string.thermal_index_system_desc),
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
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
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
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
private fun ThermalIndexOptionCard(
    option: ThermalIndexOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    GlassmorphicCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(option.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(option.shortDescRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(option.detailDescRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
            
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class ThermalIndexOption(
    val key: String,
    val nameRes: Int,
    val shortDescRes: Int,
    val icon: ImageVector,
    val detailDescRes: Int
)