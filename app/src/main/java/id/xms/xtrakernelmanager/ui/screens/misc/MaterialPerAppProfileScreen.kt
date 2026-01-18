package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

data class AppProfile(
    val packageName: String,
    val appName: String,
    val profileType: ProfileType = ProfileType.DEFAULT,
)

enum class ProfileType(val displayName: String, val description: String) {
    DEFAULT("Default", "Use system defaults"),
    PERFORMANCE("Performance", "Maximum performance mode"),
    BALANCED("Balanced", "Balance between performance and battery"),
    POWER_SAVE("Power Save", "Extend battery life"),
    GAMING("Gaming", "Optimized for games"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPerAppProfileScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var appProfiles by remember { mutableStateOf(getInstalledApps(context)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<AppProfile?>(null) }
    
    val filteredApps = remember(appProfiles, searchQuery) {
        if (searchQuery.isBlank()) {
            appProfiles.sortedBy { it.appName }
        } else {
            appProfiles.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.appName }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Per App Profile",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${appProfiles.count { it.profileType != ProfileType.DEFAULT }} apps configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                ),
            )

            // Profile Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileType.entries.filter { it != ProfileType.DEFAULT }.forEach { type ->
                    Surface(
                        color = getProfileColor(type).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = getProfileColor(type),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // App List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppProfileItem(
                        app = app,
                        isSelected = selectedApp?.packageName == app.packageName,
                        onClick = {
                            selectedApp = if (selectedApp?.packageName == app.packageName) null else app
                        },
                        onProfileChange = { newProfile ->
                            appProfiles = appProfiles.map {
                                if (it.packageName == app.packageName) {
                                    it.copy(profileType = newProfile)
                                } else it
                            }
                            // TODO: Save to preferences/viewModel
                        },
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun AppProfileItem(
    app: AppProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onProfileChange: (ProfileType) -> Unit,
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (app.profileType != ProfileType.DEFAULT) {
                getProfileColor(app.profileType).copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // App icon
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(getAppIcon(context, app.packageName))
                        .crossfade(true)
                        .build(),
                    contentDescription = app.appName,
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                app.appName.take(2).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Android,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium),
                )

                // App info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Profile Badge
                if (app.profileType != ProfileType.DEFAULT) {
                    Surface(
                        color = getProfileColor(app.profileType).copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = app.profileType.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = getProfileColor(app.profileType),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                // Expand Icon
                Icon(
                    if (isSelected) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Expanded Profile Selection
            AnimatedVisibility(visible = isSelected) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Text(
                        "Select Profile",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    ProfileType.entries.forEach { profile ->
                        ProfileOptionItem(
                            profile = profile,
                            isSelected = app.profileType == profile,
                            onClick = { onProfileChange(profile) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    profile: ProfileType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            getProfileColor(profile).copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, getProfileColor(profile))
        } else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(getProfileColor(profile).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    getProfileIcon(profile),
                    null,
                    tint = getProfileColor(profile),
                    modifier = Modifier.size(18.dp),
                )
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) getProfileColor(profile) else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Checkmark
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    tint = getProfileColor(profile),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun getProfileColor(profile: ProfileType): androidx.compose.ui.graphics.Color {
    return when (profile) {
        ProfileType.DEFAULT -> MaterialTheme.colorScheme.outline
        ProfileType.PERFORMANCE -> MaterialTheme.colorScheme.error
        ProfileType.BALANCED -> MaterialTheme.colorScheme.primary
        ProfileType.POWER_SAVE -> MaterialTheme.colorScheme.tertiary
        ProfileType.GAMING -> MaterialTheme.colorScheme.secondary
    }
}

private fun getProfileIcon(profile: ProfileType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (profile) {
        ProfileType.DEFAULT -> Icons.Rounded.Settings
        ProfileType.PERFORMANCE -> Icons.Rounded.Speed
        ProfileType.BALANCED -> Icons.Rounded.Balance
        ProfileType.POWER_SAVE -> Icons.Rounded.BatterySaver
        ProfileType.GAMING -> Icons.Rounded.SportsEsports
    }
}

private fun getInstalledApps(context: android.content.Context): List<AppProfile> {
    return try {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        apps.filter { 
            // Only show user-installed apps (not system apps)
            (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
        }.map { info ->
            AppProfile(
                packageName = info.packageName,
                appName = pm.getApplicationLabel(info).toString(),
            )
        }
    } catch (e: Exception) {
        // Fallback to mock data
        listOf(
            AppProfile("com.instagram.android", "Instagram"),
            AppProfile("com.whatsapp", "WhatsApp"),
            AppProfile("com.spotify.music", "Spotify"),
            AppProfile("com.discord", "Discord"),
            AppProfile("com.netflix.mediaclient", "Netflix"),
            AppProfile("com.miHoYo.GenshinImpact", "Genshin Impact", ProfileType.GAMING),
            AppProfile("com.tencent.ig", "PUBG Mobile", ProfileType.GAMING),
            AppProfile("com.mobile.legends", "Mobile Legends", ProfileType.GAMING),
        )
    }
}

private fun getAppIcon(context: android.content.Context, packageName: String): android.graphics.drawable.Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
    }
}
