package id.xms.xtrakernelmanager.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R

enum class PowerAction(val labelRes: Int, val icon: ImageVector, val command: String) {
    Reboot(R.string.reboot, Icons.Rounded.RestartAlt, "su -c reboot"),
    PowerOff(R.string.power_off, Icons.Rounded.PowerSettingsNew, "su -c reboot -p"),
    Recovery(
        R.string.recovery,
        Icons.Rounded.SystemSecurityUpdateWarning,
        "su -c reboot recovery"
    ),
    Bootloader(
        R.string.bootloader,
        Icons.Rounded.SettingsSystemDaydream,
        "su -c reboot bootloader"
    ),
    SystemUI(R.string.restart_ui, Icons.Rounded.Refresh, "su -c pkill -f com.android.systemui"),
    LockScreen(
        R.string.lockscreen,
        Icons.Rounded.Lock,
        "su -c input keyevent 26",
    );

    @Composable
    fun getLabel(): String {
        return stringResource(id = labelRes)
    }
}

@Composable
fun PowerAction.getLocalizedLabel(): String {
    return stringResource(id = this.labelRes)
}
