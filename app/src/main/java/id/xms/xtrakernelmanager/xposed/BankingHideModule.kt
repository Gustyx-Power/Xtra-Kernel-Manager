package id.xms.xtrakernelmanager.xposed

import android.accessibilityservice.AccessibilityServiceInfo
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

/**
 * BankingHideModule - Xposed Module for hiding accessibility services per-app
 * 
 * This module hooks into AccessibilityManager methods to filter out XKM's Game Monitor
 * accessibility service from the list returned to configured apps.
 * 
 * REQUIREMENTS:
 * - LSPosed (Zygisk) or EdXposed installed and active
 * - Module enabled in LSPosed Manager
 * - Only "Android System" needs to be selected in LSPosed scope
 * 
 * CONFIGURATION:
 * - Apps are configured through XKM settings (per-app basis)
 * - Uses fallback list for common banking apps
 */
@InjectYukiHookWithXposed
class BankingHideModule : IYukiHookXposedInit {
    
    companion object {
        private const val TAG = "XKM-AccessibilityHide"
        
        // XKM's accessibility service package and ID
        private const val XKM_PACKAGE = "id.xms.xtrakernelmanager"
        private const val XKM_DEV_PACKAGE = "id.xms.xtrakernelmanager.dev"
        
        // Common banking and payment apps (fallback list)
        private val BANKING_APPS = setOf(
            "id.co.bankbkemobile.digitalbank",
            "id.co.bri.brimo",
            "com.bca",
            "id.co.bankmandiri.livin",
            "id.co.bni.mobilebni",
            "id.co.bankjago.app",
            "id.dana",
            "ovo.id",
            "com.gojek.app",
            "com.shopee.id",
            "com.telkom.mwallet",
            "id.co.bca.blu",
            "com.ocbc.mobile",
            "id.neobank",
            "com.btpn.dc",
            "net.npointl.permatanet",
            "id.co.cimbniaga.mobile.android",
            "com.maybank2u.life",
            "id.co.bankmega.meganet",
            "com.panin.mpin"
        )
    }
    
    override fun onInit() = YukiHookAPI.configs {
        isDebug = false
    }
    
    override fun onHook() = YukiHookAPI.encase {
        val currentPackage = packageName
        
        // Check if current app should have accessibility hidden
        if (shouldHideAccessibilityForApp(currentPackage)) {
            YLog.info(tag = TAG, msg = "Hooking accessibility for app: $currentPackage")
            
            // Hook AccessibilityManager.getEnabledAccessibilityServiceList
            "android.view.accessibility.AccessibilityManager".toClass().method {
                name = "getEnabledAccessibilityServiceList"
                paramCount = 1
            }.hook {
                after {
                    val originalList = result as? List<*> ?: return@after
                    
                    @Suppress("UNCHECKED_CAST")
                    val filteredList = (originalList as List<AccessibilityServiceInfo>)
                        .filter { serviceInfo ->
                            !isXkmService(serviceInfo)
                        }
                    
                    if (filteredList.size != originalList.size) {
                        YLog.debug(tag = TAG, msg = "Filtered ${originalList.size - filteredList.size} XKM services from enabled list")
                    }
                    
                    result = filteredList
                }
            }
            
            // Hook AccessibilityManager.getInstalledAccessibilityServiceList
            "android.view.accessibility.AccessibilityManager".toClass().method {
                name = "getInstalledAccessibilityServiceList"
            }.hook {
                after {
                    val originalList = result as? List<*> ?: return@after
                    
                    @Suppress("UNCHECKED_CAST")
                    val filteredList = (originalList as List<AccessibilityServiceInfo>)
                        .filter { serviceInfo ->
                            !isXkmService(serviceInfo)
                        }
                    
                    if (filteredList.size != originalList.size) {
                        YLog.debug(tag = TAG, msg = "Filtered ${originalList.size - filteredList.size} XKM services from installed list")
                    }
                    
                    result = filteredList
                }
            }
            
            // Hook Settings.Secure.getString for ENABLED_ACCESSIBILITY_SERVICES
            "android.provider.Settings.Secure".toClass().method {
                name = "getString"
                paramCount = 2
            }.hook {
                after {
                    val key = args(1).string() ?: return@after
                    
                    if (key == "enabled_accessibility_services") {
                        val originalValue = result?.toString() ?: return@after
                        
                        // Filter out XKM services from the string
                        val filteredValue = originalValue
                            .split(":")
                            .filter { service ->
                                !service.contains(XKM_PACKAGE) &&
                                !service.contains(XKM_DEV_PACKAGE) &&
                                !service.contains("GameMonitorService")
                            }
                            .joinToString(":")
                        
                        if (filteredValue != originalValue) {
                            YLog.debug(tag = TAG, msg = "Filtered ENABLED_ACCESSIBILITY_SERVICES setting")
                        }
                        
                        result = filteredValue
                    }
                }
            }
            
            // Hook PackageManager.getInstalledPackages to hide XKM if needed
            "android.content.pm.PackageManager".toClass().method {
                name = "getInstalledPackages"
                paramCount = 1
            }.hook {
                after {
                    val originalList = result as? List<*> ?: return@after
                    
                    @Suppress("UNCHECKED_CAST")
                    val filteredList = (originalList as List<android.content.pm.PackageInfo>)
                        .filter { packageInfo ->
                            val pkg = packageInfo.packageName ?: ""
                            pkg != XKM_PACKAGE && pkg != XKM_DEV_PACKAGE
                        }
                    
                    if (filteredList.size != originalList.size) {
                        YLog.debug(tag = TAG, msg = "Filtered XKM packages from installed packages list")
                    }
                    
                    result = filteredList
                }
            }
            
            YLog.info(tag = TAG, msg = "Successfully hooked accessibility APIs for $currentPackage")
        }
    }
    
