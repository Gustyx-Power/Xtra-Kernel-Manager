package id.xms.xtrakernelmanager.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.xms.xtrakernelmanager.data.repository.SystemRepository
import id.xms.xtrakernelmanager.service.BatteryStatsService
import id.xms.xtrakernelmanager.utils.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiscViewModel @Inject constructor(
    private val application: Application,
    private val preferenceManager: PreferenceManager,
    private val systemRepository: SystemRepository
) : AndroidViewModel(application) {

    private val _batteryStatsEnabled = MutableStateFlow(false)
    val batteryStatsEnabled: StateFlow<Boolean> = _batteryStatsEnabled.asStateFlow()

    private val _batteryNotificationEnabled = MutableStateFlow(false)
    val batteryNotificationEnabled: StateFlow<Boolean> = _batteryNotificationEnabled.asStateFlow()

    private val _kgslSkipZeroingEnabled = MutableStateFlow(false)
    val kgslSkipZeroingEnabled: StateFlow<Boolean> = _kgslSkipZeroingEnabled.asStateFlow()

    private val _isKgslFeatureAvailable = MutableStateFlow(false)
    val isKgslFeatureAvailable: StateFlow<Boolean> = _isKgslFeatureAvailable.asStateFlow()

    init {
        // Load saved preferences on init
        _batteryStatsEnabled.value = preferenceManager.getBatteryStatsEnabled()
        _batteryNotificationEnabled.value = preferenceManager.getBatteryStatsEnabled()
        _kgslSkipZeroingEnabled.value = preferenceManager.getKgslSkipZeroing()
        
        // Check if KGSL feature is available
        _isKgslFeatureAvailable.value = systemRepository.isKgslFeatureAvailable()
    }

    fun toggleBatteryStats(enabled: Boolean) {
        viewModelScope.launch {
            _batteryStatsEnabled.value = enabled
            _batteryNotificationEnabled.value = enabled

            // Save preference for auto-start on boot - THIS IS THE KEY!
            preferenceManager.setBatteryStatsEnabled(enabled)

            if (enabled) {
                // Start the battery stats service
                val serviceIntent = Intent(application, BatteryStatsService::class.java)
                try {
                    application.startForegroundService(serviceIntent)
                } catch (e: Exception) {
                    // Handle service start error
                    _batteryStatsEnabled.value = false
                    _batteryNotificationEnabled.value = false
                    preferenceManager.setBatteryStatsEnabled(false)
                }
            } else {
                // Stop the battery stats service and disable auto-start
                val serviceIntent = Intent(application, BatteryStatsService::class.java)
                application.stopService(serviceIntent)
                preferenceManager.setBatteryStatsEnabled(false)
            }
        }
    }

    fun toggleKgslSkipZeroing(enabled: Boolean) {
        viewModelScope.launch {
            // Try to set the value in the kernel
            val success = systemRepository.setKgslSkipZeroing(enabled)
            
            if (success) {
                // Update state and save preference
                _kgslSkipZeroingEnabled.value = enabled
                preferenceManager.setKgslSkipZeroing(enabled)
            } else {
                // If failed, revert the state to the actual value
                _kgslSkipZeroingEnabled.value = systemRepository.getKgslSkipZeroing()
                preferenceManager.setKgslSkipZeroing(_kgslSkipZeroingEnabled.value)
            }
        }
    }
}