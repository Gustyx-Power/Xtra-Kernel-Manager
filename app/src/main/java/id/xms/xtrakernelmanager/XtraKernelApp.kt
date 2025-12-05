package id.xms.xtrakernelmanager

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Configuration
import android.util.DisplayMetrics
import com.topjohnwu.superuser.Shell

class XtraKernelApp : Application() {

    companion object {
        private const val TARGET_DENSITY_DPI = 410

        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(10)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Force app to use 410 DPI
        setAppDensity()

        // Initialize root shell in background
        Shell.getShell { }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply density when configuration changes
        setAppDensity()
    }

    @SuppressLint("DiscouragedApi")
    @Suppress("DEPRECATION")
    private fun setAppDensity() {
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        // Set density to 410 DPI (using 420 as closest standard value)
        configuration.densityDpi = DisplayMetrics.DENSITY_420
        displayMetrics.density = TARGET_DENSITY_DPI / 160f
        displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale

        // Apply the configuration
        resources.updateConfiguration(configuration, displayMetrics)
    }
}
