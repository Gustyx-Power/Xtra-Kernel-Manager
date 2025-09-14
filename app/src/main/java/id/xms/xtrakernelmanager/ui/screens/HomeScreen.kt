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
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.*
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


private const val TAG_SYSTEM_INFO_UTIL = "SystemInfoUtil"
private const val VALUE_UNKNOWN_SYS_INFO = "Unknown"

fun getSystemInfoFromDevice(): SystemInfo { // Ubah nama fungsi agar lebih jelas
    Log.d(TAG_SYSTEM_INFO_UTIL, "Mengambil SystemInfo (API based)...")
    var socName = VALUE_UNKNOWN_SYS_INFO

    // Get SoC information
    try {
        val manufacturerProcess = Runtime.getRuntime().exec("getprop ro.soc.manufacturer")
        val manufacturer = BufferedReader(InputStreamReader(manufacturerProcess.inputStream)).readLine()?.trim()
        manufacturerProcess.waitFor()
        manufacturerProcess.destroy()

        val modelProcess = Runtime.getRuntime().exec("getprop ro.soc.model")
        val model = BufferedReader(InputStreamReader(modelProcess.inputStream)).readLine()?.trim()
        modelProcess.waitFor()
        modelProcess.destroy()

        if (!manufacturer.isNullOrBlank() && !model.isNullOrBlank()) {
            socName = when {
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM7475", ignoreCase = true) -> "Qualcomm® Snapdragon™ 7+ Gen 2"
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8650", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8 Gen 3"
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8635", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8s Gen 3"
                manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM845", ignoreCase = true) || model.equals("sdm845", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 845"
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8250", ignoreCase = true) -> "Qualcomm® Snapdragon™ 870"
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8150", ignoreCase = true) -> "Qualcomm® Snapdragon™ 860"
                manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SM7435-AB", ignoreCase = true) || model.equals("SM7435", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 7s Gen 2"
                manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SM8735", ignoreCase = true) || model.equals("sm8735", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 8s Gen 4"
                manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM665", ignoreCase = true) || model.equals("sdm665", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 665"
                manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM660", ignoreCase = true) || model.equals("sdm660", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 660"
                manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8750", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8 Elite"
                manufacturer.equals("Mediatek", ignoreCase = true) && (model.equals("MT6785V/CD", ignoreCase = true) || model.equals("MT6785", ignoreCase = true)) -> "MediaTek Helio G95"
                manufacturer.equals("Mediatek", ignoreCase = true) && (model.equals("MT6877V/TTZA", ignoreCase = true) || model.equals("MT6877V", ignoreCase = true)) -> "MediaTek Dimensity 1080"
                manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6833GP", ignoreCase = true) -> "MediaTek Dimensity 6080"
                manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6769Z", ignoreCase = true) -> "MediaTek Helio G85"
                manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6989W", ignoreCase = true) -> "MediaTek Dimensity 9300+"
                else -> "$manufacturer $model"
            }
        }
    } catch (e: Exception) {
        Log.w(TAG_SYSTEM_INFO_UTIL, "Gagal mendapatkan info SOC dari getprop", e)
    }

    // Get display information
    var screenResolution = VALUE_UNKNOWN_SYS_INFO
    var displayTechnology = VALUE_UNKNOWN_SYS_INFO
    var refreshRate = VALUE_UNKNOWN_SYS_INFO
    var screenDpi = VALUE_UNKNOWN_SYS_INFO
    var gpuRenderer = VALUE_UNKNOWN_SYS_INFO

    try {
        // Get screen resolution - Try multiple methods like AIDA64
        try {
            // Method 1: Use wm size command
            val widthProcess = Runtime.getRuntime().exec("wm size")
            val sizeOutput = BufferedReader(InputStreamReader(widthProcess.inputStream)).readLine()?.trim()
            widthProcess.waitFor()
            widthProcess.destroy()

            if (!sizeOutput.isNullOrBlank() && sizeOutput.contains("Physical size:")) {
                val resolution = sizeOutput.substringAfter("Physical size: ").trim()
                if (resolution.isNotBlank() && resolution != sizeOutput && resolution.contains("x")) {
                    screenResolution = resolution
                }
            }

            // Method 2: If wm size fails, try dumpsys display
            if (screenResolution == VALUE_UNKNOWN_SYS_INFO) {
                val displayDumpProcess = Runtime.getRuntime().exec("dumpsys display")
                val displayReader = BufferedReader(InputStreamReader(displayDumpProcess.inputStream))
                var line: String?
                while (displayReader.readLine().also { line = it } != null) {
                    val currentLine = line
                    if (currentLine != null && currentLine.contains("real") && currentLine.contains("x")) {
                        val realMatch = Regex("real (\\d+) x (\\d+)").find(currentLine)
                        if (realMatch != null) {
                            screenResolution = "${realMatch.groupValues[1]}x${realMatch.groupValues[2]}"
                            break
                        }
                    }
                }
                displayDumpProcess.waitFor()
                displayDumpProcess.destroy()
            }
        } catch (e: Exception) {
            Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to get screen resolution", e)
        }

        // Get DPI with multiple fallback methods
        try {
            // Method 1: wm density
            val densityProcess = Runtime.getRuntime().exec("wm density")
            val densityOutput = BufferedReader(InputStreamReader(densityProcess.inputStream)).readLine()?.trim()
            densityProcess.waitFor()
            densityProcess.destroy()

            if (!densityOutput.isNullOrBlank() && densityOutput.contains("Physical density:")) {
                val dpi = densityOutput.substringAfter("Physical density: ").trim()
                if (dpi.isNotBlank() && dpi != densityOutput) {
                    screenDpi = "${dpi}dpi"
                }
            }

            // Method 2: Try getprop for DPI
            if (screenDpi == VALUE_UNKNOWN_SYS_INFO) {
                val dpiPropProcess = Runtime.getRuntime().exec("getprop ro.sf.lcd_density")
                val dpiPropOutput = BufferedReader(InputStreamReader(dpiPropProcess.inputStream)).readLine()?.trim()
                dpiPropProcess.waitFor()
                dpiPropProcess.destroy()

                if (!dpiPropOutput.isNullOrBlank()) {
                    screenDpi = "${dpiPropOutput}dpi"
                }
            }
        } catch (e: Exception) {
            Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to get DPI", e)
        }

        // Get refresh rate with comprehensive detection like AIDA64
        try {
            // Method 1: dumpsys display for current refresh rate
            val displayDumpProcess = Runtime.getRuntime().exec("dumpsys display")
            val displayReader = BufferedReader(InputStreamReader(displayDumpProcess.inputStream))
            var line: String?
            var foundRefreshRate = false

            while (displayReader.readLine().also { line = it } != null && !foundRefreshRate) {
                val currentLine = line
                when {
                    currentLine?.contains("refreshRate=") == true -> {
                        val rateMatch = Regex("refreshRate=([\\d.]+)").find(currentLine)
                        if (rateMatch != null) {
                            val rate = rateMatch.groupValues[1].toFloatOrNull()
                            if (rate != null) {
                                refreshRate = "${rate.toInt()}Hz"
                                foundRefreshRate = true
                            }
                        }
                    }
                    currentLine?.contains("mRefreshRate") == true -> {
                        val rateMatch = Regex("mRefreshRate=([\\d.]+)").find(currentLine)
                        if (rateMatch != null) {
                            val rate = rateMatch.groupValues[1].toFloatOrNull()
                            if (rate != null) {
                                refreshRate = "${rate.toInt()}Hz"
                                foundRefreshRate = true
                            }
                        }
                    }
                    currentLine?.contains("fps") == true && currentLine.contains("=") -> {
                        val fpsMatch = Regex("([\\d.]+)\\s*fps").find(currentLine)
                        if (fpsMatch != null) {
                            val fps = fpsMatch.groupValues[1].toFloatOrNull()
                            if (fps != null && fps > 30) {
                                refreshRate = "${fps.toInt()}Hz"
                                foundRefreshRate = true
                            }
                        }
                    }
                }
            }
            displayDumpProcess.waitFor()
            displayDumpProcess.destroy()

            // Method 2: Try surface flinger properties
            if (!foundRefreshRate) {
                val refreshProps = listOf(
                    "ro.surface_flinger.max_frame_buffer_acquired_buffers",
                    "ro.surface_flinger.vsync_event_phase_offset_ns",
                    "debug.sf.frame_rate_multiple_threshold",
                    "ro.vendor.display.default_fps"
                )

                for (prop in refreshProps) {
                    try {
                        val propProcess = Runtime.getRuntime().exec("getprop $prop")
                        val propOutput = BufferedReader(InputStreamReader(propProcess.inputStream)).readLine()?.trim()
                        propProcess.waitFor()
                        propProcess.destroy()

                        if (!propOutput.isNullOrBlank() && propOutput != "0") {
                            val rate = propOutput.toFloatOrNull()
                            if (rate != null && rate >= 60) {
                                refreshRate = "${rate.toInt()}Hz"
                                break
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }

            // Method 3: Check common refresh rate files
            if (refreshRate == VALUE_UNKNOWN_SYS_INFO) {
                val refreshRateFiles = listOf(
                    "/sys/class/drm/card0-DSI-1/modes",
                    "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/modes"
                )

                for (file in refreshRateFiles) {
                    try {
                        val process = Runtime.getRuntime().exec("cat $file")
                        val reader = BufferedReader(InputStreamReader(process.inputStream))
                        val output = reader.readLine()?.trim()
                        process.waitFor()
                        process.destroy()

                        if (!output.isNullOrBlank()) {
                            // Parse mode string like "1080x2400@60Hz" or similar
                            val modeMatch = Regex("@(\\d+)Hz").find(output)
                            if (modeMatch != null) {
                                refreshRate = "${modeMatch.groupValues[1]}Hz"
                                break
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }

            // Default fallback - but try to be smarter about it
            if (refreshRate == VALUE_UNKNOWN_SYS_INFO) {
                refreshRate = "60Hz" // Conservative default
            }

        } catch (e: Exception) {
            Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to get refresh rate", e)
            refreshRate = "60Hz"
        }

        // Get GPU renderer with enhanced detection
        try {
            var foundGpu = false

            // Method 1: Try OpenGL renderer string
            try {
                val glProcess = Runtime.getRuntime().exec("getprop ro.hardware.egl")
                val glOutput = BufferedReader(InputStreamReader(glProcess.inputStream)).readLine()?.trim()
                glProcess.waitFor()
                glProcess.destroy()

                if (!glOutput.isNullOrBlank()) {
                    gpuRenderer = when {
                        glOutput.contains("adreno", ignoreCase = true) -> {
                            val adrenoMatch = Regex("adreno([\\d]+)", RegexOption.IGNORE_CASE).find(glOutput)
                            if (adrenoMatch != null) {
                                "Adreno ${adrenoMatch.groupValues[1]}"
                            } else {
                                "Adreno GPU"
                            }
                        }
                        glOutput.contains("mali", ignoreCase = true) -> {
                            val maliMatch = Regex("mali[\\s-]?([\\w\\d]+)", RegexOption.IGNORE_CASE).find(glOutput)
                            if (maliMatch != null) {
                                "Mali ${maliMatch.groupValues[1].uppercase()}"
                            } else {
                                "Mali GPU"
                            }
                        }
                        glOutput.contains("powervr", ignoreCase = true) -> "PowerVR GPU"
                        else -> glOutput.capitalizeWords()
                    }
                    foundGpu = true
                }
            } catch (e: Exception) {
                Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to get GPU from egl property", e)
            }

            // Method 2: Try Vulkan properties
            if (!foundGpu) {
                val vulkanProcess = Runtime.getRuntime().exec("getprop ro.hardware.vulkan")
                val vulkanOutput = BufferedReader(InputStreamReader(vulkanProcess.inputStream)).readLine()?.trim()
                vulkanProcess.waitFor()
                vulkanProcess.destroy()

                if (!vulkanOutput.isNullOrBlank()) {
                    gpuRenderer = vulkanOutput.capitalizeWords()
                    foundGpu = true
                }
            }

            // Method 3: Try GPU-specific properties
            if (!foundGpu) {
                val gpuProps = listOf(
                    "ro.vendor.gpu.available_frequencies",
                    "ro.opengles.version",
                    "debug.sf.hw"
                )

                for (prop in gpuProps) {
                    try {
                        val propProcess = Runtime.getRuntime().exec("getprop $prop")
                        val propOutput = BufferedReader(InputStreamReader(propProcess.inputStream)).readLine()?.trim()
                        propProcess.waitFor()
                        propProcess.destroy()

                        if (!propOutput.isNullOrBlank() && propOutput != "0") {
                            when {
                                propOutput.contains("adreno", ignoreCase = true) -> {
                                    gpuRenderer = "Adreno GPU"
                                    foundGpu = true
                                    break
                                }
                                propOutput.contains("mali", ignoreCase = true) -> {
                                    gpuRenderer = "Mali GPU"
                                    foundGpu = true
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }

            if (!foundGpu) {
                gpuRenderer = VALUE_UNKNOWN_SYS_INFO
            }
        } catch (e: Exception) {
            Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to get GPU info", e)
            gpuRenderer = VALUE_UNKNOWN_SYS_INFO
        }

        // Get display technology with enhanced detection
        try {
            var foundTech = false

            // Method 1: Vendor display panel properties
            val panelProps = listOf(
                "ro.vendor.display.panel_name",
                "ro.vendor.display.type",
                "vendor.display.panel_type",
                "ro.hardware.display"
            )

            for (prop in panelProps) {
                try {
                    val panelProcess = Runtime.getRuntime().exec("getprop $prop")
                    val panelOutput = BufferedReader(InputStreamReader(panelProcess.inputStream)).readLine()?.trim()
                    panelProcess.waitFor()
                    panelProcess.destroy()

                    if (!panelOutput.isNullOrBlank()) {
                        val detectedTech = when {
                            panelOutput.contains("amoled", ignoreCase = true) -> "AMOLED"
                            panelOutput.contains("super_amoled", ignoreCase = true) -> "Super AMOLED"
                            panelOutput.contains("dynamic_amoled", ignoreCase = true) -> "Dynamic AMOLED"
                            panelOutput.contains("oled", ignoreCase = true) -> "OLED"
                            panelOutput.contains("poled", ignoreCase = true) -> "P-OLED"
                            panelOutput.contains("ips", ignoreCase = true) -> "IPS LCD"
                            panelOutput.contains("tft", ignoreCase = true) -> "TFT LCD"
                            panelOutput.contains("lcd", ignoreCase = true) -> "LCD"
                            panelOutput.contains("ltps", ignoreCase = true) -> "LTPS LCD"
                            else -> null
                        }
                        if (detectedTech != null) {
                            displayTechnology = detectedTech
                            foundTech = true
                            break
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Method 2: Try display driver information
            if (!foundTech) {
                try {
                    val driverProcess = Runtime.getRuntime().exec("find /sys/class/drm -name 'card*' -type d")
                    val driverReader = BufferedReader(InputStreamReader(driverProcess.inputStream))
                    var line: String?
                    while (driverReader.readLine().also { line = it } != null) {
                        val currentLine = line
                        if (currentLine?.contains("card0") == true) {
                            val statusProcess = Runtime.getRuntime().exec("cat $currentLine/card0-DSI-1/status 2>/dev/null")
                            val status = BufferedReader(InputStreamReader(statusProcess.inputStream)).readLine()?.trim()
                            statusProcess.waitFor()
                            statusProcess.destroy()

                            if (status == "connected") {
                                // Try to determine technology from DSI connection
                                displayTechnology = "OLED" // DSI is commonly used with OLED panels
                                foundTech = true
                                break
                            }
                        }
                    }
                    driverProcess.waitFor()
                    driverProcess.destroy()
                } catch (e: Exception) {
                    Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to check display driver", e)
                }
            }

            // Method 3: Fallback - try to infer from device characteristics
            if (!foundTech) {
                val deviceModel = android.os.Build.MODEL?.lowercase() ?: ""
                displayTechnology = when {
                    deviceModel.contains("galaxy") && deviceModel.contains("s") -> "Dynamic AMOLED"
                    deviceModel.contains("galaxy") && deviceModel.contains("a") -> "Super AMOLED"
                    deviceModel.contains("pixel") && deviceModel.matches(Regex(".*pixel\\s*[6-9].*")) -> "OLED"
                    deviceModel.contains("oneplus") || deviceModel.contains("oppo") -> "AMOLED"
                    deviceModel.contains("xiaomi") || deviceModel.contains("redmi") -> "AMOLED"
                    else -> "LCD" // Conservative fallback
                }
            }
        } catch (e: Exception) {
            Log.w(TAG_SYSTEM_INFO_UTIL, "Failed to detect display technology", e)
            displayTechnology = "LCD"
        }

    } catch (e: Exception) {
        Log.w(TAG_SYSTEM_INFO_UTIL, "Gagal mendapatkan info display", e)
        // Set fallbacks
        if (screenResolution == VALUE_UNKNOWN_SYS_INFO) screenResolution = VALUE_UNKNOWN_SYS_INFO
        if (displayTechnology == VALUE_UNKNOWN_SYS_INFO) displayTechnology = "LCD"
        if (refreshRate == VALUE_UNKNOWN_SYS_INFO) refreshRate = "60Hz"
        if (screenDpi == VALUE_UNKNOWN_SYS_INFO) screenDpi = VALUE_UNKNOWN_SYS_INFO
        if (gpuRenderer == VALUE_UNKNOWN_SYS_INFO) gpuRenderer = VALUE_UNKNOWN_SYS_INFO
    }

    return SystemInfo(
        model = android.os.Build.MODEL,
        codename = android.os.Build.DEVICE,
        androidVersion = android.os.Build.VERSION.RELEASE,
        sdk = android.os.Build.VERSION.SDK_INT,
        fingerprint = android.os.Build.FINGERPRINT,
        soc = socName,
        screenResolution = screenResolution,
        displayTechnology = displayTechnology,
        refreshRate = refreshRate,
        screenDpi = screenDpi,
        gpuRenderer = gpuRenderer
    ).also { Log.d(TAG_SYSTEM_INFO_UTIL, "SystemInfo: $it") }
}

// Extension function to capitalize words
private fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = hiltViewModel()
    val storageViewModel: id.xms.xtrakernelmanager.ui.viewmodel.StorageInfoViewModel = hiltViewModel()

    // Kumpulkan semua state dari ViewModel
    val cpuInfo by vm.cpuInfo.collectAsState()
    val batteryInfo by vm.batteryInfo.collectAsState()
    val memoryInfo by vm.memoryInfo.collectAsState()
    val deepSleepInfo by vm.deepSleep.collectAsState()
    val rootStatus by vm.rootStatus.collectAsState()
    val kernelInfo by vm.kernelInfo.collectAsState()
    val appVersion by vm.appVersion.collectAsState()
    val systemInfoState by vm.systemInfo.collectAsState()
    val storageInfo by storageViewModel.storageInfo.collectAsState()

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
                val socNameToDisplay = currentSystemInfo?.soc?.takeIf { it.isNotBlank() && it != VALUE_UNKNOWN_SYS_INFO } ?: cpuInfo.soc.takeIf { it.isNotBlank() && it != "Unknown SoC" && it != "N/A" } ?: "CPU"
                CpuCard(socNameToDisplay, cpuInfo, false, modifier)
            }

            /* 2. Merged card */
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


            /* 3. Kernel */
            val currentKernel = kernelInfo
            if (currentKernel != null) {
                FadeInEffect { modifier ->
                    KernelCard(currentKernel, modifier)
                }
            } else {
                // Opsional: Placeholder untuk KernelCard
            }


            /* 4. About */
            FadeInEffect { modifier ->
                AboutCard(false, modifier)
            }
        }
    }
}



