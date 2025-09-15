package id.xms.xtrakernelmanager.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.*
import id.xms.xtrakernelmanager.viewmodel.GraphDataViewModel
import id.xms.xtrakernelmanager.viewmodel.HomeViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.text.isNotBlank


@Composable
fun FadeInEffect(
    shimmerEnabled: Boolean = false,
    content: @Composable (Modifier) -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    val transition = rememberTransition(visibleState, label = "FadeInTransition")
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alpha"
    ) { if (it) 1f else 0f }

    val shimmerBrush = if (shimmerEnabled) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )
        val translateAnim = rememberInfiniteTransition(label = "shimmerTransitionFadeIn").animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ), label = "shimmerTranslateFadeIn"
        )
        Brush.linearGradient(
            colors = if (shimmerColors.size >= 2) shimmerColors else listOf(Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.2f)),
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset(x = translateAnim.value, y = translateAnim.value)
        )
    } else null

    Box(modifier = Modifier.alpha(alpha)) {
        content(if (shimmerBrush != null) Modifier.graphicsLayer(alpha = 0.99f)
        else Modifier)
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = hiltViewModel()
    val storageViewModel: id.xms.xtrakernelmanager.ui.viewmodel.StorageInfoViewModel = hiltViewModel()
    val graphDataViewModel: id.xms.xtrakernelmanager.viewmodel.GraphDataViewModel = viewModel()

    // Kumpulkan semua state dari ViewModel
    val cpuInfo by vm.cpuInfo.collectAsState()
    val gpuInfo by vm.gpuInfo.collectAsState()
    val batteryInfo by vm.batteryInfo.collectAsState()
    val memoryInfo by vm.memoryInfo.collectAsState()
    val deepSleepInfo by vm.deepSleep.collectAsState()
    val rootStatus by vm.rootStatus.collectAsState()
    val kernelInfo by vm.kernelInfo.collectAsState()
    val appVersion by vm.appVersion.collectAsState()
    val systemInfoState by vm.systemInfo.collectAsState()
    val cpuClusters by vm.cpuClusters.collectAsState()
    val storageInfo by storageViewModel.storageInfo.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }

    val fullTitle = stringResource(R.string.n0kz_kernel_manager)
    val displayedTitle = fullTitle

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val systemUiController = rememberSystemUiController()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorAtElevation = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val topBarContainerColor by remember {
        derivedStateOf {
            lerp(
                surfaceColor,
                surfaceColorAtElevation,
                scrollBehavior.state.overlappedFraction
            )
        }
    }
    val darkTheme = isSystemInDarkTheme()

    LaunchedEffect(topBarContainerColor, darkTheme) {
        systemUiController.setStatusBarColor(
            color = topBarContainerColor,
            darkIcons = !darkTheme
        )
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(200)
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(200)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.power_off)) },
                            icon = { Icon(Icons.Filled.PowerSettingsNew, contentDescription = null) },
                            onClick = { Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p")) },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.reboot_recovery)) },
                            icon = { Icon(Icons.Filled.SettingsBackupRestore, contentDescription = null) },
                            onClick = { Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot recovery")) }
                        )
                        
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.reboot_bootloader)) },
                            icon = { Icon(Icons.Filled.Build, contentDescription = null) },
                            onClick = { Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot bootloader")) }
                        )
                        
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.reboot_system)) },
                            icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                            onClick = { Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot")) }
                        )
                    }
                }
                
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                ) {
                    val iconRotation by animateFloatAsState(
                        targetValue = if (showFabMenu) 45f else 0f,
                        animationSpec = tween(durationMillis = 300), label = "fabIconRotation"
                    )
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Toggle FAB Menu",
                        modifier = Modifier.graphicsLayer(rotationZ = iconRotation)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayedTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /* 1. CPU */
                FadeInEffect { modifier ->
                    val currentSystemInfo = systemInfoState
                    val socNameToDisplay = currentSystemInfo?.soc?.takeIf { it.isNotBlank() && it != "Unknown" } ?: cpuInfo.soc.takeIf { it.isNotBlank() && it != "Unknown SoC" && it != "N/A" } ?: "CPU"
                    CpuCard(socNameToDisplay, cpuInfo, cpuClusters, false, modifier, graphDataViewModel)
                }

                /* 2. GPU */
                FadeInEffect { modifier ->
                    GpuCard(gpuInfo, modifier, graphDataViewModel)
                }

                /* 3. Merged card */
                val currentBattery = batteryInfo
                val currentMemory = memoryInfo
                val currentDeepSleep = deepSleepInfo
                val currentRoot = rootStatus
                val currentVersion = appVersion
                val currentSystem = systemInfoState

                if (currentBattery != null && currentMemory != null && currentDeepSleep != null &&
                    currentRoot != null && currentVersion != null && currentSystem != null) {
                    FadeInEffect { modifier ->
                        MergedSystemCard(
                            b = currentBattery,
                            d = currentDeepSleep,
                            rooted = currentRoot,
                            version = currentVersion,
                            mem = currentMemory,
                            systemInfo = currentSystem,
                            storageInfo = storageInfo,
                            modifier = modifier
                        )
                    }
                } else {
                    FadeInEffect { modifier ->
                        Box(modifier.fillMaxWidth().height(200.dp).background(Color.LightGray.copy(alpha = 0.5f))) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

                /* 4. Kernel */
                val currentKernel = kernelInfo
                if (currentKernel != null) {
                    FadeInEffect { modifier ->
                        KernelCard(currentKernel, modifier)
                    }
                } else {
                    // Opsional: Placeholder untuk KernelCard
                }

                /* 5. About */
                FadeInEffect { modifier ->
                    AboutCard(false, modifier)
                }
            }
        }
    }
}