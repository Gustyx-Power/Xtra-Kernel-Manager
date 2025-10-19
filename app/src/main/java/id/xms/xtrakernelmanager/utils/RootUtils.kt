package id.xms.xtrakernelmanager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootUtils {

    private var rootGranted: Boolean? = null

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (rootGranted == null) {
            rootGranted = Shell.isAppGrantedRoot() ?: false
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
