package id.xms.xtrakernelmanager.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.*
import org.json.JSONArray

class GameMonitorService : AccessibilityService() {

  companion object {
    private const val TAG = "GameMonitorService"
  }

  private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private lateinit var preferencesManager: PreferencesManager
  private var enabledGamePackages: Set<String> = emptySet()

  // Cache last package to avoid redundant checks/logs
  private var lastPackageName: String = ""

  override fun onServiceConnected() {
    super.onServiceConnected()
    Log.d(TAG, "Accessibility Service Connected")
    preferencesManager = PreferencesManager(applicationContext)

    // Load initial games list
    serviceScope.launch { loadGameList() }
  }

  private var stopJob: Job? = null

  // Whitelist of system packages that shouldn't unwantedly kill the overlay
  private val ignoredPackages =
      setOf(
          "com.android.systemui",
          "android",
          "com.google.android.inputmethod.latin", // Gboard
          "com.google.android.permissioncontroller",
          "com.android.permissioncontroller",
          "com.google.android.packageinstaller",
          "com.android.packageinstaller",
          "com.google.android.gms",
          "com.google.android.play.games",
          "com.android.vending",
          "com.google.android.webview",
          "com.xiaomi.xmsf", // Xiaomi service framework
          "com.miui.securitycenter", // MIUI Security
          "id.xms.xtrakernelmanager", // Self
          "id.xms.xtrakernelmanager.dev", // Self (debug)
      )

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null) return

    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      val packageName = event.packageName?.toString() ?: return

      if (packageName == lastPackageName) return
      lastPackageName = packageName

      Log.d(TAG, "Window changed: $packageName")

      if (enabledGamePackages.contains(packageName)) {
        Log.d(TAG, "Game detected: $packageName. Ensuring Overlay is ON.")
        stopJob?.cancel()
        stopJob = null
        startGameOverlay()
      } else if (ignoredPackages.contains(packageName) || packageName.contains("inputmethod")) {
        // Ignore system UI, keyboards, Google services
        Log.d(TAG, "Ignoring system/transient package: $packageName")
        stopJob?.cancel()
        stopJob = null
      } else {
        // Potential exit - Stopping immediately as requested
        Log.d(TAG, "Non-game package detected: $packageName. Stopping overlay.")

        stopJob?.cancel()
        stopJob = null
        stopGameOverlay()
      }
    }
  }

  override fun onInterrupt() {
    Log.d(TAG, "Accessibility Service Interrupted")
  }

  private suspend fun loadGameList() {
    try {
      preferencesManager.getGameApps().collect { json ->
        enabledGamePackages = parseEnabledGamePackages(json)
        Log.d(TAG, "Updated game list: ${enabledGamePackages.size} games")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error collecting game list", e)
    }
  }

  private fun parseEnabledGamePackages(json: String): Set<String> {
    return try {
      val jsonArray = JSONArray(json)
      val packages = mutableSetOf<String>()
      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.opt(i)
        if (item is String) {
          packages.add(item)
        } else if (item is org.json.JSONObject) {
          val enabled = item.optBoolean("enabled", true)
          if (enabled) {
            packages.add(item.optString("packageName"))
          }
        }
      }
      packages
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse game apps: ${e.message}")
      emptySet()
    }
  }

  private fun startGameOverlay() {
    try {
      val intent = Intent(applicationContext, GameOverlayService::class.java)
      startService(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start overlay service", e)
    }
  }

  private fun stopGameOverlay() {
    try {
      val intent = Intent(applicationContext, GameOverlayService::class.java)
      stopService(intent)
    } catch (e: Exception) {
      // Ignore if not running
      Log.e(TAG, "Failed to stop overlay service", e)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    stopGameOverlay()
    Log.d(TAG, "Service Destroyed")
  }
}
