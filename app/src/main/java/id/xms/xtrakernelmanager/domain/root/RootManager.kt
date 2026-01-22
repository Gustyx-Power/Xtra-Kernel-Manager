package id.xms.xtrakernelmanager.domain.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import id.xms.xtrakernelmanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootManager {

  private const val TAG = "RootManager"

  // Debug bypass untuk device tertentu (tambahkan model lain jika diperlukan)
  private val DEBUG_BYPASS_DEVICES = setOf("I2219")

  /** Check if current device is in debug bypass list. Only active in DEBUG builds. */
  fun isDebugBypassDevice(): Boolean {
    return BuildConfig.DEBUG && android.os.Build.MODEL in DEBUG_BYPASS_DEVICES
  }

  suspend fun isRootAvailable(): Boolean =
      withContext(Dispatchers.IO) {
        // Bypass untuk debug device
        if (isDebugBypassDevice()) {
          Log.d(TAG, "Debug bypass active for device: ${android.os.Build.MODEL}")
          return@withContext true
        }
        val isRoot = Shell.getShell().isRoot
        Log.d(TAG, "Root available: $isRoot")
        isRoot
      }

  suspend fun executeCommand(command: String): Result<String> =
      withContext(Dispatchers.IO) {
        try {
          Log.d(TAG, "Executing: $command")
          val result = Shell.cmd(command).exec()
          if (result.isSuccess) {
            val output = result.out.joinToString("\n")
            Log.d(TAG, "Success: $output")
            Result.success(output)
          } else {
            val error = result.err.joinToString("\n")
            Log.e(TAG, "Failed: $error")
            Result.failure(Exception("Command failed: $error"))
          }
        } catch (e: Exception) {
          Log.e(TAG, "Exception: ${e.message}", e)
          Result.failure(e)
        }
      }

  suspend fun readFile(path: String): Result<String> =
      withContext(Dispatchers.IO) { executeCommand("cat $path 2>/dev/null") }

  suspend fun writeFile(path: String, content: String): Result<Unit> =
      withContext(Dispatchers.IO) { executeCommand("echo '$content' > $path").map { Unit } }

  suspend fun fileExists(path: String): Boolean =
      withContext(Dispatchers.IO) {
        val result = Shell.cmd("[ -f $path ] && echo exists || [ -d $path ] && echo exists").exec()
        result.out.firstOrNull() == "exists"
      }

  /** Get a system property value */
  suspend fun getProp(name: String): String =
      withContext(Dispatchers.IO) { executeCommand("getprop $name").getOrNull()?.trim() ?: "" }

  /** Set a system property value (requires root for persist.* properties) */
  suspend fun setProp(name: String, value: String): Result<Unit> =
      withContext(Dispatchers.IO) { executeCommand("setprop $name $value").map { Unit } }

  /** Write value to a kernel node/file */
  suspend fun writeToNode(path: String, value: String): Result<Unit> =
      withContext(Dispatchers.IO) { executeCommand("echo '$value' > $path").map { Unit } }

  /** Read value from a kernel node/file */
  suspend fun readFromNode(path: String): String =
      withContext(Dispatchers.IO) {
        executeCommand("cat $path 2>/dev/null").getOrNull()?.trim() ?: ""
      }

  /** Install a KernelSU module from file path */
  suspend fun installKernelSuModule(modulePath: String): Result<Unit> =
      withContext(Dispatchers.IO) {
        Log.d(TAG, "Installing KernelSU module: $modulePath")
        // KernelSU uses ksud to install modules
        val result = executeCommand("ksud module install $modulePath")
        if (result.isSuccess) {
          Log.d(TAG, "Module installed successfully")
          Result.success(Unit)
        } else {
          // Fallback: try magisk module install
          Log.d(TAG, "ksud failed, trying magisk install")
          val magiskResult = executeCommand("magisk --install-module $modulePath")
          if (magiskResult.isSuccess) {
            Log.d(TAG, "Module installed via magisk")
            Result.success(Unit)
          } else {
            Result.failure(Exception("Failed to install module"))
          }
        }
      }

  /** Check if a KernelSU/Magisk module is installed */
  suspend fun isModuleInstalled(moduleId: String): Boolean =
      withContext(Dispatchers.IO) {
        // Check KernelSU modules
        val ksuCheck = Shell.cmd("test -d /data/adb/modules/$moduleId && echo exists").exec()
        ksuCheck.out.firstOrNull() == "exists"
      }

  /** Reboot the device */
  suspend fun rebootDevice(): Result<Unit> =
      withContext(Dispatchers.IO) {
        Log.d(TAG, "Rebooting device...")
        executeCommand("reboot").map { Unit }
      }

  /** Remove a KernelSU/Magisk module by deleting its folder */
  suspend fun removeModule(moduleId: String): Result<Unit> =
      withContext(Dispatchers.IO) {
        Log.d(TAG, "Removing module: $moduleId")
        val modulePath = "/data/adb/modules/$moduleId"
        val result = executeCommand("rm -rf $modulePath")
        if (result.isSuccess) {
          Log.d(TAG, "Module removed successfully: $moduleId")
          Result.success(Unit)
        } else {
          Log.e(TAG, "Failed to remove module: $moduleId")
          Result.failure(Exception("Failed to remove module"))
        }
      }
}
