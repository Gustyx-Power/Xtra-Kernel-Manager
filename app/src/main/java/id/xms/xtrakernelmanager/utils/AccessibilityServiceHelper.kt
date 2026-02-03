package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import id.xms.xtrakernelmanager.service.GameMonitorService

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