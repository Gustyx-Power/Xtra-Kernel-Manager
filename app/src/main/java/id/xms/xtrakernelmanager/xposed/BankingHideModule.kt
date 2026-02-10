package id.xms.xtrakernelmanager.xposed

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Binder
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences

@InjectYukiHookWithXposed
class BankingHideModule : IYukiHookXposedInit {
    
    companion object {
        private const val TAG = "XKM-AccessibilityHide"
        private const val XKM_PACKAGE = "id.xms.xtrakernelmanager"
        private const val XKM_DEV_PACKAGE = "id.xms.xtrakernelmanager.dev"
        private const val PREFS_NAME = "xkm_sync_prefs"
        
        
        fun isXkmService(serviceInfo: AccessibilityServiceInfo): Boolean {
            val serviceId = serviceInfo.id ?: ""
            val resolveInfo = serviceInfo.resolveInfo
            val servicePackage = resolveInfo?.serviceInfo?.packageName ?: ""
            val serviceName = resolveInfo?.serviceInfo?.name ?: ""
            
            return servicePackage == XKM_PACKAGE ||
                    servicePackage == XKM_DEV_PACKAGE ||
                    serviceName.contains("GameMonitorService") ||
                    serviceName.contains("XtraAccessibility") ||
                    serviceId.contains(XKM_PACKAGE) ||
                    serviceId.contains(XKM_DEV_PACKAGE) ||
                    serviceId.contains("xtrakernelmanager", ignoreCase = true)
        }
        
        fun isXkmServiceString(serviceString: String): Boolean {
            return serviceString.contains(XKM_PACKAGE) ||
                    serviceString.contains(XKM_DEV_PACKAGE) ||
                    serviceString.contains("GameMonitorService") ||
                    serviceString.contains("XtraAccessibility") ||
                    serviceString.contains("xtrakernelmanager", ignoreCase = true)
        }
    }
    
    private var xPrefs: XSharedPreferences? = null
    private var packageManager: android.content.pm.PackageManager? = null
    private val uidToPackageCache = mutableMapOf<Int, String?>()
    
    // Performance optimizations - cache untuk mengurangi parsing berulang
    private var detectorAppsCache: Set<String>? = null
    private var appsToHideCache: Set<String>? = null
    private var featureEnabledCache: Boolean? = null
    private var lastAccessibilityQueryTime: Long = 0
    
    private var selectedAppsCache: Set<String>? = null
    private var lastCacheTime: Long = 0
    private val CACHE_DURATION = 30_000L
    private val MAX_UID_CACHE_SIZE = 500
    private val MIN_QUERY_INTERVAL = 100L // Rate limiting untuk mencegah spam calls
    
    override fun onInit() = YukiHookAPI.configs {
        isDebug = true
    }
    
    override fun onHook() = YukiHookAPI.encase {
        val currentPackage = packageName
        
        YLog.info(tag = TAG, msg = "XKM Banking Hide Module loaded for package: $currentPackage")
        
        if (currentPackage == "android") {
            YLog.info(tag = TAG, msg = "Hooking System Server - AccessibilityManagerService")
            hookAccessibilityManagerService()
        } else if (currentPackage == "com.android.providers.settings") {
            YLog.info(tag = TAG, msg = "Hooking Settings Provider")
            hookSettingsProvider()
        }
    }
    
