package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.provider.Settings
import android.text.TextUtils

class BankingAppReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BankingAppReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "id.xms.xtrakernelmanager.BANKING_APP_DETECTED" -> {
                Log.d(TAG, "Banking app detected - considering accessibility service disable")
                
                // Option 1: Just log and let user know
                // This is safer than automatically disabling
                Log.i(TAG, "Banking app is active. Accessibility service is in safe mode.")
                
                // Option 2: Show notification to user about banking mode
                // You could implement a notification here
                
                // Option 3: Temporarily disable accessibility (advanced)
                // This would require system-level permissions and is not recommended
                // disableAccessibilityService(context)
            }
        }
    }
    
    // This method is for reference only - requires system permissions
    private fun disableAccessibilityService(context: Context) {
        try {
            val serviceName = "${context.packageName}/.service.GameMonitorService"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            if (enabledServices != null && enabledServices.contains(serviceName)) {
                val newServices = enabledServices.replace(serviceName, "")
                    .replace("::", ":")
                    .trim(':')
                
                // This requires WRITE_SECURE_SETTINGS permission (system app only)
                Settings.Secure.putString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    newServices
                )
                
                Log.d(TAG, "Accessibility service disabled for banking security")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable accessibility service", e)
        }
    }
}