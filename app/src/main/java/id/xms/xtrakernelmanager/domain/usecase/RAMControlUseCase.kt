package id.xms.xtrakernelmanager.domain.usecase

import id.xms.xtrakernelmanager.domain.root.RootManager

class RAMControlUseCase {

    suspend fun setSwappiness(value: Int): Result<Unit> {
        return RootManager.writeFile("/proc/sys/vm/swappiness", value.toString())
    }

    suspend fun setZRAMSize(sizeBytes: Long): Result<Unit> {
        val resetResult = RootManager.executeCommand("swapoff /dev/block/zram0")
        if (resetResult.isFailure) return Result.failure(resetResult.exceptionOrNull()!!)

        val sizeResult = RootManager.writeFile(
            "/sys/block/zram0/disksize",
            sizeBytes.toString()
        )
        if (sizeResult.isFailure) return sizeResult

        RootManager.executeCommand("mkswap /dev/block/zram0")
        return RootManager.executeCommand("swapon /dev/block/zram0").map { Unit }
    }

    suspend fun setDirtyRatio(value: Int): Result<Unit> {
        return RootManager.writeFile("/proc/sys/vm/dirty_ratio", value.toString())
    }

    suspend fun setMinFreeMem(value: Int): Result<Unit> {
        return RootManager.writeFile("/proc/sys/vm/min_free_kbytes", value.toString())
    }
}
