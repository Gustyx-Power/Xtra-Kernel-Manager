package id.xms.xtrakernelmanager.ui.screens.setup

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.utils.AccessibilityServiceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupScreen(onSetupComplete: (String) -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val pagerState = rememberPagerState(pageCount = { 3 })

  // Permission States
  var isRootGranted by remember { mutableStateOf(false) }
  var isUsageGranted by remember { mutableStateOf(false) }
  var isNotificationGranted by remember { mutableStateOf(false) }
  var isStorageGranted by remember { mutableStateOf(false) }
  var isAccessibilityGranted by remember { mutableStateOf(false) }
  var isOverlayGranted by remember { mutableStateOf(false) }
  var isBatteryOptimizationDisabled by remember { mutableStateOf(false) }

  // Layout Selection State
  var selectedLayout by remember { mutableStateOf("material") }

  // Navigation Handler
  val canGoBack = pagerState.currentPage > 0
  BackHandler(enabled = canGoBack) {
    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
  }

  // Animated Background
  val infiniteTransition = rememberInfiniteTransition(label = "background")
  val animatedOffset by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
          animation = tween(20000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
      ),
      label = "offset"
  )

  // Check Permissions Function
  fun checkPermissions() {
    scope.launch(Dispatchers.IO) {
      val root = RootManager.isRootAvailable()

      // Usage Access
      val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
      val mode =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
          } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
          }
      val usage = mode == AppOpsManager.MODE_ALLOWED

      // Notification
      val notification =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
          } else {
            true
          }

      // Storage
      val storage =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
          } else {
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
          }

      // Accessibility
      val accessibility = Settings.Secure.getString(
          context.contentResolver,
          Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      )?.contains(context.packageName) == true

      // Overlay
      val overlay = Settings.canDrawOverlays(context)

      // Battery Optimization
      val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
      val batteryOptimization = powerManager.isIgnoringBatteryOptimizations(context.packageName)

      withContext(Dispatchers.Main) {
        isRootGranted = root
        isUsageGranted = usage
        isNotificationGranted = notification
        isStorageGranted = storage
        isAccessibilityGranted = accessibility
        isOverlayGranted = overlay
        isBatteryOptimizationDisabled = batteryOptimization
      }
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) checkPermissions()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(Unit) {
    checkPermissions()
    while (true) {
      delay(2000)
      checkPermissions()
    }
  }

  Box(
      modifier = Modifier
          .fillMaxSize()
          .background(
              Brush.radialGradient(
                  colors = listOf(
                      Color(0xFF1A1A2E),
                      Color(0xFF0F0F1E),
                      Color(0xFF000000)
                  ),
                  center = Offset(500f, 500f)
              )
          )
  ) {
    // Animated Background Particles
    AnimatedBackgroundParticles(animatedOffset)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
      // Modern Progress Indicator
      ModernProgressIndicator(
          currentPage = pagerState.currentPage,
          totalPages = pagerState.pageCount,
          modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
      )

      HorizontalPager(
          state = pagerState,
          modifier = Modifier.weight(1f).fillMaxWidth(),
          userScrollEnabled = false,
      ) { page ->
        when (page) {
          0 -> WelcomePage(onNext = { scope.launch { pagerState.animateScrollToPage(1) } })
          1 -> PermissionsPage(
              isRootGranted = isRootGranted,
              isUsageGranted = isUsageGranted,
              isNotificationGranted = isNotificationGranted,
              isStorageGranted = isStorageGranted,
              isAccessibilityGranted = isAccessibilityGranted,
              isOverlayGranted = isOverlayGranted,
              isBatteryOptimizationDisabled = isBatteryOptimizationDisabled,
              onCheckPermissions = { checkPermissions() },
              onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
              onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
          )
          2 -> LayoutSelectionPage(
              selectedLayout = selectedLayout,
              onLayoutSelected = { selectedLayout = it },
              onFinish = { onSetupComplete(selectedLayout) },
              onBack = { scope.launch { pagerState.animateScrollToPage(1) } },
          )
        }
      }
    }
  }
}

