package id.xms.xtrakernelmanager.data.preferences

import android.os.Environment
import com.akuleshov7.ktoml.Toml
import id.xms.xtrakernelmanager.data.model.TuningConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class TomlConfigManager {

    private val configDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "XtraKernelManager"
    )

    private val defaultConfigFile = File(configDir, "tuning_config.toml")

    init {
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }

    suspend fun exportConfig(config: TuningConfig, file: File = defaultConfigFile): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val tomlString = Toml.encodeToString(config)
                file.writeText(tomlString)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun importConfig(file: File = defaultConfigFile): TuningConfig? =
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) return@withContext null
                val tomlString = file.readText()
                Toml.decodeFromString<TuningConfig>(tomlString)
            } catch (e: Exception) {
                null
            }
        }

    suspend fun listSavedConfigs(): List<File> = withContext(Dispatchers.IO) {
        configDir.listFiles { file -> file.extension == "toml" }?.toList() ?: emptyList()
    }
}
