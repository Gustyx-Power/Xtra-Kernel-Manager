package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WelcomePage() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_welcome_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_welcome_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Speed,
      title = stringResource(R.string.setup_feature_performance),
      description = stringResource(R.string.setup_feature_performance_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.BatteryChargingFull,
      title = stringResource(R.string.setup_feature_battery),
      description = stringResource(R.string.setup_feature_battery_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Gamepad,
      title = stringResource(R.string.setup_feature_gaming),
      description = stringResource(R.string.setup_feature_gaming_desc)
    )
  }
}

@Composable
fun PerformancePage() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_performance_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_performance_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Speed,
      title = stringResource(R.string.setup_feature_cpu_governor),
      description = stringResource(R.string.setup_feature_cpu_governor_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Memory,
      title = stringResource(R.string.setup_feature_gpu_scaling),
      description = stringResource(R.string.setup_feature_gpu_scaling_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Tune,
      title = stringResource(R.string.setup_feature_io_scheduler),
      description = stringResource(R.string.setup_feature_io_scheduler_desc)
    )
  }
}

@Composable
fun BatteryPage() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_battery_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_battery_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.PowerSettingsNew,
      title = stringResource(R.string.setup_feature_power_profiles),
      description = stringResource(R.string.setup_feature_power_profiles_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Nightlight,
      title = stringResource(R.string.setup_feature_sleep_optimization),
      description = stringResource(R.string.setup_feature_sleep_optimization_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Analytics,
      title = stringResource(R.string.setup_feature_battery_stats),
      description = stringResource(R.string.setup_feature_battery_stats_desc)
    )
  }
}

@Composable
fun GamingPage() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_gaming_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_gaming_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Gamepad,
      title = stringResource(R.string.setup_feature_game_booster),
      description = stringResource(R.string.setup_feature_game_booster_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.TouchApp,
      title = stringResource(R.string.setup_feature_touch_response),
      description = stringResource(R.string.setup_feature_touch_response_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupFeatureCard(
      icon = Icons.Rounded.Thermostat,
      title = stringResource(R.string.setup_feature_thermal_control),
      description = stringResource(R.string.setup_feature_thermal_control_desc)
    )
  }
}


