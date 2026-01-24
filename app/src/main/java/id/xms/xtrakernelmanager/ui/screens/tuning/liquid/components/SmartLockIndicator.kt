package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.LockPolicyType
import id.xms.xtrakernelmanager.data.model.ThermalEventType
import id.xms.xtrakernelmanager.domain.SmartCPULocker
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.delay

/**
 * Smart Lock Indicator with glassmorphic design for Liquid UI
 */
@Composable
fun SmartLockIndicator(
    modifier: Modifier = Modifier,
    viewModel: TuningViewModel,
    onLockClick: () -> Unit = {},
    onUnlockClick: () -> Unit = {}
) {
    val isLocked by viewModel.isCpuFrequencyLocked.collectAsState()
    val lockStatus by viewModel.cpuLockStatus.collectAsState()
    val thermalEvent by viewModel.thermalEvents.collectAsState()
    
    var showTooltip by remember { mutableStateOf(false) }
    
    // Auto-hide thermal events after 5 seconds for warnings
    LaunchedEffect(thermalEvent) {
        if (thermalEvent != null && thermalEvent!!.type == ThermalEventType.WARNING) {
            delay(5000)
            // Reset thermal event state for warnings only
        }
    }
    
    Column(modifier = modifier) {
        // Main Lock Status Indicator
        Surface(
            onClick = if (isLocked) onUnlockClick else onLockClick,
            shape = CircleShape,
            color = when {
                thermalEvent?.type == ThermalEventType.CRITICAL -> Color.Red
                thermalEvent?.type == ThermalEventType.EMERGENCY -> Color(0xFFFF9800)
                thermalEvent?.type == ThermalEventType.WARNING -> Color(0xFFFFC107)
                lockStatus?.isThermalOverrideActive == true -> Color(0xFFFF9800)
                isLocked -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when {
                        thermalEvent?.type == ThermalEventType.CRITICAL -> Icons.Default.Warning
                        thermalEvent?.type == ThermalEventType.EMERGENCY -> Icons.Default.Whatshot
                        thermalEvent?.type == ThermalEventType.WARNING -> Icons.Default.Thermostat
                        isLocked && lockStatus?.isThermalOverrideActive == true -> Icons.Default.LockOpen
                        isLocked -> Icons.Default.Lock
                        else -> Icons.Default.LockOpen
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Status Text with animation
        AnimatedVisibility(
            visible = isLocked || thermalEvent != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    thermalEvent?.type == ThermalEventType.CRITICAL -> Color.Red
                    thermalEvent?.type == ThermalEventType.EMERGENCY -> Color(0xFFFF9800)
                    thermalEvent?.type == ThermalEventType.WARNING -> Color(0xFFFFC107)
                    lockStatus?.isThermalOverrideActive == true -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = when {
                        thermalEvent?.type == ThermalEventType.CRITICAL -> "CRITICAL"
                        thermalEvent?.type == ThermalEventType.EMERGENCY -> "EMERGENCY"
                        thermalEvent?.type == ThermalEventType.WARNING -> "WARNING"
                        lockStatus?.isThermalOverrideActive == true -> "OVERRIDE"
                        isLocked -> "LOCKED"
                        else -> "UNLOCKED"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        
        // Detailed tooltip/panel
        AnimatedVisibility(
            visible = showTooltip && (isLocked || thermalEvent != null),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (thermalEvent != null) {
                        Text(
                            text = thermalEvent!!.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (thermalEvent!!.type) {
                                ThermalEventType.CRITICAL -> Color.Red
                                ThermalEventType.EMERGENCY -> Color(0xFFFF9800)
                                ThermalEventType.WARNING -> Color(0xFFFFC107)
                                ThermalEventType.RESTORE_SAFE -> Color(0xFF4CAF50)
                                ThermalEventType.RESTORE_FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = "Temp: ${"%.1f".format(thermalEvent!!.temperature)}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (isLocked && lockStatus != null) {
                        Text(
                            text = "Policy: ${lockStatus!!.policyType.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Thermal: ${lockStatus!!.thermalPolicy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (lockStatus!!.isThermalOverrideActive) {
                            Text(
                                text = "🔥 Thermal Override Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (lockStatus!!.needsAttention) {
                            Text(
                                text = "⚠️ Needs Attention",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lock policy type indicator with description
 */
@Composable
fun LockPolicyIndicator(
    policyType: LockPolicyType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = when (policyType) {
            LockPolicyType.MANUAL -> MaterialTheme.colorScheme.primary
            LockPolicyType.SMART -> Color(0xFF4CAF50)
            LockPolicyType.GAME -> Color(0xFFFF5722)
            LockPolicyType.BATTERY_SAVING -> Color(0xFF2196F3)
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when (policyType) {
                    LockPolicyType.MANUAL -> Icons.Default.Settings
                    LockPolicyType.SMART -> Icons.Default.Thermostat
                    LockPolicyType.GAME -> Icons.Default.SportsEsports
                    LockPolicyType.BATTERY_SAVING -> Icons.Default.Battery4Bar
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = when (policyType) {
                    LockPolicyType.MANUAL -> "MANUAL"
                    LockPolicyType.SMART -> "SMART"
                    LockPolicyType.GAME -> "GAME"
                    LockPolicyType.BATTERY_SAVING -> "ECO"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}