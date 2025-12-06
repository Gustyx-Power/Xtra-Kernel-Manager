package id.xms.xtrakernelmanager.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.max

// --- PREFERENCES MANAGER (Untuk menyimpan info update) ---
object UpdatePrefs {
    private const val PREF_NAME = "update_prefs"
    private const val KEY_PENDING_VERSION = "pending_version"
    private const val KEY_UPDATE_URL = "update_url"
    private const val KEY_CHANGELOG = "update_changelog"

    fun savePendingUpdate(context: Context, version: String, url: String, changelog: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_PENDING_VERSION, version)
            putString(KEY_UPDATE_URL, url)
            putString(KEY_CHANGELOG, changelog)
            apply()
        }
    }

    fun getPendingUpdate(context: Context): UpdateConfig? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val version = prefs.getString(KEY_PENDING_VERSION, null) ?: return null
        val url = prefs.getString(KEY_UPDATE_URL, "") ?: ""
        val changelog = prefs.getString(KEY_CHANGELOG, "") ?: ""
        return UpdateConfig(version, changelog, url, force = true) // Diasumsikan force jika tersimpan
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

data class UpdateConfig(
    val version: String = "",
    val changelog: String = "",
    val url: String = "",
    val force: Boolean = false
)

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            XtraKernelManagerTheme {
                SplashScreenContent(
                    onNavigateToMain = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreenContent(onNavigateToMain: () -> Unit) {
    val context = LocalContext.current

    var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showOfflineLockDialog by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }
    var startExitAnimation by remember { mutableStateOf(false) }

    // --- LOGIC UTAMA (ANTI-LOOPHOLE) ---
    LaunchedEffect(Unit) {
        val minSplashTime = launch { delay(2000) }

        // 1. CEK DATA LOKAL DULU: Apakah ada hutang update?
        val pendingUpdate = UpdatePrefs.getPendingUpdate(context)
        
        if (pendingUpdate != null && isUpdateAvailable(BuildConfig.VERSION_NAME, pendingUpdate.version)) {
            if (isInternetAvailable(context)) {
                minSplashTime.join()
                updateConfig = pendingUpdate
                isChecking = false
                showUpdateDialog = true
                
                val freshConfig = withTimeoutOrNull(3000L) { fetchUpdateConfig() }
                if (freshConfig != null) {
                    // Update info dialog dengan data terbaru
                    updateConfig = freshConfig
                    // Update penyimpanan lokal
                    UpdatePrefs.savePendingUpdate(context, freshConfig.version, freshConfig.url, freshConfig.changelog)
                }
            } else {
                minSplashTime.join()
                isChecking = false
                showOfflineLockDialog = true
            }
        } else {
            if (pendingUpdate != null) {
                UpdatePrefs.clear(context)
            }

            // 2. PROSES NORMAL: Cek Firebase baru
            if (isInternetAvailable(context)) {
                try {
                    val config = withTimeoutOrNull(5000L) { fetchUpdateConfig() }
                    minSplashTime.join()

                    if (config != null && isUpdateAvailable(BuildConfig.VERSION_NAME, config.version)) {
                        // Update Ditemukan!
                        UpdatePrefs.savePendingUpdate(context, config.version, config.url, config.changelog)
                        
                        updateConfig = config
                        isChecking = false
                        showUpdateDialog = true
                    } else {
                        isChecking = false
                        startExitAnimation = true
                    }
                } catch (e: Exception) {
                    minSplashTime.join()
                    isChecking = false
                    startExitAnimation = true
                }
            } else {
                minSplashTime.join()
                isChecking = false
                startExitAnimation = true
            }
        }
    }

    if (startExitAnimation) {
        LaunchedEffect(Unit) {
            delay(500)
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainerLowest))),
        contentAlignment = Alignment.Center
    ) {
        BackgroundCircles()

        AnimatedVisibility(
            visible = !startExitAnimation,
            enter = fadeIn(),
            exit = fadeOut() + scaleOut(targetScale = 1.5f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = R.drawable.logo_a), contentDescription = "Logo", modifier = Modifier.size(80.dp).scale(1.2f))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Xtra Kernel Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(48.dp))
                if (isChecking) {
                    ModernLoader()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Initializing...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (showUpdateDialog && updateConfig != null) {
            ForceUpdateDialog(
                config = updateConfig!!,
                onUpdateClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateConfig!!.url))
                        context.startActivity(intent)
                    } catch (e: Exception) { Log.e("OTA", "Browser error", e) }
                }
            )
        }

        if (showOfflineLockDialog) {
            OfflineLockDialog(
                onRetry = {
                    val intent = (context as ComponentActivity).intent
                    context.finish()
                    context.startActivity(intent)
                }
            )
        }
    }
}

// --- HELPERS ---

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

suspend fun fetchUpdateConfig(): UpdateConfig? = suspendCancellableCoroutine { continuation ->
    val database = Firebase.database("https://xtrakernelmanager-default-rtdb.asia-southeast1.firebasedatabase.app")
    val myRef = database.getReference("update")
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val versionRaw = snapshot.child("version").value
                val version = versionRaw?.toString() ?: ""
                val changelog = snapshot.child("changelog").getValue(String::class.java) ?: ""
                val url = snapshot.child("url").getValue(String::class.java) ?: ""
                val force = snapshot.child("force").getValue(Boolean::class.java) ?: false
                if (continuation.isActive) continuation.resume(UpdateConfig(version, changelog, url, force))
            } catch (e: Exception) { if (continuation.isActive) continuation.resume(null) }
        }
        override fun onCancelled(error: DatabaseError) { if (continuation.isActive) continuation.resume(null) }
    }
    myRef.addListenerForSingleValueEvent(listener)
    continuation.invokeOnCancellation { myRef.removeEventListener(listener) }
}

