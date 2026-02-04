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

data class AppProfile(
    val packageName: String,
    val appName: String,
    val profileType: ProfileType = ProfileType.DEFAULT,
    val refreshRate: RefreshRate = RefreshRate.DEFAULT,
)


enum class ProfileType(@StringRes val displayNameRes: Int, @StringRes val descriptionRes: Int) {
  DEFAULT(R.string.profile_default, R.string.profile_desc_default),
  PERFORMANCE(R.string.profile_performance, R.string.profile_desc_performance),
  BALANCED(R.string.profile_balanced, R.string.profile_desc_balanced),
  POWER_SAVE(R.string.profile_power_save, R.string.profile_desc_power_save),
  GAMING(R.string.profile_gaming, R.string.profile_desc_gaming),
}

enum class RefreshRate(@StringRes val displayNameRes: Int, val value: String) {
  DEFAULT(R.string.refresh_rate_default, "def"),
  HZ_60(R.string.refresh_rate_60, "60"),
  HZ_90(R.string.refresh_rate_90, "90"),
  HZ_120(R.string.refresh_rate_120, "120"),
  HZ_144(R.string.refresh_rate_144, "144"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPerAppProfileScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val context = LocalContext.current
  var appProfiles by remember { mutableStateOf(getInstalledApps(context)) }
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
          baseList.filter { it.profileType == selectedFilter }
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

            ProfileType.entries
                .filter { it != ProfileType.DEFAULT }
                .forEach { type ->
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
             it.profileType != ProfileType.DEFAULT || it.refreshRate != RefreshRate.DEFAULT 
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
                ActiveConfigCard(app = app, onClick = { expandedAppPackage = app.packageName })
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
              isExpanded = expandedAppPackage == app.packageName,
              onToggleExpand = {
                  expandedAppPackage = if (expandedAppPackage == app.packageName) null else app.packageName
              },
              onUpdate = { updatedApp ->
                   appProfiles = appProfiles.map {
                      if (it.packageName == updatedApp.packageName) updatedApp else it
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
  val color = getProfileColor(app.profileType)

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
              getProfileIcon(app.profileType),
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
            if(app.profileType != ProfileType.DEFAULT) stringResource(app.profileType.displayNameRes) else stringResource(app.refreshRate.displayNameRes),
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
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdate: (AppProfile) -> Unit
) {
  val context = LocalContext.current
  val isCustomized = app.profileType != ProfileType.DEFAULT || app.refreshRate != RefreshRate.DEFAULT
  val profileColor = getProfileColor(app.profileType)

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
                  if (app.profileType != ProfileType.DEFAULT) {
                       Surface(
                           color = profileColor.copy(alpha = 0.1f), 
                           shape = RoundedCornerShape(8.dp)
                       ) {
                           Text(
                                stringResource(app.profileType.displayNameRes),
                               style = MaterialTheme.typography.labelSmall,
                               color = profileColor,
                               modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                           )
                       }
                  }
                  if (app.refreshRate != RefreshRate.DEFAULT) {
                       Surface(
                           color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), 
                           shape = RoundedCornerShape(8.dp)
                       ) {
                           Text(
                                stringResource(app.refreshRate.displayNameRes),
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
                  currentValue = stringResource(app.profileType.displayNameRes),
                  icon = Icons.Rounded.Settings,
                  isModified = app.profileType != ProfileType.DEFAULT
              ) { dismiss ->
                  ProfileType.entries.forEach { type ->
                      DropdownMenuItem(
                          text = { Text(stringResource(type.displayNameRes)) },
                          onClick = { 
                              onUpdate(app.copy(profileType = type))
                              dismiss()
                          },
                          leadingIcon = { 
                              val icon = getProfileIcon(type)
                              val color = getProfileColor(type)
                              Icon(icon, null, tint = if(type == ProfileType.DEFAULT) MaterialTheme.colorScheme.onSurfaceVariant else color)
                          },
                          trailingIcon = {
                              if(app.profileType == type) Icon(Icons.Rounded.Check, null)
                          }
                      )
                  }
              }

             // Refresh Rate Dropdown
              ConfigDropdownRow(
                  label = stringResource(id.xms.xtrakernelmanager.R.string.per_app_refresh_rate),
                  currentValue = stringResource(app.refreshRate.displayNameRes),
                  icon = Icons.Rounded.Refresh,
                  isModified = app.refreshRate != RefreshRate.DEFAULT
              ) { dismiss ->
                  RefreshRate.entries.forEach { rate ->
                      DropdownMenuItem(
                          text = { Text(stringResource(rate.displayNameRes)) },
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
    ProfileType.DEFAULT -> MaterialTheme.colorScheme.outline
    ProfileType.PERFORMANCE -> MaterialTheme.colorScheme.primary
    ProfileType.BALANCED -> MaterialTheme.colorScheme.secondary
    ProfileType.POWER_SAVE -> MaterialTheme.colorScheme.tertiary
    ProfileType.GAMING -> MaterialTheme.colorScheme.inversePrimary
  }
}

private fun getProfileIcon(profile: ProfileType): ImageVector {
  return when (profile) {
    ProfileType.DEFAULT -> Icons.Rounded.Settings
    ProfileType.PERFORMANCE -> Icons.Rounded.RocketLaunch
    ProfileType.BALANCED -> Icons.Rounded.Balance
    ProfileType.POWER_SAVE -> Icons.Rounded.BatteryChargingFull
    ProfileType.GAMING -> Icons.Rounded.SportsEsports
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
          )
        }
  } catch (e: Exception) {
    // Fallback to mock data
    listOf(
        AppProfile("com.instagram.android", "Instagram"),
        AppProfile("com.whatsapp", "WhatsApp"),
        AppProfile("com.spotify.music", "Spotify"),
        AppProfile("com.discord", "Discord"),
        AppProfile("com.netflix.mediaclient", "Netflix"),
        AppProfile("com.miHoYo.GenshinImpact", "Genshin Impact", ProfileType.GAMING, RefreshRate.HZ_120),
        AppProfile("com.tencent.ig", "PUBG Mobile", ProfileType.GAMING, RefreshRate.HZ_90),
        AppProfile("com.mobile.legends", "Mobile Legends", ProfileType.GAMING, RefreshRate.HZ_120),
    )
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
