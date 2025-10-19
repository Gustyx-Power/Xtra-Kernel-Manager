package id.xms.xtrakernelmanager.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.utils.RootUtils
import id.xms.xtrakernelmanager.utils.SysfsUtils
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

        val selinux = RootUtils.executeCommand("getenforce").getOrNull() ?: "Unknown"
        val fingerprint = Build.FINGERPRINT
        val isTestKey = fingerprint.contains("test-keys")

        SystemInfo(
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            kernelVersion = System.getProperty("os.version") ?: "Unknown",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            abi = Build.SUPPORTED_ABIS.joinToString(", "),
            fingerprint = if (isTestKey) "test-keys" else "release-keys",
            selinuxStatus = selinux.trim(),
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
