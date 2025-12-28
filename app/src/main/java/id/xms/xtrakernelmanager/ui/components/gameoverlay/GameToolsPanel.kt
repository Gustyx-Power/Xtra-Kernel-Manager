package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Game Tool Item Data
 */
data class GameToolState(
    val esportsMode: Boolean = false,
    val touchGuard: Boolean = false,
    val blockNotifications: Boolean = false,
    val doNotDisturb: Boolean = false,
    val autoRejectCalls: Boolean = false,
    val lockBrightness: Boolean = false
)

/**
 * Game Tools Panel Component
 * 
 * Panel with gaming utility toggles:
 * - Mode Esports
 * - Touch Guard
 * - Block Notifications
 * - DND
 * - Auto Reject Calls
 * - Lock Brightness
 * - Screenshot
 * - Screen Record
 */
@Composable
fun GameToolsPanel(
    toolState: GameToolState,
    onEsportsModeChange: (Boolean) -> Unit,
    onTouchGuardChange: (Boolean) -> Unit,
    onBlockNotificationsChange: (Boolean) -> Unit,
    onDndChange: (Boolean) -> Unit,
    onAutoRejectCallsChange: (Boolean) -> Unit,
    onLockBrightnessChange: (Boolean) -> Unit,
    onScreenshot: () -> Unit,
    onScreenRecord: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Esports Mode - Highlighted
        EsportsModeCard(
            isEnabled = toolState.esportsMode,
            onToggle = onEsportsModeChange,
            accentColor = accentColor
        )
        
        // Toggle Tools Grid (2 columns)
        Text(
            text = "Pengaturan Gaming",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameToolToggleCard(
                icon = Icons.Outlined.TouchApp,
                selectedIcon = Icons.Filled.TouchApp,
                label = "Pencegah\nSalah Sentuh",
                isEnabled = toolState.touchGuard,
                onToggle = onTouchGuardChange,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            
            GameToolToggleCard(
                icon = Icons.Outlined.NotificationsOff,
                selectedIcon = Icons.Filled.NotificationsOff,
                label = "Blokir\nNotifikasi",
                isEnabled = toolState.blockNotifications,
                onToggle = onBlockNotificationsChange,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameToolToggleCard(
                icon = Icons.Outlined.DoNotDisturbOn,
                selectedIcon = Icons.Filled.DoNotDisturbOn,
                label = "Jangan\nGanggu",
                isEnabled = toolState.doNotDisturb,
                onToggle = onDndChange,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            
            GameToolToggleCard(
                icon = Icons.Outlined.PhoneDisabled,
                selectedIcon = Icons.Filled.PhoneDisabled,
                label = "Tolak\nPanggilan",
                isEnabled = toolState.autoRejectCalls,
                onToggle = onAutoRejectCallsChange,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameToolToggleCard(
                icon = Icons.Outlined.LightMode,
                selectedIcon = Icons.Filled.LightMode,
                label = "Kunci\nKecerahan",
                isEnabled = toolState.lockBrightness,
                onToggle = onLockBrightnessChange,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            
            // Empty space for grid alignment
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Divider
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // Action Buttons
        Text(
            text = "Aksi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameToolActionButton(
                icon = Icons.Outlined.Screenshot,
                label = "Screenshot",
                onClick = onScreenshot,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            
            GameToolActionButton(
                icon = Icons.Outlined.Videocam,
                label = "Rekam Layar",
                onClick = onScreenRecord,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Esports Mode Card - Premium highlighted card
 */
@Composable
private fun EsportsModeCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color
) {
    val backgroundColor = if (isEnabled) {
        Color(0xFFFF5722).copy(alpha = 0.15f)
    } else {
        Color(0xFF1A1A1A)
    }
    
    val borderColor = if (isEnabled) {
        Color(0xFFFF5722).copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onToggle(!isEnabled) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                contentDescription = null,
                tint = if (isEnabled) Color(0xFFFF5722) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Mode Esports",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color(0xFFFF5722) else Color.White
                )
                Text(
                    text = "Optimasi maksimal untuk gaming",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        EsportsAnimatedSwitch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            activeColor = Color(0xFFFF5722)
        )
    }
}

/**
 * Game Tool Toggle Card
 */
@Composable
private fun GameToolToggleCard(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isEnabled) {
        accentColor.copy(alpha = 0.12f)
    } else {
        Color(0xFF1A1A1A)
    }
    
    val borderColor = if (isEnabled) {
        accentColor.copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onToggle(!isEnabled) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isEnabled) selectedIcon else icon,
            contentDescription = label,
            tint = if (isEnabled) accentColor else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isEnabled) FontWeight.Medium else FontWeight.Normal,
            color = if (isEnabled) accentColor else Color.White,
            lineHeight = 11.sp,
            maxLines = 2
        )
    }
}

/**
 * Game Tool Action Button
 */
@Composable
private fun GameToolActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
