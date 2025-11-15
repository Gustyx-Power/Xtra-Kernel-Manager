package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.domain.root.RootManager

class GPUControlUseCase {

    private val TAG = "GPUControlUseCase"

    suspend fun getGPUInfo(): GPUInfo {
        val gpuPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0",
            "/sys/kernel/gpu"
        )

        val basePath = gpuPaths.firstOrNull {
            RootManager.executeCommand("[ -d $it ] && echo exists")
                .getOrNull()?.trim() == "exists"
        } ?: gpuPaths[0]

        val availableFreqs = RootManager.executeCommand("cat $basePath/gpu_available_frequencies 2>/dev/null")
            .getOrNull()?.split(" ")?.mapNotNull { it.toIntOrNull()?.div(1000000) } ?: emptyList()

        val currentFreq = RootManager.executeCommand("cat $basePath/gpuclk 2>/dev/null")
            .getOrNull()?.trim()?.toIntOrNull()?.div(1000000) ?: 0

        val powerLevel = RootManager.executeCommand("cat $basePath/default_pwrlevel 2>/dev/null")
            .getOrNull()?.trim()?.toIntOrNull() ?: 0

        val currentRenderer = detectCurrentRenderer()

        return GPUInfo(
            vendor = "Unknown",
            renderer = "Unknown",
            openglVersion = "Unknown",
            currentFreq = currentFreq,
            minFreq = availableFreqs.minOrNull() ?: 0,
            maxFreq = availableFreqs.maxOrNull() ?: 0,
            availableFreqs = availableFreqs,
            powerLevel = powerLevel,
            rendererType = currentRenderer
        )
    }

    private suspend fun detectCurrentRenderer(): String {
        val rendererProp = RootManager.executeCommand("getprop debug.hwui.renderer")
            .getOrNull()?.trim() ?: ""

        val persistentRenderer = getPersistentGpuRenderer()

        Log.d(TAG, "Runtime renderer: '$rendererProp', persistent: '$persistentRenderer'")

        val effectiveRenderer = if (rendererProp.isEmpty() || rendererProp == "null") {
            persistentRenderer.ifEmpty { "" }
        } else {
            rendererProp
        }

        return when {
            effectiveRenderer.isEmpty() || effectiveRenderer == "null" -> "OpenGL ES"
            effectiveRenderer.equals("opengl", ignoreCase = true) -> "OpenGL ES"
            effectiveRenderer.equals("vulkan", ignoreCase = true) -> "Vulkan"
            effectiveRenderer.equals("skiagl", ignoreCase = true) -> "SkiaGL"
            effectiveRenderer.equals("skiavk", ignoreCase = true) -> "SkiaVulkan"
            effectiveRenderer.equals("angle", ignoreCase = true) -> "ANGLE"
            else -> "OpenGL ES"
        }
    }

    private suspend fun getPersistentGpuRenderer(): String {
        val sources = listOf(
            "/vendor/build.prop",
            "/system/build.prop",
            "/system/etc/system.prop"
        )

        for (source in sources) {
            try {
                val content = RootManager.executeCommand("cat $source 2>/dev/null")
                    .getOrNull() ?: continue

                if (content.isNotEmpty()) {
                    val lines = content.lines()
                    val rendererLine = lines.find { it.trim().startsWith("debug.hwui.renderer=") }
                    if (rendererLine != null) {
                        val value = rendererLine.substringAfter("=").trim()
                        if (value.isNotEmpty()) {
                            Log.d(TAG, "Found persistent renderer in $source: $value")
                            return value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read $source", e)
            }
        }

        return ""
    }

    suspend fun setGPUFrequency(minFreq: Int, maxFreq: Int): Result<Unit> {
        val basePath = "/sys/class/kgsl/kgsl-3d0"

        val minResult = RootManager.executeCommand(
            "echo ${minFreq * 1000000} > $basePath/min_clock_mhz 2>/dev/null"
        )
        val maxResult = RootManager.executeCommand(
            "echo ${maxFreq * 1000000} > $basePath/max_clock_mhz 2>/dev/null"
        )

        return if (minResult.isSuccess && maxResult.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to set GPU frequency"))
        }
    }

    suspend fun setGPUPowerLevel(level: Int): Result<Unit> {
        val basePath = "/sys/class/kgsl/kgsl-3d0"
        return RootManager.executeCommand("echo $level > $basePath/default_pwrlevel 2>/dev/null")
            .map { Unit }
    }

    suspend fun setGPURenderer(renderer: String): Result<Unit> {
        Log.d(TAG, "Setting GPU renderer to: '$renderer'")

        val propertyValue = when (renderer) {
            "OpenGL ES" -> "opengl"
            "Vulkan" -> "vulkan"
            "SkiaGL" -> "skiagl"
            "SkiaVulkan" -> "skiavk"
            "ANGLE" -> "angle"
            else -> "opengl"
        }

        return try {
            var success = true

            // Clear old properties
            val clearCommands = listOf(
                "setprop debug.hwui.renderer \"\"",
                "setprop debug.hwui.skia_atrace_enabled \"\"",
                "setprop ro.hwui.use_vulkan \"\"",
                "setprop debug.angle.backend \"\""
            )

            clearCommands.forEach { cmd ->
                RootManager.executeCommand(cmd)
            }

            // Set new runtime property
            val setResult = RootManager.executeCommand("setprop debug.hwui.renderer \"$propertyValue\"")
            if (setResult.isFailure) {
                success = false
            }

            // Set additional properties
            when (renderer) {
                "Vulkan", "SkiaVulkan" -> {
                    RootManager.executeCommand("setprop ro.hwui.use_vulkan true")
                    Log.i(TAG, "Set Vulkan properties")
                }
                "ANGLE" -> {
                    RootManager.executeCommand("setprop debug.angle.backend opengl")
                }
            }

            // Make settings persistent
            val persistentSuccess = makePersistentGpuRendererSettings(renderer, propertyValue)
            if (!persistentSuccess) {
                Log.w(TAG, "Failed to make settings persistent, but runtime settings applied")
            }

            if (renderer.contains("Vulkan") && success) {
                Log.i(TAG, "Vulkan settings applied. REBOOT required to activate.")
            }

            if (success) {
                Log.i(TAG, "Successfully set GPU renderer to: '$renderer'")
                val actualValue = RootManager.executeCommand("getprop debug.hwui.renderer")
                    .getOrNull()?.trim()
                Log.d(TAG, "Verification - actual value: '$actualValue'")
            }

            if (success) Result.success(Unit) else Result.failure(Exception("Failed to set renderer"))

        } catch (e: Exception) {
            Log.e(TAG, "Exception while setting GPU renderer", e)
            Result.failure(e)
        }
    }

    private suspend fun makePersistentGpuRendererSettings(renderer: String, propertyValue: String): Boolean {
        Log.d(TAG, "Making GPU renderer settings persistent for: $renderer")

        try {
            // Try vendor prop first
            val vendorSuccess = setPersistentViaVendorProp(renderer, propertyValue)
            if (vendorSuccess) {
                Log.i(TAG, "Successfully applied GPU settings via vendor.prop")
                return true
            }

            Log.w(TAG, "Vendor prop approach failed, trying alternatives...")

            // Try system.prop
            try {
                if (setPersistentViaSystemProp(renderer, propertyValue)) {
                    Log.i(TAG, "Successfully applied persistent setting via system.prop")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "System.prop approach failed", e)
            }

            // Try init.d script
            try {
                if (setPersistentViaInitD(renderer, propertyValue)) {
                    Log.i(TAG, "Successfully applied persistent setting via init.d")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Init.d approach failed", e)
            }

            return false

        } catch (e: Exception) {
            Log.e(TAG, "Exception in makePersistentGpuRendererSettings", e)
            return false
        }
    }

    private suspend fun setPersistentViaVendorProp(renderer: String, propertyValue: String): Boolean {
        val vendorPropPath = "/vendor/build.prop"
        val tempPath = "/data/local/tmp/vendor.prop.tmp"

        try {
            val exists = RootManager.executeCommand("test -f $vendorPropPath && echo exists")
                .getOrNull()?.trim() == "exists"

            if (!exists) return false

            val remountResult = RootManager.executeCommand("mount -o remount,rw /vendor")
            if (remountResult.isFailure) return false

            val currentContent = RootManager.executeCommand("cat $vendorPropPath")
                .getOrNull() ?: ""

            if (currentContent.isEmpty()) {
                RootManager.executeCommand("mount -o remount,ro /vendor")
                return false
            }

            val cleanedContent = currentContent.lines()
                .filterNot { line ->
                    val trimmed = line.trim()
                    trimmed.startsWith("debug.hwui.renderer=") ||
                            trimmed.startsWith("ro.hwui.use_vulkan=") ||
                            trimmed.startsWith("debug.angle.backend=")
                }
                .joinToString("\n")

            val newContent = buildString {
                append(cleanedContent)
                if (propertyValue.isNotEmpty()) {
                    appendLine()
                    appendLine("debug.hwui.renderer=$propertyValue")
                    when (renderer) {
                        "Vulkan", "SkiaVulkan" -> appendLine("ro.hwui.use_vulkan=true")
                        "ANGLE" -> appendLine("debug.angle.backend=opengl")
                    }
                }
            }

            val writeCmd = "cat > $tempPath << 'XTRAEOF'\n$newContent\nXTRAEOF"
            val writeResult = RootManager.executeCommand(writeCmd)

            if (writeResult.isFailure) {
                RootManager.executeCommand("mount -o remount,ro /vendor")
                return false
            }

            val copyResult = RootManager.executeCommand("cp $tempPath $vendorPropPath")

            if (copyResult.isSuccess) {
                RootManager.executeCommand("chmod 644 $vendorPropPath")
                RootManager.executeCommand("chown root:root $vendorPropPath")
                Log.i(TAG, "Successfully updated vendor.prop")
            }

            RootManager.executeCommand("rm -f $tempPath")
            RootManager.executeCommand("mount -o remount,ro /vendor")

            return copyResult.isSuccess

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaVendorProp", e)
            RootManager.executeCommand("mount -o remount,ro /vendor")
            RootManager.executeCommand("rm -f $tempPath")
            return false
        }
    }

    private suspend fun setPersistentViaSystemProp(renderer: String, propertyValue: String): Boolean {
        val systemPropPath = "/system/etc/system.prop"
        val tempPath = "/data/local/tmp/system.prop.tmp"

        try {
            RootManager.executeCommand("mount -o remount,rw /system")

            val currentContent = RootManager.executeCommand("cat $systemPropPath 2>/dev/null")
                .getOrNull() ?: ""

            val cleanedContent = currentContent.lines()
                .filterNot { line ->
                    val trimmed = line.trim()
                    trimmed.startsWith("debug.hwui.renderer=") ||
                            trimmed.startsWith("ro.hwui.use_vulkan=") ||
                            trimmed.startsWith("debug.angle.backend=")
                }
                .joinToString("\n")

            val newContent = buildString {
                if (cleanedContent.isNotBlank()) append(cleanedContent)
                if (propertyValue.isNotEmpty()) {
                    appendLine()
                    appendLine("debug.hwui.renderer=$propertyValue")
                    when (renderer) {
                        "Vulkan", "SkiaVulkan" -> appendLine("ro.hwui.use_vulkan=true")
                        "ANGLE" -> appendLine("debug.angle.backend=opengl")
                    }
                }
            }

            val writeCmd = "cat > $tempPath << 'XTRAEOF'\n$newContent\nXTRAEOF"
            RootManager.executeCommand(writeCmd)

            val copyResult = RootManager.executeCommand("cp $tempPath $systemPropPath")

            if (copyResult.isSuccess) {
                RootManager.executeCommand("chmod 644 $systemPropPath")
                RootManager.executeCommand("chown root:root $systemPropPath")
                Log.i(TAG, "Successfully updated system.prop")
            }

            RootManager.executeCommand("rm -f $tempPath")
            RootManager.executeCommand("mount -o remount,ro /system")

            return copyResult.isSuccess

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaSystemProp", e)
            RootManager.executeCommand("mount -o remount,ro /system")
            RootManager.executeCommand("rm -f $tempPath")
            return false
        }
    }

    private suspend fun setPersistentViaInitD(renderer: String, propertyValue: String): Boolean {
        val initdPath = "/system/etc/init.d"
        val scriptPath = "$initdPath/99gpu_renderer"
        val tempPath = "/data/local/tmp/99gpu_renderer.tmp"

        try {
            val exists = RootManager.executeCommand("test -d $initdPath && echo exists")
                .getOrNull()?.trim() == "exists"

            RootManager.executeCommand("mount -o remount,rw /system")

            if (!exists) {
                RootManager.executeCommand("mkdir -p $initdPath")
            }

            val scriptContent = buildString {
                appendLine("#!/system/bin/sh")
                appendLine("# GPU Renderer Configuration Script")
                appendLine("# Generated by Xtra Kernel Manager")
                appendLine()
                if (propertyValue.isNotEmpty()) {
                    appendLine("setprop debug.hwui.renderer \"$propertyValue\"")
                    when (renderer) {
                        "Vulkan", "SkiaVulkan" -> appendLine("setprop ro.hwui.use_vulkan true")
                        "ANGLE" -> appendLine("setprop debug.angle.backend opengl")
                    }
                }
                appendLine()
                appendLine("# End of GPU Renderer Configuration")
            }

            val writeCmd = "cat > $tempPath << 'XTRAEOF'\n$scriptContent\nXTRAEOF"
            RootManager.executeCommand(writeCmd)

            val copyResult = RootManager.executeCommand("cp $tempPath $scriptPath")

            if (copyResult.isSuccess) {
                RootManager.executeCommand("chmod 755 $scriptPath")
                RootManager.executeCommand("chown root:root $scriptPath")
                Log.i(TAG, "Successfully created init.d script")
            }

            RootManager.executeCommand("rm -f $tempPath")
            RootManager.executeCommand("mount -o remount,ro /system")

            return copyResult.isSuccess

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaInitD", e)
            RootManager.executeCommand("mount -o remount,ro /system")
            RootManager.executeCommand("rm -f $tempPath")
            return false
        }
    }

    suspend fun performReboot(): Result<Unit> {
        return try {
            RootManager.executeCommand("sync")
            RootManager.executeCommand("sleep 1 && reboot")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyRendererChange(expectedRenderer: String): Result<Boolean> {
        return try {
            kotlinx.coroutines.delay(2000)

            val currentProp = RootManager.executeCommand("getprop debug.hwui.renderer")
                .getOrNull()?.trim() ?: ""

            val propValue = when (expectedRenderer) {
                "OpenGL ES" -> "opengl"
                "Vulkan" -> "vulkan"
                "SkiaGL" -> "skiagl"
                "SkiaVulkan" -> "skiavk"
                "ANGLE" -> "angle"
                else -> "opengl"
            }

            val isMatch = currentProp.equals(propValue, ignoreCase = true)

            Result.success(isMatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkMagiskAvailability(): Boolean {
        return RootManager.executeCommand("which resetprop 2>/dev/null")
            .getOrNull()?.contains("resetprop") == true
    }
}
