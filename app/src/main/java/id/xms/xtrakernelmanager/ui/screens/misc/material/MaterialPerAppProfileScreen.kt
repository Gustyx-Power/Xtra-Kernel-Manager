package id.xms.xtrakernelmanager.ui.screens.misc.material

import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import androidx.annotation.StringRes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

enum class ProfileType(@StringRes val displayNameRes: Int, @StringRes val descriptionRes: Int, val governor: String, val thermalPreset: String) {
  PERFORMANCE(R.string.profile_performance, R.string.profile_desc_performance, "performance", "Extreme"),
  BALANCED(R.string.profile_balanced, R.string.profile_desc_balanced, "schedutil", "Dynamic"),
  POWER_SAVE(R.string.profile_power_save, R.string.profile_desc_power_save, "powersave", "Class 0"),
}

enum class RefreshRate(@StringRes val displayNameRes: Int, val value: Int) {
  DEFAULT(R.string.refresh_rate_default, 0),
  HZ_60(R.string.refresh_rate_60, 60),
  HZ_90(R.string.refresh_rate_90, 90),
  HZ_120(R.string.refresh_rate_120, 120),
  HZ_144(R.string.refresh_rate_144, 144),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPerAppProfileScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val preferencesManager = remember { PreferencesManager(context) }
  
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
  var selectedFilter by remember { mutableStateOf<ProfileType?>(null) }
  
  // Inline expansion state
  var expandedAppPackage by remember { mutableStateOf<String?>(null) }

  val filteredApps =
      remember(appProfiles, searchQuery, selectedFilter) {
        val baseList =
            if (searchQuery.isBlank()) {
              appProfiles.sortedBy { it.appName }
            } else {
              appProfiles
                  .filter {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                  }
                  .sortedBy { it.appName }
            }

        if (selectedFilter != null) {
          baseList.filter { getProfileTypeFromApp(it) == selectedFilter }
        } else {
          baseList
        }
      }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
            title = { Text(stringResource(id.xms.xtrakernelmanager.R.string.per_app_profile), fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            navigationIcon = {
              IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
      },
  ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      // Search & Filters Row
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(id.xms.xtrakernelmanager.R.string.search_apps_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                ),
        )

        // Filter Dropdown
        Box {
          var filterExpanded by remember { mutableStateOf(false) }

          FilledTonalIconButton(
              onClick = { filterExpanded = true },
              modifier = Modifier.size(56.dp),
              shape = MaterialTheme.shapes.large,
              colors =
                  IconButtonDefaults.filledTonalIconButtonColors(
                      containerColor =
                          if (selectedFilter != null)
                              getProfileColor(selectedFilter!!).copy(alpha = 0.2f)
                          else MaterialTheme.colorScheme.surfaceContainerHigh,
                      contentColor =
                          if (selectedFilter != null) getProfileColor(selectedFilter!!)
                          else MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          ) {
            Icon(
                imageVector =
                    if (selectedFilter != null) Icons.Rounded.FilterListOff
                    else Icons.Rounded.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(24.dp),
            )
          }

          DropdownMenu(
              expanded = filterExpanded,
              onDismissRequest = { filterExpanded = false },
              shape = MaterialTheme.shapes.large,
              containerColor = MaterialTheme.colorScheme.surfaceContainer,
          ) {
            DropdownMenuItem(
                text = { Text(stringResource(id.xms.xtrakernelmanager.R.string.filter_all)) },
                onClick = {
                  selectedFilter = null
                  filterExpanded = false
                },
                leadingIcon = { if (selectedFilter == null) Icon(Icons.Rounded.Check, null) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ProfileType.entries.forEach { type ->
                  DropdownMenuItem(
                      text = { Text(stringResource(type.displayNameRes), color = getProfileColor(type)) },
                      onClick = {
                        selectedFilter = type
                        filterExpanded = false
                      },
                      leadingIcon = {
                        if (selectedFilter == type) {
                          Icon(Icons.Rounded.Check, null, tint = getProfileColor(type))
                        } else {
                          Icon(getProfileIcon(type), null, tint = getProfileColor(type))
                        }
                      },
                  )
                }
          }
        }
      }

      // App List
      LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Active Configs Section (Only show if no filter and search)
        val activeConfigs = appProfiles.filter { 
             it.governor != "schedutil" || it.thermalPreset != "Not Set" || it.refreshRate != 0
        }
        
        if (searchQuery.isEmpty() && selectedFilter == null && activeConfigs.isNotEmpty()) {
          item {
            Text(
                stringResource(id.xms.xtrakernelmanager.R.string.per_app_active_configs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            )
          }

          item {
            LazyRow(
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(activeConfigs) { app ->
                ActiveConfigCard(
                    app = app, 
                    onClick = { expandedAppPackage = app.packageName }
                )
              }
            }
          }

          item {
            Text(
                stringResource(id.xms.xtrakernelmanager.R.string.per_app_all_apps),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            )
          }
        }

        items(filteredApps, key = { it.packageName }) { app ->
          ExpressiveAppItem(
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

        item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom padding
      }
    }
  }
}

@Composable
fun ActiveConfigCard(app: AppProfile, onClick: () -> Unit) {
  val context = LocalContext.current
  val profileType = getProfileTypeFromApp(app)
  val color = getProfileColor(profileType)

  Card(
      onClick = onClick,
      modifier = Modifier.size(width = 160.dp, height = 180.dp),
      shape = MaterialTheme.shapes.extraLarge,
      colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(getAppIcon(context, app.packageName))
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.medium),
        )

        Surface(color = color, shape = CircleShape, modifier = Modifier.size(24.dp)) {
          Icon(
              getProfileIcon(profileType),
              null,
              tint = MaterialTheme.colorScheme.surface,
              modifier = Modifier.padding(4.dp),
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
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if(app.refreshRate != 0) "${app.refreshRate}Hz" else stringResource(profileType.displayNameRes),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
fun ExpressiveAppItem(
    app: AppProfile,
    availableRefreshRates: List<Int>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdate: (AppProfile) -> Unit
) {
  val context = LocalContext.current
  val isCustomized = app.governor != "schedutil" || app.thermalPreset != "Not Set" || app.refreshRate != 0
  val profileType = getProfileTypeFromApp(app)
  val profileColor = getProfileColor(profileType)

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { onToggleExpand() },
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isCustomized) {
                    profileColor.copy(alpha = 0.05f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                  }
          ),
      border =
          if (isCustomized) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                profileColor.copy(alpha = 0.3f),
            )
          } else null,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // App icon
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(getAppIcon(context, app.packageName))
                    .crossfade(true)
                    .build(),
            contentDescription = app.appName,
            loading = {
              Box(
                  modifier =
                      Modifier.size(56.dp)
                          .clip(MaterialTheme.shapes.large)
                          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                  contentAlignment = Alignment.Center,
              ) {
                Text(
                    app.appName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
              }
            },
            error = {
              Box(
                  modifier =
                      Modifier.size(56.dp)
                          .clip(MaterialTheme.shapes.large)
                          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                  contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Rounded.Android, null)
              }
            },
            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.large),
        )

