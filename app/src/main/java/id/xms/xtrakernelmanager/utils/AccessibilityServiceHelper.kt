package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import id.xms.xtrakernelmanager.service.GameMonitorService

import com.topjohnwu.superuser.Shell
import android.os.Build

object AccessibilityServiceHelper {
    private const val TAG = "AccessibilityServiceHelper"

    /**
     * Check if the GameMonitorService accessibility service is enabled
     */
    fun isGameMonitorServiceEnabled(context: Context): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val serviceName = "${context.packageName}/${GameMonitorService::class.java.name}"
            enabledServices?.contains(serviceName) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check accessibility service status: ${e.message}")
            false
        }
    }

    /**
     * Open accessibility settings to allow user to enable the service
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open accessibility settings: ${e.message}")
        }
    }

    /**
     * Try to bypass Android 13+ Restricted Settings using Root
     */
    fun bypassRestrictedSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                Shell.cmd("appops set ${context.packageName} ACCESS_RESTRICTED_SETTINGS allow").submit()
                Log.d(TAG, "Attempted to bypass Restricted Settings via Root")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bypass restricted settings: ${e.message}")
            }
        }
    }

    /**
     * Get the service name for display purposes
     */
    fun getServiceName(context: Context): String {
        return "${context.packageName}/${GameMonitorService::class.java.name}"
    }

    /**
     * Check if accessibility services are available on this device
     */
    fun isAccessibilityAvailable(context: Context): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            accessibilityManager.isEnabled
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check accessibility availability: ${e.message}")
            false
        }
    }
}