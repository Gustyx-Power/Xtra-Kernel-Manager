package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.min

@Composable
fun LiquidInfoScreen() {
    val uriHandler = LocalUriHandler.current
    val sourceUrl = stringResource(R.string.info_source_code_url)
    val plingUrl = stringResource(R.string.info_pling_url)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            HeroSection(scrollState.value)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatsSection()
                TeamSection()
                FeaturesSection()
                InfoLinksSection(
                    onSourceCodeClick = { uriHandler.openUri(sourceUrl) },
                    onPlingClick = { uriHandler.openUri(plingUrl) }
                )
                FooterSection()
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeroSection(scrollOffset: Int) {
    val parallaxFactor = min(scrollOffset / 400f, 1f)
    val scale = 1f - (parallaxFactor * 0.2f)
    val alpha = 1f - (parallaxFactor * 0.8f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                translationY = -scrollOffset * 0.4f
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .blur(30.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF007AFF).copy(0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                GlassmorphicCard(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(26.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_a),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }
            }

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge("v${BuildConfig.VERSION_NAME}", Color(0xFF007AFF))
                Badge(BuildConfig.BUILD_TYPE.uppercase(), Color(0xFFAF52DE))
            }

            Text(
                text = stringResource(R.string.info_tagline),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    val isLight = !isSystemInDarkTheme()
    Surface(
        color = color.copy(if (isLight) 0.15f else 0.2f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color
        )
    }
}

@Composable
private fun StatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(Modifier.weight(1f), "2", "Founders", Icons.Rounded.Star, Color(0xFFFFD700))
        StatCard(Modifier.weight(1f), "3", "Contributors", Icons.Rounded.Code, Color(0xFF34C759))
        StatCard(Modifier.weight(1f), "8", "Testers", Icons.Rounded.BugReport, Color(0xFFFF9500))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    val isLight = !isSystemInDarkTheme()
    GlassmorphicCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(if (isLight) 0.15f else 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TeamSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Team", "13 members")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item { TeamMember(R.drawable.team_dev_dimsvel, "Pavelc4", "Founder", Color(0xFF007AFF)) }
            item { TeamMember(R.drawable.team_dev_gustyx, "Gustyx-Power", "Founder", Color(0xFFAF52DE)) }
            
            item { TeamMember(R.drawable.team_contributor_pandu, "Ziyu", "Contributor", Color(0xFF34C759)) }
            item { TeamMember(R.drawable.team_contributor_shimoku, "Shimoku", "Contributor", Color(0xFF34C759)) }
            item { TeamMember(R.drawable.team_contributor_rio, stringResource(R.string.team_contributor_rio), "Contributor", Color(0xFF34C759)) }
            
            item { TeamMember(R.drawable.team_tester_achmad, stringResource(R.string.team_tester_achmad), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_hasan, stringResource(R.string.team_tester_hasan), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_reffan, stringResource(R.string.team_tester_reffan), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_wil, stringResource(R.string.team_tester_wil), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_sm_tester, stringResource(R.string.team_tester_shadow_monarch), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_azhar, stringResource(R.string.team_tester_azhar), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_juni, stringResource(R.string.team_tester_juni), "Tester", Color(0xFFFF9500)) }
            item { TeamMember(R.drawable.team_tester_sleep, stringResource(R.string.team_tester_sleep), "Tester", Color(0xFFFF9500)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            val isLight = !isSystemInDarkTheme()
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(if (isLight) 0.5f else 0.3f),
                shape = CircleShape
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TeamMember(imageRes: Int, name: String, role: String, color: Color) {
    val isLight = !isSystemInDarkTheme()
    GlassmorphicCard(
        modifier = Modifier.width(100.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, color, CircleShape)
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                shape = CircleShape,
                color = color.copy(if (isLight) 0.15f else 0.2f)
            ) {
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeaturesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Features")
        
        val features = listOf(
            Triple(stringResource(R.string.info_feature_1), Icons.Rounded.Speed, Color(0xFF007AFF)),
            Triple(stringResource(R.string.info_feature_2), Icons.Rounded.Thermostat, Color(0xFFFF3B30)),
            Triple(stringResource(R.string.info_feature_3), Icons.Rounded.BatteryChargingFull, Color(0xFF34C759)),
            Triple(stringResource(R.string.info_feature_4), Icons.Rounded.Palette, Color(0xFFFF9500)),
            Triple(stringResource(R.string.info_feature_5), Icons.Rounded.Security, Color(0xFF5856D6))
        )
        
        GlassmorphicCard(contentPadding = PaddingValues(0.dp)) {
            Column {
                features.forEachIndexed { index, (text, icon, color) ->
                    FeatureRow(text, icon, color)
                    if (index < features.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(0.06f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String, icon: ImageVector, color: Color) {
    val isLight = !isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(if (isLight) 0.15f else 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoLinksSection(onSourceCodeClick: () -> Unit, onPlingClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("About")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                Modifier.weight(1f),
                Icons.Rounded.Groups,
                "Community",
                stringResource(R.string.info_community_name),
                Color(0xFF007AFF)
            )
            InfoCard(
                Modifier.weight(1f),
                Icons.Rounded.Code,
                "License",
                stringResource(R.string.info_license_type),
                Color(0xFF5856D6)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                Modifier.weight(1f),
                Icons.Rounded.Code,
                "Source Code",
                Color(0xFF007AFF),
                onSourceCodeClick
            )
            ActionButton(
                Modifier.weight(1f),
                Icons.Rounded.Download,
                "Download",
                Color(0xFF34C759),
                onPlingClick,
                outlined = true
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val isLight = !isSystemInDarkTheme()
    GlassmorphicCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(if (isLight) 0.15f else 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(22.dp), tint = color)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    val isLight = !isSystemInDarkTheme()
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (outlined) {
                    Modifier.border(1.5.dp, color.copy(0.3f), RoundedCornerShape(14.dp))
                } else {
                    Modifier.background(color.copy(if (isLight) 0.15f else 0.2f))
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.info_copyright, "2025"),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
        )
        Text(
            text = "Made with ❤️ in Indonesia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
        )
    }
}
