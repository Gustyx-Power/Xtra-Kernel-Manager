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
    
    private var selectedAppsCache: Set<String>? = null
    private var featureEnabledCache: Boolean? = null
    private var lastCacheTime: Long = 0
    private val CACHE_DURATION = 30_000L
    
    override fun onInit() = YukiHookAPI.configs {
        isDebug = true
    }
    
    override fun onHook() = YukiHookAPI.encase {
        val currentPackage = packageName
        
        YLog.info(tag = TAG, msg = "XKM Banking Hide Module loaded for package: $currentPackage")
        
        if (currentPackage == "android") {
            YLog.info(tag = TAG, msg = "Hooking System Server - AccessibilityManagerService")
            hookAccessibilityManagerService()
            hookSettingsProvider()
            hookPackageManagerService()
        }
        
        if (currentPackage == "com.android.providers.settings") {
            YLog.info(tag = TAG, msg = "Hooking SettingsProvider process")
            hookSettingsProviderProcess()
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
                    val callingUid = Binder.getCallingUid()
                    val callingPackage = getCallingPackageFromUid()
                    val originalList = result as? List<*>
                    val serviceCount = originalList?.size ?: 0
                    
                    YLog.debug(tag = TAG, msg = "[AMS-ENABLED] UID=$callingUid pkg=$callingPackage services=$serviceCount")
                    
                    if (!shouldHideFromPackage(callingPackage)) {
                        YLog.debug(tag = TAG, msg = "[AMS-ENABLED] Not hiding for: $callingPackage")
                        return@after
                    }
                    
                    if (originalList == null) return@after
                    
                    @Suppress("UNCHECKED_CAST")
                    val typedList = originalList as List<AccessibilityServiceInfo>
                    
                    typedList.forEachIndexed { index, service ->
                        val pkg = service.resolveInfo?.serviceInfo?.packageName ?: "unknown"
                        val name = service.resolveInfo?.serviceInfo?.name ?: "unknown"
                        val isXkm = isXkmService(service)
                        YLog.debug(tag = TAG, msg = "[AMS-ENABLED] Service[$index]: pkg=$pkg name=$name isXKM=$isXkm")
                    }
                    
                    val filteredList = typedList.filter { serviceInfo -> !isXkmService(serviceInfo) }
                    
                    if (filteredList.size != originalList.size) {
                        YLog.info(tag = TAG, msg = "[AMS-ENABLED] [$callingPackage] Filtered ${originalList.size - filteredList.size} XKM services")
                    }
                    
                    result = filteredList
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "Failed to hook getEnabledAccessibilityServiceList: ${it.message}")
            }
            
            amsClass.method {
                name = "getInstalledAccessibilityServiceList"
            }.hook {
                after {
                    val callingPackage = getCallingPackageFromUid()
                    if (!shouldHideFromPackage(callingPackage)) return@after
                    
                    val originalList = result as? List<*> ?: return@after
                    
                    @Suppress("UNCHECKED_CAST")
                    val filteredList = (originalList as List<AccessibilityServiceInfo>)
                        .filter { serviceInfo -> !isXkmService(serviceInfo) }
                    
                    if (filteredList.size != originalList.size) {
                        YLog.info(tag = TAG, msg = "[AMS] [$callingPackage] Filtered ${originalList.size - filteredList.size} XKM services from installed list")
                    }
                    
                    result = filteredList
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "Failed to hook getInstalledAccessibilityServiceList: ${it.message}")
            }
            
            YLog.info(tag = TAG, msg = "AccessibilityManagerService hooks applied!")
        } else {
            YLog.error(tag = TAG, msg = "AccessibilityManagerService class not found!")
        }
        
        hookClientSideAccessibilityManager()
    }
    
    private fun PackageParam.hookClientSideAccessibilityManager() {
        "android.view.accessibility.AccessibilityManager".toClass().method {
            name = "getEnabledAccessibilityServiceList"
            paramCount = 1
        }.hook {
            after {
                val callingPackage = getCallingPackageFromUid()
                if (!shouldHideFromPackage(callingPackage)) return@after
                
                val originalList = result as? List<*> ?: return@after
                
                @Suppress("UNCHECKED_CAST")
                val filteredList = (originalList as List<AccessibilityServiceInfo>)
                    .filter { serviceInfo -> !isXkmService(serviceInfo) }
                
                if (filteredList.size != originalList.size) {
                    YLog.info(tag = TAG, msg = "[AM] [$callingPackage] Filtered ${originalList.size - filteredList.size} XKM services")
                }
                
                result = filteredList
            }
        }
        
        "android.view.accessibility.AccessibilityManager".toClass().method {
            name = "getInstalledAccessibilityServiceList"
        }.hook {
            after {
                val callingPackage = getCallingPackageFromUid()
                if (!shouldHideFromPackage(callingPackage)) return@after
                
                val originalList = result as? List<*> ?: return@after
                
                @Suppress("UNCHECKED_CAST")
                val filteredList = (originalList as List<AccessibilityServiceInfo>)
                    .filter { serviceInfo -> !isXkmService(serviceInfo) }
                
                if (filteredList.size != originalList.size) {
                    YLog.info(tag = TAG, msg = "[AM] [$callingPackage] Filtered ${originalList.size - filteredList.size} XKM services")
                }
                
                result = filteredList
            }
        }
    }
    
    private fun PackageParam.hookSettingsProvider() {
        YLog.info(tag = TAG, msg = "Setting up SettingsProvider hooks...")
        
        val settingsProviderClass = "com.android.providers.settings.SettingsProvider".toClassOrNull()
        if (settingsProviderClass != null) {
            YLog.info(tag = TAG, msg = "Found SettingsProvider class, hooking call method...")
            
            settingsProviderClass.method {
                name = "call"
            }.hookAll {
                after {
                    try {
                        val callingUid = Binder.getCallingUid()
                        val callingPackage = getCallingPackageFromUid()
                        
                        val bundle = result as? android.os.Bundle ?: return@after
                        val value = bundle.getString("value") ?: return@after
                        
                        if (!value.contains("/") || !value.contains(".")) return@after
                        if (!value.contains(XKM_PACKAGE) && !value.contains(XKM_DEV_PACKAGE)) return@after
                        
                        YLog.debug(tag = TAG, msg = "[SettingsProvider.call] UID=$callingUid pkg=$callingPackage value contains XKM")
                        
                        if (!shouldHideFromPackage(callingPackage)) {
                            YLog.debug(tag = TAG, msg = "[SettingsProvider.call] Not hiding for: $callingPackage")
                            return@after
                        }
                        
                        val filteredValue = value
                            .split(":")
                            .filter { service -> !isXkmServiceString(service) }
                            .joinToString(":")
                        
                        if (filteredValue != value) {
                            bundle.putString("value", filteredValue)
                            YLog.info(tag = TAG, msg = "[SettingsProvider.call] [$callingPackage] FILTERED XKM from Settings!")
                        }
                    } catch (e: Exception) { }
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "SettingsProvider.call hook failed: ${it.message}")
            }
            
            YLog.info(tag = TAG, msg = "SettingsProvider hooks applied!")
        } else {
            YLog.error(tag = TAG, msg = "SettingsProvider class not found!")
        }
        
        try {
            "android.provider.Settings\$Secure".toClass().method {
                name = "getString"
                paramCount = 2
            }.hook {
                after {
                    val key = args(1).cast<String>() ?: return@after
                    
                    if (key == "enabled_accessibility_services") {
                        val callingUid = Binder.getCallingUid()
                        val callingPackage = getCallingPackageFromUid()
                        val originalValue = result?.toString() ?: return@after
                        
                        if (originalValue.contains(XKM_PACKAGE) || originalValue.contains(XKM_DEV_PACKAGE)) {
                            YLog.debug(tag = TAG, msg = "[Settings.Secure] UID=$callingUid pkg=$callingPackage querying enabled_accessibility_services")
                            
                            if (!shouldHideFromPackage(callingPackage)) return@after
                            
                            val filteredValue = originalValue
                                .split(":")
                                .filter { service -> !isXkmServiceString(service) }
                                .joinToString(":")
                            
                            if (filteredValue != originalValue) {
                                YLog.info(tag = TAG, msg = "[Settings.Secure] [$callingPackage] FILTERED XKM!")
                                result = filteredValue
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            YLog.debug(tag = TAG, msg = "Settings.Secure hook failed: ${e.message}")
        }
    }
    
    private fun PackageParam.hookSettingsProviderProcess() {
        YLog.info(tag = TAG, msg = "Setting up SettingsProvider process hooks...")
        
        val settingsProviderClass = "com.android.providers.settings.SettingsProvider".toClassOrNull()
        
        if (settingsProviderClass != null) {
            settingsProviderClass.method {
                name = "query"
            }.hookAll {
                after {
                    try {
                        val cursor = result as? android.database.Cursor ?: return@after
                        val callingUid = Binder.getCallingUid()
                        val callingPackage = getCallingPackageFromUid()
                        
                        if (!shouldHideFromPackage(callingPackage)) return@after
                        
                        YLog.debug(tag = TAG, msg = "[SettingsProvider.query] UID=$callingUid pkg=$callingPackage")
                    } catch (e: Exception) { }
                }
            }.onHookingFailure {
                YLog.debug(tag = TAG, msg = "SettingsProvider.query hook failed: ${it.message}")
            }
            
            settingsProviderClass.method {
                name = "call"
            }.hookAll {
                after {
                    try {
                        val callingUid = Binder.getCallingUid()
                        val callingPackage = getCallingPackageFromUid()
                        
                        val bundle = result as? android.os.Bundle ?: return@after
                        val value = bundle.getString("value") ?: return@after
                        
                        if (!value.contains("/") || !value.contains(".")) return@after
                        if (!value.contains(XKM_PACKAGE) && !value.contains(XKM_DEV_PACKAGE)) return@after
                        
                        YLog.debug(tag = TAG, msg = "[SettingsProvider.call] UID=$callingUid pkg=$callingPackage contains XKM service")
                        
                        if (!shouldHideFromPackage(callingPackage)) {
                            YLog.debug(tag = TAG, msg = "[SettingsProvider.call] Not hiding for: $callingPackage")
                            return@after
                        }
                        
                        val filteredValue = value
                            .split(":")
                            .filter { service -> !isXkmServiceString(service) }
                            .joinToString(":")
                        
                        if (filteredValue != value) {
                            bundle.putString("value", filteredValue)
                            YLog.info(tag = TAG, msg = "[SettingsProvider.call] [$callingPackage] FILTERED XKM from Settings!")
                        }
                    } catch (e: Exception) { }
                }
            }.onHookingFailure {
                YLog.error(tag = TAG, msg = "SettingsProvider.call hook failed: ${it.message}")
            }
            
            YLog.info(tag = TAG, msg = "SettingsProvider process hooks applied!")
        } else {
            YLog.error(tag = TAG, msg = "SettingsProvider class not found in SettingsProvider process!")
        }
    }
    
    private fun PackageParam.hookPackageManagerService() {
        val pmsClass = "com.android.server.pm.PackageManagerService".toClassOrNull()
        
        if (pmsClass != null) {
            pmsClass.method {
                name = "getInstalledPackages"
            }.hook {
                after {
                    val callingPackage = getCallingPackageFromUid()
                    if (!shouldHideFromPackage(callingPackage)) return@after
                    
                    val result = result ?: return@after
                    
                    val listField = result::class.java.getDeclaredField("mList")
                    listField.isAccessible = true
                    
                    @Suppress("UNCHECKED_CAST")
                    val originalList = listField.get(result) as? MutableList<android.content.pm.PackageInfo> ?: return@after
                    
                    val filteredList = originalList.filter { packageInfo ->
                        val pkg = packageInfo.packageName ?: ""
                        pkg != XKM_PACKAGE && pkg != XKM_DEV_PACKAGE
                    }
                    
                    if (filteredList.size != originalList.size) {
                        listField.set(result, filteredList.toMutableList())
                        YLog.info(tag = TAG, msg = "[PMS] [$callingPackage] Filtered XKM from installed packages")
                    }
                }
            }.onHookingFailure {
                YLog.debug(tag = TAG, msg = "PMS getInstalledPackages hook failed: ${it.message}")
            }
            
            pmsClass.method {
                name = "getPackageInfo"
            }.hook {
                before {
                    val pkgName = args(0).cast<String>() ?: return@before
                    val callingPackage = getCallingPackageFromUid()
                    
                    if (!shouldHideFromPackage(callingPackage)) return@before
                    
                    if (pkgName == XKM_PACKAGE || pkgName == XKM_DEV_PACKAGE) {
                        YLog.info(tag = TAG, msg = "[PMS] [$callingPackage] Blocking getPackageInfo for $pkgName")
                        result = null
                    }
                }
            }.onHookingFailure {
                YLog.debug(tag = TAG, msg = "PMS getPackageInfo hook failed: ${it.message}")
            }
            
            pmsClass.method {
                name = "getApplicationInfo"
            }.hook {
                before {
                    val pkgName = args(0).cast<String>() ?: return@before
                    val callingPackage = getCallingPackageFromUid()
                    
                    if (!shouldHideFromPackage(callingPackage)) return@before
                    
                    if (pkgName == XKM_PACKAGE || pkgName == XKM_DEV_PACKAGE) {
                        YLog.info(tag = TAG, msg = "[PMS] [$callingPackage] Blocking getApplicationInfo for $pkgName")
                        result = null
                    }
                }
            }.onHookingFailure {
                YLog.debug(tag = TAG, msg = "PMS getApplicationInfo hook failed: ${it.message}")
            }
        } else {
            YLog.debug(tag = TAG, msg = "PackageManagerService class not found")
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
    
    private fun shouldHideFromPackage(packageName: String?): Boolean {
        if (packageName == null) {
            YLog.debug(tag = TAG, msg = "[ShouldHide] packageName is null")
            return false
        }
        
        if (packageName == XKM_PACKAGE || packageName == XKM_DEV_PACKAGE) return false
        if (packageName == "android") return false
        
        val enabled = isFeatureEnabled()
        if (!enabled) {
            YLog.debug(tag = TAG, msg = "[ShouldHide] Feature disabled for: $packageName")
            return false
        }
        
        val selectedApps = getSelectedApps()
        val shouldHide = selectedApps.contains(packageName)
        YLog.debug(tag = TAG, msg = "[ShouldHide] pkg=$packageName selected=$shouldHide apps=$selectedApps")
        return shouldHide
    }
    
    private fun isFeatureEnabled(): Boolean {
        val now = System.currentTimeMillis()
        if (featureEnabledCache != null && (now - lastCacheTime) < CACHE_DURATION) {
            return featureEnabledCache!!
        }
        
        val enabled = loadFeatureEnabled()
        featureEnabledCache = enabled
        lastCacheTime = now
        return enabled
    }
    
    private fun loadFeatureEnabled(): Boolean {
        return try {
            val prefs = getXPrefs()
            if (prefs != null) {
                prefs.reload()
                val enabledStr = prefs.getString("hide_accessibility_enabled", "false") ?: "false"
                val enabled = enabledStr.equals("true", ignoreCase = true)
                YLog.debug(tag = TAG, msg = "Feature enabled from prefs: $enabled (raw: $enabledStr)")
                return enabled
            }
            readEnabledFromFile()
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error loading feature enabled: ${e.message}")
            false
        }
    }
    
    private fun getSelectedApps(): Set<String> {
        val now = System.currentTimeMillis()
        if (selectedAppsCache != null && (now - lastCacheTime) < CACHE_DURATION) {
            return selectedAppsCache!!
        }
        
        val apps = loadSelectedApps()
        selectedAppsCache = apps
        lastCacheTime = now
        
        if (apps.isNotEmpty()) {
            YLog.debug(tag = TAG, msg = "Loaded ${apps.size} selected apps: $apps")
        }
        
        return apps
    }
    
    private fun loadSelectedApps(): Set<String> {
        return try {
            val prefs = getXPrefs()
            if (prefs != null) {
                prefs.reload()
                val jsonString = prefs.getString("hide_accessibility_apps", "[]") ?: "[]"
                return parseAppsJson(jsonString)
            }
            readAppsFromFile()
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error loading selected apps: ${e.message}")
            emptySet()
        }
    }
    
    private fun getXPrefs(): XSharedPreferences? {
        if (xPrefs == null) {
            try {
                xPrefs = XSharedPreferences(XKM_PACKAGE, PREFS_NAME)
                xPrefs?.makeWorldReadable()
                
                if (xPrefs?.file?.canRead() != true) {
                    xPrefs = XSharedPreferences(XKM_DEV_PACKAGE, PREFS_NAME)
                    xPrefs?.makeWorldReadable()
                }
                
                val canRead = xPrefs?.file?.canRead() == true
                YLog.info(tag = TAG, msg = "XSharedPreferences canRead: $canRead, path: ${xPrefs?.file?.absolutePath}")
            } catch (e: Exception) {
                YLog.error(tag = TAG, msg = "Failed to init XSharedPreferences: ${e.message}")
                xPrefs = null
            }
        }
        return xPrefs
    }
    
    private fun readEnabledFromFile(): Boolean {
        val paths = listOf(
            "/data/data/$XKM_PACKAGE/shared_prefs/$PREFS_NAME.xml",
            "/data/data/$XKM_DEV_PACKAGE/shared_prefs/$PREFS_NAME.xml"
        )
        
        for (path in paths) {
            try {
                val file = java.io.File(path)
                if (file.exists() && file.canRead()) {
                    val content = file.readText()
                    
                    val stringRegex = """<string name="hide_accessibility_enabled">([^<]*)</string>""".toRegex()
                    val stringMatch = stringRegex.find(content)
                    if (stringMatch != null) {
                        val enabled = stringMatch.groupValues[1].equals("true", ignoreCase = true)
                        YLog.debug(tag = TAG, msg = "Read enabled from file (string): $enabled")
                        return enabled
                    }
                    
                    val boolRegex = """<boolean name="hide_accessibility_enabled" value="([^"]*)"[^/]*/>""".toRegex()
                    val boolMatch = boolRegex.find(content)
                    if (boolMatch != null) {
                        val enabled = boolMatch.groupValues[1].equals("true", ignoreCase = true)
                        YLog.debug(tag = TAG, msg = "Read enabled from file (bool): $enabled")
                        return enabled
                    }
                }
            } catch (e: Exception) { }
        }
        return false
    }
    
    private fun readAppsFromFile(): Set<String> {
        val paths = listOf(
            "/data/data/$XKM_PACKAGE/shared_prefs/$PREFS_NAME.xml",
            "/data/data/$XKM_DEV_PACKAGE/shared_prefs/$PREFS_NAME.xml"
        )
        
        for (path in paths) {
            try {
                val file = java.io.File(path)
                if (file.exists() && file.canRead()) {
                    val content = file.readText()
                    val regex = """<string name="hide_accessibility_apps">([^<]*)</string>""".toRegex()
                    val match = regex.find(content)
                    if (match != null) {
                        return parseAppsJson(match.groupValues[1])
                    }
                }
            } catch (e: Exception) { }
        }
        return emptySet()
    }
    
    private fun parseAppsJson(jsonString: String): Set<String> {
        return try {
            if (jsonString.isEmpty() || jsonString == "[]") {
                emptySet()
            } else {
                val jsonArray = org.json.JSONArray(jsonString)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
            }
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Failed to parse apps JSON: ${e.message}")
            emptySet()
        }
    }
}