        // App info
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = app.appName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
              text = app.packageName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
          
          if(isCustomized && !isExpanded) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Surface(
                      color = profileColor.copy(alpha = 0.1f), 
                      shape = RoundedCornerShape(8.dp)
                  ) {
                      Text(
                          stringResource(profileType.displayNameRes),
                          style = MaterialTheme.typography.labelSmall,
                          color = profileColor,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                  }
                  if (app.refreshRate != 0) {
                      Surface(
                          color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), 
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
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
      }
      
      // Inline Expansion Content
      AnimatedVisibility(visible = isExpanded) {
          Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
              HorizontalDivider(
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                  modifier = Modifier.padding(bottom = 12.dp)
              )
              
              // Performance Profile Dropdown
              ConfigDropdownRow(
                  label = stringResource(id.xms.xtrakernelmanager.R.string.per_app_performance_profile),
                  currentValue = stringResource(getProfileTypeFromApp(app).displayNameRes),
                  icon = Icons.Rounded.Settings,
                  isModified = true
              ) { dismiss ->
                  ProfileType.entries.forEach { type ->
                      DropdownMenuItem(
                          text = { Text(stringResource(type.displayNameRes)) },
                          onClick = { 
                              onUpdate(app.copy(
                                  governor = type.governor,
                                  thermalPreset = type.thermalPreset
                              ))
                              dismiss()
                          },
                          leadingIcon = { 
                              val icon = getProfileIcon(type)
                              val color = getProfileColor(type)
                              Icon(icon, null, tint = color)
                          },
                          trailingIcon = {
                              if(getProfileTypeFromApp(app) == type) Icon(Icons.Rounded.Check, null)
                          }
                      )
                  }
              }

             // Refresh Rate Dropdown - Only show if device supports >60Hz
              if (availableRefreshRates.isNotEmpty()) {
                  ConfigDropdownRow(
                      label = stringResource(id.xms.xtrakernelmanager.R.string.per_app_refresh_rate),
                      currentValue = if(app.refreshRate == 0) stringResource(RefreshRate.DEFAULT.displayNameRes) else "${app.refreshRate}Hz",
                      icon = Icons.Rounded.Refresh,
                      isModified = app.refreshRate != 0
                  ) { dismiss ->
                      // Default option
                      DropdownMenuItem(
                          text = { Text(stringResource(RefreshRate.DEFAULT.displayNameRes)) },
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
fun ConfigDropdownRow(
    label: String,
    currentValue: String,
    icon: ImageVector,
    isModified: Boolean,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                 tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                 modifier = Modifier.size(24.dp)
             )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Box {
             Surface(
                 shape = RoundedCornerShape(12.dp),
                 color = MaterialTheme.colorScheme.surfaceContainerHigh,
                 modifier = Modifier.height(36.dp).clickable { expanded = true }
             ) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.padding(horizontal = 12.dp)
                 ) {
                     Text(
                         currentValue,
                         style = MaterialTheme.typography.labelMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = if(isModified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Icon(
                         Icons.Rounded.ArrowDropDown,
                         null,
                         modifier = Modifier.size(16.dp),
                         tint = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                 }
             }
             
             DropdownMenu(
                 expanded = expanded,
                 onDismissRequest = { expanded = false },
                 shape = RoundedCornerShape(16.dp),
                 containerColor = MaterialTheme.colorScheme.surfaceContainer,
                 tonalElevation = 4.dp
             ) {
                 content { expanded = false }
             }
        }
    }
}

@Composable
private fun getProfileColor(profile: ProfileType): Color {
  return when (profile) {
    ProfileType.PERFORMANCE -> MaterialTheme.colorScheme.primary
    ProfileType.BALANCED -> MaterialTheme.colorScheme.secondary
    ProfileType.POWER_SAVE -> MaterialTheme.colorScheme.tertiary
  }
}

private fun getProfileIcon(profile: ProfileType): ImageVector {
  return when (profile) {
    ProfileType.PERFORMANCE -> Icons.Rounded.RocketLaunch
    ProfileType.BALANCED -> Icons.Rounded.Balance
    ProfileType.POWER_SAVE -> Icons.Rounded.BatteryChargingFull
  }
}

private fun getInstalledApps(context: android.content.Context): List<AppProfile> {
  return try {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
    apps
        .filter {
          // Only show user-installed apps (not system apps)
          (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
        }
        .map { info ->
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

private fun getAppIcon(
    context: android.content.Context,
    packageName: String,
): android.graphics.drawable.Drawable? {
  return try {
    context.packageManager.getApplicationIcon(packageName)
  } catch (e: Exception) {
    null
  }
}

// Helper function to determine ProfileType from AppProfile
private fun getProfileTypeFromApp(app: AppProfile): ProfileType {
    return when {
        app.governor == "performance" && app.thermalPreset == "Extreme" -> ProfileType.PERFORMANCE
        app.governor == "powersave" && app.thermalPreset == "Class 0" -> ProfileType.POWER_SAVE
        app.governor == "schedutil" && app.thermalPreset == "Dynamic" -> ProfileType.BALANCED
        // Default to BALANCED if no match
        else -> ProfileType.BALANCED
    }
}

// Helper functions for refresh rate detection
private fun getDeviceMaxRefreshRate(context: android.content.Context): Int {
    return try {
        val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
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
        else -> emptyList() // 60Hz or less = no refresh rate options
    }
}

// Parse profiles from JSON
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

// Save a single profile
private suspend fun saveProfile(
    preferencesManager: PreferencesManager,
    updatedApp: AppProfile,
    allApps: List<AppProfile>
) {
    // Get current saved profiles
    val currentJson = preferencesManager.getAppProfiles().first()
    val currentProfiles = parseProfiles(currentJson).toMutableList()
    
    // Get system default governor to determine if this is truly customized
    // We'll use the first cluster's governor as the system default
    // Note: In a real scenario, you might want to pass this as a parameter
    val systemDefaultGovernor = updatedApp.governor // This will be set correctly from the UI
    
    // Check if this app is customized (not default)
    // An app is considered default if it uses system governor and "Not Set" thermal, and no refresh rate
    val isCustomized = updatedApp.thermalPreset != "Not Set" || updatedApp.refreshRate != 0
    
    if (isCustomized) {
        // Add or update the profile
        val existingIndex = currentProfiles.indexOfFirst { it.packageName == updatedApp.packageName }
        if (existingIndex >= 0) {
            currentProfiles[existingIndex] = updatedApp
        } else {
            currentProfiles.add(updatedApp)
        }
    } else {
        // Remove the profile if it's set to default
        currentProfiles.removeAll { it.packageName == updatedApp.packageName }
    }
    
    // Save to preferences
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
