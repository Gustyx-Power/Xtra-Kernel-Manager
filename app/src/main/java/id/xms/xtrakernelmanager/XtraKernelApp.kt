package id.xms.xtrakernelmanager

import android.app.Application
import com.topjohnwu.superuser.Shell

class XtraKernelApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
        }
    }
}
