package id.xms.xtrakernelmanager.ui.screens.misc.shared

import android.content.pm.PackageManager
import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ProcessModels"

data class ProcessInfo(
    val pid: Int,
    val packageName: String,
    val memoryMB: Float,
    val cpuPercent: Float = 0f,
)

suspend fun loadRunningProcesses(context: android.content.Context): List<ProcessInfo> =
    withContext(Dispatchers.IO) {
      try {
        Log.d(TAG, "Loading running processes with realtime memory data...")
        
        // Get list of launchable apps first
        val launchableApps = getLaunchableApps(context)
        Log.d(TAG, "Filtering by ${launchableApps.size} launchable apps")
        
        // Method 1: Try ps command with RSS (Resident Set Size - actual RAM usage)
        Log.d(TAG, "Method 1: Trying ps -A command...")
        val psResult = RootManager.executeCommand("ps -A -o PID,RSS,NAME")
        if (psResult.isSuccess) {
          val output = psResult.getOrNull() ?: ""
          Log.d(TAG, "ps command output length: ${output.length}, first 200 chars: ${output.take(200)}")
          if (output.length > 100) { // Ensure we got actual data
            val processes = parseProcessesFromPs(output, launchableApps)
            if (processes.isNotEmpty()) {
              Log.d(TAG, "✓ Found ${processes.size} processes from ps with realtime RSS")
              return@withContext processes
            }
          }
        }
        
        // Method 2: Try simpler ps format without options
        Log.d(TAG, "Method 2: Trying simple ps command...")
        val psSimpleResult = RootManager.executeCommand("ps")
        if (psSimpleResult.isSuccess) {
          val output = psSimpleResult.getOrNull() ?: ""
          Log.d(TAG, "Simple ps output length: ${output.length}, first 200 chars: ${output.take(200)}")
          if (output.length > 100) {
            val processes = parseSimplePs(output, launchableApps)
            if (processes.isNotEmpty()) {
              Log.d(TAG, "✓ Found ${processes.size} processes from simple ps")
              return@withContext processes
            }
          }
        }
        
        // Method 3: Fallback to dumpsys meminfo (more detailed but slower)
        Log.d(TAG, "Method 3: Trying dumpsys meminfo...")
        val meminfoResult = RootManager.executeCommand("dumpsys meminfo")
        if (meminfoResult.isSuccess) {
          val output = meminfoResult.getOrNull() ?: ""
          Log.d(TAG, "meminfo output length: ${output.length}")
          if (output.length > 100) {
            val processes = parseProcessesFromMeminfo(output, launchableApps)
            if (processes.isNotEmpty()) {
              Log.d(TAG, "✓ Found ${processes.size} processes from meminfo")
              return@withContext processes
            }
          }
        }
        
        // If all methods fail, return empty list
        Log.w(TAG, "⚠ No processes found from all methods")
        return@withContext emptyList()
      } catch (e: Exception) {
        Log.e(TAG, "Error loading processes", e)
        emptyList()
      }
    }

/**
 * Get list of launchable app package names (apps that appear in launcher)
 * This is cached to avoid repeated PackageManager queries
 */
private var launchableAppsCache: Set<String>? = null
private var launchableAppsCacheTime: Long = 0
private const val CACHE_VALIDITY_MS = 60000L // 1 minute

private fun getLaunchableApps(context: android.content.Context): Set<String> {
  val currentTime = System.currentTimeMillis()
  
  // Return cached result if still valid
  if (launchableAppsCache != null && (currentTime - launchableAppsCacheTime) < CACHE_VALIDITY_MS) {
    return launchableAppsCache!!
  }
  
  val launchableApps = mutableSetOf<String>()
  
  try {
    val pm = context.packageManager
    val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
    mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    
    val apps = pm.queryIntentActivities(mainIntent, 0)
    for (app in apps) {
      launchableApps.add(app.activityInfo.packageName)
    }
    
    Log.d(TAG, "Found ${launchableApps.size} launchable apps")
  } catch (e: Exception) {
    Log.e(TAG, "Error getting launchable apps", e)
  }
  
  // Update cache
  launchableAppsCache = launchableApps
  launchableAppsCacheTime = currentTime
  
  return launchableApps
}

