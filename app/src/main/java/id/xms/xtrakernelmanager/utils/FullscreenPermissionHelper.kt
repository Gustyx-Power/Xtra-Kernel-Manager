package id.xms.xtrakernelmanager.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Helper untuk request permission fullscreen/overlay di berbagai ROM
 * Mendukung: OxygenOS (OnePlus), MIUI (Xiaomi), ColorOS (OPPO), AOSP
 */
object FullscreenPermissionHelper {
    private const val TAG = "FullscreenPermission"

    /**
     * Cek apakah app sudah memiliki permission overlay
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Request overlay permission dengan fallback untuk berbagai ROM
     */
    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                activity.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open overlay settings", e)
                // Fallback ke app settings
                openAppSettings(activity)
            }
        }
    }

    /**
     * Request battery optimization exemption (untuk background service)
     */
    fun requestBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings", e)
            }
        }
    }

    /**
     * Buka app settings untuk manual permission
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
        }
    }

    /**
     * Buka permission settings khusus untuk MIUI
     */
    fun openMiuiPermissionSettings(context: Context) {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            intent.putExtra("extra_pkgname", context.packageName)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open MIUI permission settings", e)
            openAppSettings(context)
        }
    }

    /**
     * Buka permission settings khusus untuk OxygenOS (OnePlus)
     */
    fun openOxygenOSPermissionSettings(context: Context) {
        try {
            val intent = Intent()
            intent.component = ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open OxygenOS permission settings", e)
            openAppSettings(context)
        }
    }

    /**
     * Buka permission settings khusus untuk ColorOS (OPPO)
     */
    fun openColorOSPermissionSettings(context: Context) {
        try {
            val intent = Intent()
            intent.component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ColorOS permission settings", e)
            openAppSettings(context)
        }
    }

    /**
     * Deteksi ROM dan buka settings yang sesuai
     */
    fun openRomSpecificSettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        when {
            manufacturer.contains("xiaomi") || brand.contains("xiaomi") || 
            manufacturer.contains("redmi") || brand.contains("redmi") -> {
                openMiuiPermissionSettings(context)
            }
            manufacturer.contains("oneplus") || brand.contains("oneplus") -> {
                openOxygenOSPermissionSettings(context)
            }
            manufacturer.contains("oppo") || brand.contains("oppo") || 
            manufacturer.contains("realme") || brand.contains("realme") -> {
                openColorOSPermissionSettings(context)
            }
            else -> {
                // AOSP atau ROM lain
                openAppSettings(context)
            }
        }
    }

    /**
     * Request semua permission yang diperlukan untuk fullscreen
     */
    fun requestAllFullscreenPermissions(activity: Activity) {
        // 1. Overlay permission
        if (!hasOverlayPermission(activity)) {
            requestOverlayPermission(activity)
        }
        
        // 2. Battery optimization (optional tapi recommended)
        requestBatteryOptimization(activity)
        
        // 3. ROM-specific settings
        openRomSpecificSettings(activity)
    }

    /**
     * Cek ROM type
     */
    fun getRomType(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            manufacturer.contains("xiaomi") || brand.contains("xiaomi") || 
            manufacturer.contains("redmi") || brand.contains("redmi") -> "MIUI"
            manufacturer.contains("oneplus") || brand.contains("oneplus") -> "OxygenOS"
            manufacturer.contains("oppo") || brand.contains("oppo") -> "ColorOS"
            manufacturer.contains("realme") || brand.contains("realme") -> "RealmeUI"
            manufacturer.contains("samsung") || brand.contains("samsung") -> "OneUI"
            manufacturer.contains("huawei") || brand.contains("huawei") -> "EMUI"
            else -> "AOSP"
        }
    }
}
