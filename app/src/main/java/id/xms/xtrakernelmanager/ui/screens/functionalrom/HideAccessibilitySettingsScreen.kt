package id.xms.xtrakernelmanager.ui.screens.functionalrom

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import id.xms.xtrakernelmanager.data.model.HideAccessibilityTab
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSelected: Boolean,
    val isBankingApp: Boolean = false,
    val isAccessibilityApp: Boolean = false 
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HideAccessibilitySettingsScreen(
    config: HideAccessibilityConfig,
    onNavigateBack: () -> Unit,
    onConfigChange: (HideAccessibilityConfig) -> Unit,
    onRefreshLSPosedStatus: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Use key to remember apps list per tab to prevent flickering
    var apps by remember(config.currentTab) { mutableStateOf<List<AppItem>>(emptyList()) }
    var isLoading by remember(config.currentTab) { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val currentSelectedApps = when (config.currentTab) {
        HideAccessibilityTab.APPS_TO_HIDE -> config.appsToHide
        HideAccessibilityTab.DETECTOR_APPS -> config.detectorApps
    }
    
    LaunchedEffect(config.currentTab) {
        isLoading = true
        loadInstalledApps(context, currentSelectedApps, config.currentTab) { loadedApps ->
            apps = loadedApps
            isLoading = false
        }
    }
    
    LaunchedEffect(currentSelectedApps) {
        if (apps.isNotEmpty()) {
            apps = apps.map { app ->
                app.copy(isSelected = currentSelectedApps.contains(app.packageName))
            }
        }
    }
    
    // Filter apps based on search query
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isEmpty()) {
            apps.sortedWith(compareByDescending<AppItem> { it.isBankingApp }.thenBy { it.appName })
        } else {
            apps.filter { 
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }.sortedWith(compareByDescending<AppItem> { it.isBankingApp }.thenBy { it.appName })
        }
    }
    
    // Statistics
    val selectedCount = apps.count { it.isSelected }
    val relevantCount = when (config.currentTab) {
        HideAccessibilityTab.APPS_TO_HIDE -> apps.count { it.isAccessibilityApp && it.isSelected }
        HideAccessibilityTab.DETECTOR_APPS -> apps.count { it.isBankingApp && it.isSelected }
    }
    val relevantLabel = when (config.currentTab) {
        HideAccessibilityTab.APPS_TO_HIDE -> "Accessibility Apps"
        HideAccessibilityTab.DETECTOR_APPS -> "Banking Apps"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hide Accessibility Service") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshLSPosedStatus) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh LSPosed Status")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LSPosed Module Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (config.isLSPosedModuleActive) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (config.isLSPosedModuleActive) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (config.isLSPosedModuleActive) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "LSPosed Module Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (config.isLSPosedModuleActive) 
                                "Module is active and ready to hide accessibility services" 
                            else "Module is not active. Please enable the XKM module in LSPosed Manager",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Main Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hide Accessibility Service",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable hiding accessibility services from selected apps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.isEnabled,
                                onCheckedChange = { enabled ->
                                    onConfigChange(config.copy(isEnabled = enabled))
                                },
                                enabled = config.isLSPosedModuleActive
                            )
                        }
                    }
                }
            }
            
            // Tab Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Choose Apps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HideAccessibilityTab.values().forEach { tab ->
                                FilterChip(
                                    onClick = { 
                                        onConfigChange(config.copy(currentTab = tab))
                                    },
                                    label = { 
                                        Text(
                                            text = tab.displayName,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = config.currentTab == tab,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = config.currentTab.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Statistics Card
            if (config.isEnabled) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$selectedCount",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Selected Apps",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$relevantCount",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = relevantLabel,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search apps") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // Apps List
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(filteredApps) { app ->
                        AppItemCard(
                            app = app,
                            currentTab = config.currentTab,
                            onToggle = { packageName, isSelected ->
                                // Update the appropriate set based on current tab
                                val newConfig = when (config.currentTab) {
                                    HideAccessibilityTab.APPS_TO_HIDE -> {
                                        val updatedApps = if (isSelected) {
                                            config.appsToHide + packageName
                                        } else {
                                            config.appsToHide - packageName
                                        }
                                        config.copy(appsToHide = updatedApps)
                                    }
                                    HideAccessibilityTab.DETECTOR_APPS -> {
                                        val updatedApps = if (isSelected) {
                                            config.detectorApps + packageName
                                        } else {
                                            config.detectorApps - packageName
                                        }
                                        config.copy(detectorApps = updatedApps)
                                    }
                                }
                                onConfigChange(newConfig)
                                apps = apps.map { 
                                    if (it.packageName == packageName) it.copy(isSelected = isSelected) else it 
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItemCard(
    app: AppItem,
    currentTab: HideAccessibilityTab,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(app.packageName, !app.isSelected) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (app.isBankingApp && currentTab == HideAccessibilityTab.DETECTOR_APPS) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "BANK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (app.packageName.contains("xtrakernelmanager") && currentTab == HideAccessibilityTab.APPS_TO_HIDE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "XKM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (app.isAccessibilityApp && currentTab == HideAccessibilityTab.APPS_TO_HIDE && !app.packageName.contains("xtrakernelmanager")) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "ACCESSIBILITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Checkbox(
                checked = app.isSelected,
                onCheckedChange = { onToggle(app.packageName, it) }
            )
        }
    }
}

private suspend fun loadInstalledApps(
    context: Context,
    selectedApps: Set<String>,
    currentTab: HideAccessibilityTab,
    onResult: (List<AppItem>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val bankingKeywords = setOf(
                "bank", "banking", "finance", "financial", "payment", "wallet", "pay", "money",
                "credit", "debit", "card", "loan", "insurance", "investment", "trading", "crypto",
                "seabank", "bca", "mandiri", "bni", "bri", "cimb", "danamon", "permata", "ocbc",
                "gopay", "ovo", "dana", "linkaja", "shopeepay", "jenius", "blu", "tmrw", "digibank"
            )
            
            val accessibilityKeywords = setOf(
                "accessibility", "xposed", "magisk", "root", "kernel", "manager", "tweak", "mod",
                "xtrakernelmanager", "dynamicspotspoof", "lsposed", "edxposed", "xprivacylua"
            )
            
            val apps = installedApps
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } 
                .mapNotNull { appInfo ->
                    try {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = appInfo.packageName
                        if (packageName.contains("xtrakernelmanager") && currentTab == HideAccessibilityTab.DETECTOR_APPS) {
                            return@mapNotNull null
                        }
                        
                        val isBankingApp = bankingKeywords.any { keyword ->
                            appName.contains(keyword, ignoreCase = true) ||
                            packageName.contains(keyword, ignoreCase = true)
                        }
                        
                        val isAccessibilityApp = accessibilityKeywords.any { keyword ->
                            appName.contains(keyword, ignoreCase = true) ||
                            packageName.contains(keyword, ignoreCase = true)
                        } || packageName.contains("xtrakernelmanager") 
                        
                        // Filter apps based on current tab
                        val shouldInclude = when (currentTab) {
                            HideAccessibilityTab.APPS_TO_HIDE -> {
                                if (packageName.contains("xtrakernelmanager")) {
                                    true
                                } else {
                                    isAccessibilityApp || !isBankingApp
                                }
                            }
                            HideAccessibilityTab.DETECTOR_APPS -> {
                                // Show banking apps and security apps
                                isBankingApp || appName.contains("security", ignoreCase = true) ||
                                packageName.contains("security", ignoreCase = true)
                            }
                        }
                        
                        if (!shouldInclude) return@mapNotNull null
                        
                        AppItem(
                            packageName = packageName,
                            appName = appName,
                            isSelected = selectedApps.contains(packageName),
                            isBankingApp = isBankingApp,
                            isAccessibilityApp = isAccessibilityApp
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedWith(
                    when (currentTab) {
                        HideAccessibilityTab.APPS_TO_HIDE -> {
                            compareBy<AppItem> { !it.packageName.contains("xtrakernelmanager") }
                                .thenByDescending { it.isAccessibilityApp }
                                .thenBy { it.appName }
                        }
                        HideAccessibilityTab.DETECTOR_APPS -> {
                            compareByDescending<AppItem> { it.isBankingApp }.thenBy { it.appName }
                        }
                    }
                )
            
            withContext(Dispatchers.Main) {
                onResult(apps)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}