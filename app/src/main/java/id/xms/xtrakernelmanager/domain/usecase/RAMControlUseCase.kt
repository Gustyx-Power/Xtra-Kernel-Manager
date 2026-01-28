package id.xms.xtrakernelmanager.domain.usecase

import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.root.RootManager

class RAMControlUseCase {

  companion object {
    @Volatile private var cachedZramStatus: ZramStatus? = null
    @Volatile private var zramStatusCacheTime: Long = 0L
    private const val ZRAM_CACHE_TTL_MS = 10000L // 10 seconds cache

    /** Force refresh cache on next call */
    fun invalidateZramCache() {
      cachedZramStatus = null
      zramStatusCacheTime = 0L
    }
  }

  // ============ GETTERS ============

  /** Get current swappiness value (0-200) */
  suspend fun getSwappiness(): Int {
    val result = RootManager.executeCommand("cat /proc/sys/vm/swappiness 2>/dev/null")
    return result.getOrNull()?.trim()?.toIntOrNull() ?: 60
  }

  /** Get current dirty_ratio value */
  suspend fun getDirtyRatio(): Int {
    val result = RootManager.executeCommand("cat /proc/sys/vm/dirty_ratio 2>/dev/null")
    return result.getOrNull()?.trim()?.toIntOrNull() ?: 20
  }

  /** Get current min_free_kbytes value */
  suspend fun getMinFreeMem(): Int {
    val result = RootManager.executeCommand("cat /proc/sys/vm/min_free_kbytes 2>/dev/null")
    return result.getOrNull()?.trim()?.toIntOrNull() ?: 8192
  }

  /** Get current ZRAM disksize in MB */
  suspend fun getZramSizeMb(): Int {
    val nativeSize = NativeLib.readZramSize()
    if (nativeSize != null && nativeSize > 0) {
      return (nativeSize / (1024 * 1024)).toInt()
    }
    val result = RootManager.executeCommand("cat /sys/block/zram0/disksize 2>/dev/null")
    val sizeBytes = result.getOrNull()?.trim()?.toLongOrNull() ?: 0L
    return (sizeBytes / (1024 * 1024)).toInt()
  }

  /** Get ZRAM status (active/inactive) and used size */
  data class ZramStatus(
      val isActive: Boolean,
      val usedMb: Int, // Original data size (uncompressed) in MB
      val compressedMb: Int, // Compressed data size in MB
      val totalMb: Int, // Total ZRAM disk size in MB
      val compressionRatio: Float,
  )

  fun getZramStatusCached(): ZramStatus? = cachedZramStatus

  suspend fun getZramStatus(): ZramStatus {
    val now = System.currentTimeMillis()
    cachedZramStatus?.let { cached ->
      if (now - zramStatusCacheTime < ZRAM_CACHE_TTL_MS) {
        return cached
      }
    }

    val nativeTotalBytes = NativeLib.readZramSize()
    val nativeRatio = NativeLib.getZramCompressionRatio()
    val nativeOrigDataBytes = NativeLib.getZramOrigDataSize()
    val nativeCompressedBytes = NativeLib.getZramCompressedSize()

    if (nativeTotalBytes != null && nativeTotalBytes > 0) {
      val totalMb = (nativeTotalBytes / (1024 * 1024)).toInt()
      val ratio = nativeRatio ?: 0f
      // Use orig_data_size directly - this is the uncompressed data stored in ZRAM
      val usedMb = ((nativeOrigDataBytes ?: 0L) / (1024 * 1024)).toInt()
      val compressedMb = ((nativeCompressedBytes ?: 0L) / (1024 * 1024)).toInt()
      val status =
          ZramStatus(
              isActive = true,
              usedMb = usedMb,
              compressedMb = compressedMb,
              totalMb = totalMb,
              compressionRatio = ratio,
          )
      // Cache result
      cachedZramStatus = status
      zramStatusCacheTime = now
      return status
    }

    val combinedResult =
        RootManager.executeCommand(
                "grep -q 'zram0' /proc/swaps && echo 'ACTIVE' && cat /sys/block/zram0/disksize && cat /sys/block/zram0/mm_stat || echo 'INACTIVE'"
            )
            .getOrNull()
            ?.trim()

    if (
        combinedResult == null ||
            combinedResult.startsWith("INACTIVE") ||
            !combinedResult.startsWith("ACTIVE")
    ) {
      val status = ZramStatus(false, 0, 0, 0, 0f)
      cachedZramStatus = status
      zramStatusCacheTime = now
      return status
    }

    // Parse combined output: "ACTIVE\n<disksize>\n<mm_stat>"
    val lines = combinedResult.lines()
    if (lines.size < 3) {
      val status = ZramStatus(false, 0, 0, 0, 0f)
      cachedZramStatus = status
      zramStatusCacheTime = now
      return status
    }

    val disksize = lines[1].trim().toLongOrNull() ?: 0L
    val mmStatParts = lines[2].trim().split("\\s+".toRegex())

    val origDataSize = if (mmStatParts.isNotEmpty()) mmStatParts[0].toLongOrNull() ?: 0L else 0L
    val compDataSize = if (mmStatParts.size >= 2) mmStatParts[1].toLongOrNull() ?: 0L else 0L

    val totalMb = (disksize / (1024 * 1024)).toInt()
    val usedMb = (origDataSize / (1024 * 1024)).toInt()
    val compressedMb = (compDataSize / (1024 * 1024)).toInt()
    val ratio = if (compDataSize > 0) origDataSize.toFloat() / compDataSize.toFloat() else 0f

    val status = ZramStatus(true, usedMb, compressedMb, totalMb, ratio)
    cachedZramStatus = status
    zramStatusCacheTime = now
    return status
  }