    /**
     * Check if accessibility should be hidden for the given app
     * Reads from XKM preferences with fallback to default list
     */
    private fun shouldHideAccessibilityForApp(packageName: String): Boolean {
        return try {
            // Try to read from XKM preferences first
            val hideAccessibilityApps = getHideAccessibilityApps()
            val isEnabled = getHideAccessibilityEnabled()
            
            if (!isEnabled) {
                return false
            }
            
            // Check if app is in configured list or fallback list
            hideAccessibilityApps.contains(packageName) || BANKING_APPS.contains(packageName)
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error checking app configuration: ${e.message}")
            // Fallback to hardcoded list
            BANKING_APPS.contains(packageName)
        }
    }
    
    /**
     * Get list of apps that should have accessibility hidden from XKM preferences
     */
    private fun getHideAccessibilityApps(): Set<String> {
        return try {
            // Try to read from system properties first
            val hideAppsProperty = getSystemProperty("persist.xkm.hide_accessibility_apps", "")
            if (hideAppsProperty.isNotEmpty()) {
                return hideAppsProperty.split(",").filter { it.isNotEmpty() }.toSet()
            }
            
            // Try to read from shared preferences file directly
            val appsFromFile = readAppsFromPrefsFile()
            if (appsFromFile.isNotEmpty()) {
                return appsFromFile
            }
            
            // Fallback to empty set
            emptySet()
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error loading hide accessibility apps: ${e.message}")
            emptySet()
        }
    }
    
    /**
     * Read apps list from XKM preferences file
     */
    private fun readAppsFromPrefsFile(): Set<String> {
        return try {
            // Try to read from /data/data/id.xms.xtrakernelmanager/shared_prefs/xkm_sync_prefs.xml
            val prefsFile = java.io.File("/data/data/id.xms.xtrakernelmanager/shared_prefs/xkm_sync_prefs.xml")
            if (prefsFile.exists()) {
                val content = prefsFile.readText()
                // Simple XML parsing to extract hide_accessibility_apps value
                val regex = """<string name="hide_accessibility_apps">(.*?)</string>""".toRegex()
                val match = regex.find(content)
                if (match != null) {
                    val jsonString = match.groupValues[1]
                    val jsonArray = org.json.JSONArray(jsonString)
                    val apps = mutableSetOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        apps.add(jsonArray.getString(i))
                    }
                    return apps
                }
            }
            emptySet()
        } catch (e: Exception) {
            YLog.debug(tag = TAG, msg = "Could not read from prefs file: ${e.message}")
            emptySet()
        }
    }
    
    /**
     * Check if hide accessibility feature is enabled
     */
    private fun getHideAccessibilityEnabled(): Boolean {
        return try {
            // Try system property first
            val enabledProperty = getSystemProperty("persist.xkm.hide_accessibility_enabled", "")
            if (enabledProperty.isNotEmpty()) {
                return enabledProperty.toBoolean()
            }
            
            // Try to read from shared preferences file
            val prefsFile = java.io.File("/data/data/id.xms.xtrakernelmanager/shared_prefs/xkm_sync_prefs.xml")
            if (prefsFile.exists()) {
                val content = prefsFile.readText()
                val regex = """<string name="hide_accessibility_enabled">(.*?)</string>""".toRegex()
                val match = regex.find(content)
                if (match != null) {
                    return match.groupValues[1].toBoolean()
                }
            }
            
            // Default to true for fallback list
            true
        } catch (e: Exception) {
            YLog.error(tag = TAG, msg = "Error checking hide accessibility enabled: ${e.message}")
            true // Default to enabled for fallback list
        }
    }
    
    /**
     * Get system property value
     */
    private fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, defaultValue) as String
        } catch (e: Exception) {
            YLog.debug(tag = TAG, msg = "Could not read system property $key: ${e.message}")
            defaultValue
        }
    }
    
    /**
     * Check if the given AccessibilityServiceInfo belongs to XKM
     */
    private fun isXkmService(serviceInfo: AccessibilityServiceInfo): Boolean {
        val serviceId = serviceInfo.id ?: ""
        val resolveInfo = serviceInfo.resolveInfo
        val servicePackage = resolveInfo?.serviceInfo?.packageName ?: ""
        val serviceName = resolveInfo?.serviceInfo?.name ?: ""
        
        return servicePackage == XKM_PACKAGE ||
                servicePackage == XKM_DEV_PACKAGE ||
                serviceName.contains("GameMonitorService") ||
                serviceId.contains(XKM_PACKAGE) ||
                serviceId.contains(XKM_DEV_PACKAGE)
    }
}
