package id.xms.xtrakernelmanager.domain.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootManager {

    private const val TAG = "RootManager"

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val isRoot = Shell.getShell().isRoot
        Log.d(TAG, "Root available: $isRoot")
        isRoot
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
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

    suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        executeCommand("cat $path 2>/dev/null")
    }

    suspend fun writeFile(path: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeCommand("echo '$content' > $path").map { Unit }
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("[ -f $path ] && echo exists || [ -d $path ] && echo exists").exec()
        result.out.firstOrNull() == "exists"
    }

    /**
     * Get a system property value
     */
    suspend fun getProp(name: String): String = withContext(Dispatchers.IO) {
        executeCommand("getprop $name").getOrNull()?.trim() ?: ""
    }

    /**
     * Set a system property value (requires root for persist.* properties)
     */
    suspend fun setProp(name: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeCommand("setprop $name $value").map { Unit }
    }

    /**
     * Write value to a kernel node/file
     */
    suspend fun writeToNode(path: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeCommand("echo '$value' > $path").map { Unit }
    }

    /**
     * Read value from a kernel node/file
     */
    suspend fun readFromNode(path: String): String = withContext(Dispatchers.IO) {
        executeCommand("cat $path 2>/dev/null").getOrNull()?.trim() ?: ""
    }
}
