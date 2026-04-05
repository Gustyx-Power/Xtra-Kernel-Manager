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
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicThermalIndexSelectionScreen(
    viewModel: TuningViewModel,
    currentIndex: String,
    onNavigateBack: () -> Unit,
    onIndexSelected: (String) -> Unit
) {
    val thermalOptions = remember {
        listOf(
            ClassicThermalIndexOption("Not Set", R.string.thermal_not_set, "No thermal management applied", Icons.Default.Block, "Default system behavior"),
            ClassicThermalIndexOption("Class 0", R.string.thermal_class_0, "Balanced thermal management", Icons.Default.Speed, "Optimal balance between performance and temperature"),
            ClassicThermalIndexOption("Extreme", R.string.thermal_extreme, "Maximum performance mode", Icons.Default.Whatshot, "High performance with increased heat generation"),
            ClassicThermalIndexOption("Dynamic", R.string.thermal_dynamic, "Adaptive thermal control", Icons.Default.AutoMode, "Automatically adjusts based on usage patterns"),
            ClassicThermalIndexOption("Incalls", R.string.thermal_incalls, "Optimized for voice calls", Icons.Default.Call, "Reduces heat during phone calls"),
            ClassicThermalIndexOption("Thermal 20", R.string.thermal_20, "Custom thermal profile", Icons.Default.LocalFireDepartment, "Advanced thermal management profile")
        )
    }

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
                            text = "Thermal Index",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Choose thermal management mode",
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
                    title = "Thermal Index System",
                    description = "Thermal index controls how your device manages heat generation and performance throttling. Choose the mode that best fits your usage pattern.",
                    icon = Icons.Default.Thermostat,
                    color = ClassicColors.Error
                )
            }
            
            // Options List with stable keys
            items(
                count = thermalOptions.size,
                key = { index -> thermalOptions[index].key }
            ) { index ->
                val option = thermalOptions[index]
                val isSelected = option.key == currentIndex
                
                ClassicThermalIndexOptionCard(
                    option = option,
                    isSelected = isSelected,
                    onClick = { onIndexSelected(option.key) }
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
private fun ClassicThermalIndexOptionCard(
    option: ClassicThermalIndexOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "thermal_index_card_scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) 
            ClassicColors.Error.copy(alpha = 0.15f)
        else 
            ClassicColors.Surface,
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, ClassicColors.Error)
        else null,
        shadowElevation = if (isSelected) 4.dp else 1.dp
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
                        if (isSelected) ClassicColors.Error
                        else ClassicColors.Error.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = if (isSelected) ClassicColors.Background
                    else ClassicColors.Error,
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
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = option.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = option.detailedDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            }
            
            // Selection indicator
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
                        .background(ClassicColors.Error),
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
    }
}

data class ClassicThermalIndexOption(
    val key: String,
    val nameRes: Int,
    val shortDescription: String,
    val icon: ImageVector,
    val detailedDescription: String
)
