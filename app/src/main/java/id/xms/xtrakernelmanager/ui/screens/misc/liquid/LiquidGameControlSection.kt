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

@Composable
fun LiquidGameControlSection(
    viewModel: MiscViewModel,
    onNavigateToDetail: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLightTheme = !isSystemInDarkTheme()

    val gameAppsJson by viewModel.gameApps.collectAsState()

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
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail() },
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
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
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
