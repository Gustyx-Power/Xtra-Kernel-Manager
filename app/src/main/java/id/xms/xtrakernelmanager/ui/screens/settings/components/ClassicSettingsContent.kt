package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicSettingsContent(
    preferencesManager: PreferencesManager,
    currentLayout: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isAndroid10Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Surface,
                    titleContentColor = ClassicColors.OnSurface
                )
            )
        },
        containerColor = ClassicColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Layout Selection Section
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ClassicColors.OnSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClassicLayoutSettingItem(
                    title = stringResource(R.string.settings_layout_material),
                    description = stringResource(R.string.settings_layout_material_desc),
                    isSelected = currentLayout == "material",
                    isEnabled = isAndroid10Plus,
                    onClick = {
                        if (isAndroid10Plus) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                preferencesManager.setLayoutStyle("material")
                                onNavigateBack()
                            }
                        }
                    }
                )

                ClassicLayoutSettingItem(
                    title = stringResource(R.string.settings_layout_frosted),
                    description = stringResource(R.string.settings_layout_frosted_desc),
                    isSelected = currentLayout == "liquid",
                    isEnabled = isAndroid10Plus,
                    onClick = {
                        if (isAndroid10Plus) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                preferencesManager.setLayoutStyle("liquid")
                                onNavigateBack()
                            }
                        }
                    }
                )

                ClassicLayoutSettingItem(
                    title = stringResource(R.string.settings_layout_classic),
                    description = stringResource(R.string.settings_layout_classic_desc),
                    isSelected = currentLayout == "classic",
                    isEnabled = true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            preferencesManager.setLayoutStyle("classic")
                            onNavigateBack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ClassicLayoutSettingItem(
    title: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ClassicColors.Primary.copy(alpha = 0.1f) else ClassicColors.Surface,
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) 
                        ClassicColors.Primary.copy(alpha = if (isEnabled) 1f else 0.38f)
                    else 
                        ClassicColors.OnSurface.copy(alpha = if (isEnabled) 1f else 0.38f)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        ClassicColors.Primary.copy(alpha = if (isEnabled) 0.7f else 0.38f)
                    else
                        ClassicColors.OnSurfaceVariant.copy(alpha = if (isEnabled) 1f else 0.38f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ClassicColors.Primary.copy(alpha = if (isEnabled) 1f else 0.38f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
