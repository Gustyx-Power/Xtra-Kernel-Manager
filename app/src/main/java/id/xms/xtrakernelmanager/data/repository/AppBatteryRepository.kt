package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
import id.xms.xtrakernelmanager.data.model.BatteryUsageType
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppBatteryRepository {
  private const val TAG = "AppBatteryRepository"

  suspend fun getAppBatteryUsage(context: Context): List<AppBatteryStats> =
      withContext(Dispatchers.IO) {
        val result = RootManager.executeCommand("dumpsys batterystats --charged")
        if (result.isFailure) return@withContext emptyList()

        val output = result.getOrNull() ?: return@withContext emptyList()
        val statsList = mutableListOf<AppBatteryStats>()
        val packageManager = context.packageManager

        val lines = output.lines()
        var isInPowerSection = false
        var totalDrain = 0.0

        for (line in lines) {
          val trimmed = line.trim()
          
          if (line.contains("Estimated power use (mAh):")) {
            isInPowerSection = true
            continue
          }

          if (isInPowerSection) {
            if (trimmed.startsWith("Capacity:")) {
               val computedMatch = Regex("Computed drain: ([\\d.]+)").find(trimmed)
               if (computedMatch != null) {
                   totalDrain = computedMatch.groupValues[1].toDoubleOrNull() ?: 0.0
               }
               continue
            }

            if (trimmed.startsWith("UID ", ignoreCase = true) || trimmed.startsWith("Uid ", ignoreCase = true)) {
                try {
                    val parts = trimmed.split(":")
                    if (parts.size >= 2) {
                        val uidStr = parts[0].substringAfter(" ").trim()
                        val valPart = parts[1].trim().split(" ")[0]
                        val mah = valPart.toDoubleOrNull() ?: 0.0
                        
                        var percent = 0.0
                        if (trimmed.contains("%)")) {
                            val percentMatch = Regex("\\(([\\d.]+)%\\)").find(trimmed)
                            if (percentMatch != null) {
                                percent = percentMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                            }
                        }
                        
                        if (percent == 0.0 && totalDrain > 0) {
                            percent = (mah / totalDrain) * 100.0
                        }

                        if (mah > 0) {
                            val realUid = parseUid(uidStr)
                            val (appName, pkgName, icon, type) = resolveUid(packageManager, realUid)

                            statsList.add(
                                AppBatteryStats(
                                    uid = realUid,
                                    packageName = pkgName,
                                    appName = appName,
                                    icon = icon,
                                    percent = percent,
                                    usageType = type,
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse line: $line", e)
                }
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("Capacity:") && !trimmed.startsWith("Global") && !trimmed.startsWith("screen:") && !trimmed.startsWith("(")) {
                 if (line.startsWith("  Per-app") || line.startsWith("  All partial")) {
                     isInPowerSection = false
                 }
            }
          }
        }

        statsList.sortedByDescending { it.percent }
      }

  private fun parseUid(uidStr: String): Int {
    return try {
      if (uidStr.startsWith("u0a")) {
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

    if (uid == 1000) return PkgInfo("Android System", "android", null, BatteryUsageType.SYSTEM)
    if (uid == 0) return PkgInfo("Root / Kernel", "root", null, BatteryUsageType.SYSTEM)
    if (uid < 10000) {
      val name = try { pm.getNameForUid(uid) } catch(e: Exception) { null } ?: "System ($uid)"
      return PkgInfo(name, name, null, BatteryUsageType.SYSTEM)
    }

    return try {
      val packages = pm.getPackagesForUid(uid)
      if (!packages.isNullOrEmpty()) {
        val pkgName = packages[0]
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
      val type: BatteryUsageType,
  )
}
