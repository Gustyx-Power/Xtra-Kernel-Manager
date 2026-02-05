package id.xms.xtrakernelmanager.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import id.xms.xtrakernelmanager.service.AlternativeGameDetectionService
import id.xms.xtrakernelmanager.service.GameMonitorService

object ServiceToggleUtil {
    
    private const val TAG = "ServiceToggleUtil"
    
    /**
     * Enable/Disable Accessibility Service programmatically
     * This helps avoid Play Protect detection by disabling the service by default
     */
    fun toggleAccessibilityService(context: Context, enabled: Boolean) {
        try {
            val componentName = ComponentName(context, GameMonitorService::class.java)
            val newState = if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            context.packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )
            
            Log.d(TAG, "Accessibility service ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle accessibility service", e)
        }
    }
    
    /**
     * Start alternative game detection service
     * Uses UsageStats API instead of Accessibility Service
     */
    fun startAlternativeDetection(context: Context) {
        try {
            val intent = Intent(context, AlternativeGameDetectionService::class.java)
            context.startForegroundService(intent)
            Log.d(TAG, "Alternative detection service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alternative detection service", e)
        }
    }
    
    /**
     * Stop alternative game detection service
     */
    fun stopAlternativeDetection(context: Context) {
        try {
            val intent = Intent(context, AlternativeGameDetectionService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Alternative detection service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop alternative detection service", e)
        }
    }
    
    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, GameMonitorService::class.java)
            val enabledState = context.packageManager.getComponentEnabledSetting(componentName)
            enabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
            enabledState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check accessibility service state", e)
            false
        }
    }
    
    /**
     * Switch to safe mode - disable accessibility service and use alternative detection
     */
    fun enableSafeMode(context: Context) {
        Log.d(TAG, "Enabling safe mode")
        toggleAccessibilityService(context, false)
        startAlternativeDetection(context)
    }
    
    /**
     * Switch to full mode - enable accessibility service and stop alternative detection
     */
    fun enableFullMode(context: Context) {
        Log.d(TAG, "Enabling full mode")
        stopAlternativeDetection(context)
        toggleAccessibilityService(context, true)
    }
}