@Composable
fun AnimatedBackgroundParticles(offset: Float) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val particleCount = 30
    val centerX = size.width / 2
    val centerY = size.height / 2

    for (i in 0 until particleCount) {
      val angle = (offset + i * (360f / particleCount)) * (Math.PI / 180f).toFloat()
      val radius = (i % 3 + 1) * 150f
      val x = centerX + cos(angle) * radius
      val y = centerY + sin(angle) * radius
      val alpha = 0.1f + (i % 3) * 0.05f

      drawCircle(
          color = Color.White.copy(alpha = alpha),
          radius = 2f + (i % 3) * 2f,
          center = Offset(x, y),
          style = Fill
      )
    }
  }
}

@Composable
fun ModernProgressIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(totalPages) { index ->
      val isActive = index == currentPage
      val isPassed = index < currentPage

      Box(
          modifier = Modifier
              .padding(horizontal = 4.dp)
              .height(4.dp)
              .width(if (isActive) 32.dp else 16.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(
                  when {
                      isActive -> Color(0xFF00D9FF)
                      isPassed -> Color(0xFF00D9FF).copy(alpha = 0.5f)
                      else -> Color.White.copy(alpha = 0.2f)
                  }
              )
              .animateContentSize()
      )
    }
  }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "welcome")
  val scale by infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.05f,
      animationSpec = infiniteRepeatable(
          animation = tween(2000, easing = EaseInOutCubic),
          repeatMode = RepeatMode.Reverse
      ),
      label = "scale"
  )

  val rotation by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
          animation = tween(30000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
      ),
      label = "rotation"
  )

  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 32.dp)
          .padding(top = 60.dp, bottom = 40.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
      // Futuristic Logo Container
      Box(
          modifier = Modifier.size(200.dp),
          contentAlignment = Alignment.Center
      ) {
        // Rotating Ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
          drawCircle(
              color = Color(0xFF00D9FF),
              radius = size.minDimension / 2,
              style = Stroke(width = 3f)
          )
          drawCircle(
              color = Color(0xFF00D9FF).copy(alpha = 0.3f),
              radius = size.minDimension / 2 - 10f,
              style = Stroke(width = 1f)
          )
        }

        // Center Logo from logo_a.xml
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00D9FF).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .border(2.dp, Color(0xFF00D9FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              painter = painterResource(id = R.drawable.logo_a),
              contentDescription = null,
              modifier = Modifier.size(80.dp),
              tint = Color.Unspecified
          )
        }
      }

      // Title and Description
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
            text = stringResource(R.string.setup_welcome),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.setup_welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
      }
    }

    // Modern CTA Button
    FuturisticButton(
        text = stringResource(R.string.setup_get_started),
        onClick = onNext,
        modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun PermissionsPage(
    isRootGranted: Boolean,
    isUsageGranted: Boolean,
    isNotificationGranted: Boolean,
    isStorageGranted: Boolean,
    isAccessibilityGranted: Boolean,
    isOverlayGranted: Boolean,
    isBatteryOptimizationDisabled: Boolean,
    onCheckPermissions: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val requestPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onCheckPermissions()
      }

  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp)
          .padding(top = 16.dp, bottom = 24.dp)
  ) {
    // Header
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
      Text(
          text = stringResource(R.string.setup_permissions_title),
          style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold
          ),
          color = Color.White,
          textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
          text = stringResource(R.string.setup_permissions_desc),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Scrollable Permissions List
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Root Access (Mandatory)
      ModernPermissionCard(
          icon = Icons.Rounded.Security,
          title = stringResource(R.string.setup_root_access),
          description = stringResource(R.string.setup_root_desc),
          isGranted = isRootGranted,
          isMandatory = true,
          onGrant = { com.topjohnwu.superuser.Shell.getShell {} }
      )

      // Accessibility Service
      ModernPermissionCard(
          icon = Icons.Rounded.Accessibility,
          title = "Accessibility Service",
          description = "Required for game detection and automatic profile switching",
          isGranted = isAccessibilityGranted,
          isMandatory = false,
          onGrant = {
            AccessibilityServiceHelper.bypassRestrictedSettings(context)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
          }
      )

      // Usage Access
      ModernPermissionCard(
          icon = Icons.Rounded.DataUsage,
          title = stringResource(R.string.setup_usage_access),
          description = stringResource(R.string.setup_usage_desc),
          isGranted = isUsageGranted,
          isMandatory = false,
          onGrant = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
          }
      )

      // Overlay Permission
      ModernPermissionCard(
          icon = Icons.Rounded.Layers,
          title = "Display Over Other Apps",
          description = "Required for game overlay and floating windows",
          isGranted = isOverlayGranted,
          isMandatory = false,
          onGrant = {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
          }
      )

      // Battery Optimization
      ModernPermissionCard(
          icon = Icons.Rounded.BatteryChargingFull,
          title = "Battery Optimization",
          description = "Disable to keep background services running",
          isGranted = isBatteryOptimizationDisabled,
          isMandatory = false,
          onGrant = {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
          }
      )

      // Notifications
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ModernPermissionCard(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.setup_notifications),
            description = stringResource(R.string.setup_notifications_desc),
            isGranted = isNotificationGranted,
            isMandatory = false,
            onGrant = { 
              requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) 
            }
        )
      }

      // Storage
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        ModernPermissionCard(
            icon = Icons.Rounded.Storage,
            title = stringResource(R.string.setup_storage_access),
            description = stringResource(R.string.setup_storage_desc),
            isGranted = isStorageGranted,
            isMandatory = false,
            onGrant = {
              requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Continue Button
    val canProceed = isRootGranted

    FuturisticButton(
        text = if (canProceed) stringResource(R.string.setup_select) 
               else stringResource(R.string.setup_root_required),
        onClick = onNext,
        enabled = canProceed,
        modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun LayoutSelectionPage(
    selectedLayout: String,
    onLayoutSelected: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
) {
  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp)
          .padding(top = 16.dp, bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Header
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
      Text(
          text = stringResource(R.string.setup_choose_style),
          style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold
          ),
          color = Color.White,
          textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
          text = stringResource(R.string.setup_choose_style_desc),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Layout Options
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      ModernLayoutCard(
          title = stringResource(R.string.layout_material),
          description = stringResource(R.string.layout_material_desc),
          icon = Icons.Rounded.ViewQuilt,
          isSelected = selectedLayout == "material",
          onSelect = { onLayoutSelected("material") }
      )

      ModernLayoutCard(
          title = stringResource(R.string.layout_legacy),
          description = stringResource(R.string.layout_legacy_desc),
          icon = Icons.Rounded.BlurOn,
          isSelected = selectedLayout == "liquid",
          onSelect = { onLayoutSelected("liquid") }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Finish Button
    FuturisticButton(
        text = stringResource(
            R.string.setup_continue_with,
            if (selectedLayout == "material") stringResource(R.string.layout_material)
            else stringResource(R.string.layout_legacy)
        ),
        onClick = onFinish,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.setup_root_note),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
    )
  }
}

// Modern Permission Card Component
@Composable
fun ModernPermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isMandatory: Boolean = false,
    onGrant: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }

  Box(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(
              Brush.linearGradient(
                  colors = listOf(
                      Color.White.copy(alpha = 0.05f),
                      Color.White.copy(alpha = 0.02f)
                  )
              )
          )
          .border(
              width = 1.dp,
              color = if (isGranted) Color(0xFF00D9FF).copy(alpha = 0.5f)
              else Color.White.copy(alpha = 0.1f),
              shape = RoundedCornerShape(20.dp)
          )
          .clickable(
              interactionSource = interactionSource,
              indication = null,
              onClick = onGrant
          )
          .padding(16.dp)
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Icon Container
      Box(
          modifier = Modifier
              .size(56.dp)
              .background(
                  if (isGranted) Color(0xFF00D9FF).copy(alpha = 0.2f)
                  else Color.White.copy(alpha = 0.05f),
                  RoundedCornerShape(16.dp)
              )
              .border(
                  1.dp,
                  if (isGranted) Color(0xFF00D9FF).copy(alpha = 0.5f)
                  else Color.White.copy(alpha = 0.1f),
                  RoundedCornerShape(16.dp)
              ),
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(28.dp)
        )
      }

      // Text Content
      Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.SemiBold
              ),
              color = Color.White
          )
          if (isMandatory) {
            Text(
                text = "REQUIRED",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF6B6B),
                modifier = Modifier
                    .background(
                        Color(0xFFFF6B6B).copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
      }

      // Status Indicator
      Box(
          modifier = Modifier
              .size(32.dp)
              .background(
                  if (isGranted) Color(0xFF00D9FF).copy(alpha = 0.2f)
                  else Color.White.copy(alpha = 0.05f),
                  CircleShape
              )
              .border(
                  1.dp,
                  if (isGranted) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.2f),
                  CircleShape
              ),
          contentAlignment = Alignment.Center
      ) {
        if (isGranted) {
          Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = Color(0xFF00D9FF),
              modifier = Modifier.size(18.dp)
          )
        } else {
          Icon(
              imageVector = Icons.Rounded.ChevronRight,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.5f),
              modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

// Modern Layout Card Component
@Composable
fun ModernLayoutCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val scale by animateFloatAsState(
      targetValue = if (isSelected) 1.02f else 1f,
      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
      label = "scale"
  )

  Box(
      modifier = Modifier
          .fillMaxWidth()
          .scale(scale)
          .clip(RoundedCornerShape(24.dp))
          .background(
              if (isSelected) Brush.linearGradient(
                  colors = listOf(
                      Color(0xFF00D9FF).copy(alpha = 0.2f),
                      Color(0xFF00D9FF).copy(alpha = 0.05f)
                  )
              ) else Brush.linearGradient(
                  colors = listOf(
                      Color.White.copy(alpha = 0.05f),
                      Color.White.copy(alpha = 0.02f)
                  )
              )
          )
          .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.1f),
              shape = RoundedCornerShape(24.dp)
          )
          .clickable(
              interactionSource = interactionSource,
              indication = null,
              onClick = onSelect
          )
          .padding(20.dp)
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Icon
      Box(
          modifier = Modifier
              .size(64.dp)
              .background(
                  if (isSelected) Color(0xFF00D9FF).copy(alpha = 0.3f)
                  else Color.White.copy(alpha = 0.05f),
                  RoundedCornerShape(18.dp)
              )
              .border(
                  1.dp,
                  if (isSelected) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.1f),
                  RoundedCornerShape(18.dp)
              ),
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(32.dp)
        )
      }

      // Text
      Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
      }

      // Selection Indicator
      if (isSelected) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFF00D9FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

// Futuristic Button Component
@Composable
fun FuturisticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
  val interactionSource = remember { MutableInteractionSource() }
  val scale by animateFloatAsState(
      targetValue = if (enabled) 1f else 0.95f,
      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
      label = "scale"
  )

  Box(
      modifier = modifier
          .height(56.dp)
          .scale(scale)
          .clip(RoundedCornerShape(16.dp))
          .background(
              if (enabled) Brush.horizontalGradient(
                  colors = listOf(
                      Color(0xFF00D9FF),
                      Color(0xFF0099CC)
                  )
              ) else Brush.horizontalGradient(
                  colors = listOf(
                      Color.White.copy(alpha = 0.1f),
                      Color.White.copy(alpha = 0.05f)
                  )
              )
          )
          .clickable(
              interactionSource = interactionSource,
              indication = null,
              enabled = enabled,
              onClick = onClick
          ),
      contentAlignment = Alignment.Center
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = if (enabled) Color.Black else Color.White.copy(alpha = 0.3f)
    )
  }
}

