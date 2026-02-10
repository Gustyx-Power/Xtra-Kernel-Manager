package id.xms.xtrakernelmanager.ui.splash

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.xms.xtrakernelmanager.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.max

// --- DATA CLASSES ---
data class UpdateConfig(
    val version: String = "",
    val changelog: String = "",
    val url: String = "",
    val force: Boolean = false,
)

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

// --- HELPER FUNCTIONS ---

/**
 * Check if the device has root access using libsu
 */
suspend fun checkRootAccess(): Boolean =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            // Check debug bypass first
            val model = android.os.Build.MODEL
            val isDebugBuild = BuildConfig.DEBUG
            if (isDebugBuild && model == "I2219") {
                Log.d("RootCheck", "Debug bypass active for Vivo I2219")
                return@withContext true
            }

            // Use libsu Shell which properly handles Magisk 28+ root requests
            val shell = com.topjohnwu.superuser.Shell.getShell()
            val isRoot = shell.isRoot
            Log.d("RootCheck", "Root check via libsu: $isRoot")
            isRoot
        } catch (e: Exception) {
            Log.e("RootCheck", "Root check failed: ${e.message}")
            false
        }
    }

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
    val database = FirebaseDatabase.getInstance(
        "https://xtrakernelmanager-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    val myRef = database.getReference("update")
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val versionRaw = snapshot.child("version").value
                val version = versionRaw?.toString() ?: ""
                val changelog = snapshot.child("changelog").getValue(String::class.java) ?: ""
                val url = snapshot.child("url").getValue(String::class.java) ?: ""
                val force = snapshot.child("force").getValue(Boolean::class.java) ?: false
                if (continuation.isActive)
                    continuation.resume(UpdateConfig(version, changelog, url, force))
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            if (continuation.isActive) continuation.resume(null)
        }
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
                return true // Remote lebih tinggi (2.1 > 2.0)
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
        lowerSuffix.startsWith("release") -> 100 // Release adalah yang tertinggi
        lowerSuffix.startsWith("stable") -> 90 // Stable setara release
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