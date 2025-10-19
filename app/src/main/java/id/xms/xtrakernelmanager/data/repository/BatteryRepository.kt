package id.xms.xtrakernelmanager.data.repository

import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.utils.SysfsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatteryRepository {

    suspend fun getBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        val chargeFull = SysfsUtils.getBatteryChargeFull()
        val chargeFullDesign = SysfsUtils.getBatteryChargeFullDesign()
        val healthPercentage = if (chargeFullDesign > 0) {
            (chargeFull.toFloat() / chargeFullDesign.toFloat()) * 100f
        } else {
            100f
        }

        BatteryInfo(
            level = SysfsUtils.getBatteryCapacity(),
            temperature = SysfsUtils.getBatteryTemp(),
            voltage = SysfsUtils.getBatteryVoltage(),
            current = SysfsUtils.getBatteryCurrent(),
            health = SysfsUtils.getBatteryHealth(),
            status = SysfsUtils.readSysfsFile("/sys/class/power_supply/battery/status") ?: "Unknown",
            cycleCount = SysfsUtils.getBatteryCycleCount(),
            chargeFull = chargeFull,
            chargeFullDesign = chargeFullDesign,
            capacity = SysfsUtils.getBatteryCapacity(),
            healthPercentage = healthPercentage
        )
    }
}
