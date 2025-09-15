package id.xms.xtrakernelmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GraphData(
    val cpuLoadHistory: List<Float> = emptyList(),
    val cpuSpeedHistory: List<Float> = emptyList(),
    val gpuHistory: List<Float> = emptyList(),
    val cpuGraphMode: GraphMode = GraphMode.LOAD
)

enum class GraphMode {
    SPEED,
    LOAD
}

class GraphDataViewModel : ViewModel() {
    private val _graphData = MutableStateFlow(GraphData())
    val graphData: StateFlow<GraphData> = _graphData.asStateFlow()

    fun addCpuLoadData(dataPoint: Float) {
        viewModelScope.launch {
            val currentData = _graphData.value
            val newHistory = (currentData.cpuLoadHistory + dataPoint).takeLast(50)
            _graphData.value = currentData.copy(cpuLoadHistory = newHistory)
        }
    }

    fun addCpuSpeedData(dataPoint: Float) {
        viewModelScope.launch {
            val currentData = _graphData.value
            val newHistory = (currentData.cpuSpeedHistory + dataPoint).takeLast(50)
            _graphData.value = currentData.copy(cpuSpeedHistory = newHistory)
        }
    }

    fun addGpuData(dataPoint: Float) {
        viewModelScope.launch {
            val currentData = _graphData.value
            val newHistory = (currentData.gpuHistory + dataPoint).takeLast(50)
            _graphData.value = currentData.copy(gpuHistory = newHistory)
        }
    }

    fun setCPUGraphMode(mode: GraphMode) {
        viewModelScope.launch {
            val currentData = _graphData.value
            _graphData.value = currentData.copy(cpuGraphMode = mode)
        }
    }

    fun resetCPUGraphHistory() {
        viewModelScope.launch {
            val currentData = _graphData.value
            val updatedData = if (currentData.cpuGraphMode == GraphMode.LOAD) {
                currentData.copy(cpuLoadHistory = emptyList())
            } else {
                currentData.copy(cpuSpeedHistory = emptyList())
            }
            _graphData.value = updatedData
        }
    }
}