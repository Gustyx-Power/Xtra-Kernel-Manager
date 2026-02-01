package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Helper class for Accessibility Service management
 * Especially useful for ColorOS, MIUI, and other aggressive battery optimization ROMs
 */
object AccessibilityServiceHelper {
    
    private const val TAG = "AccessibilityHelper"
    
    /**
     * Check if Game Monitor accessibility service is enabled
     */
    fun isServiceEnabled(context: Context): Boolean {
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            )
            
            if (accessibilityEnabled == 0) return false
            
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            if (enabledServices.isNullOrEmpty()) return false
            
            val packageName = context.packageName
            val serviceName = "GameMonitorService"
            
            enabledServices.contains(packageName) && enabledServices.contains(serviceName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service", e)
            false
        }
    }
    
    /**
     * Open accessibility settings
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open accessibility settings", e)
        }
    }
    
    /**
     * Get user-friendly message for ColorOS/MIUI users
     */
    fun getColorOSInstructions(): String {
        return """
            For ColorOS/OPPO devices:
            1. Enable the accessibility service
            2. Go to Settings > Battery > App Battery Management
            3. Find XKM and set to "No restrictions"
            4. Go to Settings > App Management > XKM
            5. Enable "Auto-start" and "Run in background"
        """.trimIndent()
    }
    
    /**
     * Get user-friendly message for MIUI users
     */
    fun getMIUIInstructions(): String {
        return """
            For MIUI/Xiaomi devices:
            1. Enable the accessibility service
            2. Go to Settings > Apps > Manage apps > XKM
            3. Enable "Autostart"
            4. Set Battery saver to "No restrictions"
            5. Lock the app in Recent apps
        """.trimIndent()
    }
}