@Composable
fun PermissionsPage(
  hasRootAccess: Boolean,
  hasAccessibilityPermission: Boolean,
  hasUsagePermission: Boolean,
  hasOverlayPermission: Boolean,
  hasBatteryOptimization: Boolean,
  hasNotificationPermission: Boolean,
  onRootCheck: (Boolean) -> Unit,
  onAccessibilityClick: (Boolean) -> Unit,
  onUsageClick: (Boolean) -> Unit,
  onOverlayClick: (Boolean) -> Unit,
  onBatteryClick: (Boolean) -> Unit,
  onNotificationClick: (Boolean) -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var detectedRootManager by remember { mutableStateOf<Pair<String, String>?>(null) }
  var showForceStopWarning by remember { mutableStateOf(false) }
  
  // Function to check all permissions
  fun checkAllPermissions() {
    CoroutineScope(Dispatchers.IO).launch {
      // Check root access using RootManager
      val rootGranted = RootManager.isRootAvailable()
      
      withContext(Dispatchers.Main) {
        onRootCheck(rootGranted)
        
        // Check accessibility permission
        val accessibilityEnabled = try {
          val accessibilityManager = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) 
            as android.view.accessibility.AccessibilityManager
          val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
          )
          enabledServices?.contains(context.packageName) == true
        } catch (e: Exception) {
          false
        }
        onAccessibilityClick(accessibilityEnabled)
        
        // Check usage access permission
        val usageGranted = try {
          val appOpsManager = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
          val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
              android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
              android.os.Process.myUid(),
              context.packageName
            )
          } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
              android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
              android.os.Process.myUid(),
              context.packageName
            )
          }
          mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
          false
        }
        onUsageClick(usageGranted)
        
        // Check overlay permission
        val overlayGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          android.provider.Settings.canDrawOverlays(context)
        } else {
          true
        }
        onOverlayClick(overlayGranted)
        
        // Check battery optimization
        val batteryOptimized = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
          powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
          true
        }
        onBatteryClick(batteryOptimized)
        
        // Check notification permission
        val notificationGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
          androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
          ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
          true
        }
        onNotificationClick(notificationGranted)
      }
    }
  }
  
  LaunchedEffect(Unit) {
    // Initial permission check
    checkAllPermissions()
    
    // Detect root manager by app name
    val targetNames = listOf("KernelSU", "Magisk", "APatch", "SukiSU")
    val packageManager = context.packageManager
    val installedApps = packageManager.getInstalledApplications(0)
    
    for (app in installedApps) {
      try {
        val appName = packageManager.getApplicationLabel(app).toString()
        for (targetName in targetNames) {
          if (appName.contains(targetName, ignoreCase = true)) {
            detectedRootManager = Pair(app.packageName, appName)
            break
          }
        }
        if (detectedRootManager != null) break
      } catch (e: Exception) {
        // Continue to next app
      }
    }
  }
  
  // Re-check permissions when user returns to this page
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        checkAllPermissions()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  
  // Check if detected manager requires force stop
  val requiresForceStop = detectedRootManager?.second?.let { name ->
    name.contains("KernelSU", ignoreCase = true) || name.contains("SukiSU", ignoreCase = true)
  } ?: false
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_permissions_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_permissions_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupPermissionCard(
      icon = Icons.Rounded.Security,
      title = stringResource(R.string.setup_permission_root),
      description = if (detectedRootManager != null) {
        stringResource(R.string.setup_permission_root_detected, detectedRootManager!!.second)
      } else {
        stringResource(R.string.setup_permission_root_desc)
      },
      isGranted = hasRootAccess,
      onClick = if (detectedRootManager != null) {
        {
          try {
            val intent = context.packageManager.getLaunchIntentForPackage(detectedRootManager!!.first)
            if (intent != null) {
              context.startActivity(intent)
              // Show warning after opening manager if it requires force stop
              if (!hasRootAccess && requiresForceStop) {
                showForceStopWarning = true
              }
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      } else null
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    // Force stop warning for KernelSU/SukiSU
    if (showForceStopWarning && requiresForceStop && !hasRootAccess) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFEF3C7),
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.setup_root_force_stop_title),
              style = MaterialTheme.typography.titleSmall,
              color = Color(0xFF92400E),
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = stringResource(R.string.setup_root_force_stop_desc, detectedRootManager?.second ?: ""),
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF92400E),
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    SetupPermissionCard(
      icon = Icons.Rounded.Accessibility,
      title = stringResource(R.string.setup_permission_accessibility),
      description = stringResource(R.string.setup_permission_accessibility_desc),
      isGranted = hasAccessibilityPermission,
      onClick = {
        try {
          val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
          context.startActivity(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupPermissionCard(
      icon = Icons.Rounded.QueryStats,
      title = stringResource(R.string.setup_permission_usage),
      description = stringResource(R.string.setup_permission_usage_desc),
      isGranted = hasUsagePermission,
      onClick = {
        try {
          val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
          context.startActivity(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupPermissionCard(
      icon = Icons.Rounded.Layers,
      title = stringResource(R.string.setup_permission_overlay),
      description = stringResource(R.string.setup_permission_overlay_desc),
      isGranted = hasOverlayPermission,
      onClick = {
        try {
          val intent = android.content.Intent(
            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
          )
          context.startActivity(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupPermissionCard(
      icon = Icons.Rounded.BatteryFull,
      title = stringResource(R.string.setup_permission_battery),
      description = stringResource(R.string.setup_permission_battery_desc),
      isGranted = hasBatteryOptimization,
      onClick = {
        try {
          val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
          context.startActivity(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupPermissionCard(
      icon = Icons.Rounded.Notifications,
      title = stringResource(R.string.setup_permission_notification),
      description = stringResource(R.string.setup_permission_notification_desc),
      isGranted = hasNotificationPermission,
      onClick = {
        try {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            (context as? android.app.Activity)?.requestPermissions(
              arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
              1001
            )
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    )
  }
}


@Composable
fun StyleSelectionPage(
  selectedStyle: String,
  isAndroid10Plus: Boolean,
  onStyleSelected: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = stringResource(R.string.setup_style_title),
      style = MaterialTheme.typography.headlineLarge,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.setup_style_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF94A3B8),
      fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    SetupStyleCard(
      title = stringResource(R.string.setup_style_material),
      description = stringResource(R.string.setup_style_material_desc),
      isSelected = selectedStyle == "material",
      isEnabled = isAndroid10Plus,
      onClick = { if (isAndroid10Plus) onStyleSelected("material") }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupStyleCard(
      title = stringResource(R.string.setup_style_frosted),
      description = stringResource(R.string.setup_style_frosted_desc),
      isSelected = selectedStyle == "frosted",
      isEnabled = isAndroid10Plus,
      onClick = { if (isAndroid10Plus) onStyleSelected("frosted") }
    )
    Spacer(modifier = Modifier.height(16.dp))

    SetupStyleCard(
      title = stringResource(R.string.setup_style_classic),
      description = stringResource(R.string.setup_style_classic_desc),
      isSelected = selectedStyle == "classic",
      isEnabled = true,
      onClick = { onStyleSelected("classic") }
    )
  }
}

@Composable
fun CompletionPage(
  onComplete: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.logo_a),
        contentDescription = "XKM Logo",
        modifier = Modifier.size(120.dp)
      )
      
      Spacer(modifier = Modifier.height(32.dp))
      
      Text(
        text = stringResource(R.string.setup_complete_title),
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      Text(
        text = stringResource(R.string.setup_complete_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF94A3B8),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(64.dp))
      
      FloatingActionButton(
        onClick = onComplete,
        modifier = Modifier.size(80.dp),
        containerColor = Color(0xFF38BDF8),
        contentColor = Color.White
      ) {
        Icon(
          imageVector = Icons.Rounded.ArrowForward,
          contentDescription = "Start",
          modifier = Modifier.size(32.dp)
        )
      }
    }
  }
}
