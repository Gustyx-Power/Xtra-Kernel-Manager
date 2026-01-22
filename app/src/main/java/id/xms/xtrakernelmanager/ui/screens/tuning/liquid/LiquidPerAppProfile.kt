package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.service.AppProfileService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidPerAppProfile(preferencesManager: PreferencesManager, availableGovernors: List<String>) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var expanded by remember { mutableStateOf(false) }
  var showAddDialog by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }
  var editingProfile by remember { mutableStateOf<AppProfile?>(null) }

  val isEnabled by preferencesManager.isPerAppProfileEnabled().collectAsState(initial = false)
  val profilesJson by preferencesManager.getAppProfiles().collectAsState(initial = "[]")

  val profiles = remember(profilesJson) { parseProfiles(profilesJson) }

  // Check usage stats permission
  var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()
      ) {
        hasUsagePermission = hasUsageStatsPermission(context)
      }

  // Thermal presets
  val thermalPresets = listOf("Not Set", "Class 0", "Extreme", "Dynamic", "Incalls", "Thermal 20")

  // Default governors if none provided
  val governors =
      if (availableGovernors.isEmpty()) {
        listOf("schedutil", "performance", "powersave", "ondemand", "conservative", "interactive")
      } else {
        availableGovernors
      }

  // Auto-start service if enabled and has permission
  LaunchedEffect(isEnabled, hasUsagePermission) {
    android.util.Log.d(
        "PerAppProfile",
        "LaunchedEffect: isEnabled=$isEnabled, hasPermission=$hasUsagePermission",
    )
    if (isEnabled && hasUsagePermission) {
      val serviceIntent = Intent(context, AppProfileService::class.java)
      try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          context.startForegroundService(serviceIntent)
        } else {
          context.startService(serviceIntent)
        }
        android.util.Log.d("PerAppProfile", "Service start requested")
      } catch (e: Exception) {
        android.util.Log.e("PerAppProfile", "Failed to start service: ${e.message}")
      }
    }
  }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                          Brush.linearGradient(
                              colors =
                                  listOf(
                                      MaterialTheme.colorScheme.tertiary,
                                      MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(28.dp),
            )
          }

          Column {
            Text(
                text = stringResource(R.string.per_app_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.per_app_profile_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                    .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription =
                  if (expanded) stringResource(R.string.collapse)
                  else stringResource(R.string.expand),
              modifier = Modifier.size(32.dp),
              tint = MaterialTheme.colorScheme.tertiary,
          )
        }
      }

      // Expanded content
      AnimatedVisibility(
          visible = expanded,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
      ) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Permission warning
          if (!hasUsagePermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
            ) {
              Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      Icons.Default.Warning,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onErrorContainer,
                  )
                  Text(
                      stringResource(R.string.per_app_profile_usage_required),
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onErrorContainer,
                  )
                }
                Text(
                    stringResource(R.string.per_app_profile_usage_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Button(
                    onClick = {
                      val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                      permissionLauncher.launch(intent)
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                ) {
                  Text(stringResource(R.string.per_app_profile_grant_permission))
                }
              }
            }
          }

          // Enable/Disable toggle
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f),
              ) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint =
                        if (isEnabled) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column {
                  Text(
                      stringResource(R.string.per_app_profile_enable),
                      fontWeight = FontWeight.Medium,
                  )
                  Text(
                      if (isEnabled) stringResource(R.string.per_app_profile_monitoring_active)
                      else stringResource(R.string.per_app_profile_monitoring_disabled),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))
              LottieSwitchControlled(
                  checked = isEnabled,
                  onCheckedChange = { enabled ->
                    scope.launch {
                      preferencesManager.setPerAppProfileEnabled(enabled)
                      val serviceIntent = Intent(context, AppProfileService::class.java)
                      if (enabled && hasUsagePermission) {
                        // Use startForegroundService for Android O+
                        try {
                          if (
                              android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                          ) {
                            context.startForegroundService(serviceIntent)
                          } else {
                            context.startService(serviceIntent)
                          }
                          android.widget.Toast.makeText(
                                  context,
                                  context.getString(R.string.per_app_profile_service_starting),
                                  android.widget.Toast.LENGTH_SHORT,
                              )
                              .show()
                        } catch (e: Exception) {
                          android.widget.Toast.makeText(
                                  context,
                                  context.getString(
                                      R.string.per_app_profile_service_failed,
                                      e.message ?: "",
                                  ),
                                  android.widget.Toast.LENGTH_LONG,
                              )
                              .show()
                        }
                      } else {
                        context.stopService(serviceIntent)
                        android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.per_app_profile_service_stopped),
                                android.widget.Toast.LENGTH_SHORT,
                            )
                            .show()
                      }
                    }
                  },
                  enabled = hasUsagePermission,
                  width = 80.dp,
                  height = 40.dp,
                  scale = 2.2f,
              )
            }
          }

          // Profiles list
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                    stringResource(R.string.per_app_profile_count, profiles.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { showAddDialog = true }) {
                  Icon(
                      Icons.Default.Add,
                      contentDescription = stringResource(R.string.per_app_profile_add),
                      tint = MaterialTheme.colorScheme.tertiary,
                  )
                }
              }

              if (profiles.isEmpty()) {
                Text(
                    stringResource(R.string.per_app_profile_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                          // Remove profile from list
                          val newProfiles =
                              profiles.filter { it.packageName != profile.packageName }
                          saveProfiles(preferencesManager, newProfiles)

                          // Show toast
                          android.widget.Toast.makeText(
                                  context,
                                  context.getString(R.string.per_app_profile_deleted),
                                  android.widget.Toast.LENGTH_SHORT,
                              )
                              .show()
                        }
                      },
                      onToggle = { enabled ->
                        scope.launch {
                          val newProfiles =
                              profiles.map {
                                if (it.packageName == profile.packageName) {
                                  it.copy(enabled = enabled)
                                } else it
                              }
                          saveProfiles(preferencesManager, newProfiles)
                        }
                      },
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
        },
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
            val newProfiles =
                profiles.map {
                  if (it.packageName == updatedProfile.packageName) updatedProfile else it
                }
            saveProfiles(preferencesManager, newProfiles)
            showEditDialog = false
            editingProfile = null
          }
        },
    )
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
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (profile.enabled)
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
        LottieSwitchControlled(
            checked = profile.enabled,
            onCheckedChange = onToggle,
            width = 60.dp,
            height = 30.dp,
            scale = 2.0f,
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
    onConfirm: (AppProfile) -> Unit,
) {
  var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var selectedApp by remember { mutableStateOf<Pair<String, String>?>(null) }
  var selectedGovernor by remember { mutableStateOf(governors.firstOrNull() ?: "schedutil") }
  var selectedThermal by remember { mutableStateOf(thermalPresets.first()) }
  var selectedRefreshRate by remember { mutableStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }

  // Get device max refresh rate
  val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
  val availableRefreshRates = remember(maxRefreshRate) { getAvailableRefreshRates(maxRefreshRate) }

  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      val pm = context.packageManager
      val mainIntent =
          Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
      val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
      val apps =
          resolveInfos
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

  val filteredApps =
      remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else
            installedApps.filter {
              it.second.contains(searchQuery, ignoreCase = true) ||
                  it.first.contains(searchQuery, ignoreCase = true)
            }
      }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Header with gradient
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.primary,
                                )
                        )
                    )
                    .padding(16.dp)
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
                Icons.Rounded.AddCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.per_app_profile_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
          }
        }

        // Display max refresh rate info if > 60Hz
        if (maxRefreshRate > 60) {
          Card(
              colors =
                  CardDefaults.cardColors(
                      containerColor =
                          MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                  ),
              shape = RoundedCornerShape(12.dp),
          ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                  Icons.Rounded.Speed,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(20.dp),
              )
              Text(
                  text = stringResource(R.string.per_app_profile_display_info, maxRefreshRate),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }
        }

        Box(modifier = Modifier.heightIn(max = 350.dp)) {
          if (selectedApp == null) {
            // App selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  label = { Text(stringResource(R.string.per_app_profile_search)) },
                  leadingIcon = { Icon(Icons.Default.Search, null) },
                  modifier = Modifier.fillMaxWidth(),
                  singleLine = true,
                  shape = RoundedCornerShape(16.dp),
              )

              if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                  CircularProgressIndicator()
                }
              } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  items(filteredApps) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedApp = app },
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                      Row(
                          modifier = Modifier.fillMaxWidth().padding(12.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(12.dp),
                      ) {
                        // App icon
                        val appIcon =
                            remember(app.first) {
                              try {
                                context.packageManager.getApplicationIcon(app.first)
                              } catch (e: Exception) {
                                null
                              }
                            }

                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                          if (appIcon != null) {
                            AsyncImage(
                                model = appIcon,
                                contentDescription = app.second,
                                modifier = Modifier.size(40.dp),
                            )
                          } else {
                            Box(
                                modifier =
                                    Modifier.size(40.dp)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                              Text(
                                  text = app.second.take(1).uppercase(),
                                  style = MaterialTheme.typography.titleMedium,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                              )
                            }
                          }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                          Text(app.second, fontWeight = FontWeight.Medium)
                          Text(
                              app.first,
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                  }
                }
              }
            }
          } else {
            // Profile configuration with scrollable content
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              item {
                // Selected app card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                      // Selected app icon
                      val selectedAppIcon =
                          remember(selectedApp?.first) {
                            try {
                              selectedApp?.first?.let {
                                context.packageManager.getApplicationIcon(it)
                              }
                            } catch (e: Exception) {
                              null
                            }
                          }

                      Box(
                          modifier = Modifier.size(44.dp).clip(CircleShape),
                          contentAlignment = Alignment.Center,
                      ) {
                        if (selectedAppIcon != null) {
                          AsyncImage(
                              model = selectedAppIcon,
                              contentDescription = selectedApp?.second,
                              modifier = Modifier.size(44.dp),
                          )
                        } else {
                          Box(
                              modifier =
                                  Modifier.size(44.dp)
                                      .background(MaterialTheme.colorScheme.tertiary),
                              contentAlignment = Alignment.Center,
                          ) {
                            Text(
                                text = selectedApp!!.second.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                            )
                          }
                        }
                      }
                      Column {
                        Text(selectedApp!!.second, fontWeight = FontWeight.Bold)
                        Text(
                            selectedApp!!.first,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                      }
                    }
                    IconButton(onClick = { selectedApp = null }) {
                      Icon(Icons.Default.Close, "Change app")
                    }
                  }
                }
              }

              // Governor dropdown
              item {
                var governorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = governorExpanded,
                    onExpandedChange = { governorExpanded = it },
                ) {
                  OutlinedTextField(
                      value = selectedGovernor,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text(stringResource(R.string.per_app_profile_governor)) },
                      leadingIcon = {
                        Icon(Icons.Rounded.Memory, null, tint = MaterialTheme.colorScheme.primary)
                      },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
                      modifier = Modifier.menuAnchor().fillMaxWidth(),
                      shape = RoundedCornerShape(16.dp),
                  )
                  ExposedDropdownMenu(
                      expanded = governorExpanded,
                      onDismissRequest = { governorExpanded = false },
                  ) {
                    governors.forEach { gov ->
                      DropdownMenuItem(
                          text = { Text(gov) },
                          onClick = {
                            selectedGovernor = gov
                            governorExpanded = false
                          },
                      )
                    }
                  }
                }
              }

              // Thermal dropdown
              item {
                var thermalExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = thermalExpanded,
                    onExpandedChange = { thermalExpanded = it },
                ) {
                  OutlinedTextField(
                      value = selectedThermal,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text(stringResource(R.string.per_app_profile_thermal)) },
                      leadingIcon = {
                        Icon(Icons.Rounded.Thermostat, null, tint = MaterialTheme.colorScheme.error)
                      },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
                      modifier = Modifier.menuAnchor().fillMaxWidth(),
                      shape = RoundedCornerShape(16.dp),
                  )
                  ExposedDropdownMenu(
                      expanded = thermalExpanded,
                      onDismissRequest = { thermalExpanded = false },
                  ) {
                    thermalPresets.forEach { preset ->
                      DropdownMenuItem(
                          text = { Text(preset) },
                          onClick = {
                            selectedThermal = preset
                            thermalExpanded = false
                          },
                      )
                    }
                  }
                }
              }

              // Refresh rate selection (only if device supports > 60Hz)
              if (availableRefreshRates.isNotEmpty()) {
                item {
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Icon(
                          Icons.Rounded.Speed,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.secondary,
                          modifier = Modifier.size(20.dp),
                      )
                      Text(
                          text = stringResource(R.string.per_app_profile_refresh_rate),
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Medium,
                      )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      // "Not Set" option
                      RefreshRateChip(
                          rate = 0,
                          label = stringResource(R.string.per_app_profile_refresh_rate_not_set),
                          isSelected = selectedRefreshRate == 0,
                          onClick = { selectedRefreshRate = 0 },
                          modifier = Modifier.weight(1f),
                      )

                      // Available rates
                      availableRefreshRates.forEach { rate ->
                        RefreshRateChip(
                            rate = rate,
                            label = "${rate}Hz",
                            isSelected = selectedRefreshRate == rate,
                            onClick = { selectedRefreshRate = rate },
                            modifier = Modifier.weight(1f),
                        )
                      }
                    }

                    // Warning about ROM compatibility
                    Text(
                        text = stringResource(R.string.per_app_profile_refresh_rate_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                  }
                }
              }
            }
          }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Text(stringResource(R.string.per_app_profile_cancel))
          }
          Button(
              onClick = {
                selectedApp?.let { app ->
                  onConfirm(
                      AppProfile(
                          packageName = app.first,
                          appName = app.second,
                          governor = selectedGovernor,
                          thermalPreset = selectedThermal,
                          refreshRate = selectedRefreshRate,
                          enabled = true,
                      )
                  )
                }
              },
              enabled = selectedApp != null,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Text(stringResource(R.string.per_app_profile_add_button))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: AppProfile,
    governors: List<String>,
    thermalPresets: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (AppProfile) -> Unit,
) {
  val context = LocalContext.current
  var selectedGovernor by remember { mutableStateOf(profile.governor) }
  var selectedThermal by remember { mutableStateOf(profile.thermalPreset) }
  var selectedRefreshRate by remember { mutableStateOf(profile.refreshRate) }

  // Get device max refresh rate
  val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
  val availableRefreshRates = remember(maxRefreshRate) { getAvailableRefreshRates(maxRefreshRate) }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Header with gradient
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                )
                        )
                    )
                    .padding(16.dp)
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.per_app_profile_edit),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
          }
        }

        // App info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            // Profile app icon
            val profileAppIcon =
                remember(profile.packageName) {
                  try {
                    context.packageManager.getApplicationIcon(profile.packageName)
                  } catch (e: Exception) {
                    null
                  }
                }

            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
              if (profileAppIcon != null) {
                AsyncImage(
                    model = profileAppIcon,
                    contentDescription = profile.appName,
                    modifier = Modifier.size(48.dp),
                )
              } else {
                Box(
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center,
                ) {
                  Text(
                      text = profile.appName.take(1).uppercase(),
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onTertiary,
                  )
                }
              }
            }
            Column {
              Text(
                  profile.appName,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleMedium,
              )
              Text(
                  profile.packageName,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }

        // Governor dropdown
        var governorExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = governorExpanded,
            onExpandedChange = { governorExpanded = it },
        ) {
          OutlinedTextField(
              value = selectedGovernor,
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.per_app_profile_governor)) },
              leadingIcon = {
                Icon(Icons.Rounded.Memory, null, tint = MaterialTheme.colorScheme.primary)
              },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
          )
          ExposedDropdownMenu(
              expanded = governorExpanded,
              onDismissRequest = { governorExpanded = false },
          ) {
            governors.forEach { gov ->
              DropdownMenuItem(
                  text = { Text(gov) },
                  onClick = {
                    selectedGovernor = gov
                    governorExpanded = false
                  },
              )
            }
          }
        }

        // Thermal dropdown
        var thermalExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = thermalExpanded,
            onExpandedChange = { thermalExpanded = it },
        ) {
          OutlinedTextField(
              value = selectedThermal,
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.per_app_profile_thermal)) },
              leadingIcon = {
                Icon(Icons.Rounded.Thermostat, null, tint = MaterialTheme.colorScheme.error)
              },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
          )
          ExposedDropdownMenu(
              expanded = thermalExpanded,
              onDismissRequest = { thermalExpanded = false },
          ) {
            thermalPresets.forEach { preset ->
              DropdownMenuItem(
                  text = { Text(preset) },
                  onClick = {
                    selectedThermal = preset
                    thermalExpanded = false
                  },
              )
            }
          }
        }

        // Refresh rate selection (only if device supports > 60Hz)
        if (availableRefreshRates.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                  Icons.Rounded.Speed,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(20.dp),
              )
              Text(
                  text = stringResource(R.string.per_app_profile_refresh_rate),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Medium,
              )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              // "Not Set" option
              RefreshRateChip(
                  rate = 0,
                  label = stringResource(R.string.per_app_profile_refresh_rate_not_set),
                  isSelected = selectedRefreshRate == 0,
                  onClick = { selectedRefreshRate = 0 },
                  modifier = Modifier.weight(1f),
              )

              // Available rates
              availableRefreshRates.forEach { rate ->
                RefreshRateChip(
                    rate = rate,
                    label = "${rate}Hz",
                    isSelected = selectedRefreshRate == rate,
                    onClick = { selectedRefreshRate = rate },
                    modifier = Modifier.weight(1f),
                )
              }
            }

            // Warning about ROM compatibility
            Text(
                text = stringResource(R.string.per_app_profile_refresh_rate_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp),
            )
          }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Text(stringResource(R.string.per_app_profile_cancel))
          }
          Button(
              onClick = {
                onConfirm(
                    profile.copy(
                        governor = selectedGovernor,
                        thermalPreset = selectedThermal,
                        refreshRate = selectedRefreshRate,
                    )
                )
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Text(stringResource(R.string.per_app_profile_save_button))
          }
        }
      }
    }
  }
}

