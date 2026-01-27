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

suspend fun loadRunningProcesses(): List<ProcessInfo> =
    withContext(Dispatchers.IO) {
      try {
        Log.d(TAG, "Loading running processes...")
        // Try to get running processes using ps command
        val psResult = RootManager.executeCommand("ps -A -o PID,RSS,NAME")
        if (psResult.isSuccess) {
          val output = psResult.getOrNull() ?: ""
          Log.d(TAG, "ps command output length: ${output.length}")
          val processes = parseProcessesFromPs(output)
          if (processes.isNotEmpty()) {
            Log.d(TAG, "Found ${processes.size} processes from ps")
            return@withContext processes
          }
        }
        
        // Fallback: try dumpsys meminfo
        Log.d(TAG, "Trying dumpsys meminfo fallback...")
        val meminfoResult = RootManager.executeCommand("dumpsys meminfo")
        if (meminfoResult.isSuccess) {
          val output = meminfoResult.getOrNull() ?: ""
          Log.d(TAG, "meminfo output length: ${output.length}")
          val processes = parseProcessesFromMeminfo(output)
          if (processes.isNotEmpty()) {
            Log.d(TAG, "Found ${processes.size} processes from meminfo")
            return@withContext processes
          }
        }
        
        // If both fail, return empty list
        Log.w(TAG, "No processes found from both methods")
        return@withContext emptyList()
      } catch (e: Exception) {
        Log.e(TAG, "Error loading processes", e)
        emptyList()
      }
    }

private fun parseProcessesFromPs(output: String): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val lines = output.lines()
  
  Log.d(TAG, "Parsing ps output, total lines: ${lines.size}")
  
  // Skip header line
  lines.drop(1).forEach { line ->
    try {
      val parts = line.trim().split(Regex("\\s+"))
      if (parts.size >= 3) {
        val pid = parts[0].toIntOrNull() ?: return@forEach
        val rssKB = parts[1].toIntOrNull() ?: return@forEach
        val processName = parts[2]
        
        // Filter for package names (contains dots) and has reasonable memory usage
        if (processName.contains(".") && rssKB > 10000) {
          // Extract package name from process name
          val packageName = when {
            processName.contains(":") -> processName.substringBefore(":")
            else -> processName
          }
          
          // Skip system processes
          if (!packageName.startsWith("com.android.") && 
              !packageName.startsWith("android.") &&
              !packageName.startsWith("system_")) {
            processes.add(
              ProcessInfo(
                pid = pid,
                packageName = packageName,
                memoryMB = rssKB / 1024f,
                cpuPercent = 0f
              )
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing ps line: $line", e)
    }
  }
  
  Log.d(TAG, "Parsed ${processes.size} processes from ps")
  return processes
}

private fun parseProcessesFromMeminfo(output: String): List<ProcessInfo> {
  val processes = mutableListOf<ProcessInfo>()
  val lines = output.lines()
  
  Log.d(TAG, "Parsing meminfo output, total lines: ${lines.size}")
  
  var currentPackage = ""
  var currentPid = 0
  var currentMemory = 0
  
  lines.forEach { line ->
    try {
      // Look for process headers like "Applications Memory Usage (in Kilobytes):"
      // Then lines like "  12345: com.example.app (pid 12345)"
      if (line.contains("pid") && line.contains(":")) {
        val pidMatch = Regex("""(\d+):\s+(\S+)\s+\(pid\s+(\d+)""").find(line)
        if (pidMatch != null) {
          currentMemory = pidMatch.groupValues[1].toIntOrNull() ?: 0
          currentPackage = pidMatch.groupValues[2]
          currentPid = pidMatch.groupValues[3].toIntOrNull() ?: 0
          
          if (currentMemory > 10000 && 
              !currentPackage.startsWith("com.android.") &&
              !currentPackage.startsWith("android.")) {
            processes.add(
              ProcessInfo(
                pid = currentPid,
                packageName = currentPackage,
                memoryMB = currentMemory / 1024f,
                cpuPercent = 0f
              )
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing meminfo line: $line", e)
    }
  }
  
  Log.d(TAG, "Parsed ${processes.size} processes from meminfo")
  return processes
}

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
