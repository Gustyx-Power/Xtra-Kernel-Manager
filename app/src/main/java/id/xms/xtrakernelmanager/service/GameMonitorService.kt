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
  private val handler = android.os.Handler(android.os.Looper.getMainLooper())
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
    serviceScope.launch { 
      loadGameList()
    }
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null) return

    val packageName = event.packageName?.toString() ?: return
    
    // Avoid redundant processing
    if (packageName == lastPackageName) return
    lastPackageName = packageName

    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      Log.d(TAG, "Window changed: $packageName")

      // Check if it's a game app
      if (enabledGamePackages.contains(packageName)) {
        Log.d(TAG, "Game detected: $packageName")
        startGameOverlay()
      } else {
        // Other apps - stop overlay
        stopGameOverlay()
      }
    }
  }

  override fun onInterrupt() {
    Log.d(TAG, "Accessibility Service Interrupted")
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "Accessibility Service Destroyed")
    serviceScope.cancel()
  }

  private fun createNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("XKM Game Monitor")
      .setContentText("Monitoring for game apps")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setOngoing(true)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Game Monitor Service",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Monitors for game applications"
        setShowBadge(false)
      }
      
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun startForegroundImmediately() {
    val notification = createNotification()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
    Log.d(TAG, "Started foreground service")
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
      Log.e(TAG, "Failed to stop overlay service", e)
    }
  }

  private suspend fun loadGameList() {
    try {
      preferencesManager.getGameApps().collect { json ->
        enabledGamePackages = parseEnabledGamePackages(json)
        Log.d(TAG, "Updated game list: ${enabledGamePackages.size} games")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error loading game list", e)
    }
  }

  private fun parseEnabledGamePackages(json: String): Set<String> {
    return try {
      val jsonArray = JSONArray(json)
      val packages = mutableSetOf<String>()
      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.opt(i)
        when (item) {
          is String -> packages.add(item)
          else -> {
            val obj = item as? org.json.JSONObject
            val packageName = obj?.optString("packageName")
            val enabled = obj?.optBoolean("enabled", false) ?: false
            if (!packageName.isNullOrEmpty() && enabled) {
              packages.add(packageName)
            }
          }
        }
      }
      packages
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing game packages", e)
      emptySet()
    }
  }
}