  /** Get swap file size in MB */
  suspend fun getSwapFileSizeMb(): Int {
    val swapPath = "/data/swap/swapfile"
    val result = RootManager.executeCommand("stat -c%s $swapPath 2>/dev/null || echo 0")
    val sizeBytes = result.getOrNull()?.trim()?.toLongOrNull() ?: 0L
    return (sizeBytes / (1024 * 1024)).toInt()
  }

  /** Get swap file status */
  data class SwapFileStatus(val isActive: Boolean, val sizeMb: Int, val usedMb: Int)

  suspend fun getSwapFileStatus(): SwapFileStatus {
    val swapPath = "/data/swap/swapfile"

    // Check if swap file is active
    val swapsResult = RootManager.executeCommand("grep '$swapPath' /proc/swaps 2>/dev/null")
    val isActive = swapsResult.isSuccess && !swapsResult.getOrNull().isNullOrBlank()

    if (!isActive) {
      val sizeMb = getSwapFileSizeMb()
      return SwapFileStatus(false, sizeMb, 0)
    }

    // Parse /proc/swaps for this file
    val line = swapsResult.getOrNull()?.trim() ?: ""
    val parts = line.split("\\s+".toRegex())
    val sizeMb = if (parts.size > 2) (parts[2].toLongOrNull() ?: 0L) / 1024 else 0L
    val usedMb = if (parts.size > 3) (parts[3].toLongOrNull() ?: 0L) / 1024 else 0L

    return SwapFileStatus(true, sizeMb.toInt(), usedMb.toInt())
  }

  /** Get overall memory stats */
  data class MemoryStats(
      val totalRamMb: Int,
      val freeRamMb: Int,
      val availableRamMb: Int,
      val bufferedMb: Int,
      val cachedMb: Int,
      val totalSwapMb: Int,
      val freeSwapMb: Int,
  )

  suspend fun getMemoryStats(): MemoryStats {
    val nativeMemInfo = NativeLib.readMemInfo()
    if (nativeMemInfo != null) {
      return MemoryStats(
          totalRamMb = (nativeMemInfo.totalKb / 1024).toInt(),
          freeRamMb = (nativeMemInfo.freeKb / 1024).toInt(),
          availableRamMb = (nativeMemInfo.availableKb / 1024).toInt(),
          bufferedMb = (nativeMemInfo.buffersKb / 1024).toInt(),
          cachedMb = (nativeMemInfo.cachedKb / 1024).toInt(),
          totalSwapMb = (nativeMemInfo.swapTotalKb / 1024).toInt(),
          freeSwapMb = (nativeMemInfo.swapFreeKb / 1024).toInt(),
      )
    }
    
    val result = RootManager.executeCommand("cat /proc/meminfo")
    val meminfo = result.getOrNull() ?: ""

    fun extractKb(key: String): Long {
      val regex = "$key:\\s*(\\d+)\\s*kB".toRegex()
      return regex.find(meminfo)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }

    return MemoryStats(
        totalRamMb = (extractKb("MemTotal") / 1024).toInt(),
        freeRamMb = (extractKb("MemFree") / 1024).toInt(),
        availableRamMb = (extractKb("MemAvailable") / 1024).toInt(),
        bufferedMb = (extractKb("Buffers") / 1024).toInt(),
        cachedMb = (extractKb("Cached") / 1024).toInt(),
        totalSwapMb = (extractKb("SwapTotal") / 1024).toInt(),
        freeSwapMb = (extractKb("SwapFree") / 1024).toInt(),
    )
  }

