package id.xms.xtrakernelmanager.ui.screens.home

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CpuInfo
import id.xms.xtrakernelmanager.data.model.GpuInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.data.repository.SystemInfoRepository
import id.xms.xtrakernelmanager.utils.RootUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasRoot: Boolean = false,
    val cpuInfo: CpuInfo = CpuInfo(),
    val gpuInfo: GpuInfo = GpuInfo(),
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val systemInfo: SystemInfo = SystemInfo(),
    val cpuHistory: List<Float> = emptyList(),
    val gpuRenderer: String = "",
    val gpuVendor: String = "",
    val openGLVersion: String = "",
    val vulkanVersion: String = "",
    val error: String? = null
)

class HomeViewModel(
    private val context: Context,
    private val kernelRepository: KernelRepository,
    private val batteryRepository: BatteryRepository,
    private val systemInfoRepository: SystemInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkRootAccess()
        loadSystemInfo()
        detectGpuInfo()
        startRealtimeMonitoring()
    }

    private fun checkRootAccess() {
        viewModelScope.launch {
            val hasRoot = RootUtils.isRootAvailable()
            _uiState.value = _uiState.value.copy(hasRoot = hasRoot)
        }
    }

    private fun loadSystemInfo() {
        viewModelScope.launch {
            try {
                val systemInfo = systemInfoRepository.getSystemInfo()
                _uiState.value = _uiState.value.copy(
                    systemInfo = systemInfo,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun detectGpuInfo() {
        viewModelScope.launch {
            try {
                val gpuInfo = getOpenGLInfo()
                _uiState.value = _uiState.value.copy(
                    gpuRenderer = gpuInfo.renderer,
                    gpuVendor = gpuInfo.vendor,
                    openGLVersion = gpuInfo.version
                )
            } catch (e: Exception) {
                // Fallback to ActivityManager
                try {
                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val configInfo = activityManager.deviceConfigurationInfo
                    val glVersion = configInfo.glEsVersion

                    _uiState.value = _uiState.value.copy(
                        openGLVersion = "OpenGL ES $glVersion",
                        gpuRenderer = android.os.Build.HARDWARE,
                        gpuVendor = android.os.Build.MANUFACTURER
                    )
                } catch (e2: Exception) {
                    _uiState.value = _uiState.value.copy(
                        openGLVersion = "OpenGL ES 3.0+",
                        gpuRenderer = "Unknown",
                        gpuVendor = "Unknown"
                    )
                }
            }
        }
    }

    private suspend fun getOpenGLInfo(): GpuInfoResult = withContext(Dispatchers.Default) {
        var display: EGLDisplay? = null
        var context: EGLContext? = null
        var surface: EGLSurface? = null

        try {
            // Get EGL display
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }

            // Initialize EGL
            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                throw RuntimeException("eglInitialize failed")
            }

            // Configure EGL
            val configAttribs = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)) {
                throw RuntimeException("eglChooseConfig failed")
            }

            if (numConfigs[0] == 0 || configs[0] == null) {
                throw RuntimeException("No EGL config found")
            }

            // Create EGL context
            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            context = EGL14.eglCreateContext(
                display,
                configs[0],
                EGL14.EGL_NO_CONTEXT,
                contextAttribs,
                0
            )
            if (context == null || context == EGL14.EGL_NO_CONTEXT) {
                throw RuntimeException("eglCreateContext failed")
            }

            // Create PBuffer surface
            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )
            surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)
            if (surface == null || surface == EGL14.EGL_NO_SURFACE) {
                throw RuntimeException("eglCreatePbufferSurface failed")
            }

            // Make context current
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                throw RuntimeException("eglMakeCurrent failed")
            }

            // Get GPU info
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            val versionString = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"

            GpuInfoResult(renderer, vendor, versionString)

        } catch (e: Exception) {
            throw e
        } finally {
            // Cleanup
            try {
                if (display != null && display != EGL14.EGL_NO_DISPLAY) {
                    EGL14.eglMakeCurrent(
                        display,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT
                    )
                    if (context != null && context != EGL14.EGL_NO_CONTEXT) {
                        EGL14.eglDestroyContext(display, context)
                    }
                    if (surface != null && surface != EGL14.EGL_NO_SURFACE) {
                        EGL14.eglDestroySurface(display, surface)
                    }
                    EGL14.eglTerminate(display)
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun startRealtimeMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val cpuInfo = kernelRepository.getCpuInfo()
                    val gpuInfo = kernelRepository.getGpuInfo()
                    val batteryInfo = batteryRepository.getBatteryInfo()

                    val newHistory = (_uiState.value.cpuHistory + cpuInfo.load).takeLast(30)

                    _uiState.value = _uiState.value.copy(
                        cpuInfo = cpuInfo,
                        gpuInfo = gpuInfo.copy(
                            renderer = _uiState.value.gpuRenderer,
                            vendor = _uiState.value.gpuVendor,
                            openGLVersion = _uiState.value.openGLVersion
                        ),
                        batteryInfo = batteryInfo,
                        cpuHistory = newHistory,
                        error = null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                delay(1000) // Update every second
            }
        }
    }

    fun refresh() {
        loadSystemInfo()
        detectGpuInfo()
    }
}

private data class GpuInfoResult(
    val renderer: String,
    val vendor: String,
    val version: String
)
