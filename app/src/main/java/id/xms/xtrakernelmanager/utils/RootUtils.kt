package id.xms.xtrakernelmanager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootUtils {

    private var rootGranted: Boolean? = null

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (rootGranted == null) {
            try {
                // Method 1: Try LibSu
                val libsuResult = Shell.getShell().isRoot
                if (libsuResult) {
                    rootGranted = true
                    return@withContext true
                }

                // Method 2: Try direct command
                val result = Shell.cmd("su -c id").exec()
                if (result.isSuccess && result.out.any { it.contains("uid=0") }) {
                    rootGranted = true
                    return@withContext true
                }

                // Method 3: Check su binary
                val whichSu = Shell.cmd("which su").exec()
                if (whichSu.isSuccess && whichSu.out.isNotEmpty()) {
                    rootGranted = true
                    return@withContext true
                }

                rootGranted = false
            } catch (e: Exception) {
                rootGranted = false
            }
        }
        rootGranted ?: false
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                Result.success(result.out.joinToString("\n"))
            } else {
                Result.failure(Exception("Command failed: ${result.err.joinToString("\n")}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeCommands(vararg commands: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val result = Shell.cmd(*commands).exec()
                if (result.isSuccess) {
                    Result.success(result.out)
                } else {
                    Result.failure(Exception("Commands failed: ${result.err.joinToString("\n")}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        executeCommand("cat $path").getOrNull()?.trim()
    }

    suspend fun writeFile(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        executeCommand("echo '$value' > $path").isSuccess
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        executeCommand("[ -f $path ] && echo 'exists' || echo 'not_exists'")
            .getOrNull()?.contains("exists") ?: false
    }

    suspend fun directoryExists(path: String): Boolean = withContext(Dispatchers.IO) {
        executeCommand("[ -d $path ] && echo 'exists' || echo 'not_exists'")
            .getOrNull()?.contains("exists") ?: false
    }

    suspend fun listFiles(path: String): List<String> = withContext(Dispatchers.IO) {
        executeCommand("ls -1 $path").getOrNull()?.split("\n")?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}
