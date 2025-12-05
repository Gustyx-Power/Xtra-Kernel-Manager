package id.xms.xtrakernelmanager.domain.usecase

import id.xms.xtrakernelmanager.domain.root.RootManager

class RAMControlUseCase {

    suspend fun setSwappiness(value: Int): Result<Unit> {
        return RootManager.writeFile("/proc/sys/vm/swappiness", value.toString())
    }

    suspend fun getAvailableCompressionAlgorithms(): List<String> {
        val result = RootManager.executeCommand("cat /sys/block/zram0/comp_algorithm 2>/dev/null || echo 'lz4'")
        return if (result.isSuccess) {
            val output = result.getOrNull() ?: "lz4"
            // Output format: "lzo lzo-rle lz4 [lz4hc] 842 zstd"
            // Parse and extract available algorithms
            output.replace("[", "").replace("]", "")
                .split(" ")
                .filter { it.isNotBlank() }
        } else {
            listOf("lz4", "lzo", "lzo-rle", "zstd", "lz4hc", "842")
        }
    }

    suspend fun getCurrentCompressionAlgorithm(): String {
        val result = RootManager.executeCommand("cat /sys/block/zram0/comp_algorithm 2>/dev/null || echo 'lz4'")
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
        onLog: ((String) -> Unit)? = null
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

    suspend fun setSwapFileSizeMb(
        sizeMb: Int,
        onLog: ((String) -> Unit)? = null
    ): Result<Unit> {
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

        val swapsCheck = RootManager.executeCommand("grep -q '$swapPath' /proc/swaps && echo active || echo inactive")
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
        onLog: ((String) -> Unit)?
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