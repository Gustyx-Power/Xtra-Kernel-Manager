package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LocalBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.layerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.rememberLayerBackdrop
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedSettingsContent(
    preferencesManager: PreferencesManager,
    currentLayout: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val backdrop = rememberLayerBackdrop()
    val isAndroid10Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer with gradient and wavy blob
        Box(modifier = Modifier.fillMaxSize()) {
            // Base gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Wavy blob ornament overlay with Monet colors
            id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
                modifier = Modifier.fillMaxSize(),
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                strokeColor = Color.Black.copy(alpha = 0.6f),
                blobAlpha = 0.55f
            )

            // Capture layer for backdrop
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop))
        }

        // Content Layer
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            Scaffold(
                topBar = {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button
                            Surface(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Title
                            Text(
                                text = stringResource(R.string.settings_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Spacer to balance the layout
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Layout Selection Section
                    Text(
                        text = stringResource(R.string.settings_appearance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FrostedLayoutSettingItem(
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

                        FrostedLayoutSettingItem(
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

                        FrostedLayoutSettingItem(
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
    }
}

@Composable
fun FrostedLayoutSettingItem(
    title: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Surface(
            onClick = onClick,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else
                Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        if (!isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Android 10+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
