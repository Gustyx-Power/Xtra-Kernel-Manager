package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.RAMControlUseCase
import id.xms.xtrakernelmanager.service.AppProfileService
import id.xms.xtrakernelmanager.service.BatteryInfoService
import id.xms.xtrakernelmanager.service.GameMonitorService
import id.xms.xtrakernelmanager.utils.AccessibilityServiceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

class BootReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "BootReceiver"
    private const val BATTERY_SERVICE_DELAY_MS = 15000L // 15 seconds delay for Android 15+
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      Log.d(TAG, "Boot completed received")

      val pendingResult = goAsync()

      CoroutineScope(Dispatchers.IO).launch {
        try {
          // Start BatteryInfoService if enabled (with delay for Android 15+)
          startBatteryServiceIfEnabled(context)

          // Start AppProfileService if per-app profiles are enabled
          startAppProfileServiceIfEnabled(context)

          // Start GameMonitorService if there are enabled games
          startGameMonitorServiceIfEnabled(context)

          // Apply RAM configuration (ZRAM, Swap, VM params)
          applyRamConfigOnBoot(context)

          // Apply CPU configuration if enabled
          applyCpuConfigOnBoot(context)

          // Apply I/O scheduler configuration if enabled
          applyIOConfigOnBoot(context)

          // Apply TCP congestion configuration if enabled
          applyTCPConfigOnBoot(context)

          // Apply additional RAM configuration if enabled
          applyAdditionalRAMConfigOnBoot(context)
        } finally {
          pendingResult.finish()
        }
      }
    }
  }

  private suspend fun startBatteryServiceIfEnabled(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val showBatteryNotif = preferencesManager.isShowBatteryNotif().first()

      if (showBatteryNotif) {
        Log.d(TAG, "Battery notification enabled, scheduling service start...")

        // For Android 15+ (API 35+), dataSync FGS cannot start directly from BOOT_COMPLETED
        // We need to delay the start to avoid ForegroundServiceStartNotAllowedException
        if (Build.VERSION.SDK_INT >= 35) {
          Log.d(TAG, "Android 15+ detected, using delayed start (${BATTERY_SERVICE_DELAY_MS}ms)")
          scheduleDelayedServiceStart(context)
        } else {
          // For older versions, start immediately
          startBatteryServiceDirect(context)
        }
      } else {
        Log.d(TAG, "Battery notification disabled, skipping service start")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start BatteryInfoService: ${e.message}")
    }
  }

  private fun scheduleDelayedServiceStart(context: Context) {
    // Use Handler with delay for more reliable start on Android 15+
    Handler(Looper.getMainLooper())
        .postDelayed(
            {
              try {
                CoroutineScope(Dispatchers.Main).launch {
                  try {
                    val preferencesManager = PreferencesManager(context)
                    val showBatteryNotif = preferencesManager.isShowBatteryNotif().first()

                    if (showBatteryNotif) {
                      startBatteryServiceDirect(context)
                    }
                  } catch (e: Exception) {
                    Log.e(TAG, "Delayed service start failed: ${e.message}")
                  }
                }
              } catch (e: Exception) {
                Log.e(TAG, "Handler post failed: ${e.message}")
              }
            },
            BATTERY_SERVICE_DELAY_MS,
        )
  }

  private fun startBatteryServiceDirect(context: Context) {
    try {
      val serviceIntent = Intent(context, BatteryInfoService::class.java)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
      } else {
        context.startService(serviceIntent)
      }
      Log.d(TAG, "BatteryInfoService started successfully")
    } catch (e: android.app.ForegroundServiceStartNotAllowedException) {
      // Android 15+ has stricter restrictions - this should be caught now
      Log.w(TAG, "ForegroundService not allowed: ${e.message}")
      Log.w(TAG, "Service will be started when user opens the app")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start BatteryInfoService: ${e.message}")
    }
  }

  private suspend fun startAppProfileServiceIfEnabled(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val profilesJson = preferencesManager.getAppProfiles().first()

      // Check if there are any profiles registered
      val hasProfiles =
          try {
            val jsonArray = JSONArray(profilesJson)
            jsonArray.length() > 0
          } catch (e: Exception) {
            false
          }

      if (hasProfiles) {
        Log.d(TAG, "Profiles found, starting AppProfileService...")

        // For Android 15+ (API 35+), use delayed start
        if (Build.VERSION.SDK_INT >= 35) {
          Log.d(TAG, "Android 15+ detected, using delayed start for AppProfileService")
          Handler(Looper.getMainLooper())
              .postDelayed({ startAppProfileServiceDirect(context) }, BATTERY_SERVICE_DELAY_MS)
        } else {
          startAppProfileServiceDirect(context)
        }
      } else {
        Log.d(TAG, "No profiles registered, skipping AppProfileService start")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start AppProfileService: ${e.message}")
    }
  }

  private fun startAppProfileServiceDirect(context: Context) {
    try {
      val serviceIntent = Intent(context, AppProfileService::class.java)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
      } else {
        context.startService(serviceIntent)
      }
      Log.d(TAG, "AppProfileService started successfully")
    } catch (e: android.app.ForegroundServiceStartNotAllowedException) {
      Log.w(TAG, "ForegroundService not allowed for AppProfileService: ${e.message}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start AppProfileService: ${e.message}")
    }
  }

  private suspend fun startGameMonitorServiceIfEnabled(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val gameAppsJson = preferencesManager.getGameApps().first()

      // Check if there are any enabled games
      val hasEnabledGames =
          try {
            val jsonArray = JSONArray(gameAppsJson)
            var hasEnabled = false
            for (i in 0 until jsonArray.length()) {
              val obj = jsonArray.getJSONObject(i)
              if (obj.optBoolean("enabled", true)) {
                hasEnabled = true
                break
              }
            }
            hasEnabled
          } catch (e: Exception) {
            false
          }

      if (hasEnabledGames) {
        Log.d(TAG, "Enabled games found, checking GameMonitorService accessibility status...")
        
        // GameMonitorService is an AccessibilityService and cannot be started programmatically
        // It must be enabled by the user through Android's accessibility settings
        if (AccessibilityServiceHelper.isGameMonitorServiceEnabled(context)) {
          Log.d(TAG, "GameMonitorService accessibility is enabled")
        } else {
          Log.w(TAG, "GameMonitorService accessibility is not enabled. User must enable it manually in Settings > Accessibility")
          Log.i(TAG, "Service name: ${AccessibilityServiceHelper.getServiceName(context)}")
        }
      } else {
        Log.d(TAG, "No enabled games, GameMonitorService not needed")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to check GameMonitorService: ${e.message}")
    }
  }

  private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    return AccessibilityServiceHelper.isGameMonitorServiceEnabled(context)
  }

  private suspend fun applyRamConfigOnBoot(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val config = preferencesManager.getRamConfig().first()
      val ramUseCase = RAMControlUseCase()

      Log.d(TAG, "Applying RAM config on boot...")

      // Apply VM parameters
      if (config.swappiness > 0) ramUseCase.setSwappiness(config.swappiness)
      if (config.dirtyRatio > 0) ramUseCase.setDirtyRatio(config.dirtyRatio)
      if (config.minFreeMem > 0) ramUseCase.setMinFreeMem(config.minFreeMem)

      // Apply ZRAM
      if (config.zramSize > 0) {
        Log.d(TAG, "Setting ZRAM size: ${config.zramSize} MB")
        // TODO: Save/Load compression algorithm from prefs
        ramUseCase.setZRAMSize(config.zramSize.toLong() * 1024L * 1024L)
      }

      // Apply Swap File
      if (config.swapSize > 0) {
        ramUseCase.setSwapFileSizeMb(config.swapSize) { Log.d(TAG, "Swap: $it") }
      } else {
        // Fallback: Activate existing swap if not managed by XKM size pref
        activateSwapFile()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply RAM config: ${e.message}")
      activateSwapFile() // Ultimate fallback
    }
  }

  private suspend fun applyCpuConfigOnBoot(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()

      if (!cpuSetOnBoot) {
        Log.d(TAG, "CPU set on boot disabled, skipping CPU configuration")
        return
      }

      Log.d(TAG, "Applying CPU configuration on boot...")
      val cpuUseCase = id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase()

      // Get current CPU clusters
      val clusters = cpuUseCase.detectClusters()
      if (clusters.isEmpty()) {
        Log.w(TAG, "No CPU clusters detected, skipping CPU configuration")
        return
      }

      // Apply CPU frequency and governor settings from preferences
      clusters.forEach { cluster ->
        try {
          // Get saved cluster configuration from preferences
          val clusterMinFreq = preferencesManager.getClusterMinFreq(cluster.clusterNumber).first()
          val clusterMaxFreq = preferencesManager.getClusterMaxFreq(cluster.clusterNumber).first()
          val clusterGovernor = preferencesManager.getClusterGovernor(cluster.clusterNumber).first()

          // Apply frequency settings if they were saved
          if (clusterMinFreq > 0 && clusterMaxFreq > 0) {
            Log.d(TAG, "Setting cluster ${cluster.clusterNumber} frequency: $clusterMinFreq - $clusterMaxFreq MHz")
            cpuUseCase.setClusterFrequency(cluster.clusterNumber, clusterMinFreq, clusterMaxFreq)
          }

          // Apply governor if it was saved and is available
          if (clusterGovernor.isNotBlank() && cluster.availableGovernors.contains(clusterGovernor)) {
            Log.d(TAG, "Setting cluster ${cluster.clusterNumber} governor: $clusterGovernor")
            cpuUseCase.setClusterGovernor(cluster.clusterNumber, clusterGovernor)
          }

          // Apply core online/offline states
          cluster.cores.forEach { coreId ->
            val coreEnabled = preferencesManager.isCpuCoreEnabled(coreId).first()
            if (!coreEnabled && coreId != 0) { // Never disable core 0
              Log.d(TAG, "Disabling CPU core $coreId")
              cpuUseCase.setCoreOnline(coreId, false)
            }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to apply configuration for cluster ${cluster.clusterNumber}: ${e.message}")
        }
      }

      Log.d(TAG, "CPU configuration applied successfully on boot")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply CPU config on boot: ${e.message}")
    }
  }

  private suspend fun applyIOConfigOnBoot(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val ioSetOnBoot = preferencesManager.getIOSetOnBoot().first()

      if (!ioSetOnBoot) {
        Log.d(TAG, "I/O set on boot disabled, skipping I/O scheduler configuration")
        return
      }

      val ioScheduler = preferencesManager.getIOScheduler().first()
      if (ioScheduler.isBlank()) {
        Log.d(TAG, "No I/O scheduler configured, skipping")
        return
      }

      Log.d(TAG, "Applying I/O scheduler configuration on boot: $ioScheduler")

      // Apply I/O scheduler to all block devices
      val blockDevices = listOf("sda", "sdb", "sdc", "mmcblk0", "mmcblk1", "nvme0n1")
      
      blockDevices.forEach { device ->
        val schedulerPath = "/sys/block/$device/queue/scheduler"
        val checkDevice = RootManager.executeCommand("test -f $schedulerPath && echo exists || echo notfound")
        
        if (checkDevice.getOrNull()?.trim() == "exists") {
          Log.d(TAG, "Setting I/O scheduler for $device to $ioScheduler")
          val result = RootManager.executeCommand("echo '$ioScheduler' > $schedulerPath 2>/dev/null")
          if (result.isFailure) {
            Log.w(TAG, "Failed to set I/O scheduler for $device: ${result.exceptionOrNull()?.message}")
          }
        }
      }

      Log.d(TAG, "I/O scheduler configuration applied successfully on boot")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply I/O config on boot: ${e.message}")
    }
  }

  private suspend fun applyTCPConfigOnBoot(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val tcpSetOnBoot = preferencesManager.getTCPSetOnBoot().first()

      if (!tcpSetOnBoot) {
        Log.d(TAG, "TCP set on boot disabled, skipping TCP congestion configuration")
        return
      }

      val tcpCongestion = preferencesManager.getTCPCongestion().first()
      if (tcpCongestion.isBlank()) {
        Log.d(TAG, "No TCP congestion algorithm configured, skipping")
        return
      }

      Log.d(TAG, "Applying TCP congestion configuration on boot: $tcpCongestion")

      // Apply TCP congestion control algorithm
      val result = RootManager.executeCommand("echo '$tcpCongestion' > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null")
      
      if (result.isSuccess) {
        Log.d(TAG, "TCP congestion control set to $tcpCongestion successfully")
      } else {
        Log.e(TAG, "Failed to set TCP congestion control: ${result.exceptionOrNull()?.message}")
      }

      Log.d(TAG, "TCP congestion configuration applied successfully on boot")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply TCP config on boot: ${e.message}")
    }
  }

  private suspend fun applyAdditionalRAMConfigOnBoot(context: Context) {
    try {
      val preferencesManager = PreferencesManager(context)
      val ramSetOnBoot = preferencesManager.getRAMSetOnBoot().first()

      if (!ramSetOnBoot) {
        Log.d(TAG, "Additional RAM set on boot disabled, skipping additional RAM configuration")
        return
      }

      Log.d(TAG, "Applying additional RAM configuration on boot...")
      val ramUseCase = RAMControlUseCase()

      // Get current RAM configuration from preferences
      val ramConfig = preferencesManager.getRamConfig().first()
      val swappiness = ramConfig.swappiness
      val dirtyRatio = ramConfig.dirtyRatio
      val minFreeMem = ramConfig.minFreeMem

      // Apply VM parameters if they are non-default values
      if (swappiness != 60) { // Default swappiness is 60
        Log.d(TAG, "Setting swappiness to $swappiness")
        ramUseCase.setSwappiness(swappiness)
      }

      if (dirtyRatio != 20) { // Default dirty ratio is 20
        Log.d(TAG, "Setting dirty ratio to $dirtyRatio")
        ramUseCase.setDirtyRatio(dirtyRatio)
      }

      if (minFreeMem > 0) {
        Log.d(TAG, "Setting min free memory to $minFreeMem KB")
        ramUseCase.setMinFreeMem(minFreeMem)
      }

      Log.d(TAG, "Additional RAM configuration applied successfully on boot")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply additional RAM config on boot: ${e.message}")
    }
  }

  private suspend fun activateSwapFile() {
    val swapPath = "/data/swap/swapfile"

    // Check if swap file exists
    val checkExists =
        RootManager.executeCommand("test -f $swapPath && echo exists || echo notfound")
    if (checkExists.getOrNull()?.trim() != "exists") {
      Log.d("BootReceiver", "Swap file not found, skipping activation")
      return
    }

    // Check if swap is already active
    val checkActive =
        RootManager.executeCommand(
            "grep -q '$swapPath' /proc/swaps && echo active || echo inactive"
        )
    if (checkActive.getOrNull()?.trim() == "active") {
      Log.d("BootReceiver", "Swap file already active")
      return
    }

    // Activate swap file
    Log.d("BootReceiver", "Activating swap file...")
    val result = RootManager.executeCommand("swapon $swapPath 2>&1")

    if (result.isSuccess) {
      Log.d("BootReceiver", "Swap file activated successfully")
    } else {
      Log.e("BootReceiver", "Failed to activate swap file: ${result.exceptionOrNull()?.message}")
    }
  }
}
