package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

enum class LiquidProfileType(val displayName: String, val governor: String, val thermalPreset: String) {
    PERFORMANCE("Performance", "performance", "Extreme"),
    BALANCED("Balanced", "schedutil", "Dynamic"),
    POWER_SAVE("Power Save", "powersave", "Class 0"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidPerAppProfileScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Detect device max refresh rate
    val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
    val availableRefreshRates = remember(maxRefreshRate) { 
        getAvailableRefreshRates(maxRefreshRate) 
    }
    
    // Load profiles from PreferencesManager
    val profilesJson by preferencesManager.getAppProfiles().collectAsState(initial = "[]")
    val savedProfiles = remember(profilesJson) { parseProfiles(profilesJson) }
    
    // Get installed apps and merge with saved profiles
    val installedApps = remember { getInstalledApps(context) }
    val appProfiles = remember(installedApps, savedProfiles) {
        installedApps.map { app ->
            savedProfiles.find { it.packageName == app.packageName } ?: app
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<LiquidProfileType?>(null) }
    
    // Inline expansion state
    var expandedAppPackage by remember { mutableStateOf<String?>(null) }

    val filteredApps = remember(appProfiles, searchQuery, selectedFilter) {
        val baseList = if (searchQuery.isBlank()) {
            appProfiles.sortedBy { it.appName }
        } else {
            appProfiles.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.appName }
        }

        if (selectedFilter != null) {
            baseList.filter { getProfileTypeFromApp(it) == selectedFilter }
        } else {
            baseList
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
            Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                            )
                        }
                        Text(
                            text = "Per-App Profiles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search & Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Search Field
                    GlassmorphicCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                                ),
                                decorationBox = @Composable { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Search apps...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor().copy(alpha = 0.5f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(18.dp),
                                        tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                                    )
                                }
                            }
                        }
                    }

                    // Filter Button
                    Box {
                        var filterExpanded by remember { mutableStateOf(false) }
                        
                        GlassmorphicCard(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { filterExpanded = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedFilter != null) Icons.Rounded.FilterListOff else Icons.Rounded.FilterList,
                                    contentDescription = "Filter",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selectedFilter != null) 
                                        MaterialTheme.colorScheme.primary 
                                    else id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    selectedFilter = null
                                    filterExpanded = false
                                },
                                leadingIcon = { if (selectedFilter == null) Icon(Icons.Rounded.Check, null) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            LiquidProfileType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        selectedFilter = type
                                        filterExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedFilter == type) {
                                            Icon(Icons.Rounded.Check, null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // App List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Active Configs Section
                    val activeConfigs = appProfiles.filter { 
                        it.governor != "schedutil" || it.thermalPreset != "Not Set" || it.refreshRate != 0
                    }
                    
                    if (searchQuery.isEmpty() && selectedFilter == null && activeConfigs.isNotEmpty()) {
                        item {
                            Text(
                                "Active Configurations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(),
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(activeConfigs) { app ->
                                    LiquidActiveConfigCard(
                                        app = app,
                                        onClick = { expandedAppPackage = app.packageName }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                "All Apps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(),
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                        }
                    }

                    items(filteredApps, key = { it.packageName }) { app ->
                        LiquidExpressiveAppItem(
                            app = app,
                            availableRefreshRates = availableRefreshRates,
                            isExpanded = expandedAppPackage == app.packageName,
                            onToggleExpand = {
                                expandedAppPackage = if (expandedAppPackage == app.packageName) null else app.packageName
                            },
                            onUpdate = { updatedApp ->
                                scope.launch {
                                    saveProfile(preferencesManager, updatedApp, appProfiles)
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}


@Composable
fun LiquidActiveConfigCard(app: AppProfile, onClick: () -> Unit) {
    val context = LocalContext.current
    val profileType = getProfileTypeFromApp(app)
    val color = getLiquidProfileColor(profileType)

    GlassmorphicCard(
        modifier = Modifier
            .size(width = 160.dp, height = 180.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(getAppIcon(context, app.packageName))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                )

                Surface(
                    color = color,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        getLiquidProfileIcon(profileType),
                        null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Column {
                Text(
                    app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if(app.refreshRate != 0) "${app.refreshRate}Hz" else profileType.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LiquidExpressiveAppItem(
    app: AppProfile,
    availableRefreshRates: List<Int>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdate: (AppProfile) -> Unit
) {
    val context = LocalContext.current
    val isCustomized = app.governor != "schedutil" || app.thermalPreset != "Not Set" || app.refreshRate != 0
    val profileType = getProfileTypeFromApp(app)
    val profileColor = getLiquidProfileColor(profileType)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpand() },
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // App icon
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(getAppIcon(context, app.packageName))
                        .crossfade(true)
                        .build(),
                    contentDescription = app.appName,
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                app.appName.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Android, null)
                        }
                    },
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                )

                // App info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor().copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if(isCustomized && !isExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = profileColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    profileType.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = profileColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (app.refreshRate != 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "${app.refreshRate}Hz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Icon(
                    if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
            }
            
            // Inline Expansion Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor().copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Performance Profile Dropdown
                    LiquidConfigDropdownRow(
                        label = "Performance Profile",
                        currentValue = profileType.displayName,
                        icon = Icons.Rounded.Settings,
                        isModified = true
                    ) { dismiss ->
                        LiquidProfileType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = { 
                                    onUpdate(app.copy(
                                        governor = type.governor,
                                        thermalPreset = type.thermalPreset
                                    ))
                                    dismiss()
                                },
                                leadingIcon = { 
                                    Icon(
                                        getLiquidProfileIcon(type),
                                        null,
                                        tint = getLiquidProfileColor(type)
                                    )
                                },
                                trailingIcon = {
                                    if(getProfileTypeFromApp(app) == type) Icon(Icons.Rounded.Check, null)
                                }
                            )
                        }
                    }

                    // Refresh Rate Dropdown - Only show if device supports >60Hz
                    if (availableRefreshRates.isNotEmpty()) {
                        LiquidConfigDropdownRow(
                            label = "Refresh Rate",
                            currentValue = if(app.refreshRate == 0) "Default" else "${app.refreshRate}Hz",
                            icon = Icons.Rounded.Refresh,
                            isModified = app.refreshRate != 0
                        ) { dismiss ->
                            // Default option
                            DropdownMenuItem(
                                text = { Text("Default") },
                                onClick = { 
                                    onUpdate(app.copy(refreshRate = 0))
                                    dismiss()
                                },
                                leadingIcon = {
                                    if(app.refreshRate == 0) Icon(Icons.Rounded.Check, null)
                                }
                            )
                            
                            // Available refresh rates
                            availableRefreshRates.forEach { rate ->
                                DropdownMenuItem(
                                    text = { Text("${rate}Hz") },
                                    onClick = { 
                                        onUpdate(app.copy(refreshRate = rate))
                                        dismiss()
                                    },
                                    leadingIcon = {
                                        if(app.refreshRate == rate) Icon(Icons.Rounded.Check, null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidConfigDropdownRow(
    label: String,
    currentValue: String,
    icon: ImageVector,
    isModified: Boolean,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                null,
                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(),
                modifier = Modifier.size(24.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
            )
        }
        
        Box {
            GlassmorphicCard(
                modifier = Modifier
                    .height(36.dp)
                    .clickable { expanded = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        currentValue,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if(isModified) 
                            MaterialTheme.colorScheme.primary 
                        else id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                    )
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                content { expanded = false }
            }
        }
    }
}

// Helper functions
@Composable
private fun getLiquidProfileColor(profile: LiquidProfileType): Color {
    return when (profile) {
        LiquidProfileType.PERFORMANCE -> MaterialTheme.colorScheme.primary
        LiquidProfileType.BALANCED -> MaterialTheme.colorScheme.secondary
        LiquidProfileType.POWER_SAVE -> MaterialTheme.colorScheme.tertiary
    }
}

private fun getLiquidProfileIcon(profile: LiquidProfileType): ImageVector {
    return when (profile) {
        LiquidProfileType.PERFORMANCE -> Icons.Rounded.RocketLaunch
        LiquidProfileType.BALANCED -> Icons.Rounded.Balance
        LiquidProfileType.POWER_SAVE -> Icons.Rounded.BatteryChargingFull
    }
}

private fun getProfileTypeFromApp(app: AppProfile): LiquidProfileType {
    return when {
        app.governor == "performance" && app.thermalPreset == "Extreme" -> LiquidProfileType.PERFORMANCE
        app.governor == "powersave" && app.thermalPreset == "Class 0" -> LiquidProfileType.POWER_SAVE
        app.governor == "schedutil" && app.thermalPreset == "Dynamic" -> LiquidProfileType.BALANCED
        else -> LiquidProfileType.BALANCED
    }
}

private fun getDeviceMaxRefreshRate(context: Context): Int {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = context.display ?: windowManager.defaultDisplay
            display.supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 60
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            display.refreshRate.toInt()
        }
    } catch (e: Exception) {
        60
    }
}

private fun getAvailableRefreshRates(maxRate: Int): List<Int> {
    return when {
        maxRate >= 144 -> listOf(60, 90, 120, 144)
        maxRate >= 120 -> listOf(60, 90, 120)
        maxRate >= 90 -> listOf(60, 90)
        else -> emptyList()
    }
}

private fun getInstalledApps(context: Context): List<AppProfile> {
    return try {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        apps.filter {
            (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
        }.map { info ->
            AppProfile(
                packageName = info.packageName,
                appName = pm.getApplicationLabel(info).toString(),
                governor = "schedutil",
                thermalPreset = "Not Set",
                refreshRate = 0,
                enabled = true
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun getAppIcon(context: Context, packageName: String): android.graphics.drawable.Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
    }
}

private fun parseProfiles(json: String): List<AppProfile> {
    return try {
        val jsonArray = JSONArray(json)
        val profiles = mutableListOf<AppProfile>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            profiles.add(
                AppProfile(
                    packageName = obj.getString("packageName"),
                    appName = obj.getString("appName"),
                    governor = obj.optString("governor", "schedutil"),
                    thermalPreset = obj.optString("thermalPreset", "Not Set"),
                    refreshRate = obj.optInt("refreshRate", 0),
                    enabled = obj.optBoolean("enabled", true),
                )
            )
        }
        profiles
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun saveProfile(
    preferencesManager: PreferencesManager,
    updatedApp: AppProfile,
    allApps: List<AppProfile>
) {
    val currentJson = preferencesManager.getAppProfiles().first()
    val currentProfiles = parseProfiles(currentJson).toMutableList()
    
    val isCustomized = updatedApp.thermalPreset != "Not Set" || updatedApp.refreshRate != 0
    
    if (isCustomized) {
        val existingIndex = currentProfiles.indexOfFirst { it.packageName == updatedApp.packageName }
        if (existingIndex >= 0) {
            currentProfiles[existingIndex] = updatedApp
        } else {
            currentProfiles.add(updatedApp)
        }
    } else {
        currentProfiles.removeAll { it.packageName == updatedApp.packageName }
    }
    
    val jsonArray = JSONArray()
    currentProfiles.forEach { profile ->
        val obj = JSONObject().apply {
            put("packageName", profile.packageName)
            put("appName", profile.appName)
            put("governor", profile.governor)
            put("thermalPreset", profile.thermalPreset)
            put("refreshRate", profile.refreshRate)
            put("enabled", profile.enabled)
        }
        jsonArray.put(obj)
    }
    preferencesManager.saveAppProfiles(jsonArray.toString())
}
