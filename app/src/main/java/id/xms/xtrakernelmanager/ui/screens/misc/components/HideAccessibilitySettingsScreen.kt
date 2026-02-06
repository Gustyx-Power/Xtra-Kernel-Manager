package id.xms.xtrakernelmanager.ui.screens.misc.components

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HideAccessibilitySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    
    var isEnabled by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Load initial data
    LaunchedEffect(Unit) {
        // Load enabled state
        isEnabled = preferencesManager.getString("hide_accessibility_enabled", "false").toBoolean()
        
        // Load apps list
        loadInstalledApps(context, preferencesManager) { loadedApps ->
            apps = loadedApps
            isLoading = false
        }
    }
    
    // Filter apps based on search query
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isEmpty()) {
            apps
        } else {
            apps.filter { 
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hide Accessibility") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Enable/Disable Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            text = "Hide XKM accessibility service from selected apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { enabled ->
                            isEnabled = enabled
                            scope.launch {
                                preferencesManager.setHideAccessibilityEnabled(enabled)
                                // Also update sync preferences for Xposed module
                                preferencesManager.setString("hide_accessibility_enabled", enabled.toString())
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "LSPosed Configuration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "In LSPosed Manager, only enable this module for 'Android System' (com.android.system). Do not select individual apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search apps") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Apps list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        AppSelectionItem(
                            app = app,
                            onToggle = { packageName, selected ->
                                // Update local state immediately
                                apps = apps.map { 
                                    if (it.packageName == packageName) {
                                        it.copy(isSelected = selected)
                                    } else {
                                        it
                                    }
                                }
                                
                                // Save to preferences immediately
                                scope.launch {
                                    val selectedApps = apps.filter { it.isSelected }.map { it.packageName }
                                    val jsonArray = JSONArray(selectedApps)
                                    preferencesManager.setHideAccessibilityApps(jsonArray.toString())
                                    
                                    // Also update sync preferences for Xposed module
                                    preferencesManager.setString("hide_accessibility_apps", jsonArray.toString())
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
private fun AppSelectionItem(
    app: AppItem,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Checkbox(
                checked = app.isSelected,
                onCheckedChange = { selected ->
                    onToggle(app.packageName, selected)
                }
            )
        }
    }
}

private suspend fun loadInstalledApps(
    context: android.content.Context,
    preferencesManager: PreferencesManager,
    onResult: (List<AppItem>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            // Get currently selected apps from sync preferences (for immediate access)
            val selectedAppsJson = preferencesManager.getString("hide_accessibility_apps", "[]")
            
            val selectedApps = try {
                val jsonArray = JSONArray(selectedAppsJson)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
            } catch (e: Exception) {
                emptySet<String>()
            }
            
            // Filter and map to AppItem
            val appItems = installedApps
                .filter { appInfo ->
                    // Only show user apps and some system apps that might need hiding
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    appInfo.packageName.contains("bank", ignoreCase = true) ||
                    appInfo.packageName.contains("payment", ignoreCase = true) ||
                    appInfo.packageName.contains("wallet", ignoreCase = true) ||
                    appInfo.packageName.contains("dana", ignoreCase = true) ||
                    appInfo.packageName.contains("ovo", ignoreCase = true) ||
                    appInfo.packageName.contains("gojek", ignoreCase = true) ||
                    appInfo.packageName.contains("shopee", ignoreCase = true) ||
                    appInfo.packageName.contains("seabank", ignoreCase = true) ||
                    appInfo.packageName.contains("bca", ignoreCase = true) ||
                    appInfo.packageName.contains("bri", ignoreCase = true) ||
                    appInfo.packageName.contains("bni", ignoreCase = true) ||
                    appInfo.packageName.contains("mandiri", ignoreCase = true)
                }
                .map { appInfo ->
                    AppItem(
                        packageName = appInfo.packageName,
                        appName = try {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            appInfo.packageName
                        },
                        isSelected = selectedApps.contains(appInfo.packageName)
                    )
                }
                .sortedBy { it.appName }
            
            withContext(Dispatchers.Main) {
                onResult(appItems)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}