package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.service.GameMonitorService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class GameApp(val packageName: String, val appName: String, val enabled: Boolean = true)

@Composable
fun LiquidGameControlSection(viewModel: MiscViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLightTheme = !isSystemInDarkTheme()

    val gameAppsJson by viewModel.gameApps.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var showAddGameDialog by remember { mutableStateOf(false) }

    val gameApps = remember(gameAppsJson) { parseGameApps(gameAppsJson) }
    val enabledCount = gameApps.count { it.enabled }

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasUsageAccessPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasUsageAccessPermission = hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
    }

    val usageAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsageAccessPermission = hasUsageStatsPermission(context)
    }

    LaunchedEffect(enabledCount, hasOverlayPermission, hasUsageAccessPermission) {
        if (enabledCount > 0 && hasOverlayPermission && hasUsageAccessPermission) {
            startGameMonitorService(context)
        } else {
            stopGameMonitorService(context)
        }
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isLightTheme) Color(0xFFAF52DE).copy(0.15f)
                                else Color(0xFFBF5AF2).copy(0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.game_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when {
                                gameApps.isEmpty() -> stringResource(R.string.game_control_no_games)
                                enabledCount == 0 -> "${gameApps.size} games (all disabled)"
                                else -> "$enabledCount active ${if (enabledCount == 1) "game" else "games"}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enabledCount > 0) {
                                if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            },
                            fontWeight = if (enabledCount > 0) FontWeight.Medium else FontWeight.Normal,
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            // Permission warnings
            if (!hasOverlayPermission) {
                PermissionCard(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    text = stringResource(R.string.game_control_grant_overlay),
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                )
            }

            if (hasOverlayPermission && !hasUsageAccessPermission) {
                PermissionCard(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    text = stringResource(R.string.game_control_grant_usage_access),
                    color = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A),
                    onClick = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        usageAccessLauncher.launch(intent)
                    }
                )
            }

            // Expandable game list
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

                    Text(
                        text = "Game Apps",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )

                    if (gameApps.isEmpty()) {
                        EmptyGameState()
                    } else {
                        gameApps.forEach { game ->
                            GameAppCard(
                                game = game,
                                context = context,
                                isLightTheme = isLightTheme,
                                onToggle = { newEnabled ->
                                    val updatedList = gameApps.map {
                                        if (it.packageName == game.packageName) it.copy(enabled = newEnabled)
                                        else it
                                    }
                                    scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                                },
                                onDelete = {
                                    val updatedList = gameApps.filter { it.packageName != game.packageName }
                                    scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                                }
                            )
                        }
                    }

                    // Add game button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isLightTheme) Color(0xFFAF52DE).copy(0.15f)
                                else Color(0xFFBF5AF2).copy(0.2f)
                            )
                            .clickable { showAddGameDialog = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.game_control_add_game),
                                fontWeight = FontWeight.Medium,
                                color = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddGameDialog) {
        AddGameDialog(
            context = context,
            existingPackages = gameApps.map { it.packageName },
            onDismiss = { showAddGameDialog = false },
            onConfirm = { newGame ->
                val updatedList = gameApps + newGame
                scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                showAddGameDialog = false
            }
        )
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.15f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyGameState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            )
            Text(
                text = "No games added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }
    }
}

@Composable
private fun GameAppCard(
    game: GameApp,
    context: Context,
    isLightTheme: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (game.enabled) {
                    if (isLightTheme) Color(0xFFAF52DE).copy(0.1f)
                    else Color(0xFFBF5AF2).copy(0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f)
                }
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val appIcon = remember(game.packageName) {
                    try {
                        context.packageManager.getApplicationIcon(game.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }

                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        AsyncImage(
                            model = appIcon,
                            contentDescription = game.appName,
                            modifier = Modifier.size(44.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (game.enabled) {
                                        if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = game.appName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (game.enabled) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.appName,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = game.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LiquidToggle(
                    checked = game.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun AddGameDialog(
    context: Context,
    existingPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (GameApp) -> Unit
) {
    var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            installedApps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
                    .filter { appInfo ->
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 &&
                            !existingPackages.contains(appInfo.packageName) &&
                            pm.getLaunchIntentForPackage(appInfo.packageName) != null
                    }
                    .map { appInfo ->
                        appInfo.packageName to pm.getApplicationLabel(appInfo).toString()
                    }
                    .sortedBy { it.second.lowercase() }
            }
            isLoading = false
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.second.contains(searchQuery, ignoreCase = true) ||
                it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredApps) { app ->
                                AppListItem(
                                    app = app,
                                    context = context,
                                    onClick = { onConfirm(GameApp(app.first, app.second, enabled = true)) }
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: Pair<String, String>,
    context: Context,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val appIcon = remember(app.first) {
                try {
                    context.packageManager.getApplicationIcon(app.first)
                } catch (e: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    AsyncImage(
                        model = appIcon,
                        contentDescription = app.second,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.second.take(1).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.second,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = app.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Helper functions
private fun startGameMonitorService(context: Context) {
    try {
        val intent = Intent(context, GameMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    } catch (e: Exception) {
        // Service might already be running
    }
}

private fun stopGameMonitorService(context: Context) {
    try {
        val intent = Intent(context, GameMonitorService::class.java)
        context.stopService(intent)
    } catch (e: Exception) {
        // Ignore
    }
}

private fun parseGameApps(json: String): List<GameApp> {
    return try {
        val jsonArray = JSONArray(json)
        val apps = mutableListOf<GameApp>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            apps.add(
                GameApp(
                    packageName = obj.getString("packageName"),
                    appName = obj.getString("appName"),
                    enabled = obj.optBoolean("enabled", true)
                )
            )
        }
        apps
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeGameApps(apps: List<GameApp>): String {
    val jsonArray = JSONArray()
    apps.forEach { app ->
        val obj = JSONObject().apply {
            put("packageName", app.packageName)
            put("appName", app.appName)
            put("enabled", app.enabled)
        }
        jsonArray.put(obj)
    }
    return jsonArray.toString()
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == android.app.AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}
