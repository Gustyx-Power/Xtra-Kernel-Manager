package id.xms.xtrakernelmanager.ui.screens.setup

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    
    // Layout Selection State
    var selectedLayout by remember { mutableStateOf("material") }

    // Navigation Handler
    val canGoBack = pagerState.currentPage > 0
    BackHandler(enabled = canGoBack) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    // Animated Gradient Background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), label = "offset1"
    )
    val color1 = MaterialTheme.colorScheme.primaryContainer
    val color2 = MaterialTheme.colorScheme.tertiaryContainer
    val color3 = MaterialTheme.colorScheme.surface

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

            // Notification
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else { true }

            // Storage
            val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                true 
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
        while(true) { delay(2000); checkPermissions() }
    }

    // Simplified M3 Container
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Pager Indicator
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
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        
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
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when(page) {
                    0 -> WelcomePage(
                        onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                    1 -> PermissionsPage(
                        isRootGranted = isRootGranted,
                        isUsageGranted = isUsageGranted,
                        isNotificationGranted = isNotificationGranted,
                        isStorageGranted = isStorageGranted,
                        onCheckPermissions = { checkPermissions() },
                        onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
                        onBack = { scope.launch { pagerState.animateScrollToPage(0) } }
                    )
                    2 -> LayoutSelectionPage(
                        selectedLayout = selectedLayout,
                        onLayoutSelected = { selectedLayout = it },
                        onFinish = { onSetupComplete(selectedLayout) },
                         onBack = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                }
            }
        }
    }
}
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 48.dp), // Push down slightly from top, but overall higher than Center
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp) // Use explicit spacing instead of Center
    ) {
        
        // Bouncing Icon Animation
        val infiniteTransition = rememberInfiniteTransition(label = "bounce")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        // Glassy Icon Container
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GlassyCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.setup_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.setup_welcome_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_get_started),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
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
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onCheckPermissions() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassyCard {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = stringResource(R.string.setup_permissions_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.setup_permissions_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // High contrast
                        textAlign = TextAlign.Center
                    )
                 }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            PermissionItem(
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.setup_root_access),
                description = stringResource(R.string.setup_root_desc),
                isGranted = isRootGranted,
                onGrant = {
                     com.topjohnwu.superuser.Shell.getShell {  } 
                },
                isMandatory = true
            )

            PermissionItem(
                icon = Icons.Rounded.DataUsage,
                title = stringResource(R.string.setup_usage_access),
                description = stringResource(R.string.setup_usage_desc),
                isGranted = isUsageGranted,
                onGrant = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.setup_notifications),
                    description = stringResource(R.string.setup_notifications_desc),
                    isGranted = isNotificationGranted,
                    onGrant = {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
            
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                 PermissionItem(
                    icon = Icons.Rounded.Storage,
                    title = stringResource(R.string.setup_storage_access),
                    description = stringResource(R.string.setup_storage_desc),
                    isGranted = isStorageGranted,
                    onGrant = {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Footer
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val canProceed = isRootGranted
            
            Button(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canProceed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    contentColor = if (canProceed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (canProceed) 6.dp else 0.dp)
            ) {
                if (canProceed) {
                    Text(stringResource(R.string.setup_select), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    // Arrow removed here
                } else {
                    Text(stringResource(R.string.setup_root_required), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun LayoutSelectionPage(
    selectedLayout: String,
    onLayoutSelected: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassyCard {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = stringResource(R.string.setup_choose_style),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.setup_choose_style_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                 }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Material 3 Card
            LayoutOptionCard(
                title = stringResource(R.string.layout_material),
                description = stringResource(R.string.layout_material_desc),
                icon = Icons.Rounded.ViewQuilt,
                isSelected = selectedLayout == "material",
                onSelect = { onLayoutSelected("material") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legacy Card
            LayoutOptionCard(
                title = stringResource(R.string.layout_legacy),
                description = stringResource(R.string.layout_legacy_desc),
                icon = Icons.Rounded.BlurOn,
                isSelected = selectedLayout == "legacy",
                onSelect = { onLayoutSelected("legacy") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
             AnimatedContent(targetState = selectedLayout, label = "Button Text") { layout -> 
                 val layoutName = if(layout == "material") stringResource(R.string.layout_material) else stringResource(R.string.layout_legacy)
                 Text(
                    text = stringResource(R.string.setup_continue_with, layoutName),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
             }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.setup_root_note),
             style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Replaced "Glassy" with standard M3 Surface Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh, 
                RoundedCornerShape(24.dp)
            )
            .padding(24.dp), // Inner padding
        content = content
    )
}

@Composable
fun LayoutOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Glassy Surface for Options too
    val containerColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
    else 
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)

    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        interactionSource = interactionSource,
         elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
         Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
             Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                 Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                 Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
     // Glassy Permission Item
    Card(
        onClick = { onGrant() }, // Allow clicking even if granted (to re-check or view settings)
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // M3 Shape Container
            val shapeColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .drawWithCache {
                        val roundedPolygon = RoundedPolygon.star(
                            numVerticesPerRadius = 12, // Scallop/Cookie shape
                            radius = size.minDimension / 2,
                            innerRadius = 0.7f,
                            rounding = androidx.graphics.shapes.CornerRounding(
                                radius = size.minDimension * 0.1f,
                                smoothing = 0.5f
                            )
                        )
                        val roundedPolygonPath = roundedPolygon.toPath().asComposePath()
                        onDrawBehind {
                             drawPath(
                                path = roundedPolygonPath,
                                color = shapeColor,
                                style = Fill
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                // Granted State
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Allowed", // Or use a resource string
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Grant Button
                FilledTonalButton(
                    onClick = onGrant,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        stringResource(R.string.setup_grant),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
