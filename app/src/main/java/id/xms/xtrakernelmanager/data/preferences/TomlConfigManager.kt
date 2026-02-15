package id.xms.xtrakernelmanager.data.preferences

import android.os.Environment
import android.util.Log
import id.xms.xtrakernelmanager.data.model.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tomlj.Toml
import org.tomlj.TomlParseResult

class TomlConfigManager(private val context: android.content.Context) {

  private val configDir =
      File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
          if (id.xms.xtrakernelmanager.BuildConfig.DEBUG) "XtraKernelManager-Dev" else "XtraKernelManager",
      )

  private val defaultConfigFile = File(configDir, "tuning_config.toml")

  init {
    if (!configDir.exists()) {
      configDir.mkdirs()
    }
  }

  private suspend fun getCurrentSOCInfo(): SOCInfo {
    return withContext(Dispatchers.IO) {
      // Use native getprop (100x faster than shell)
      val nativeLib = id.xms.xtrakernelmanager.domain.native.NativeLib

      val hardware = nativeLib.getSystemProperty("ro.hardware") ?: "unknown"
      val platform = nativeLib.getSystemProperty("ro.board.platform") ?: "unknown"
      val soc =
          nativeLib.getSystemProperty("ro.hardware.chipname")
              ?: nativeLib.getSystemProperty("ro.soc.model")
              ?: "unknown"
      val device = nativeLib.getSystemProperty("ro.product.device") ?: "unknown"
      val model = nativeLib.getSystemProperty("ro.product.model") ?: "unknown"

      SOCInfo(hardware = hardware, platform = platform, soc = soc, device = device, model = model)
    }
  }

  suspend fun configToTomlString(config: TuningConfig): String {
    val socInfo = getCurrentSOCInfo()

    val lines = mutableListOf<String>()

    // Header
    lines.add("# Xtra Kernel Manager Configuration")
    lines.add("# Export Date: ${System.currentTimeMillis()}")
    lines.add("")

    // Device Info
    lines.add("[device_info]")
    lines.add("hardware = \"${socInfo.hardware}\"")
    lines.add("platform = \"${socInfo.platform}\"")
    lines.add("soc = \"${socInfo.soc}\"")
    lines.add("device = \"${socInfo.device}\"")
    lines.add("model = \"${socInfo.model}\"")
    lines.add("")

    // CPU Clusters
    config.cpuClusters.forEach { cluster ->
      lines.add("[cpu.cluster${cluster.cluster}]")
      lines.add("min_freq = ${cluster.minFreq}")
      lines.add("max_freq = ${cluster.maxFreq}")
      lines.add("governor = \"${cluster.governor}\"")
      lines.add("set_on_boot = ${cluster.setOnBoot}")
      if (cluster.disabledCores.isNotEmpty()) {
        lines.add("disabled_cores = [${cluster.disabledCores.joinToString(", ")}]")
      }
      lines.add("")
    }

    // CPU Global Settings
    lines.add("[cpu]")
    lines.add("set_on_boot = ${config.cpuSetOnBoot}")
    lines.add("")

    // GPU
    config.gpu?.let { gpu ->
      lines.add("[gpu]")
      lines.add("min_freq = ${gpu.minFreq}")
      lines.add("max_freq = ${gpu.maxFreq}")
      lines.add("power_level = ${gpu.powerLevel}")
      lines.add("renderer = \"${gpu.renderer}\"")
      lines.add("")
    }

    // Thermal
    lines.add("[thermal]")
    lines.add("preset = \"${config.thermal.preset}\"")
    lines.add("set_on_boot = ${config.thermal.setOnBoot}")
    lines.add("")

    // RAM
    lines.add("[ram]")
    lines.add("swappiness = ${config.ram.swappiness}")
    lines.add("zram_size = ${config.ram.zramSize}")
    lines.add("dirty_ratio = ${config.ram.dirtyRatio}")
    lines.add("min_free_mem = ${config.ram.minFreeMem}")
    lines.add("swap_size = ${config.ram.swapSize}")
    lines.add("compression_algorithm = \"${config.ram.compressionAlgorithm}\"")
    lines.add("set_on_boot = ${config.ram.setOnBoot}")
    lines.add("")

    // Additional
    lines.add("[additional]")
    lines.add("io_scheduler = \"${config.additional.ioScheduler}\"")
    lines.add("tcp_congestion = \"${config.additional.tcpCongestion}\"")
    lines.add("perf_mode = \"${config.additional.perfMode}\"")
    lines.add("set_on_boot = ${config.additional.setOnBoot}")

    return lines.joinToString("\n")
  }

  suspend fun tomlStringToConfig(tomlString: String): ConfigImportResult? {
    return withContext(Dispatchers.IO) {
      try {
        val toml = Toml.parse(tomlString)

        if (toml.hasErrors()) {
          Log.e("TomlConfigManager", "TOML parsing errors: ${toml.errors()}")
          return@withContext null
        }

        val fileSOC = extractSOCFromToml(toml)
        val currentSOC = getCurrentSOCInfo()

        val isCompatible = checkSOCCompatibility(fileSOC, currentSOC)
        val compatibilityWarning =
            if (!isCompatible) {
              buildCompatibilityWarning(fileSOC, currentSOC)
            } else null

        val config = parseConfigFromToml(toml)

        ConfigImportResult(
            config = config,
            fileSOC = fileSOC,
            currentSOC = currentSOC,
            isCompatible = isCompatible,
            compatibilityWarning = compatibilityWarning,
        )
      } catch (e: Exception) {
        Log.e("TomlConfigManager", "Failed to parse TOML", e)
        null
      }
    }
  }

  private fun extractSOCFromToml(toml: TomlParseResult): SOCInfo {
    val deviceTable = toml.getTable("device_info")
    return SOCInfo(
        hardware = deviceTable?.getString("hardware") ?: "unknown",
        platform = deviceTable?.getString("platform") ?: "unknown",
        soc = deviceTable?.getString("soc") ?: "unknown",
        device = deviceTable?.getString("device") ?: "unknown",
        model = deviceTable?.getString("model") ?: "unknown",
    )
  }

  private fun checkSOCCompatibility(fileSOC: SOCInfo, currentSOC: SOCInfo): Boolean {
    val platformMatch = fileSOC.platform.equals(currentSOC.platform, ignoreCase = true)
    val hardwareMatch = fileSOC.hardware.equals(currentSOC.hardware, ignoreCase = true)
    val socMatch = fileSOC.soc.equals(currentSOC.soc, ignoreCase = true)

    return platformMatch || hardwareMatch || socMatch
  }

  private fun buildCompatibilityWarning(fileSOC: SOCInfo, currentSOC: SOCInfo): String {
    val lines = mutableListOf<String>()
    lines.add("⚠️ SOC Mismatch Warning")
    lines.add("")
    lines.add("Configuration file was exported from a different device:")
    lines.add("• From Device: ${fileSOC.model}")
    lines.add("• From SOC: ${fileSOC.soc}")
    lines.add("• From Platform: ${fileSOC.platform}")
    lines.add("")
    lines.add("Your current device:")
    lines.add("• Device: ${currentSOC.model}")
    lines.add("• SOC: ${currentSOC.soc}")
    lines.add("• Platform: ${currentSOC.platform}")
    lines.add("")
    lines.add("Applying this configuration may cause system instability or not work correctly.")
    lines.add("Proceed with caution!")
    return lines.joinToString("\n")
  }

  private fun parseConfigFromToml(toml: TomlParseResult): TuningConfig {
    val cpuClusters = mutableListOf<CPUClusterConfig>()
    var clusterIndex = 0
    val cpuKeyPrefix = "cpu.cluster"

    while (toml.contains(cpuKeyPrefix + clusterIndex)) {
      val clusterKey = cpuKeyPrefix + clusterIndex
      val clusterTable = toml.getTable(clusterKey)
      if (clusterTable != null) {
        cpuClusters.add(
            CPUClusterConfig(
                cluster = clusterIndex,
                minFreq = clusterTable.getLong("min_freq")?.toInt() ?: 0,
                maxFreq = clusterTable.getLong("max_freq")?.toInt() ?: 0,
                governor = clusterTable.getString("governor") ?: "schedutil",
                setOnBoot = clusterTable.getBoolean("set_on_boot") ?: false,
                disabledCores =
                    clusterTable.getArray("disabled_cores")?.toList()?.mapNotNull {
                      it.toString().toIntOrNull()
                    } ?: emptyList(),
            )
        )
      }
      clusterIndex++
    }

    // CPU Global Settings
    val cpuTable = toml.getTable("cpu")
    val cpuSetOnBoot = cpuTable?.getBoolean("set_on_boot") ?: false

    val gpu =
        if (toml.contains("gpu")) {
          val gpuTable = toml.getTable("gpu")
          GPUConfig(
              minFreq = gpuTable?.getLong("min_freq")?.toInt() ?: 0,
              maxFreq = gpuTable?.getLong("max_freq")?.toInt() ?: 0,
              powerLevel = gpuTable?.getLong("power_level")?.toInt() ?: 0,
              renderer = gpuTable?.getString("renderer") ?: "auto",
          )
        } else null

    val thermalTable = toml.getTable("thermal")
    val thermal = ThermalConfig(
        preset = thermalTable?.getString("preset")?.let { preset ->
          // Handle legacy "default" and empty values
          when {
            preset == "default" || preset.isEmpty() -> "Not Set"
            preset == "class0" -> "Not Set" // Legacy compatibility
            else -> preset
          }
        } ?: "Not Set",
        setOnBoot = thermalTable?.getBoolean("set_on_boot") ?: false,
    )

    val ramTable = toml.getTable("ram")
    val ram =
        RAMConfig(
            swappiness = ramTable?.getLong("swappiness")?.toInt() ?: 60,
            zramSize = ramTable?.getLong("zram_size")?.toInt() ?: 512,
            dirtyRatio = ramTable?.getLong("dirty_ratio")?.toInt() ?: 20,
            minFreeMem = ramTable?.getLong("min_free_mem")?.toInt() ?: 8192,
            swapSize = ramTable?.getLong("swap_size")?.toInt() ?: 0,
            compressionAlgorithm = ramTable?.getString("compression_algorithm") ?: "lz4",
            setOnBoot = ramTable?.getBoolean("set_on_boot") ?: false,
        )

    val additionalTable = toml.getTable("additional")
    val additional =
        AdditionalConfig(
            ioScheduler = additionalTable?.getString("io_scheduler") ?: "",
            tcpCongestion = additionalTable?.getString("tcp_congestion") ?: "",
            perfMode = additionalTable?.getString("perf_mode") ?: "balance",
            setOnBoot = additionalTable?.getBoolean("set_on_boot") ?: false,
        )

    return TuningConfig(
        cpuClusters = cpuClusters,
        gpu = gpu,
        thermal = thermal,
        ram = ram,
        additional = additional,
        cpuSetOnBoot = cpuSetOnBoot,
    )
  }

  suspend fun exportConfig(config: TuningConfig, file: File = defaultConfigFile): Result<Unit> =
      withContext(Dispatchers.IO) {
        try {
          val tomlString = configToTomlString(config)
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
          val result = tomlStringToConfig(tomlString)
          result?.config
        } catch (e: Exception) {
          null
        }
      }

  suspend fun listSavedConfigs(): List<File> =
      withContext(Dispatchers.IO) {
        configDir.listFiles { file -> file.extension == "toml" }?.toList() ?: emptyList()
      }
}

data class SOCInfo(
    val hardware: String,
    val platform: String,
    val soc: String,
    val device: String,
    val model: String,
)

data class ConfigImportResult(
    val config: TuningConfig,
    val fileSOC: SOCInfo,
    val currentSOC: SOCInfo,
    val isCompatible: Boolean,
    val compatibilityWarning: String?,
)