fun isUpdateAvailable(currentVersion: String, remoteVersion: String): Boolean {
    return try {
        Log.d("OTA", "Comparing versions: current=$currentVersion, remote=$remoteVersion")
        
        // Parse base version (sebelum tanda -)
        val currentBase = currentVersion.substringBefore("-").trim()
        val remoteBase = remoteVersion.substringBefore("-").trim()

        // Parse suffix (setelah tanda -)
        val currentSuffix = if (currentVersion.contains("-")) currentVersion.substringAfter("-").trim() else ""
        val remoteSuffix = if (remoteVersion.contains("-")) remoteVersion.substringAfter("-").trim() else ""

        // Bersihkan dan bandingkan base version (2.0, 2.1, dll)
        val cleanCurrent = currentBase.replace(Regex("[^0-9.]"), "")
        val cleanRemote = remoteBase.replace(Regex("[^0-9.]"), "")
        val cParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val rParts = cleanRemote.split(".").map { it.toIntOrNull() ?: 0 }
        val length = max(cParts.size, rParts.size)

        // Bandingkan base version
        for (i in 0 until length) {
            val c = cParts.getOrElse(i) { 0 }
            val r = rParts.getOrElse(i) { 0 }
            if (r > c) {
                Log.d("OTA", "Remote base version is higher: $r > $c")
                return true  // Remote lebih tinggi (2.1 > 2.0)
            }
            if (r < c) {
                Log.d("OTA", "Current base version is higher: $c > $r")
                return false // Current lebih tinggi (2.1 > 2.0)
            }
        }

        // Jika base version sama, bandingkan suffix
        Log.d("OTA", "Base versions equal, comparing suffixes: current='$currentSuffix', remote='$remoteSuffix'")
        
        // Get priorities for both suffixes
        val currentPriority = getSuffixPriority(currentSuffix)
        val remotePriority = getSuffixPriority(remoteSuffix)
        
        Log.d("OTA", "Suffix priorities: current=$currentPriority, remote=$remotePriority")
        
        // Hanya update available jika remote priority lebih tinggi
        val result = remotePriority > currentPriority
        Log.d("OTA", "Update available: $result")
        result
    } catch (e: Exception) {
        Log.e("OTA", "Error comparing versions: $currentVersion vs $remoteVersion", e)
        false
    }
}

/**
 * Mendapatkan priority suffix untuk perbandingan versi.
 * Urutan prioritas: (tanpa suffix/stable) < Alpha < Beta < RC < Release
 * Semakin tinggi angka = semakin baru/stabil
 */
fun getSuffixPriority(suffix: String): Int {
    if (suffix.isEmpty()) return 50 // Versi tanpa suffix (2.0) = stable release
    
    val lowerSuffix = suffix.lowercase()
    
    return when {
        lowerSuffix.startsWith("release") -> 100  // Release adalah yang tertinggi
        lowerSuffix.startsWith("stable") -> 90   // Stable setara release
        lowerSuffix.startsWith("rc") -> {
            // RC dengan nomor: RC1 = 40, RC2 = 41, dst
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            40 + num
        }
        lowerSuffix.startsWith("beta") -> {
            // Beta dengan nomor: Beta1 = 20, Beta2 = 21, dst
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            20 + num
        }
        lowerSuffix.startsWith("alpha") -> {
            // Alpha dengan nomor: Alpha1 = 10, Alpha2 = 11, dst
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            10 + num
        }
        else -> 0 // Unknown suffix = lowest priority
    }
}

/**
 * Membandingkan suffix versi (Beta1, Beta2, RC1, Alpha1, Release, dll)
 * @return true jika remote suffix lebih baru dari current suffix
 * @deprecated Use getSuffixPriority instead for more accurate comparison
 */
fun compareSuffix(currentSuffix: String, remoteSuffix: String): Boolean {
    return getSuffixPriority(remoteSuffix) > getSuffixPriority(currentSuffix)
}

// --- UI COMPONENTS ---

@Composable
fun ForceUpdateDialog(config: UpdateConfig, onUpdateClick: () -> Unit) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.CloudDownload, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Update Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("New Version: ${config.version}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SystemUpdate, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Changelog", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(config.changelog, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onUpdateClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Update Now")
                }
            }
        }
    }
}

@Composable
fun OfflineLockDialog(onRetry: () -> Unit) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.WifiOff, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connection Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "A pending update was detected previously. You must enable internet connection to update the app before proceeding.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Retry Connection")
                }
            }
        }
    }
}

@Composable
fun ModernLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "rotation")
    val brushColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(48.dp)) {
        drawArc(brush = Brush.sweepGradient(listOf(Color.Transparent, Color.Transparent, brushColor)), startAngle = rotation, sweepAngle = 200f, useCenter = false, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun BackgroundCircles() {
    val color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = color, radius = size.width * 0.6f, center = androidx.compose.ui.geometry.Offset(size.width, 0f))
        drawCircle(color = color.copy(alpha = 0.1f), radius = size.width * 0.4f, center = androidx.compose.ui.geometry.Offset(0f, size.height))
    }
}
