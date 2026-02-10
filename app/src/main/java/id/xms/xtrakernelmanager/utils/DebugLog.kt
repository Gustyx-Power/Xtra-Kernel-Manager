package id.xms.xtrakernelmanager.utils

import android.util.Log
import id.xms.xtrakernelmanager.BuildConfig

/**
 * Debug logging utility that automatically disables logs in release builds.
 * This improves performance by eliminating logging overhead in production.
 */
object DebugLog {
    @JvmStatic
    inline fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    inline fun d(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    @JvmStatic
    inline fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    @JvmStatic
    inline fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }

    @JvmStatic
    inline fun w(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    @JvmStatic
    inline fun e(tag: String, message: String) {
        // Errors are always logged, even in release
        Log.e(tag, message)
    }

    @JvmStatic
    inline fun e(tag: String, message: String, throwable: Throwable) {
        // Errors are always logged, even in release
        Log.e(tag, message, throwable)
    }

    @JvmStatic
    inline fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
}