private fun parseProcessesFromPs(output: String, launchableApps: Set<String>): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val processMap = mutableMapOf<String, ProcessInfo>() // To aggregate by package name
  val lines = output.lines()
  
  Log.d(TAG, "Parsing ps output, total lines: ${lines.size}")
  
  // Skip header line
  lines.drop(1).forEach { line ->
    try {
      val trimmedLine = line.trim()
      if (trimmedLine.isEmpty()) return@forEach
      
      val parts = trimmedLine.split(Regex("\\s+"))
      if (parts.size >= 3) {
        val pid = parts[0].toIntOrNull() ?: return@forEach
        val rssKB = parts[1].toIntOrNull() ?: return@forEach
        val processName = parts[2]
        
        // Must contain dot (package name) and use at least 1MB RAM
        if (processName.contains(".") && rssKB > 1024) {
          // Extract package name from process name (remove :service, :provider, etc)
          val packageName = when {
            processName.contains(":") -> processName.substringBefore(":")
            else -> processName
          }
          
          // Only show apps that are launchable (appear in launcher)
          if (launchableApps.contains(packageName)) {
            // Aggregate memory by package name
            if (processMap.containsKey(packageName)) {
              val existing = processMap[packageName]!!
              processMap[packageName] = ProcessInfo(
                pid = existing.pid, // Keep first PID
                packageName = packageName,
                memoryMB = existing.memoryMB + (rssKB / 1024f),
                cpuPercent = 0f
              )
              Log.d(TAG, "Aggregating: $packageName, added ${rssKB / 1024f}MB, total: ${processMap[packageName]!!.memoryMB}MB")
            } else {
              processMap[packageName] = ProcessInfo(
                pid = pid,
                packageName = packageName,
                memoryMB = rssKB / 1024f,
                cpuPercent = 0f
              )
              Log.d(TAG, "New launchable app: $packageName, PID: $pid, Memory: ${rssKB / 1024f}MB")
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing ps line: $line", e)
    }
  }
  
  processes.addAll(processMap.values)
  Log.d(TAG, "Parsed ${processes.size} launchable app processes from ps, total RAM: ${processes.sumOf { it.memoryMB.toDouble() }}MB")
  return processes.sortedByDescending { it.memoryMB }
}

private fun parseSimplePs(output: String, launchableApps: Set<String>): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val processMap = mutableMapOf<String, ProcessInfo>()
  val lines = output.lines()
  
  Log.d(TAG, "Parsing simple ps output, total lines: ${lines.size}")
  
  // Simple ps format: USER PID PPID VSZ RSS WCHAN PC NAME
  lines.drop(1).forEach { line ->
    try {
      val trimmedLine = line.trim()
      if (trimmedLine.isEmpty()) return@forEach
      
      val parts = trimmedLine.split(Regex("\\s+"))
      if (parts.size >= 9) {
        val pid = parts[1].toIntOrNull() ?: return@forEach
        val rssKB = parts[4].toIntOrNull() ?: return@forEach
        val processName = parts[8]
        
        // Must contain dot and use at least 1MB RAM
        if (processName.contains(".") && rssKB > 1024) {
          val packageName = when {
            processName.contains(":") -> processName.substringBefore(":")
            else -> processName
          }
          
          // Only show launchable apps
          if (launchableApps.contains(packageName)) {
            // Aggregate memory by package name
            if (processMap.containsKey(packageName)) {
              val existing = processMap[packageName]!!
              processMap[packageName] = ProcessInfo(
                pid = existing.pid,
                packageName = packageName,
                memoryMB = existing.memoryMB + (rssKB / 1024f),
                cpuPercent = 0f
              )
            } else {
              processMap[packageName] = ProcessInfo(
                pid = pid,
                packageName = packageName,
                memoryMB = rssKB / 1024f,
                cpuPercent = 0f
              )
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing simple ps line: $line", e)
    }
  }
  
  processes.addAll(processMap.values)
  Log.d(TAG, "Parsed ${processes.size} launchable app processes from simple ps, total RAM: ${processes.sumOf { it.memoryMB.toDouble() }}MB")
  return processes.sortedByDescending { it.memoryMB }
}

private fun parseProcessesFromMeminfo(output: String, launchableApps: Set<String>): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val processMap = mutableMapOf<String, ProcessInfo>()
  val lines = output.lines()
  
  Log.d(TAG, "Parsing meminfo output, total lines: ${lines.size}")
  
  var inAppSection = false
  
  lines.forEach { line ->
    try {
      val trimmedLine = line.trim()
      
      // Detect start of app memory section
      if (trimmedLine.contains("Total PSS by process:", ignoreCase = true) ||
          trimmedLine.contains("App Summary", ignoreCase = true)) {
        inAppSection = true
        Log.d(TAG, "Found app section start")
        return@forEach
      }
      
      // Stop at next major section
      if (inAppSection && trimmedLine.startsWith("Total") && !trimmedLine.contains("PSS")) {
        inAppSection = false
        Log.d(TAG, "App section ended")
        return@forEach
      }
      
      if (inAppSection) {
        // Parse lines like: "  123,456K: com.example.app (pid 12345)"
        // or: "  123456: com.example.app (pid 12345 / activities)"
        val memPattern = Regex("""^\s*([0-9,]+)K?:\s+(\S+)\s+\(pid\s+(\d+)""")
        val match = memPattern.find(trimmedLine)
        
        if (match != null) {
          val memoryStr = match.groupValues[1].replace(",", "")
          val packageName = match.groupValues[2]
          val pid = match.groupValues[3].toIntOrNull() ?: return@forEach
          val memoryKB = memoryStr.toIntOrNull() ?: return@forEach
          
          // Must contain dot and use at least 1MB RAM
          if (packageName.contains(".") && memoryKB > 1024) {
            // Only show launchable apps
            if (launchableApps.contains(packageName)) {
              // Aggregate memory by package name
              if (processMap.containsKey(packageName)) {
                val existing = processMap[packageName]!!
                processMap[packageName] = ProcessInfo(
                  pid = existing.pid,
                  packageName = packageName,
                  memoryMB = existing.memoryMB + (memoryKB / 1024f),
                  cpuPercent = 0f
                )
                Log.d(TAG, "Aggregating meminfo: $packageName, added ${memoryKB / 1024f}MB, total: ${processMap[packageName]!!.memoryMB}MB")
              } else {
                processMap[packageName] = ProcessInfo(
                  pid = pid,
                  packageName = packageName,
                  memoryMB = memoryKB / 1024f,
                  cpuPercent = 0f
                )
                Log.d(TAG, "New meminfo launchable app: $packageName, PID: $pid, Memory: ${memoryKB / 1024f}MB")
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing meminfo line: $line", e)
    }
  }
  
  processes.addAll(processMap.values)
  Log.d(TAG, "Parsed ${processes.size} launchable app processes from meminfo, total RAM: ${processes.sumOf { it.memoryMB.toDouble() }}MB")
  return processes.sortedByDescending { it.memoryMB }
}

// Remove the old isUserOrImportantApp function as it's no longer needed

/**
 * Safely get app icon drawable
 * Returns null if package not found or any error occurs
 */
fun getAppIconSafe(
    context: android.content.Context,
    packageName: String
): android.graphics.drawable.Drawable? {
  return try {
    Log.d(TAG, "Getting icon for package: $packageName")
    val icon = context.packageManager.getApplicationIcon(packageName)
    Log.d(TAG, "Successfully got icon for: $packageName")
    icon
  } catch (e: PackageManager.NameNotFoundException) {
    Log.w(TAG, "Package not found: $packageName")
    null
  } catch (e: Exception) {
    Log.e(TAG, "Error getting icon for $packageName", e)
    null
  }
}
