package id.xms.xtrakernelmanager.ui.screens.misc.liquid

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
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class LiquidAppItem(
    val packageName: String,
    val appName: String,
    val isSelected: Boolean,
    val isBankingApp: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidHideAccessibilityScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    
    var isEnabled by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<LiquidAppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Load initial data
    LaunchedEffect(Unit) {
        scope.launch {
            // Load enabled state
            isEnabled = preferencesManager.getString("hide_accessibility_enabled", "false").toBoolean()
            
            // Load apps list
            loadLiquidInstalledApps(context, preferencesManager) { loadedApps ->
                apps = loadedApps
                isLoading = false
            }
        }
    }
    
    // Filter apps based on search query
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isEmpty()) {
            // Show banking apps first, then others
            apps.sortedWith(compareByDescending<LiquidAppItem> { it.isBankingApp }.thenBy { it.appName })
        } else {
            apps.filter { 
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }.sortedWith(compareByDescending<LiquidAppItem> { it.isBankingApp }.thenBy { it.appName })
        }
    }
    
    // Statistics
    val selectedCount = apps.count { it.isSelected }
    
    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Compact Header
            LiquidCompactHeader(
                onNavigateBack = onNavigateBack,
                selectedCount = selectedCount,
                totalCount = apps.size
            )
            
            // Toggle with LiquidToggle
            LiquidToggleSection(
                enabled = isEnabled,
                onToggle = { enabled ->
                    isEnabled = enabled
                    scope.launch {
                        // Save to both DataStore and sync preferences
                        preferencesManager.setHideAccessibilityEnabled(enabled)
                        preferencesManager.setString("hide_accessibility_enabled", enabled.toString())
                        
                        // Also try to save to default shared preferences for better compatibility
                        try {
                            val defaultPrefs = context.getSharedPreferences("id.xms.xtrakernelmanager_preferences", Context.MODE_PRIVATE)
                            defaultPrefs.edit().putString("hide_accessibility_enabled", enabled.toString()).apply()
                        } catch (e: Exception) {
                            // Ignore if fails
                        }
                        
                        android.util.Log.d("XKM-HideAccessibility", "Hide accessibility enabled: $enabled")
                    }
                }
            )
            
            // Compact Instructions
            LiquidCompactInstructions()
            
            // Search
            LiquidCompactSearch(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it }
            )
            
            // Apps List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White.copy(alpha = 0.8f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredApps) { app ->
                        LiquidCompactAppItem(
                            app = app,
                            onToggle = { packageName, selected ->
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
                                    val jsonString = jsonArray.toString()
                                    
                                    // Save to both DataStore and sync preferences
                                    preferencesManager.setHideAccessibilityAppsToHide(jsonString)
                                    preferencesManager.setString("hide_accessibility_apps", jsonString)
                                    
                                    // Also try to save to default shared preferences for better compatibility
                                    try {
                                        val defaultPrefs = context.getSharedPreferences("id.xms.xtrakernelmanager_preferences", Context.MODE_PRIVATE)
                                        defaultPrefs.edit().putString("hide_accessibility_apps", jsonString).apply()
                                    } catch (e: Exception) {
                                        // Ignore if fails
                                    }
                                    
                                    android.util.Log.d("XKM-HideAccessibility", "Saved apps: $jsonString")
                                }
                            }
                        )
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidCompactHeader(
    onNavigateBack: () -> Unit,
    selectedCount: Int,
    totalCount: Int
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onNavigateBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Hide Accessibility",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    
                    if (totalCount > 0) {
                        Text(
                            text = "$selectedCount of $totalCount apps selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Status indicator
            Surface(
                color = if (selectedCount > 0) {
                    Color(0xFF4CAF50).copy(alpha = 0.3f)
                } else {
                    Color.White.copy(alpha = 0.2f)
                },
                shape = CircleShape
            ) {
                Text(
                    text = if (selectedCount > 0) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LiquidToggleSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = "Hide Accessibility Service",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            // Use LiquidToggle instead of Switch
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.size(width = 44.dp, height = 24.dp)
            )
        }
    }
}

@Composable
private fun LiquidCompactInstructions() {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            
            Column {
                Text(
                    text = "LSPosed Configuration",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable XKM module for selected apps below in LSPosed Manager. The module will automatically hide XKM's accessibility service from these apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LiquidCompactSearch(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search apps...", color = Color.White.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            Icons.Default.Clear, 
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(alpha = 0.4f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            )
        )
    }
}

@Composable
private fun LiquidCompactAppItem(
    app: LiquidAppItem,
    onToggle: (String, Boolean) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(app.packageName, !app.isSelected) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (app.isBankingApp) {
                            Color(0xFF4CAF50).copy(alpha = 0.3f)
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isBankingApp) Icons.Default.AccountBalance else Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (app.isBankingApp) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f)
                )
            }
            
            // App info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    if (app.isBankingApp) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Banking",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            // Checkbox
            Checkbox(
                checked = app.isSelected,
                onCheckedChange = { onToggle(app.packageName, it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White.copy(alpha = 0.4f),
                    checkmarkColor = Color.Black
                )
            )
        }
    }
}

private suspend fun loadLiquidInstalledApps(
    context: android.content.Context,
    preferencesManager: PreferencesManager,
    onResult: (List<LiquidAppItem>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            // Banking apps list for identification
            val bankingPackages = setOf(
                "id.co.bankbkemobile.digitalbank", "id.co.bri.brimo", "com.bca",
                "id.co.bankmandiri.livin", "id.co.bni.mobilebni", "id.co.bankjago.app",
                "id.dana", "ovo.id", "com.gojek.app", "com.shopee.id",
                "com.telkom.mwallet", "id.co.bca.blu", "com.ocbc.mobile",
                "id.neobank", "com.btpn.dc", "net.npointl.permatanet",
                "id.co.cimbniaga.mobile.android", "com.maybank2u.life",
                "id.co.bankmega.meganet", "com.panin.mpin"
            )
            
            // Get currently selected apps from sync preferences (for immediate access)
            val selectedAppsJson = preferencesManager.getString("hide_accessibility_apps", "[]")
            val selectedApps = try {
                val jsonArray = JSONArray(selectedAppsJson)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
            } catch (e: Exception) {
                emptySet<String>()
            }
            
            // Filter and map to LiquidAppItem
            val appItems = installedApps
                .filter { appInfo ->
                    // Show user apps and banking system apps
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    bankingPackages.contains(appInfo.packageName) ||
                    appInfo.packageName.contains("bank", ignoreCase = true) ||
                    appInfo.packageName.contains("payment", ignoreCase = true) ||
                    appInfo.packageName.contains("wallet", ignoreCase = true)
                }
                .map { appInfo ->
                    LiquidAppItem(
                        packageName = appInfo.packageName,
                        appName = try {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            appInfo.packageName
                        },
                        isSelected = selectedApps.contains(appInfo.packageName),
                        isBankingApp = bankingPackages.contains(appInfo.packageName) ||
                                appInfo.packageName.contains("bank", ignoreCase = true) ||
                                appInfo.packageName.contains("payment", ignoreCase = true) ||
                                appInfo.packageName.contains("dana") ||
                                appInfo.packageName.contains("ovo")
                    )
                }
                .sortedWith(compareByDescending<LiquidAppItem> { it.isBankingApp }.thenBy { it.appName })
            
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