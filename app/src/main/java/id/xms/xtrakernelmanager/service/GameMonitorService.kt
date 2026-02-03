package id.xms.xtrakernelmanager.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.*
import org.json.JSONArray

class GameMonitorService : AccessibilityService() {

  companion object {
    private const val TAG = "GameMonitorService"
    private const val CHANNEL_ID = "game_monitor_channel"
    private const val NOTIFICATION_ID = 2001
  }

  private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private lateinit var preferencesManager: PreferencesManager
  private var enabledGamePackages: Set<String> = emptySet()

  // Cache last package to avoid redundant checks/logs
  private var lastPackageName: String = ""

  override fun onServiceConnected() {
    super.onServiceConnected()
    Log.d(TAG, "Accessibility Service Connected")
    
    // CRITICAL: Start foreground immediately to prevent crash
    createNotificationChannel()
    startForegroundImmediately()
    
    preferencesManager = PreferencesManager(applicationContext)

    // Load initial games list
    serviceScope.launch { loadGameList() }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Game Monitor Service",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Monitors game apps for automatic overlay activation"
        setShowBadge(false)
        setSound(null, null)
      }
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun startForegroundImmediately() {
    try {
      val notification = createNotification()
      // Always call startForeground for AccessibilityService to prevent crashes
      startForeground(NOTIFICATION_ID, notification)
      Log.d(TAG, "Started foreground service successfully")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start foreground", e)
    }
  }

  private fun createNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Game Monitor Active")
      .setContentText("Monitoring for game apps")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setOngoing(true)
      .setSilent(true)
      .build()
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
        when (item) {
          is String -> {
            // Old format: simple string
            packages.add(item)
          }
          is org.json.JSONObject -> {
            // New format: JSON object with enabled flag
            val enabled = item.optBoolean("enabled", true)
            if (enabled) {
              packages.add(item.optString("packageName"))
            }
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
    
    // Try to restart service if killed by system
    try {
      val restartIntent = Intent(applicationContext, GameMonitorService::class.java)
      restartIntent.setPackage(packageName)
      sendBroadcast(restartIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send restart broadcast", e)
    }
  }
  
  override fun onUnbind(intent: Intent?): Boolean {
    Log.d(TAG, "Service Unbound - attempting to stay alive")
    // Return true to indicate we want onRebind to be called
    return true
  }
  
  override fun onRebind(intent: Intent?) {
    super.onRebind(intent)
    Log.d(TAG, "Service Rebound")
    startForegroundImmediately()
  }
}
