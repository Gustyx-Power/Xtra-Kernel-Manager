package id.xms.xtrakernelmanager.ui.screens.home

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Environment
import android.os.StatFs
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
                // Detect OpenGL via GLSurfaceView
                val glView = GLSurfaceView(context)
                glView.setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        gl?.let {
                            val renderer = it.glGetString(GL10.GL_RENDERER) ?: "Unknown"
                            val vendor = it.glGetString(GL10.GL_VENDOR) ?: "Unknown"
                            val version = it.glGetString(GL10.GL_VERSION) ?: "Unknown"

                            _uiState.value = _uiState.value.copy(
                                gpuRenderer = renderer,
                                gpuVendor = vendor,
                                openGLVersion = version
                            )
                        }
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                    override fun onDrawFrame(gl: GL10?) {}
                })
            } catch (e: Exception) {
                // Fallback: try to get from system properties
                _uiState.value = _uiState.value.copy(
                    gpuRenderer = android.os.Build.HARDWARE,
                    openGLVersion = "ES 3.0+"
                )
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
                delay(1000)
            }
        }
    }

    fun refresh() {
        loadSystemInfo()
        detectGpuInfo()
    }
}
