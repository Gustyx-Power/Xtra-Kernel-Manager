package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.usecase.FunctionalRomUseCase
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import kotlinx.coroutines.launch

@Composable
fun LiquidFunctionalRomCard(
    onNavigate: () -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val useCase = remember { FunctionalRomUseCase() }
    
    var isVipCommunity by remember { mutableStateOf<Boolean?>(null) }
    var showSecurityWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVipCommunity = useCase.checkVipCommunity()
    }

    // Security Warning Dialog
    if (showSecurityWarning) {
        AlertDialog(
            onDismissRequest = { showSecurityWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.security_warning_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.security_warning_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showSecurityWarning = false }) {
                    Text(stringResource(R.string.security_warning_button))
                }
            }
        )
    }

    val cardModifier = if (isVipCommunity == false) {
        Modifier.fillMaxWidth().blur(3.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    GlassmorphicCard(
        modifier = cardModifier,
        onClick = {
            if (isVipCommunity == true) {
                onNavigate()
            } else if (isVipCommunity == false) {
                showSecurityWarning = true
            }
        },
        enabled = isVipCommunity != null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isLightTheme) Color(0xFF007AFF).copy(0.15f)
                            else Color(0xFF0A84FF).copy(0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.functional_rom_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.functional_rom_card_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                when (isVipCommunity) {
                    null -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    true -> Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    false -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(0.7f)
                    )
                }
            }

            // VIP badge overlay
            if (isVipCommunity == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isLightTheme) Color(0xFFFFD700).copy(0.2f)
                            else Color(0xFFFFD700).copy(0.25f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLightTheme) Color(0xFFB8860B) else Color(0xFFFFD700)
                    )
                }
            }
        }
    }
}
