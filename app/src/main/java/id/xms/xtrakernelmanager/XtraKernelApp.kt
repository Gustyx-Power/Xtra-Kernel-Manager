package id.xms.xtrakernelmanager

import android.app.Application
import com.topjohnwu.superuser.Shell

class XtraKernelApp : Application() {

    companion object {
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

        // Initialize root shell in background
        Shell.getShell { }
    }
}
