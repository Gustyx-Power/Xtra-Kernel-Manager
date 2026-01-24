package id.xms.xtrakernelmanager.domain

import android.util.Log
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.ThermalControlUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Smart CPU Locker with thermal awareness and auto-restore functionality
 */
class SmartCPULocker(
    private val cpuUseCase: CPUControlUseCase,
    private val thermalUseCase: ThermalControlUseCase
) {
    private val TAG = "SmartCPULocker"
    
    // State management
    private val _lockState = MutableStateFlow(CPULockState())
    val lockState: StateFlow<CPULockState> = _lockState.asStateFlow()
    
    // Event emission for UI
    private val _thermalEvents = MutableSharedFlow<ThermalEvent>()
    val thermalEvents: SharedFlow<ThermalEvent> = _thermalEvents.asSharedFlow()
    
    // Monitoring jobs
    private var monitoringJob: Job? = null
    private var restoreJob: Job? = null
    
/**
 * Lock CPU frequencies with thermal awareness
 */
suspend fun lockCpuFrequencies(
    clusterConfigs: Map<Int, CpuClusterLockConfig>,
    policyType: LockPolicyType = LockPolicyType.MANUAL,
    thermalPolicy: String = "PolicyB"
): SmartLockResult {
    return try {
        if (_lockState.value.isLocked && policyType == LockPolicyType.MANUAL) {
            return SmartLockResult.AlreadyLocked
        }
        
        // For SMART locking, check thermal conditions first
        if (policyType == LockPolicyType.SMART) {
            val currentTemp = cpuUseCase.getCurrentCpuTemperature()
            val policy = ThermalPolicyPresets.getPolicyByName(thermalPolicy) 
                ?: ThermalPolicyPresets.POLICY_B_BALANCED
            
            if (currentTemp >= policy.criticalThreshold) {
                return SmartLockResult.ThermalOverrideActivated(currentTemp, thermalPolicy)
            }
        }
            
            // Get current cluster states for backup
            val clusters = cpuUseCase.detectClusters()
            val originalFreqs = mutableMapOf<Int, OriginalFreqConfig>()
            
            clusterConfigs.forEach { (clusterId, lockConfig) ->
                val cluster = clusters.find { it.clusterNumber == clusterId }
                if (cluster != null) {
                    originalFreqs[clusterId] = OriginalFreqConfig(
                        minFreq = cluster.currentMinFreq,
                        maxFreq = cluster.currentMaxFreq,
                        governor = cluster.governor
                    )
                }
            }
            
            // Apply frequency locks to all specified clusters
            val failedClusters = mutableListOf<Int>()
            val successfulClusters = mutableListOf<Int>()
            
            clusterConfigs.forEach { (clusterId, lockConfig) ->
                val result = cpuUseCase.lockClusterFrequency(
                    clusterId,
                    lockConfig.minFreq,
                    lockConfig.maxFreq
                )
                
                if (result.isFailure) {
                    failedClusters.add(clusterId)
                    Log.w(TAG, "Failed to lock cluster $clusterId: ${result.exceptionOrNull()?.message}")
                } else {
                    successfulClusters.add(clusterId)
                }
            }
            
            // Update lock state
            val newState = CPULockState(
                isLocked = successfulClusters.isNotEmpty(),
                clusterConfigs = clusterConfigs,
                policyType = policyType,
                thermalPolicy = thermalPolicy,
                originalFrequencies = originalFreqs
            )
            _lockState.value = newState
            
            // Start thermal monitoring for smart modes
            if (policyType == LockPolicyType.SMART && successfulClusters.isNotEmpty()) {
                startThermalMonitoring()
            }
            
            // Return appropriate result
            when {
                successfulClusters.isEmpty() -> SmartLockResult.Error("Failed to lock any clusters")
                failedClusters.isEmpty() -> SmartLockResult.Success
                else -> SmartLockResult.PartialSuccess(successfulClusters, failedClusters)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock CPU frequencies", e)
            SmartLockResult.Error("Failed to lock CPU frequencies: ${e.message}", e)
        }
    }
    
    /**
     * Unlock CPU frequencies and restore original settings
     */
    suspend fun unlockCpuFrequencies(): SmartLockResult {
        return try {
            if (!_lockState.value.isLocked) {
                return SmartLockResult.NotLocked
            }
            
            val currentState = _lockState.value
            var successCount = 0
            var totalCount = 0
            
            // Restore original frequencies for all locked clusters
            currentState.originalFrequencies.forEach { (clusterId, originalConfig) ->
                totalCount++
                val result = cpuUseCase.unlockClusterFrequency(clusterId)
                
                if (result.isSuccess) {
                    // Also restore original governor if different
                    val currentCluster = cpuUseCase.detectClusters()
                        .find { it.clusterNumber == clusterId }
                    if (currentCluster?.governor != originalConfig.governor) {
                        cpuUseCase.setClusterGovernor(clusterId, originalConfig.governor)
                    }
                    successCount++
                } else {
                    Log.w(TAG, "Failed to unlock cluster $clusterId")
                }
            }
            
            // Stop monitoring and clear state
            stopThermalMonitoring()
            restoreJob?.cancel()
            _lockState.value = CPULockState()
            
            if (successCount == totalCount) {
                SmartLockResult.Success
            } else {
                SmartLockResult.PartialSuccess(emptyList(), listOf())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlock CPU frequencies", e)
            SmartLockResult.Error("Failed to unlock CPU frequencies: ${e.message}", e)
        }
    }
    
    /**
     * Start thermal monitoring for smart locking
     */
    private fun startThermalMonitoring() {
        stopThermalMonitoring() // Ensure no duplicate monitoring
        
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _lockState.value.isLocked) {
                try {
                    val temperature = getCurrentCpuTemperature()
                    val policy = getCurrentThermalPolicy()
                    val currentState = _lockState.value
                    
                    when {
                        temperature >= policy.criticalThreshold -> {
                            handleCriticalTemperature(temperature, policy)
                        }
                        temperature >= policy.emergencyThreshold && !currentState.isThermalOverrideActive -> {
                            handleEmergencyTemperature(temperature, policy)
                        }
                        temperature >= policy.warningThreshold -> {
                            handleWarningTemperature(temperature, policy)
                        }
                        temperature <= policy.restoreThreshold && currentState.isThermalOverrideActive -> {
                            handleSafeTemperature(temperature, policy)
                        }
                    }
                    
                    _lockState.value = currentState.copy(lastTemperature = temperature)
                    
                    delay(1000) // Check every second
                } catch (e: Exception) {
                    Log.e(TAG, "Error in thermal monitoring", e)
                    delay(5000) // Wait longer on error
                }
            }
        }
    }
    
    /**
     * Stop thermal monitoring
     */
    private fun stopThermalMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    /**
     * Handle critical temperature (immediate emergency action)
     */
    private suspend fun handleCriticalTemperature(temperature: Float, policy: ThermalPolicy) {
        Log.w(TAG, "CRITICAL temperature: $temperature°C - Taking emergency action")
        
        emitThermalEvent(
            ThermalEventType.CRITICAL,
            temperature,
            policy.name,
            "Critical temperature detected! Taking emergency action."
        )
        
        when (policy.behavior.actionOnCritical) {
            ThermalAction.UNLOCK_ALL -> {
                unlockCpuFrequencies()
            }
            ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE -> {
                unlockCpuFrequencies()
                // Set all clusters to powersave governor
                cpuUseCase.detectClusters().forEach { cluster ->
                    cpuUseCase.setClusterGovernor(cluster.clusterNumber, "powersave")
                }
            }
            ThermalAction.EMERGENCY_SHUTDOWN -> {
                Log.e(TAG, "EMERGENCY SHUTDOWN triggered - implement if needed")
            }
            else -> { /* No action */ }
        }
        
        // Update lock state to reflect thermal override
        _lockState.value = _lockState.value.copy(
            isThermalOverrideActive = true
        )
    }
    
    /**
     * Handle emergency temperature (unlock for safety)
     */
    private suspend fun handleEmergencyTemperature(temperature: Float, policy: ThermalPolicy) {
        Log.w(TAG, "EMERGENCY temperature: $temperature°C - Activating thermal override")
        
        emitThermalEvent(
            ThermalEventType.EMERGENCY,
            temperature,
            policy.name,
            "Emergency thermal override activated. Frequencies unlocked for safety."
        )
        
        when (policy.behavior.actionOnEmergency) {
            ThermalAction.UNLOCK_ALL -> {
                unlockCpuFrequencies()
            }
            ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE -> {
                unlockCpuFrequencies()
                cpuUseCase.detectClusters().forEach { cluster ->
                    cpuUseCase.setClusterGovernor(cluster.clusterNumber, "powersave")
                }
            }
            else -> { /* No action */ }
        }
        
        _lockState.value = _lockState.value.copy(
            isThermalOverrideActive = true
        )
    }
    
    /**
     * Handle warning temperature (precautionary measures)
     */
    private suspend fun handleWarningTemperature(temperature: Float, policy: ThermalPolicy) {
        Log.i(TAG, "WARNING temperature: $temperature°C - Monitoring closely")
        
        emitThermalEvent(
            ThermalEventType.WARNING,
            temperature,
            policy.name,
            "Approaching thermal limits. Monitoring closely."
        )
        
        // For warning, we just emit event - no action unless configured
        // Future enhancement: reduce frequencies slightly as precaution
    }
    
    /**
     * Handle safe temperature (attempt to restore lock)
     */
    private suspend fun handleSafeTemperature(temperature: Float, policy: ThermalPolicy) {
        Log.i(TAG, "SAFE temperature: $temperature°C - Attempting to restore lock")
        
        if (policy.behavior.autoRestoreEnabled) {
            // Cancel any existing restore job
            restoreJob?.cancel()
            
            restoreJob = CoroutineScope(Dispatchers.IO).launch {
                delay(policy.restoreDelay) // Wait for stability
                
                try {
                    val currentState = _lockState.value
                    val result = lockCpuFrequencies(
                        currentState.clusterConfigs,
                        currentState.policyType,
                        currentState.thermalPolicy
                    )
                    
                    when (result) {
                        is SmartLockResult.Success,
                        is SmartLockResult.SuccessWithWarning -> {
                            _lockState.value = _lockState.value.copy(
                                isThermalOverrideActive = false
                            )
                            
                            emitThermalEvent(
                                ThermalEventType.RESTORE_SAFE,
                                temperature,
                                policy.name,
                                "CPU lock successfully restored."
                            )
                        }
                        else -> {
                            emitThermalEvent(
                                ThermalEventType.RESTORE_FAILED,
                                temperature,
                                policy.name,
                                "Failed to restore CPU lock. Will retry later."
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring CPU lock", e)
                    emitThermalEvent(
                        ThermalEventType.RESTORE_FAILED,
                        temperature,
                        policy.name,
                        "Error restoring CPU lock: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Emit thermal event for UI notification
     */
    private suspend fun emitThermalEvent(
        type: ThermalEventType,
        temperature: Float,
        policy: String,
        message: String
    ) {
        val event = ThermalEvent(
            type = type,
            temperature = temperature,
            policy = policy,
            message = message,
            affectedClusters = _lockState.value.clusterConfigs.keys.toList()
        )
        _thermalEvents.emit(event)
    }
    
    /**
     * Get current CPU temperature
     */
    private suspend fun getCurrentCpuTemperature(): Float {
        return try {
            cpuUseCase.getCurrentCpuTemperature()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get CPU temperature", e)
            0f
        }
    }
    
    /**
     * Get current thermal policy based on lock state
     */
    private fun getCurrentThermalPolicy(): ThermalPolicy {
        val policyName = _lockState.value.thermalPolicy
        return ThermalPolicyPresets.getPolicyByName(policyName)
            ?: ThermalPolicyPresets.POLICY_B_BALANCED
    }
    
    /**
     * Get current lock status for UI
     */
    fun getLockStatus(): LockStatus {
        val state = _lockState.value
        return LockStatus(
            isLocked = state.isLocked,
            policyType = state.policyType,
            thermalPolicy = state.thermalPolicy,
            isThermalOverrideActive = state.isThermalOverrideActive,
            lastTemperature = state.lastTemperature,
            lastUpdate = state.lastUpdate,
            clusterCount = state.clusterConfigs.size,
            lockedClusters = state.clusterConfigs.keys.toList(),
            retryCount = state.retryCount,
            canRetry = shouldAllowRetry()
        )
    }
    
    /**
     * Check if retry should be allowed based on retry limits
     */
    private fun shouldAllowRetry(): Boolean {
        val state = _lockState.value
        val policy = getCurrentThermalPolicy()
        val now = System.currentTimeMillis()
        
        // Reset count if more than an hour has passed
        if (now - state.lastRetryTime > 3600_000L) {
            return true
        }
        
        return state.retryCount < policy.behavior.maxRetriesPerHour
    }
    
    /**
     * Update lock state from preferences (for persistence)
     */
    suspend fun updateFromPersistedState(persistedState: CPULockState) {
        _lockState.value = persistedState
        
        // Restart monitoring if needed
        if (persistedState.isLocked && persistedState.policyType == LockPolicyType.SMART) {
            startThermalMonitoring()
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopThermalMonitoring()
        restoreJob?.cancel()
    }
}