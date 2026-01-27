package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.LiquidHeader
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiquidTuningScreen(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager,
    isRootAvailable: Boolean,
    isLoading: Boolean,
    detectionTimeoutReached: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val cpuTemperature by viewModel.cpuTemperature.collectAsState()
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val ramConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())

    // 5 Cards: CPU, GPU, Thermal, RAM, Additional
    val pagerState = rememberPagerState(pageCount = { 5 })

    // Box container with WavyBlobOrnament background
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )
        
        // Foreground Layer
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title
                    Text(
                        text = "Tuning",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Import button
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(onClick = onImportClick) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = "Import Profile",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Export button
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(onClick = onExportClick) {
                                Icon(
                                    imageVector = Icons.Rounded.Save,
                                    contentDescription = "Export Profile",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(650.dp)
        ) { page ->
            
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState
                    .currentPageOffsetFraction
            ).absoluteValue

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .fillMaxHeight()
            ) {
                when (page) {
                    0 -> RecentCPUCard(
                        clusters = cpuClusters,
                        cpuInfo = cpuInfo,
                        temperature = cpuTemperature,
                        cpuLoad = cpuLoad,
                        onClick = { onNavigate("liquid_cpu_settings") }
                    )
                    1 -> RecentGPUCard(
                        gpuInfo = gpuInfo,
                        onClick = { onNavigate("liquid_gpu_settings") }
                    )
                    2 -> RecentThermalCard(
                        thermalPreset = prefsThermal,
                        cpuTemperature = cpuTemperature,
                        onClick = { onNavigate("liquid_thermal_settings") }
                    )
                    3 -> RecentRAMCard(
                        ramConfig = ramConfig,
                        onClick = { onNavigate("liquid_ram_settings") }
                    )
                    4 -> RecentAdditionalCard(
                        onClick = { onNavigate("liquid_additional_settings") }
                    )
                }
            }
        }
        }
    }
    }
}
