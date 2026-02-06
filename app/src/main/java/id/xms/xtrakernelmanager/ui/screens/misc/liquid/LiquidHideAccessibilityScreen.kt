package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
    val bankingCount = apps.count { it.isBankingApp }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Header with back button and stats
            LiquidHideAccessibilityHeader(
                onNavigateBack = onNavigateBack,
                selectedCount = selectedCount,
                totalCount = apps.size
            )
            
            // Enable/Disable Card with better design
            LiquidToggleCard(
                enabled = isEnabled,
                onToggle = { enabled ->
                    isEnabled = enabled
                    scope.launch {
                        preferencesManager.setHideAccessibilityEnabled(enabled)
                        preferencesManager.setString("hide_accessibility_enabled", enabled.toString())
                    }
                }
            )
            
            // Instructions Card with better layout
            LiquidInstructionsCard()
            
            // Stats Card
            if (!isLoading) {
                LiquidStatsCard(
                    selectedCount = selectedCount,
                    bankingCount = bankingCount,
                    totalCount = apps.size
                )
            }
            
            // Search Card
            LiquidSearchCard(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it }
            )
            
            // Apps List with better loading state
            if (isLoading) {
                LiquidLoadingCard()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        LiquidAppSelectionItem(
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
                                    preferencesManager.setHideAccessibilityApps(jsonArray.toString())
                                    preferencesManager.setString("hide_accessibility_apps", jsonArray.toString())
                                }
                            }
                        )
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidHideAccessibilityHeader(
    onNavigateBack: () -> Unit,
    selectedCount: Int,
    totalCount: Int
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back button with better design
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onNavigateBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Hide Accessibility",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (totalCount > 0) {
                        Text(
                            text = "$selectedCount of $totalCount apps selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Status indicator
            Surface(
                color = if (selectedCount > 0) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                shape = CircleShape
            ) {
                Text(
                    text = if (selectedCount > 0) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (selectedCount > 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun LiquidToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide Accessibility Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (enabled) {
                            "XKM accessibility service will be hidden from selected apps"
                        } else {
                            "XKM accessibility service is visible to all apps"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun LiquidInstructionsCard() {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LSPosed Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "1. Install LSPosed Manager\n2. Enable XKM module for 'Android System' only\n3. Select apps below to hide accessibility from\n4. Reboot device to apply changes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                )
            }
        }
    }
}

@Composable
private fun LiquidStatsCard(
    selectedCount: Int,
    bankingCount: Int,
    totalCount: Int
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Selected apps stat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            
            // Banking apps stat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = bankingCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "Banking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            
            // Total apps stat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LiquidLoadingCard() {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Loading apps...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiquidSearchCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search apps") },
            placeholder = { Text("Type app name or package...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            Icons.Default.Clear, 
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun LiquidAppSelectionItem(
    app: LiquidAppItem,
    onToggle: (String, Boolean) -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(app.packageName, !app.isSelected) }
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder with banking indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (app.isBankingApp) {
                            Color(0xFF4CAF50).copy(alpha = if (isLightTheme) 0.15f else 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isBankingApp) Icons.Default.AccountBalance else Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (app.isBankingApp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
            
            // App info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (app.isBankingApp) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Banking",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Checkbox with better styling
            Checkbox(
                checked = app.isSelected,
                onCheckedChange = { onToggle(app.packageName, it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
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