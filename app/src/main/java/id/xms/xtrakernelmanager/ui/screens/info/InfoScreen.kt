package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@Composable
fun InfoScreen() {
    val uriHandler = LocalUriHandler.current
    val sourceUrl = stringResource(R.string.info_source_code_url)
    val plingUrl = stringResource(R.string.info_pling_url)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        // --- HEADER APP INFO ---
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 16.dp,
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo_a),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp),
                        tint = Color.Unspecified
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text("v${BuildConfig.VERSION_NAME}", modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text(BuildConfig.BUILD_TYPE.uppercase(), modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.info_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // --- COMMUNITY SECTION ---
        item(span = StaggeredGridItemSpan.FullLine) {
            GlassmorphicCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Community Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.xms),
                            contentDescription = "XMS Community",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.info_community_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = stringResource(R.string.info_community_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoStatItem(value = "2", label = "Founders")
                        InfoStatItem(value = "2", label = "Contributors")
                        InfoStatItem(value = "5", label = "Tester")
                    }
                }
            }
        }

        // --- CORE TEAM (DEVELOPERS/FOUNDERS) ---
        item(span = StaggeredGridItemSpan.FullLine) {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.StarOutline,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.info_core_team),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.info_founders_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Team Members Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TeamMemberCard(
                            modifier = Modifier.weight(1f),
                            imageResId = R.drawable.team_dev_dimsvel,
                            name = "Pavelc4",
                            role = stringResource(R.string.info_role_founder),
                            country = "🇮🇩",
                            isFounder = true
                        )
                        TeamMemberCard(
                            modifier = Modifier.weight(1f),
                            imageResId = R.drawable.team_dev_gustyx,
                            name = "Gustyx-Power",
                            role = stringResource(R.string.info_role_founder),
                            country = "🇮🇩",
                            isFounder = true
                        )
                    }
                }
            }
        }

        // --- CONTRIBUTORS ---
        item(span = StaggeredGridItemSpan.FullLine) {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Code,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.info_contributors),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.info_contributors_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Contributors Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TeamMemberCard(
                            modifier = Modifier.weight(1f),
                            imageResId = R.drawable.team_contributor_pandu,
                            name = "Ziyu",
                            role = stringResource(R.string.info_role_contributor),
                            country = "🇮🇩",
                            isFounder = false
                        )
                        TeamMemberCard(
                            modifier = Modifier.weight(1f),
                            imageResId = R.drawable.team_contributor_shimoku,
                            name = "Shimoku",
                            role = stringResource(R.string.info_role_contributor),
                            country = "🇦",
                            isFounder = false
                        )
                    }
                }
            }
        }

        // --- SOFTWARE TESTER ---
        item(span = StaggeredGridItemSpan.FullLine) {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.BugReport,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.info_quality_assurance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.info_testers_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Testers Carousel - Auto-sliding single tester display
                    val testers = listOf(
                        Triple(R.drawable.team_tester_achmad, R.string.team_tester_achmad, "🇮🇩"),
                        Triple(R.drawable.team_tester_hasan, R.string.team_tester_hasan, "🇮🇩"),
                        Triple(R.drawable.team_tester_reffan, R.string.team_tester_reffan, "🇮🇩"),
                        Triple(R.drawable.team_tester_wil, R.string.team_tester_wil, "🇮🇩"),
                        Triple(R.drawable.team_sm_tester, R.string.team_tester_shadow_monarch, "🇵🇰")
                    )
                    
                    var currentTesterIndex by remember { mutableIntStateOf(0) }
                    
                    // Auto-slide every 3 seconds
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(3000L)
                            currentTesterIndex = (currentTesterIndex + 1) % testers.size
                        }
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = currentTesterIndex,
                            transitionSpec = {
                                (slideInHorizontally(animationSpec = tween(500)) { width -> width } + fadeIn(animationSpec = tween(500)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(500)) { width -> -width } + fadeOut(animationSpec = tween(500)))
                            },
                            label = "tester_carousel"
                        ) { index ->
                            val (imageRes, nameRes, country) = testers[index]
                            TeamMemberCard(
                                modifier = Modifier.widthIn(max = 200.dp),
                                imageResId = imageRes,
                                name = stringResource(nameRes),
                                role = stringResource(R.string.info_role_tester),
                                country = country,
                                isFounder = false
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Page indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            testers.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentTesterIndex) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentTesterIndex)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- FEATURES GRID ---
        item {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.info_features),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val features = listOf(
                            stringResource(R.string.info_feature_1),
                            stringResource(R.string.info_feature_2),
                            stringResource(R.string.info_feature_3),
                            stringResource(R.string.info_feature_4),
                            stringResource(R.string.info_feature_5)
                        )
                        
                        features.forEach { feature ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Rounded.CheckCircle, 
                                    null, 
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp), 
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- PROJECT INFO ---
        item {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Info, null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.info_developer_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.info_developer_name),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItemCompact(Icons.Rounded.Code, "License", stringResource(R.string.info_license_type))
                        InfoItemCompact(Icons.Rounded.Build, "Build", BuildConfig.BUILD_TYPE)
                    }
                }
            }
        }

        // --- LINKS & ACTIONS ---
        item {
            GlassmorphicCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.info_links_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    FilledTonalButton(
                        onClick = { uriHandler.openUri(sourceUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Code, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.info_source_code))
                    }

                    OutlinedButton(
                        onClick = { uriHandler.openUri(plingUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.info_pling))
                    }
                }
            }
        }

        // --- COPYRIGHT FOOTER ---
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.info_copyright, "2025"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Made with ❤️ in Indonesia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
private fun TeamMemberCard(
    modifier: Modifier = Modifier,
    imageResId: Int,
    name: String,
    role: String,
    country: String,
    isFounder: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with gradient border
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = if (isFounder) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        },
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Name with country flag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = country,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Role Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isFounder) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (isFounder) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoItemCompact(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp)
        ) {
            Icon(icon, null, modifier = Modifier.padding(6.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
