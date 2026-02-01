package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Receiver to help restart Accessibility Service if killed by system
 */
class AccessibilityRestartReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AccessibilityRestart"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received restart request")
        
        // Check if accessibility service is still enabled in settings
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        val isEnabled = enabledServices?.contains(context.packageName) == true
        
        if (isEnabled) {
            Log.d(TAG, "Accessibility service is enabled, system should restart it automatically")
        } else {
            Log.w(TAG, "Accessibility service is disabled in settings")
        }
    }
}
