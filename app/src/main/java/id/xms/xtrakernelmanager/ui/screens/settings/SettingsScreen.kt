package id.xms.xtrakernelmanager.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.utils.Constants

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ThemeSection(uiState, viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AdvancedSection(uiState, viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AboutSection()
        }
    }
}

@Composable
private fun ThemeSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.themeMode == Constants.THEME_MODE_SYSTEM,
                    onClick = { viewModel.setThemeMode(Constants.THEME_MODE_SYSTEM) },
                    label = { Text("System") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.themeMode == Constants.THEME_MODE_LIGHT,
                    onClick = { viewModel.setThemeMode(Constants.THEME_MODE_LIGHT) },
                    label = { Text("Light") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.themeMode == Constants.THEME_MODE_DARK,
                    onClick = { viewModel.setThemeMode(Constants.THEME_MODE_DARK) },
                    label = { Text("Dark") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Theme Style",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                ThemeStyleOption(
                    title = "GlassMorphism",
                    description = "Transparent blur effect",
                    selected = uiState.themeStyle == Constants.THEME_GLASS,
                    onClick = { viewModel.setThemeStyle(Constants.THEME_GLASS) }
                )
                ThemeStyleOption(
                    title = "Material 3 Solid",
                    description = "Solid Material Design 3",
                    selected = uiState.themeStyle == Constants.THEME_SOLID,
                    onClick = { viewModel.setThemeStyle(Constants.THEME_SOLID) }
                )
                ThemeStyleOption(
                    title = "Material 3 with Glass",
                    description = "Material 3 with glassmorphism",
                    selected = uiState.themeStyle == Constants.THEME_GLASS_MATERIAL,
                    onClick = { viewModel.setThemeStyle(Constants.THEME_GLASS_MATERIAL) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Color",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Use wallpaper colors (Android 12+)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = uiState.dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
        }
    }
}

@Composable
private fun ThemeStyleOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AdvancedSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Apply on Boot",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Apply settings automatically on boot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = uiState.applyOnBoot,
                    onCheckedChange = { viewModel.setApplyOnBoot(it) }
                )
            }
        }
    }
}

@Composable
private fun AboutSection() {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AboutItem(
                icon = Icons.Default.Info,
                title = "Version",
                value = "2.0-alpha2"
            )

            AboutItem(
                icon = Icons.Default.Code,
                title = "License",
                value = "MIT Open Source"
            )

            AboutItem(
                icon = Icons.Default.Public,
                title = "Source Code",
                value = "GitHub"
            )

            AboutItem(
                icon = Icons.Default.Share,
                title = "Share",
                value = "Share with friends"
            )
        }
    }
}

@Composable
private fun AboutItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
