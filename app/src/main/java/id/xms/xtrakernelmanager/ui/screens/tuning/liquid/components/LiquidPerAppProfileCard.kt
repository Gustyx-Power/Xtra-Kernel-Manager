package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.service.AppProfileService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidPerAppProfileCard(
    preferencesManager: PreferencesManager,
    availableGovernors: List<String>,
    onNavigateToFullScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val isEnabled by preferencesManager.isPerAppProfileEnabled().collectAsState(initial = false)
    val profilesJson by preferencesManager.getAppProfiles().collectAsState(initial = "[]")
    
    val profiles = remember(profilesJson) { parseProfiles(profilesJson) }
    
    // Check usage stats permission
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsagePermission = hasUsageStatsPermission(context)
    }
    
    // Count profiles
    val profileCount = profiles.size

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToFullScreen() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
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
                    // Icon with gradient background
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
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.liquid_per_app_profiles),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.liquid_per_app_profiles_configured, profileCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Arrow icon
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Open",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Permission warning
            if (!hasUsagePermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.per_app_profile_usage_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            
            // Enable/Disable Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnabled) "Profiles Active" else "Profiles Inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            preferencesManager.setPerAppProfileEnabled(enabled)
                            
                            if (hasUsagePermission && profiles.isNotEmpty()) {
                                val serviceIntent = Intent(context, id.xms.xtrakernelmanager.service.AppProfileService::class.java)
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("PerAppProfile", "Failed to start service: ${e.message}")
                                }
                            }
                        }
                    },
                    enabled = hasUsagePermission
                )
            }
        }
    }
}


@Composable
private fun ProfileItem(
    profile: AppProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.enabled)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                val refreshRateText = if (profile.refreshRate > 0) " • ${profile.refreshRate}Hz" else ""
                Text(
                    "${profile.governor} • ${profile.thermalPreset}$refreshRateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                LiquidToggle(
                    checked = profile.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

// Helper functions
private fun getDeviceMaxRefreshRate(context: Context): Int {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
        else -> emptyList() // 60Hz or less = no refresh rate options
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
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

private suspend fun saveProfiles(
    preferencesManager: PreferencesManager,
    profiles: List<AppProfile>,
) {
    val jsonArray = JSONArray()
    profiles.forEach { profile ->
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
