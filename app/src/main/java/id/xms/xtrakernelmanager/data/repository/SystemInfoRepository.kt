package id.xms.xtrakernelmanager.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemInfoRepository(private val context: Context) {

    suspend fun getSystemInfo(): SystemInfo = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val storageStat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = storageStat.blockCountLong * storageStat.blockSizeLong
        val availableStorage = storageStat.availableBlocksLong * storageStat.blockSizeLong

        val selinux = try {
            RootUtils.executeCommand("getenforce").getOrNull()?.trim() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        val fingerprint = Build.FINGERPRINT
        val isTestKey = fingerprint.contains("test-keys")

        SystemInfo(
            androidVersion = getAndroidVersion(),
            kernelVersion = getKernelVersion(),
            deviceModel = getDeviceModel(),
            abi = getAbi(),
            fingerprint = if (isTestKey) "test-keys" else "release-keys",
            selinuxStatus = selinux,
            totalRam = memInfo.totalMem,
            availableRam = memInfo.availMem,
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            zramSize = getZramSize(),
            swappiness = SysfsUtils.getSwappiness()
        )
    }

    private suspend fun getZramSize(): Long = withContext(Dispatchers.IO) {
        SysfsUtils.readSysfsFile("/sys/block/zram0/disksize")?.toLongOrNull() ?: 0L
    }
}
