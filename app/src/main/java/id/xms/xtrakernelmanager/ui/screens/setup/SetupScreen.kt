package id.xms.xtrakernelmanager.ui.screens.setup

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xms.xtrakernelmanager.data.repository.PowerRepository
import id.xms.xtrakernelmanager.domain.root.RootManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isRootGranted by remember { mutableStateOf(false) }
    var isUsageStatsGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(false) }
    var isStorageGranted by remember { mutableStateOf(false) } // Optional/Basic
    
    var isChecking by remember { mutableStateOf(true) }

    // Check permissions function
    fun checkPermissions() {
        scope.launch(Dispatchers.IO) {
            // Root Check
            val root = RootManager.isRootAvailable()
            
            // Usage Stats Check
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            val usage = mode == AppOpsManager.MODE_ALLOWED

            // Notification Check (A13+)
            val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Granted by default < 13
            }
            
            // Storage (Basic Check) - Actually XKM mostly uses Root for sys edits. 
            // External storage is mostly for backups.
            // Let's check READ_EXTERNAL_STORAGE just in case, or assume granted if < 11?
            // Modern Android uses Scoped Storage. checking READ_MEDIA_IMAGES etc?
            // Let's stick to Notification & Usage & Root as primary "system" ones.
            // But let's check standard READ_EXTERNAL for legacy.
            val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                true // Too complex to handle granular media. Skip for setup.
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }

            withContext(Dispatchers.Main) {
                isRootGranted = root
                isUsageStatsGranted = usage
                isNotificationGranted = notif
                isStorageGranted = storage
                isChecking = false
            }
        }
    }

    // Loop Check
    LaunchedEffect(Unit) {
        while (true) {
            checkPermissions()
            delay(2000) // Poll every 2s
        }
    }
    
    // Resume Check
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

    // Launchers
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { checkPermissions() }
    
    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { checkPermissions() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Footer Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onSetupComplete,
                    enabled = isRootGranted, // Root is mandatory
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    if (isRootGranted) {
                        Text("Start XKM", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, null)
                    } else {
                        Text("Root Required", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            // Header Logo/Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.SettingsSuggest, 
                    null, 
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Welcome to XKM",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Setup necessary permissions to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            // Permission Cards
            PermissionItem(
                title = "Root Access",
                description = "Required for kernel tuning and system modifications.",
                icon = Icons.Rounded.Security,
                isGranted = isRootGranted,
                isChecking = isChecking,
                onClick = { 
                    // Trigger root request
                     scope.launch(Dispatchers.IO) { RootManager.isRootAvailable() }
                }
            )

            PermissionItem(
                title = "Usage Access",
                description = "Required for Power Insight (SOT & Drain Stats).",
                icon = Icons.Rounded.DataUsage,
                isGranted = isUsageStatsGranted,
                isChecking = isChecking,
                onClick = {
                     val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                     context.startActivity(intent)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    title = "Notifications",
                    description = "Required for profile alerts and applied services.",
                    icon = Icons.Rounded.Notifications,
                    isGranted = isNotificationGranted,
                    isChecking = isChecking,
                    onClick = {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                 PermissionItem(
                    title = "Storage",
                    description = "Required for config backups and flashing.",
                    icon = Icons.Rounded.Storage,
                    isGranted = isStorageGranted,
                    isChecking = isChecking,
                    onClick = {
                        storageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                )
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isChecking: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isGranted) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
    else 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        
    val borderColor = if (isGranted) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = { if (!isGranted) onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = if(isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            if (isGranted) {
                Icon(
                    Icons.Rounded.CheckCircle, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                 Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Grant", fontSize = 12.sp)
                }
            }
        }
    }
}