@Composable
private fun RefreshRateChip(
    rate: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val backgroundColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceContainerHighest,
          animationSpec = tween(200),
          label = "bg_color",
      )

  val borderColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
          animationSpec = tween(200),
          label = "border_color",
      )

  val scale by
      animateFloatAsState(
          targetValue = if (isSelected) 1.02f else 1f,
          animationSpec = tween(150),
          label = "scale",
      )

  Card(
      modifier =
          modifier
              .scale(scale)
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onClick)
              .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = borderColor,
                  shape = RoundedCornerShape(12.dp),
              ),
      colors = CardDefaults.cardColors(containerColor = backgroundColor),
      shape = RoundedCornerShape(12.dp),
  ) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color =
              if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
      )
    }
  }
}

private fun getDeviceMaxRefreshRate(context: Context): Int {
  return try {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val display = context.display ?: windowManager.defaultDisplay
      display.supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 60
    } else {
      @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
      display.refreshRate.toInt()
    }
  } catch (e: Exception) {
    60
  }
}

private fun getAvailableRefreshRates(maxRate: Int): List<Int> {
  return when {
    maxRate >= 120 -> listOf(60, 90, 120)
    maxRate >= 90 -> listOf(60, 90)
    else -> emptyList() // 60Hz or less = no refresh rate options
  }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
  val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
  val mode =
      appOps.checkOpNoThrow(
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
    val obj =
        JSONObject().apply {
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
