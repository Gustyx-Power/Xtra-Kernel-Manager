package id.xms.xtrakernelmanager.ui.screens.misc.section

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class GameApp(val packageName: String, val appName: String, val enabled: Boolean = true)

@Composable
fun GameControlSection(viewModel: MiscViewModel) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val gameAppsJson by viewModel.gameApps.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var showAddGameDialog by remember { mutableStateOf(false) }

  // Parse game apps from JSON
  val gameApps = remember(gameAppsJson) { parseGameApps(gameAppsJson) }

  // Count enabled games
  val enabledCount = gameApps.count { it.enabled }

  // Check overlay permission
  var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

  // Check usage access permission (needed to detect foreground app)
  var hasUsageAccessPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

  // Refresh permission state when returning from settings
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer =
        androidx.lifecycle.LifecycleEventObserver { _, event ->
          if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            hasUsageAccessPermission = hasUsageStatsPermission(context)
          }
        }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  // Overlay permission launcher
  val overlayPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()
      ) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
      }

  // Usage access permission launcher
  val usageAccessLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()
      ) {
        hasUsageAccessPermission = hasUsageStatsPermission(context)
      }

  // Start/stop GameMonitorService based on enabled games AND both permissions
  LaunchedEffect(enabledCount, hasOverlayPermission, hasUsageAccessPermission) {
    if (enabledCount > 0 && hasOverlayPermission && hasUsageAccessPermission) {
      startGameMonitorService(context)
    } else {
      stopGameMonitorService(context)
    }
  }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header row with expand/collapse
      Row(
          modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
        ) {
          Icon(
              imageVector = Icons.Default.SportsEsports,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.primary,
          )
          Column {
            Text(
                text = stringResource(R.string.game_control),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text =
                    when {
                      gameApps.isEmpty() -> stringResource(R.string.game_control_no_games)
                      enabledCount == 0 ->
                          stringResource(R.string.game_control_all_disabled, gameApps.size)
                      enabledCount > 1 ->
                          stringResource(R.string.game_control_active_games_plural, enabledCount)
                      else -> stringResource(R.string.game_control_active_games, enabledCount)
                    },
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (enabledCount > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            if (enabledCount > 0) {
              Text(
                  text = stringResource(R.string.game_control_overlay_auto),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.tertiary,
              )
            }
          }
        }

        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription =
                if (expanded) stringResource(R.string.collapse)
                else stringResource(R.string.expand),
            modifier = Modifier.size(28.dp),
        )
      }

      // Permission button if needed - Overlay
      if (!hasOverlayPermission) {
        Button(
            onClick = {
              val intent =
                  Intent(
                      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                      "package:${context.packageName}".toUri(),
                  )
              overlayPermissionLauncher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
        ) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.game_control_grant_overlay))
        }
      }

      // Permission button if needed - Usage Access
      if (hasOverlayPermission && !hasUsageAccessPermission) {
        Button(
            onClick = {
              val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
              usageAccessLauncher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
        ) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.game_control_grant_usage_access))
        }
      }

      // Expandable game list
      AnimatedVisibility(
          visible = expanded,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          HorizontalDivider()

          Text(
              text = stringResource(R.string.game_control_game_apps),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 8.dp),
          )

          Text(
              text = stringResource(R.string.game_control_toggle_info),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // Game list
          if (gameApps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
            ) {
              Box(
                  modifier = Modifier.fillMaxWidth().padding(24.dp),
                  contentAlignment = Alignment.Center,
              ) {
                Text(
                    text = stringResource(R.string.game_control_no_games_yet),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          } else {
            gameApps.forEach { game ->
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              if (game.enabled)
                                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                              else MaterialTheme.colorScheme.surfaceContainerHighest
                      ),
                  shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f),
                  ) {
                    // App icon
                    val appIcon =
                        remember(game.packageName) {
                          try {
                            context.packageManager.getApplicationIcon(game.packageName)
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
                            contentDescription = game.appName,
                            modifier = Modifier.size(40.dp),
                        )
                      } else {
                        Box(
                            modifier =
                                Modifier.size(40.dp)
                                    .background(
                                        if (game.enabled) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                          Text(
                              text = game.appName.take(1).uppercase(),
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.Bold,
                              color =
                                  if (game.enabled) MaterialTheme.colorScheme.onPrimary
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        }
                      }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = game.appName, fontWeight = FontWeight.Medium)
                      Text(
                          text = game.packageName,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }

                  Row(
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    // Delete button
                    IconButton(
                        onClick = {
                          val updatedList = gameApps.filter { it.packageName != game.packageName }
                          scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                      Icon(
                          Icons.Default.Delete,
                          contentDescription = stringResource(R.string.game_control_remove),
                          tint = MaterialTheme.colorScheme.error,
                          modifier = Modifier.size(20.dp),
                      )
                    }

                    // Toggle switch
                    LottieSwitchControlled(
                        checked = game.enabled,
                        onCheckedChange = { newEnabled ->
                          if (!hasOverlayPermission && newEnabled) {
                            // Request permission first
                            val intent =
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                            overlayPermissionLauncher.launch(intent)
                          } else {
                            val updatedList =
                                gameApps.map {
                                  if (it.packageName == game.packageName)
                                      it.copy(enabled = newEnabled)
                                  else it
                                }
                            scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                          }
                        },
                        width = 60.dp,
                        height = 30.dp,
                        scale = 2.0f,
                    )
                  }
                }
              }
            }
          }

          // Add game button
          Button(
              onClick = { showAddGameDialog = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.game_control_add_game))
          }
        }
      }
    }
  }

  // Add game dialog
  if (showAddGameDialog) {
    AddGameDialog(
        context = context,
        existingPackages = gameApps.map { it.packageName },
        onDismiss = { showAddGameDialog = false },
        onConfirm = { newGame ->
          val updatedList = gameApps + newGame
          scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
          showAddGameDialog = false
        },
    )
  }
}

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

@Composable
private fun AddGameDialog(
    context: Context,
    existingPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (GameApp) -> Unit,
) {
  var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var searchQuery by remember { mutableStateOf("") }
  val scope = rememberCoroutineScope()

  // Load installed apps
  LaunchedEffect(Unit) {
    scope.launch {
      installedApps =
          withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                  (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
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

  val filteredApps =
      remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
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
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
      Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
              Icons.Rounded.SportsEsports,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
          )
          Text(
              text = stringResource(R.string.game_control_add_game),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.game_control_search_apps)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        // App list
        Box(modifier = Modifier.heightIn(max = 300.dp)) {
          if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator()
            }
          } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(filteredApps) { app ->
                Card(
                    modifier =
                        Modifier.fillMaxWidth().clickable {
                          onConfirm(GameApp(app.first, app.second, enabled = true))
                        },
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    shape = RoundedCornerShape(10.dp),
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
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                      if (appIcon != null) {
                        AsyncImage(
                            model = appIcon,
                            contentDescription = app.second,
                            modifier = Modifier.size(36.dp),
                        )
                      } else {
                        Box(
                            modifier =
                                Modifier.size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                          Text(
                              text = app.second.take(1).uppercase(),
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.Bold,
                              color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.game_control_add_game),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }
          }
        }

        // Cancel button
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
          Text(stringResource(R.string.cancel))
        }
      }
    }
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
              enabled = obj.optBoolean("enabled", true),
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
    val obj =
        JSONObject().apply {
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
    val mode =
        appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName,
        )
    mode == android.app.AppOpsManager.MODE_ALLOWED
  } catch (e: Exception) {
    false
  }
}
