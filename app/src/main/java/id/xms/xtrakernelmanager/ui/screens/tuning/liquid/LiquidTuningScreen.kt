package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val ramConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())

    // 5 Cards: CPU, GPU, Thermal, RAM, Additional
    val pagerState = rememberPagerState(pageCount = { 5 })

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Tuning", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 24.sp
                    ) 
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import Profile") },
                                onClick = {
                                    showMenu = false
                                    onImportClick()
                                },
                                leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text("Export Profile") },
                                onClick = {
                                    showMenu = false
                                    onExportClick()
                                },
                                leadingIcon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
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
                        onClick = { onNavigate("liquid_cpu_settings") }
                    )
                    1 -> RecentGPUCard(
                        gpuInfo = gpuInfo,
                        onClick = { onNavigate("liquid_gpu_settings") }
                    )
                    2 -> RecentThermalCard(
                        thermalPreset = prefsThermal,
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
