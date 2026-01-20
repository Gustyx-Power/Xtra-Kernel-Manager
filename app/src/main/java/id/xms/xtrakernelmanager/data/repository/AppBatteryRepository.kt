package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
import id.xms.xtrakernelmanager.data.model.BatteryUsageType
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

object AppBatteryRepository {
    private const val TAG = "AppBatteryRepository"

    suspend fun getAppBatteryUsage(context: Context): List<AppBatteryStats> = withContext(Dispatchers.IO) {
        val result = RootManager.executeCommand("dumpsys batterystats --charged")
        if (result.isFailure) return@withContext emptyList()

        val output = result.getOrNull() ?: return@withContext emptyList()
        val statsList = mutableListOf<AppBatteryStats>()
        val packageManager = context.packageManager

        // Parse "Estimated power use (mAh):" section
        // Example line: "    Uid 1000: 20.5 (10%)"
        // Example line: "    Uid u0a123: 5.0 (2%)"
        
        val lines = output.lines()
        var isInPowerSection = false
        val powerLineRegex = Pattern.compile("\\s+Uid\\s+(\\S+):\\s+([\\d.]+)\\s+\\(.+\\)") // Matches "Uid <uid>: <mah>" but simpler logic below might be safer
        
        // Simpler parsing strategy: locate "Estimated power use" and read following lines until indentation changes or section ends
        
        for (line in lines) {
            if (line.contains("Estimated power use (mAh):")) {
                isInPowerSection = true
                continue
            }
            
            if (isInPowerSection) {
                if (line.isBlank()) continue
                // Stop if section ends (usually next section starts with non-indented or specific headers)
                // But dumpsys structure varies. Let's look for "Uid" lines specifically within this block.
                
                val trimmed = line.trim()
                if (trimmed.startsWith("Capacity:") || trimmed.startsWith("Global")) continue
                
                // If line doesn't start with space, we might be out of section? 
                // dumpsys output is nested. 
                // Safeguard: only process lines containing "Uid "
                
                if (trimmed.startsWith("Uid ")) {
                   try {
                       // Format: "Uid u0a196: 5.17 (3.5%)" or "Uid 0: 10.5 (10%)"
                       // Split by colon
                       val parts = trimmed.split(":")
                       if (parts.size >= 2) {
                           val uidPart = parts[0].trim().replace("Uid ", "")
                           val valPart = parts[1].trim() // "5.17 (3.5%)"
                           
                           // Extract percent
                           val percentStart = valPart.lastIndexOf("(")
                           val percentEnd = valPart.lastIndexOf("%)")
                           
                           if (percentStart != -1 && percentEnd != -1 && percentEnd > percentStart) {
                               var percentStr = valPart.substring(percentStart + 1, percentEnd)
                               // Sometimes it might be "(0.1%)" -> "0.1"
                               val percent = percentStr.toDoubleOrNull() ?: 0.0
                               
                               // Parse UID
                               // u0a196 -> needs conversion to int if possible, or mapping
                               // u0a196 is user 0 app 196 -> 10196
                               val realUid = parseUid(uidPart)
                               
                               val (appName, pkgName, icon, type) = resolveUid(packageManager, realUid)
                               
                               if (percent > 0.0) { // Filter 0%
                                   statsList.add(AppBatteryStats(
                                       uid = realUid,
                                       packageName = pkgName,
                                       appName = appName,
                                       icon = icon,
                                       percent = percent,
                                       usageType = type
                                   ))
                               }
                           }
                       }
                   } catch (e: Exception) {
                       Log.e(TAG, "Failed to parse line: $line", e)
                   }
                }
            }
        }
        
        // Sort by percent descending
        statsList.sortedByDescending { it.percent }
    }

    private fun parseUid(uidStr: String): Int {
        return try {
            if (uidStr.startsWith("u0a")) {
                 // u0a123 -> 10000 + 123 = 10123
                 val appId = uidStr.substring(3).toInt()
                 10000 + appId
            } else {
                uidStr.toInt()
            }
        } catch (e: Exception) {
            -1
        }
    }

    private fun resolveUid(pm: PackageManager, uid: Int): PkgInfo {
        if (uid == -1) return PkgInfo("Unknown", null, null, BatteryUsageType.SYSTEM)
        
        // System UIDs
        if (uid == 1000) return PkgInfo("Android System", "android", null, BatteryUsageType.SYSTEM)
        if (uid < 10000) {
             // Try to get name for system uid
             val name = pm.getNameForUid(uid) ?: "System ($uid)"
             return PkgInfo(name, name, null, BatteryUsageType.SYSTEM)
        }

        return try {
            val packages = pm.getPackagesForUid(uid)
            if (!packages.isNullOrEmpty()) {
                val pkgName = packages[0] // take first
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                PkgInfo(appName, pkgName, icon, BatteryUsageType.APP)
            } else {
                 PkgInfo("Removed App", null, null, BatteryUsageType.APP)
            }
        } catch (e: Exception) {
            PkgInfo("Unknown ($uid)", null, null, BatteryUsageType.APP)
        }
    }

    data class PkgInfo(
        val label: String,
        val pkgName: String?,
        val icon: android.graphics.drawable.Drawable?,
        val type: BatteryUsageType
    )
}