  suspend fun setSwappiness(value: Int): Result<Unit> {
    return RootManager.writeFile("/proc/sys/vm/swappiness", value.toString())
  }

  suspend fun getAvailableCompressionAlgorithms(): List<String> {
    // Try native first
    val nativeAlgos = NativeLib.getAvailableZramAlgorithms()
    if (!nativeAlgos.isNullOrEmpty()) {
      return nativeAlgos
    }

    // Fallback to shell
    val result =
        RootManager.executeCommand("cat /sys/block/zram0/comp_algorithm 2>/dev/null || echo 'lz4'")
    return if (result.isSuccess) {
      val output = result.getOrNull() ?: "lz4"
      output.replace("[", "").replace("]", "").split(" ").filter { it.isNotBlank() }
    } else {
      listOf("lz4", "lzo", "lzo-rle", "zstd", "lz4hc", "842")
    }
  }

  suspend fun getCurrentCompressionAlgorithm(): String {
    val result =
        RootManager.executeCommand("cat /sys/block/zram0/comp_algorithm 2>/dev/null || echo 'lz4'")
    return if (result.isSuccess) {
      val output = result.getOrNull() ?: "lz4"
      // Extract the current algorithm (the one in brackets)
      val regex = "\\[(\\w+(-\\w+)?)]".toRegex()
      regex.find(output)?.groupValues?.get(1) ?: output.trim().split(" ").firstOrNull() ?: "lz4"
    } else {
      "lz4"
    }
  }

  suspend fun setCompressionAlgorithm(algorithm: String): Result<Unit> {
    return RootManager.writeFile("/sys/block/zram0/comp_algorithm", algorithm)
  }

  suspend fun setZRAMSize(
      sizeBytes: Long,
      compressionAlgorithm: String = "lz4",
      onLog: ((String) -> Unit)? = null,
  ): Result<Unit> {
    val disable = sizeBytes <= 0L

    onLog?.invoke("Disabling existing ZRAM...")
    val off = RootManager.executeCommand("swapoff /dev/block/zram0 || true")
    if (off.isFailure) {
      onLog?.invoke("Failed to swapoff zram0")
      return Result.failure(off.exceptionOrNull() ?: Exception("swapoff zram0 failed"))
    }

    if (disable) {
      onLog?.invoke("Setting ZRAM disksize to 0")
      val disksize0 = RootManager.writeFile("/sys/block/zram0/disksize", "0")
      if (disksize0.isSuccess) onLog?.invoke("ZRAM disabled successfully")
      return disksize0
    }

    onLog?.invoke("Resetting ZRAM device...")
    val reset = RootManager.writeFile("/sys/block/zram0/reset", "1")
    if (reset.isFailure) {
      onLog?.invoke("Failed to reset zram0")
      return reset
    }

    onLog?.invoke("Setting compression algorithm to $compressionAlgorithm...")
    val compResult = setCompressionAlgorithm(compressionAlgorithm)
    if (compResult.isFailure) {
      onLog?.invoke("Warning: Failed to set compression algorithm, using default")
    } else {
      onLog?.invoke("Compression algorithm set to $compressionAlgorithm")
    }

    onLog?.invoke("Setting ZRAM disksize to ${sizeBytes / (1024 * 1024)} MB...")
    val sizeResult = RootManager.writeFile("/sys/block/zram0/disksize", sizeBytes.toString())
    if (sizeResult.isFailure) {
      onLog?.invoke("Failed to set disksize")
      return sizeResult
    }

    onLog?.invoke("Running mkswap on zram0...")
    val mk = RootManager.executeCommand("mkswap /dev/block/zram0")
    if (mk.isFailure) {
      onLog?.invoke("mkswap failed")
      return Result.failure(mk.exceptionOrNull() ?: Exception("mkswap zram0 failed"))
    }

    onLog?.invoke("Activating ZRAM swap...")
    val on = RootManager.executeCommand("swapon /dev/block/zram0")
    return if (on.isSuccess) {
      onLog?.invoke("ZRAM swap activated successfully")
      Result.success(Unit)
    } else {
      onLog?.invoke("swapon failed")
      Result.failure(on.exceptionOrNull() ?: Exception("swapon zram0 failed"))
    }
  }

  suspend fun setDirtyRatio(value: Int): Result<Unit> {
    return RootManager.writeFile("/proc/sys/vm/dirty_ratio", value.toString())
  }

