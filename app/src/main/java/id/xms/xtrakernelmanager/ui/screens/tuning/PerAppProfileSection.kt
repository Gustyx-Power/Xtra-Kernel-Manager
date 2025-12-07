package id.xms.xtrakernelmanager.ui.screens.tuning

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.service.AppProfileService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppProfileSection(
    preferencesManager: PreferencesManager,
    availableGovernors: List<String>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<AppProfile?>(null) }
    
    val isEnabled by preferencesManager.isPerAppProfileEnabled().collectAsState(initial = false)
    val profilesJson by preferencesManager.getAppProfiles().collectAsState(initial = "[]")
    
    val profiles = remember(profilesJson) {
        parseProfiles(profilesJson)
    }
    
    // Check usage stats permission
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsagePermission = hasUsageStatsPermission(context)
    }

    // Thermal presets
    val thermalPresets = listOf("Not Set", "Class 0", "Extreme", "Dynamic", "Incalls", "Thermal 20")
    
    // Default governors if none provided
    val governors = if (availableGovernors.isEmpty()) {
        listOf("schedutil", "performance", "powersave", "ondemand", "conservative", "interactive")
    } else {
        availableGovernors
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Per-App Profiles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Custom settings per application",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Permission warning
                    if (!hasUsagePermission) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        "Usage Access Required",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    "Grant usage access permission to detect foreground apps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                        permissionLauncher.launch(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    }
                    
                    // Enable/Disable toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = if (isEnabled) MaterialTheme.colorScheme.tertiary 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        "Enable Per-App Profiles",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        if (isEnabled) "Monitoring active" else "Monitoring disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        preferencesManager.setPerAppProfileEnabled(enabled)
                                        if (enabled && hasUsagePermission) {
                                            context.startService(Intent(context, AppProfileService::class.java))
                                        } else {
                                            context.stopService(Intent(context, AppProfileService::class.java))
                                        }
                                    }
                                },
                                enabled = hasUsagePermission
                            )
                        }
                    }
                    
                    // Profiles list
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "App Profiles (${profiles.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { showAddDialog = true }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Profile",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            
                            if (profiles.isEmpty()) {
                                Text(
                                    "No profiles configured. Tap + to add one.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                profiles.forEach { profile ->
                                    ProfileItem(
                                        profile = profile,
                                        onEdit = {
                                            editingProfile = profile
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            scope.launch {
                                                val newProfiles = profiles.filter { 
                                                    it.packageName != profile.packageName 
                                                }
                                                saveProfiles(preferencesManager, newProfiles)
                                            }
                                        },
                                        onToggle = { enabled ->
                                            scope.launch {
                                                val newProfiles = profiles.map {
                                                    if (it.packageName == profile.packageName) {
                                                        it.copy(enabled = enabled)
                                                    } else it
                                                }
                                                saveProfiles(preferencesManager, newProfiles)
                                            }
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
    
    // Add Profile Dialog
    if (showAddDialog) {
        AddProfileDialog(
            context = context,
            governors = governors,
            thermalPresets = thermalPresets,
            existingPackages = profiles.map { it.packageName },
            onDismiss = { showAddDialog = false },
            onConfirm = { profile ->
                scope.launch {
                    val newProfiles = profiles + profile
                    saveProfiles(preferencesManager, newProfiles)
                    showAddDialog = false
                }
            }
        )
    }
    
    // Edit Profile Dialog
    if (showEditDialog && editingProfile != null) {
        EditProfileDialog(
            profile = editingProfile!!,
            governors = governors,
            thermalPresets = thermalPresets,
            onDismiss = { 
                showEditDialog = false
                editingProfile = null
            },
            onConfirm = { updatedProfile ->
                scope.launch {
                    val newProfiles = profiles.map {
                        if (it.packageName == updatedProfile.packageName) updatedProfile else it
                    }
                    saveProfiles(preferencesManager, newProfiles)
                    showEditDialog = false
                    editingProfile = null
                }
            }
        )
    }
}

@Composable
private fun ProfileItem(
    profile: AppProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.enabled) 
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${profile.governor} • ${profile.thermalPreset}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Switch(
                    checked = profile.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProfileDialog(
    context: Context,
    governors: List<String>,
    thermalPresets: List<String>,
    existingPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (AppProfile) -> Unit
) {
    var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedApp by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedGovernor by remember { mutableStateOf(governors.firstOrNull() ?: "schedutil") }
    var selectedThermal by remember { mutableStateOf(thermalPresets.first()) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            // Get all apps with launcher intent (apps that can be launched from app drawer)
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val apps = resolveInfos
                .map { it.activityInfo.packageName }
                .distinct()
                .filter { it !in existingPackages }
                .mapNotNull { packageName ->
                    try {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        Pair(packageName, appName)
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.second.lowercase() }
            installedApps = apps
            isLoading = false
        }
    }
    
    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter { 
            it.second.contains(searchQuery, ignoreCase = true) ||
            it.first.contains(searchQuery, ignoreCase = true)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add App Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                if (selectedApp == null) {
                    // App selection
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search apps") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(filteredApps) { app ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { selectedApp = app },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(app.second, fontWeight = FontWeight.Medium)
                                        Text(
                                            app.first,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Profile configuration
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedApp!!.second, fontWeight = FontWeight.Bold)
                                Text(
                                    selectedApp!!.first,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { selectedApp = null }) {
                                Icon(Icons.Default.Close, "Change app")
                            }
                        }
                    }
                    
                    // Governor dropdown
                    var governorExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = governorExpanded,
                        onExpandedChange = { governorExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGovernor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Governor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = governorExpanded,
                            onDismissRequest = { governorExpanded = false }
                        ) {
                            governors.forEach { gov ->
                                DropdownMenuItem(
                                    text = { Text(gov) },
                                    onClick = {
                                        selectedGovernor = gov
                                        governorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Thermal dropdown
                    var thermalExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = thermalExpanded,
                        onExpandedChange = { thermalExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedThermal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Thermal Preset") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = thermalExpanded,
                            onDismissRequest = { thermalExpanded = false }
                        ) {
                            thermalPresets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset) },
                                    onClick = {
                                        selectedThermal = preset
                                        thermalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedApp?.let { app ->
                        onConfirm(
                            AppProfile(
                                packageName = app.first,
                                appName = app.second,
                                governor = selectedGovernor,
                                thermalPreset = selectedThermal,
                                enabled = true
                            )
                        )
                    }
                },
                enabled = selectedApp != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: AppProfile,
    governors: List<String>,
    thermalPresets: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (AppProfile) -> Unit
) {
    var selectedGovernor by remember { mutableStateOf(profile.governor) }
    var selectedThermal by remember { mutableStateOf(profile.thermalPreset) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(profile.appName, fontWeight = FontWeight.Bold)
                        Text(
                            profile.packageName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Governor dropdown
                var governorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = governorExpanded,
                    onExpandedChange = { governorExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGovernor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Governor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = governorExpanded,
                        onDismissRequest = { governorExpanded = false }
                    ) {
                        governors.forEach { gov ->
                            DropdownMenuItem(
                                text = { Text(gov) },
                                onClick = {
                                    selectedGovernor = gov
                                    governorExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Thermal dropdown
                var thermalExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = thermalExpanded,
                    onExpandedChange = { thermalExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedThermal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Thermal Preset") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = thermalExpanded,
                        onDismissRequest = { thermalExpanded = false }
                    ) {
                        thermalPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset) },
                                onClick = {
                                    selectedThermal = preset
                                    thermalExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        profile.copy(
                            governor = selectedGovernor,
                            thermalPreset = selectedThermal
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
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
                    enabled = obj.optBoolean("enabled", true)
                )
            )
        }
        profiles
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun saveProfiles(preferencesManager: PreferencesManager, profiles: List<AppProfile>) {
    val jsonArray = JSONArray()
    profiles.forEach { profile ->
        val obj = JSONObject().apply {
            put("packageName", profile.packageName)
            put("appName", profile.appName)
            put("governor", profile.governor)
            put("thermalPreset", profile.thermalPreset)
            put("enabled", profile.enabled)
        }
        jsonArray.put(obj)
    }
    preferencesManager.saveAppProfiles(jsonArray.toString())
}