    private fun PackageParam.hookSettingsProvider() {
        val settingsProviderClass = "com.android.providers.settings.SettingsProvider".toClassOrNull()
        
        if (settingsProviderClass != null) {
            YLog.info(tag = TAG, msg = "Found SettingsProvider class, applying hooks...")
                settingsProviderClass.method {
                name = "call"
                paramCount = 3
            }.hook {
                after {
                    try {
                        // More flexible parameter handling
                        if (args.size >= 2) {
                            val method = args[0] as? String
                            val key = args[1] as? String
                            
                            if (method != null && key != null) {
                                if (method == "GET_secure" && key == "enabled_accessibility_services") {
                                    val callingPackage = getCallingPackageFromUid()
                                    YLog.debug(tag = TAG, msg = "SettingsProvider: $callingPackage is reading enabled_accessibility_services")
                                    
                                    if (callingPackage != null && shouldHideFromPackage(callingPackage)) {
                                        val resultBundle = result as? android.os.Bundle
                                        val value = resultBundle?.getString("value")
                                        
                                        YLog.debug(tag = TAG, msg = "Original accessibility services value: $value")
                                        
                                        if (value != null && value.isNotEmpty()) {
                                            val filteredValue = filterAccessibiltyServicesString(value, callingPackage)
                                            
                                            if (filteredValue != value) {
                                                val newBundle = android.os.Bundle()
                                                if (resultBundle != null) {
                                                    newBundle.putAll(resultBundle)
                                                }
                                                newBundle.putString("value", filteredValue)
                                                result = newBundle
                                                
                                                YLog.info(tag = TAG, msg = "Successfully filtered enabled_accessibility_services for $callingPackage via SettingsProvider")
                                            } else {
                                                YLog.debug(tag = TAG, msg = "No filtering needed for $callingPackage - no matching services found")
                                            }
                                        } else {
                                            YLog.debug(tag = TAG, msg = "No accessibility services value found for $callingPackage")
                                        }
                                    } else {
                                        YLog.debug(tag = TAG, msg = "Not filtering for $callingPackage (not in detector list or feature disabled)")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        YLog.error(tag = TAG, msg = "Error in SettingsProvider hook: ${e.message}")
                    }
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "Failed to hook SettingsProvider.call: ${it.message}")
            }
        } else {
            YLog.error(tag = TAG, msg = "SettingsProvider class not found!")
        }
    }

    private fun filterAccessibiltyServicesString(originalValue: String, callingPackage: String): String {
        return try {
            refreshPrefsIfNeeded()
            
            // Gunakan cache untuk apps to hide
            val appsToHide = appsToHideCache ?: run {
                val appsToHideJson = xPrefs?.getString("hide_accessibility_apps_to_hide", "[]") ?: "[]"
                val apps = try {
                    val jsonArray = org.json.JSONArray(appsToHideJson)
                    (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
                } catch (e: Exception) {
                    emptySet<String>()
                }
                appsToHideCache = apps
                apps
            }
            
            if (appsToHide.isEmpty()) return originalValue
            
            val components = originalValue.split(":")
            
            // Optimasi: gunakan HashSet untuk O(1) lookup
            val appsToHideSet = appsToHide.toHashSet()
            
            val filteredComponents = components.filter { component ->
                if (component.isEmpty()) return@filter true
                
                // Optimized check - avoid .any() iteration
                val shouldHide = appsToHideSet.any { packageToHide ->
                    component.contains(packageToHide) ||
                    (packageToHide.contains("xtrakernelmanager", ignoreCase = true) && isXkmServiceString(component))
                }
                
                !shouldHide
            }
            
            val filteredValue = filteredComponents.joinToString(":")
            
            if (filteredComponents.size != components.size) {
                YLog.info(tag = TAG, msg = "Filtered Settings String for $callingPackage: ${components.size} -> ${filteredComponents.size} components")
            }
            
            filteredValue
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error filtering accessibility services string: ${e.message}")
            originalValue
        }
    }
    private fun PackageParam.hookAccessibilityManagerService() {
        val amsClass = "com.android.server.accessibility.AccessibilityManagerService".toClassOrNull()
        
        if (amsClass != null) {
            YLog.info(tag = TAG, msg = "Found AccessibilityManagerService class, applying hooks...")
            
            amsClass.method {
                name = "getEnabledAccessibilityServiceList"
            }.hook {
                after {
                    try {
                        val callingUid = Binder.getCallingUid()
                        val callingPackage = getCallingPackageFromUid()
                        
                        if (callingPackage != null && shouldHideFromPackage(callingPackage)) {
                            val originalList = result as? List<AccessibilityServiceInfo>
                            if (originalList != null) {
                                val filteredList = filterAccessibilityServices(originalList, callingPackage)
                                result = filteredList
                                YLog.debug(tag = TAG, msg = "Filtered accessibility services for $callingPackage: ${originalList.size} -> ${filteredList.size}")
                            }
                        }
                    } catch (e: Exception) {
                        YLog.error(tag = TAG, msg = "Error in getEnabledAccessibilityServiceList hook: ${e.message}")
                    }
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "Failed to hook getEnabledAccessibilityServiceList: ${it.message}")
            }
            
            amsClass.method {
                name = "getInstalledAccessibilityServiceList"
            }.hook {
                after {
                    try {
                        val callingPackage = getCallingPackageFromUid()
                        if (callingPackage != null && shouldHideFromPackage(callingPackage)) {
                            val originalList = result as? List<AccessibilityServiceInfo>
                            if (originalList != null) {
                                val filteredList = filterAccessibilityServices(originalList, callingPackage)
                                result = filteredList
                                YLog.debug(tag = TAG, msg = "Filtered installed accessibility services for $callingPackage: ${originalList.size} -> ${filteredList.size}")
                            }
                        }
                    } catch (e: Exception) {
                        YLog.error(tag = TAG, msg = "Error in getInstalledAccessibilityServiceList hook: ${e.message}")
                    }
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "Failed to hook getInstalledAccessibilityServiceList: ${it.message}")
            }
            
            YLog.info(tag = TAG, msg = "AccessibilityManagerService hooks applied!")
        } else {
            YLog.error(tag = TAG, msg = "AccessibilityManagerService class not found!")
        }
    }
    
    private fun getCallingPackageFromUid(): String? {
        return try {
            val callingUid = Binder.getCallingUid()
            
            if (callingUid == 1000) return "android"
            
            if (uidToPackageCache.containsKey(callingUid)) {
                return uidToPackageCache[callingUid]
            }
            
            if (packageManager == null) {
                try {
                    val activityThread = "android.app.ActivityThread".toClass()
                        .method { name = "currentApplication" }
                        .get()
                        .call() as? android.app.Application
                    packageManager = activityThread?.packageManager
                } catch (e: Exception) {
                    val context = "android.app.ActivityThread".toClass()
                        .method { name = "currentActivityThread" }
                        .get()
                        .call()
                    
                    val systemContext = context?.javaClass?.method { name = "getSystemContext" }
                        ?.get(context)
                        ?.call() as? android.content.Context
                    
                    packageManager = systemContext?.packageManager
                }
            }
            
            val packages = packageManager?.getPackagesForUid(callingUid)
            val packageName = packages?.firstOrNull()
            
            uidToPackageCache[callingUid] = packageName
            
            packageName
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error getting calling package: ${e.message}")
            null
        }
    }
    
    private fun shouldHideFromPackage(callingPackage: String): Boolean {
        // Rate limiting untuk mencegah spam calls
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAccessibilityQueryTime < MIN_QUERY_INTERVAL) {
            // Gunakan cache jika ada rate limiting
            return detectorAppsCache?.contains(callingPackage) ?: false
        }
        lastAccessibilityQueryTime = currentTime
        
        return try {
            refreshPrefsIfNeeded()
            
            // Gunakan cache untuk feature enabled
            val isEnabled = featureEnabledCache ?: run {
                val enabled = try {
                    xPrefs?.getBoolean("xkm_hide_accessibility_enabled", false) ?: false
                } catch (e: ClassCastException) {
                    val enabledStr = xPrefs?.getString("xkm_hide_accessibility_enabled", "false") ?: "false"
                    enabledStr.equals("true", ignoreCase = true)
                }
                featureEnabledCache = enabled
                enabled
            }
            
            if (!isEnabled) {
                return false
            }
            
            // Gunakan cache untuk detector apps
            val detectorApps = detectorAppsCache ?: run {
                val detectorAppsJson = xPrefs?.getString("hide_accessibility_detector_apps", "[]") ?: "[]"
                val apps = try {
                    val jsonArray = org.json.JSONArray(detectorAppsJson)
                    (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
                } catch (e: Exception) {
                    YLog.error(tag = TAG, msg = "Failed to parse detector apps JSON: ${e.message}")
                    emptySet<String>()
                }
                detectorAppsCache = apps
                apps
            }
            
            detectorApps.contains(callingPackage)
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error checking if should hide from package $callingPackage: ${e.message}")
            false
        }
    }
    
    private fun filterAccessibilityServices(
        originalList: List<AccessibilityServiceInfo>,
        callingPackage: String
    ): List<AccessibilityServiceInfo> {
        return try {
            refreshPrefsIfNeeded()
            
            // Gunakan cache untuk apps to hide
            val appsToHide = appsToHideCache ?: run {
                val appsToHideJson = xPrefs?.getString("hide_accessibility_apps_to_hide", "[]") ?: "[]"
                val apps = try {
                    val jsonArray = org.json.JSONArray(appsToHideJson)
                    (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
                } catch (e: Exception) {
                    YLog.error(tag = TAG, msg = "Failed to parse apps to hide JSON: ${e.message}")
                    emptySet<String>()
                }
                appsToHideCache = apps
                apps
            }
            
            if (appsToHide.isEmpty()) {
                return originalList
            }
            
            // Optimasi: gunakan HashSet untuk O(1) lookup instead of .any()
            val appsToHideSet = appsToHide.toHashSet()
            
            // Filter out services from apps that should be hidden
            val filteredList = originalList.filter { serviceInfo ->
                val servicePackage = serviceInfo.resolveInfo?.serviceInfo?.packageName ?: ""
                val serviceName = serviceInfo.resolveInfo?.serviceInfo?.name ?: ""
                val serviceId = serviceInfo.id ?: ""
                
                // Optimized check - direct HashSet lookup first
                val shouldHide = appsToHideSet.contains(servicePackage) ||
                    serviceName.contains("xtrakernelmanager", ignoreCase = true) ||
                    serviceId.contains("xtrakernelmanager", ignoreCase = true) ||
                    isXkmService(serviceInfo)
                
                !shouldHide
            }
            
            if (filteredList.size != originalList.size) {
                YLog.info(tag = TAG, msg = "Filtered accessibility services for $callingPackage: ${originalList.size} -> ${filteredList.size} services")
            }
            filteredList
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error filtering accessibility services: ${e.message}")
            originalList
        }
    }
    
    private fun refreshPrefsIfNeeded() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCacheTime > CACHE_DURATION || xPrefs == null) {
            try {
                var prefs = XSharedPreferences(XKM_DEV_PACKAGE, PREFS_NAME)
                prefs.makeWorldReadable()
                if (!prefs.file.exists() || !prefs.file.canRead()) {
                    prefs = XSharedPreferences(XKM_PACKAGE, PREFS_NAME)
                    prefs.makeWorldReadable()
                }
                
                xPrefs = prefs
                lastCacheTime = currentTime
                
                // Clear cache untuk memaksa reload
                detectorAppsCache = null
                appsToHideCache = null
                featureEnabledCache = null
                
                // Limit UID cache size untuk mencegah memory leak
                if (uidToPackageCache.size > MAX_UID_CACHE_SIZE) {
                    // Keep only the most recent entries (simple LRU simulation)
                    val entries = uidToPackageCache.entries.toList()
                    uidToPackageCache.clear()
                    entries.takeLast(MAX_UID_CACHE_SIZE / 2).forEach { (k, v) ->
                        uidToPackageCache[k] = v
                    }
                }
                
            } catch (e: Exception) {
                YLog.error(tag = TAG, msg = "Failed to refresh preferences: ${e.message}")
            }
        }
    }
}