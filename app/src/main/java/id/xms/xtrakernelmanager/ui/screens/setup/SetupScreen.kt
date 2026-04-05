package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupScreen(onSetupComplete: (String) -> Unit) {
  val pagerState = rememberPagerState(pageCount = { 7 })
  val scope = rememberCoroutineScope()
  val context = androidx.compose.ui.platform.LocalContext.current
  
  // Default to "classic" for Android 8 and 9 (API < 29), "material" for Android 10+
  val defaultLayout = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) "classic" else "material"
  var selectedLayout by remember { mutableStateOf(defaultLayout) }
  
  var hasRootAccess by remember { mutableStateOf(false) }
  var hasAccessibilityPermission by remember { mutableStateOf(false) }
  var hasUsagePermission by remember { mutableStateOf(false) }
  var hasOverlayPermission by remember { mutableStateOf(false) }
  var hasBatteryOptimization by remember { mutableStateOf(false) }
  var hasNotificationPermission by remember { mutableStateOf(false) }
  
  val allPermissionsGranted = hasRootAccess && 
    hasUsagePermission && 
    hasOverlayPermission && 
    hasBatteryOptimization && 
    hasNotificationPermission
    
  val isAndroid10Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F1419))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.systemBars)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(id = R.drawable.logo_a),
          contentDescription = "XKM Logo",
          modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.app_name_short),
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f),
        userScrollEnabled = false
      ) { page ->
        when (page) {
          0 -> WelcomePage()
          1 -> PerformancePage()
          2 -> BatteryPage()
          3 -> GamingPage()
          4 -> PermissionsPage(
            hasRootAccess = hasRootAccess,
            hasAccessibilityPermission = hasAccessibilityPermission,
            hasUsagePermission = hasUsagePermission,
            hasOverlayPermission = hasOverlayPermission,
            hasBatteryOptimization = hasBatteryOptimization,
            hasNotificationPermission = hasNotificationPermission,
            onRootCheck = { hasRootAccess = it },
            onAccessibilityClick = { hasAccessibilityPermission = it },
            onUsageClick = { hasUsagePermission = it },
            onOverlayClick = { hasOverlayPermission = it },
            onBatteryClick = { hasBatteryOptimization = it },
            onNotificationClick = { hasNotificationPermission = it }
          )
          5 -> StyleSelectionPage(
            selectedStyle = selectedLayout,
            isAndroid10Plus = isAndroid10Plus,
            onStyleSelected = { selectedLayout = it }
          )
          6 -> CompletionPage(
            onComplete = { onSetupComplete(selectedLayout) }
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (pagerState.currentPage < pagerState.pageCount - 1) {
          SetupPageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = pagerState.pageCount,
            modifier = Modifier.padding(bottom = 24.dp)
          )

          Button(
            onClick = {
              scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF38BDF8)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = when (pagerState.currentPage) {
              4 -> allPermissionsGranted // Page 4: Permissions must be granted
              5 -> {
                // Page 5: Style selection - validate based on Android version
                if (isAndroid10Plus) {
                  // Android 10+ can choose any style
                  true
                } else {
                  // Android 8-9 must choose "classic"
                  selectedLayout == "classic"
                }
              }
              else -> true
            }
          ) {
            Text(
              text = stringResource(R.string.setup_continue),
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
