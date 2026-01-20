package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

data class AppProfile(
    val packageName: String,
    val appName: String,
    val profileType: ProfileType = ProfileType.DEFAULT,
)

enum class ProfileType(val displayName: String, val description: String) {
  DEFAULT("Default", "Use system defaults"),
  PERFORMANCE("Performance", "Max performance"),
  BALANCED("Balanced", "Optimal balance"),
  POWER_SAVE("Power Save", "Extend battery"),
  GAMING("Gaming", "Gaming optimization"),
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
  var selectedApp by remember { mutableStateOf<AppProfile?>(null) }

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
            title = { Text("Per App Profile", fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
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
            placeholder = { Text("Search apps...") },
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
                text = { Text("All Apps") },
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
                      text = { Text(type.displayName, color = getProfileColor(type)) },
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
        val activeConfigs = appProfiles.filter { it.profileType != ProfileType.DEFAULT }
        if (searchQuery.isEmpty() && selectedFilter == null && activeConfigs.isNotEmpty()) {
          item {
            Text(
                "Active Configurations",
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
                ActiveConfigCard(app = app, onClick = { selectedApp = app })
              }
            }
          }

          item {
            Text(
                "All Application",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            )
          }
        }

        items(filteredApps, key = { it.packageName }) { app ->
          ExpressiveAppItem(
              app = app,
              isSelected = selectedApp?.packageName == app.packageName,
              onClick = {
                selectedApp = if (selectedApp?.packageName == app.packageName) null else app
              },
              onProfileChange = { newProfile ->
                appProfiles =
                    appProfiles.map {
                      if (it.packageName == app.packageName) {
                        it.copy(profileType = newProfile)
                      } else it
                    }
              },
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
            app.profileType.displayName,
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
    isSelected: Boolean,
    onClick: () -> Unit,
    onProfileChange: (ProfileType) -> Unit,
) {
  val context = LocalContext.current
  val isCustomized = app.profileType != ProfileType.DEFAULT

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { onClick() },
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isCustomized) {
                    getProfileColor(app.profileType).copy(alpha = 0.05f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                  }
          ),
      border =
          if (isCustomized) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                getProfileColor(app.profileType).copy(alpha = 0.3f),
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
        }

        // Status Indicator
        if (isCustomized) {
          Box(
              modifier =
                  Modifier.size(40.dp)
                      .clip(CircleShape)
                      .background(getProfileColor(app.profileType).copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                getProfileIcon(app.profileType),
                null,
                tint = getProfileColor(app.profileType),
                modifier = Modifier.size(20.dp),
            )
          }
        } else {
          Icon(
              if (isSelected) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Expanded Profile Selection
      AnimatedVisibility(visible = isSelected) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
              "Performance Profile",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 8.dp),
          )

          ProfileType.entries.forEach { profile ->
            ExpressiveProfileOption(
                profile = profile,
                isSelected = app.profileType == profile,
                onClick = { onProfileChange(profile) },
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExpressiveProfileOption(
    profile: ProfileType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
  val color = getProfileColor(profile)

  Surface(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.medium,
      color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
      border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Icon(
          getProfileIcon(profile),
          null,
          tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(24.dp),
      )

      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
        )
        if (isSelected) {
          Text(
              text = profile.description,
              style = MaterialTheme.typography.bodySmall,
              color = color.copy(alpha = 0.8f),
          )
        }
      }

      if (isSelected) {
        Icon(Icons.Rounded.Check, null, tint = color)
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

private fun getProfileIcon(profile: ProfileType): androidx.compose.ui.graphics.vector.ImageVector {
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
        AppProfile("com.miHoYo.GenshinImpact", "Genshin Impact", ProfileType.GAMING),
        AppProfile("com.tencent.ig", "PUBG Mobile", ProfileType.GAMING),
        AppProfile("com.mobile.legends", "Mobile Legends", ProfileType.GAMING),
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
