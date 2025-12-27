package id.xms.xtrakernelmanager.ui.screens.setup

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.topjohnwu.superuser.Shell
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.repository.PowerRepository
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Permission States
    var isRootGranted by remember { mutableStateOf(false) }
    var isUsageGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(false) }
    var isStorageGranted by remember { mutableStateOf(false) }

    // Check Permissions Function
    fun checkPermissions() {
        scope.launch(Dispatchers.IO) {
            val root = RootManager.isRootAvailable()
            
            // Usage Access
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            val usage = mode == AppOpsManager.MODE_ALLOWED

            // Notification (Android 13+)
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true 
            }

            // Storage
            val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                true // No longer needed for scoped storage generally, or use READ_MEDIA_*
            } else {
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }

            withContext(Dispatchers.Main) {
                isRootGranted = root
                isUsageGranted = usage
                isNotificationGranted = notification
                isStorageGranted = storage
            }
        }
    }

    // Lifecycle Observer to re-check on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Initial and Periodic Check
    LaunchedEffect(Unit) {
        checkPermissions()
        while(true) {
            delay(2000)
            checkPermissions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Top Bar / Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == iteration) 24.dp else 12.dp, 8.dp)
                            .clip(CircleShape)
                            .background(color)
                            .animateContentSize()
                    )
                }
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            userScrollEnabled = false // Force use of buttons? Or allow scroll? Let's disable to enforce flow.
        ) { page ->
            when(page) {
                0 -> WelcomePage(
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> PermissionsPage(
                    isRootGranted = isRootGranted,
                    isUsageGranted = isUsageGranted,
                    isNotificationGranted = isNotificationGranted,
                    isStorageGranted = isStorageGranted,
                    onCheckPermissions = { checkPermissions() },
                    onFinish = onSetupComplete
                )
            }
        }
    }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Reduced horizontal padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Icon or Illustrationy
        Box(
            modifier = Modifier
                .size(160.dp) // Increased size for "fuller" look
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(80.dp), // Increased icon size
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to XKM",
            style = MaterialTheme.typography.headlineLarge, // Larger headline
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "The ultimate kernel manager for focused performance and deep system control.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp), // Slightly more compact height
            shape = RoundedCornerShape(12.dp) // Slightly tighter corners
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ArrowForward, null)
        }
    }
}

@Composable
fun PermissionsPage(
    isRootGranted: Boolean,
    isUsageGranted: Boolean,
    isNotificationGranted: Boolean,
    isStorageGranted: Boolean,
    onCheckPermissions: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Launchers
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onCheckPermissions() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp), // Tighter padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp) // Reduced bottom padding
        )
        
        Text(
            text = "We need a few permissions to unleash the full power of your kernel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp) // Reduced spacing
        )

        // Root Access (Mandatory)
        PermissionItem(
            icon = Icons.Rounded.Security,
            title = "Root Access",
            description = "Required to modify kernel settings.",
            isGranted = isRootGranted,
            onGrant = {
                 com.topjohnwu.superuser.Shell.getShell {  } 
            },
            isMandatory = true
        )

        // Usage Access
        PermissionItem(
            icon = Icons.Rounded.DataUsage,
            title = "Usage Access",
            description = "Required to track screen-on time and per-app profiles.",
            isGranted = isUsageGranted,
            onGrant = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        )

        // Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                description = "To notify you about applied profiles.",
                isGranted = isNotificationGranted,
                onGrant = {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        // Storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
             PermissionItem(
                icon = Icons.Rounded.Storage,
                title = "Storage Access",
                description = "To backup and restore configurations.",
                isGranted = isStorageGranted,
                onGrant = {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Reduced spacer

        // Finish Button
        val canProceed = isRootGranted 
        
        Button(
            onClick = onFinish,
            enabled = canProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp) // Compact height
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canProceed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (canProceed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (canProceed) {
                Text("Start XKM", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Rounded.Check, null)
            } else {
                Text("Root Access Required", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    isMandatory: Boolean = false
) {
    Card(
        onClick = { if (!isGranted) onGrant() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduced vertical margin
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surfaceContainerHighest // Use clearer surface variant
        ),
        border = if (!isGranted && isMandatory) 
            ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.error)) 
        else null,
        enabled = !isGranted,
        shape = RoundedCornerShape(16.dp) // Consistent shape
    ) {
        Row(
            modifier = Modifier.padding(12.dp), // Reduced inner padding (was 16.dp)
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp) // Slightly smaller icon box
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp) // Smaller icon inside
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp // Tighter line height
                )
            }

            if (!isGranted) {
                 // Use filled tonal button for "Grant" for better visibility
                FilledTonalButton(
                    onClick = onGrant,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (title == "Root Access" || title == "Storage Access") "Grant" else "Allow",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
