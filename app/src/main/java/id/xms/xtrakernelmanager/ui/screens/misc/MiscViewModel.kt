package id.xms.xtrakernelmanager.ui.screens.misc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MiscViewModel : ViewModel() {

    private val batteryRepository = BatteryRepository()

    private val _isRootAvailable = MutableStateFlow(false)
    val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    init {
        checkRoot()
    }

    private fun checkRoot() {
        viewModelScope.launch {
            _isRootAvailable.value = RootManager.isRootAvailable()
        }
    }

    fun loadBatteryInfo(context: Context) {
        viewModelScope.launch {
            _batteryInfo.value = batteryRepository.getBatteryInfo(context)
        }
    }
}