  suspend fun setMinFreeMem(value: Int): Result<Unit> {
    return RootManager.writeFile("/proc/sys/vm/min_free_kbytes", value.toString())
  }

  suspend fun setSwapFileSizeMb(sizeMb: Int, onLog: ((String) -> Unit)? = null): Result<Unit> {
    val safeSize = sizeMb.coerceIn(0, 16_384)
    val swapPath = "/data/swap/swapfile"

    if (safeSize == 0) {
      onLog?.invoke("Disabling swap file...")
      val off = RootManager.executeCommand("swapoff $swapPath 2>/dev/null || true")
      if (off.isFailure) {
        onLog?.invoke("Failed to swapoff")
        return Result.failure(off.exceptionOrNull() ?: Exception("swapoff failed"))
      }
      onLog?.invoke("Removing swap file...")
      RootManager.executeCommand("rm -f $swapPath || true")
      onLog?.invoke("Swap file disabled")
      return Result.success(Unit)
    }

    // Cek apakah swapfile sudah ada dengan ukuran yang tepat
    onLog?.invoke("Checking existing swap file...")
    val checkSize = RootManager.executeCommand("stat -c%s $swapPath 2>/dev/null || echo 0")
    val currentSizeBytes = checkSize.getOrNull()?.trim()?.toLongOrNull() ?: 0L
    val targetSizeBytes = safeSize.toLong() * 1024L * 1024L

    val swapsCheck =
        RootManager.executeCommand(
            "grep -q '$swapPath' /proc/swaps && echo active || echo inactive"
        )
    val isActive = swapsCheck.getOrNull()?.trim() == "active"

    if (currentSizeBytes == targetSizeBytes) {
      if (isActive) {
        onLog?.invoke("Swap file already configured (${safeSize} MB), skipping recreate")
        return Result.success(Unit)
      } else {
        onLog?.invoke("Swap file exists but not active, activating...")
        val on = RootManager.executeCommand("swapon $swapPath 2>/dev/null")
        return if (on.isSuccess) {
          onLog?.invoke("Swap file activated successfully")
          Result.success(Unit)
        } else {
          onLog?.invoke("swapon failed, recreating swap file...")
          recreateSwapFile(swapPath, safeSize, onLog)
        }
      }
    }

    onLog?.invoke("Swap file size mismatch or not found, creating new swap file...")
    return recreateSwapFile(swapPath, safeSize, onLog)
  }

  private suspend fun recreateSwapFile(
      swapPath: String,
      sizeMb: Int,
      onLog: ((String) -> Unit)?,
  ): Result<Unit> {
    onLog?.invoke("Creating /data/swap directory...")
    val mkdir = RootManager.executeCommand("mkdir -p /data/swap")
    if (mkdir.isFailure) {
      onLog?.invoke("Failed to create directory")
      return Result.failure(mkdir.exceptionOrNull() ?: Exception("mkdir failed"))
    }

    onLog?.invoke("Deactivating old swap file...")
    RootManager.executeCommand("swapoff $swapPath 2>/dev/null || true")

    onLog?.invoke("Removing old swap file...")
    RootManager.executeCommand("rm -f $swapPath || true")

    onLog?.invoke("Creating ${sizeMb} MB swap file with dd (this may take a while)...")
    val dd = RootManager.executeCommand("dd if=/dev/zero of=$swapPath bs=1M count=$sizeMb 2>&1")
    if (dd.isFailure) {
      onLog?.invoke("dd command failed")
      return Result.failure(dd.exceptionOrNull() ?: Exception("dd failed"))
    }
    onLog?.invoke("Swap file created successfully")

    onLog?.invoke("Setting permissions...")
    val chmod = RootManager.executeCommand("chmod 600 $swapPath")
    if (chmod.isFailure) {
      onLog?.invoke("chmod failed")
      return Result.failure(chmod.exceptionOrNull() ?: Exception("chmod failed"))
    }

    onLog?.invoke("Running mkswap...")
    val mk = RootManager.executeCommand("mkswap $swapPath 2>&1")
    if (mk.isFailure) {
      onLog?.invoke("mkswap failed")
      return Result.failure(mk.exceptionOrNull() ?: Exception("mkswap failed"))
    }
    onLog?.invoke("mkswap completed")

    onLog?.invoke("Activating swap file...")
    val on = RootManager.executeCommand("swapon $swapPath 2>&1")
    return if (on.isSuccess) {
      onLog?.invoke("Swap file activated successfully!")
      Result.success(Unit)
    } else {
      onLog?.invoke("swapon failed")
      Result.failure(on.exceptionOrNull() ?: Exception("swapon failed"))
    }
  }
}
