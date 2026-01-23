package id.xms.xtrakernelmanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

object RootShell {
    suspend fun execute(command: String) =
        withContext(Dispatchers.IO) {
            try {
                // Using Runtime.exec to run 'su'
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)

                // Writing command
                os.writeBytes(command + "\n")
                os.writeBytes("exit\n")
                os.flush()

                // Waiting for process to finish
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
