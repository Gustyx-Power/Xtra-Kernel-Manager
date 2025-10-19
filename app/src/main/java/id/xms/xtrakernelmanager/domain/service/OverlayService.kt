package id.xms.xtrakernelmanager.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.utils.RootUtils
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.roundToInt
import android.app.ActivityManager



class OverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var kernelRepository: KernelRepository

    // View references
    private var tvFps: TextView? = null
    private var tvCpuFreq: TextView? = null
    private var tvGpuFreq: TextView? = null
    private var tvCpuTemp: TextView? = null
    private var tvRamUsage: TextView? = null

    // FPS tracking
    private var currentFps = 0
    private var hasRoot = false

    companion object {
        private const val CHANNEL_ID = "xtra_kernel_overlay"
        private const val NOTIFICATION_ID = 1002

        const val ACTION_START = "action_start_overlay"
        const val ACTION_STOP = "action_stop_overlay"

        // DRM FPS paths
        private val DRM_FPS_PATHS = listOf(
            "/sys/class/drm/sde-crtc-0/measured_fps",
            "/sys/class/drm/card0-DSI-1/measured_fps",
            "/sys/class/drm/card0/sde-crtc-0/measured_fps",
            "/sys/class/drm/card0/card0-DSI-1/measured_fps",
            "/sys/kernel/debug/dri/0/sde_crtc_0_measured_fps"
        )
    }

    override fun onCreate() {
        super.onCreate()
        kernelRepository = KernelRepository()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()

        // Check root access
        serviceScope.launch {
            hasRoot = RootUtils.isRootAvailable()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        stopSelf()
                        return START_NOT_STICKY
                    }
                }
                startForeground(NOTIFICATION_ID, createNotification())
                showOverlay()
            }
            ACTION_STOP -> {
                hideOverlay()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        try {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 20
                y = 100
            }

            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_stats, null)

            tvFps = overlayView?.findViewById(R.id.tvFps)
            tvCpuFreq = overlayView?.findViewById(R.id.tvCpuFreq)
            tvGpuFreq = overlayView?.findViewById(R.id.tvGpuFreq)
            tvCpuTemp = overlayView?.findViewById(R.id.tvCpuTemp)
            tvRamUsage = overlayView?.findViewById(R.id.tvRamUsage)

            setupDraggableOverlay(overlayView!!, layoutParams)
            windowManager.addView(overlayView, layoutParams)
            startUpdatingStats()

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun setupDraggableOverlay(view: View, layoutParams: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, layoutParams)
                    true
                }
                else -> false
            }
        }
    }

    private fun hideOverlay() {
        try {
            overlayView?.let {
                windowManager.removeView(it)
                overlayView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serviceScope.cancel()
    }

    private fun startUpdatingStats() {
        serviceScope.launch {
            while (isActive) {
                try {
                    updateStats()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(500)
            }
        }
    }

    private suspend fun updateStats() = withContext(Dispatchers.IO) {
        try {
            // Read FPS from DRM
            currentFps = readDrmFps()

            // Get CPU frequency
            val cpuFreq = readCpuFrequency()

            // Get CPU temperature
            val cpuTemp = readCpuTemperature()

            // Get GPU frequency
            val gpuFreq = readGpuFrequency()

            // REAL system RAM
            val (usedMemMB, totalMemMB) = getSystemRamInfo()

            withContext(Dispatchers.Main) {
                // Update FPS with color coding
                tvFps?.apply {
                    text = "FPS: $currentFps"
                    setTextColor(when {
                        currentFps >= 55 -> android.graphics.Color.parseColor("#00FF00")
                        currentFps >= 40 -> android.graphics.Color.parseColor("#FFD700")
                        currentFps >= 20 -> android.graphics.Color.parseColor("#FFA500")
                        else -> android.graphics.Color.parseColor("#FF0000")
                    })
                }

                // Update CPU Frequency
                tvCpuFreq?.text = if (cpuFreq > 0) "CPU: ${cpuFreq / 1000} MHz" else "CPU: N/A"

                // Update CPU Temperature
                if (cpuTemp > 0) {
                    tvCpuTemp?.apply {
                        text = "Temp: ${cpuTemp.roundToInt()}°C"
                        setTextColor(when {
                            cpuTemp < 45 -> android.graphics.Color.parseColor("#00FF00")
                            cpuTemp < 60 -> android.graphics.Color.parseColor("#FFD700")
                            else -> android.graphics.Color.parseColor("#FF0000")
                        })
                    }
                } else {
                    tvCpuTemp?.text = "Temp: N/A"
                }

                // Update GPU Frequency
                tvGpuFreq?.text = if (gpuFreq > 0) "GPU: ${gpuFreq / 1000000} MHz" else "GPU: N/A"

                // Update RAM with REAL system memory
                tvRamUsage?.apply {
                    text = "RAM: $usedMemMB / $totalMemMB MB"
                    setTextColor(when {
                        usedMemMB.toFloat() / totalMemMB < 0.6 -> android.graphics.Color.parseColor("#00FF00")
                        usedMemMB.toFloat() / totalMemMB < 0.8 -> android.graphics.Color.parseColor("#FFD700")
                        else -> android.graphics.Color.parseColor("#FF0000")
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Hybrid FPS reader (non-root first, then root)
    private suspend fun readDrmFps(): Int = withContext(Dispatchers.IO) {
        try {
            // Try non-root first
            for (path in DRM_FPS_PATHS) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val fps = file.readText().trim()
                        val fpsValue = fps.toFloatOrNull()?.roundToInt()
                        if (fpsValue != null && fpsValue > 0) {
                            return@withContext fpsValue
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Try with root if available
            if (hasRoot) {
                for (path in DRM_FPS_PATHS) {
                    try {
                        val result = RootUtils.readFile(path)
                        if (!result.isNullOrEmpty()) {
                            val fpsValue = result.trim().toFloatOrNull()?.roundToInt()
                            if (fpsValue != null && fpsValue > 0) {
                                return@withContext fpsValue
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }

            // Fallback: display refresh rate
            val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            return@withContext display.refreshRate.roundToInt()

        } catch (e: Exception) {
            return@withContext 60
        }
    }

    // Hybrid CPU frequency reader
    private suspend fun readCpuFrequency(): Long = withContext(Dispatchers.IO) {
        try {
            // Try non-root first
            for (core in 0..7) {
                val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val freq = file.readText().trim().toLongOrNull()
                        if (freq != null && freq > 0) {
                            return@withContext freq
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Try with root if available
            if (hasRoot) {
                try {
                    val cpuInfo = kernelRepository.getCpuInfo()
                    val maxFreq = cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0L
                    if (maxFreq > 0) {
                        return@withContext maxFreq
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            return@withContext 0L
        } catch (e: Exception) {
            return@withContext 0L
        }
    }

    // Hybrid CPU temperature reader
    private suspend fun readCpuTemperature(): Float = withContext(Dispatchers.IO) {
        try {
            val thermalPaths = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/hwmon/hwmon0/temp1_input",
                "/sys/class/hwmon/hwmon1/temp1_input"
            )

            // Try non-root first
            for (path in thermalPaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val temp = file.readText().trim().toFloatOrNull()
                        if (temp != null && temp > 0) {
                            return@withContext if (temp > 1000) temp / 1000 else temp
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Try with root if available
            if (hasRoot) {
                try {
                    val cpuInfo = kernelRepository.getCpuInfo()
                    if (cpuInfo.temperature > 0) {
                        return@withContext cpuInfo.temperature
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            return@withContext 0f
        } catch (e: Exception) {
            return@withContext 0f
        }
    }

    // Hybrid GPU frequency reader
    private suspend fun readGpuFrequency(): Long = withContext(Dispatchers.IO) {
        try {
            val gpuPaths = listOf(
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/kernel/gpu/gpu_clock"
            )

            // Try non-root first
            for (path in gpuPaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val freq = file.readText().trim().toLongOrNull()
                        if (freq != null && freq > 0) {
                            return@withContext freq
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Try with root if available
            if (hasRoot) {
                try {
                    val gpuInfo = kernelRepository.getGpuInfo()
                    if (gpuInfo.currentFreq > 0) {
                        return@withContext gpuInfo.currentFreq
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            return@withContext 0L
        } catch (e: Exception) {
            return@withContext 0L
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Performance Overlay")
            .setContentText("FPS: $currentFps | Tap to open")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Performance Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time performance statistics"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private suspend fun getSystemRamInfo(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        try {
            // Read from /proc/meminfo (non-root readable)
            val memInfo = File("/proc/meminfo").readText()

            val totalMatch = Regex("MemTotal:\\s+(\\d+)\\s+kB").find(memInfo)
            val availableMatch = Regex("MemAvailable:\\s+(\\d+)\\s+kB").find(memInfo)

            val totalKB = totalMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val availableKB = availableMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val totalMB = totalKB / 1024
            val usedMB = (totalKB - availableKB) / 1024

            return@withContext Pair(usedMB, totalMB)
        } catch (e: Exception) {
            // Fallback to ActivityManager
            try {
                val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)

                val totalMB = (memInfo.totalMem / (1024 * 1024)).toInt()
                val availMB = (memInfo.availMem / (1024 * 1024)).toInt()
                val usedMB = totalMB - availMB

                return@withContext Pair(usedMB, totalMB)
            } catch (ex: Exception) {
                return@withContext Pair(0, 0)
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }
}
