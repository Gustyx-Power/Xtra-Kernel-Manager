package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Github
import compose.icons.simpleicons.Telegram
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.theme.ExpressiveShapes

private val teamMembers =
    listOf(
        TeamMember(
            R.drawable.logo_a,
            "Gustyx-Power",
            "Founder & Dev",
            githubUrl = "https://github.com/Gustyx-Power",
            telegramUrl = "https://t.me/GustyxPower",
            githubUsername = "Gustyx-Power",
            shapeIndex = 19,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Pavelc4",
            "Founder & UI/UX",
            githubUrl = "https://github.com/Pavelc4",
            telegramUrl = "https://t.me/Pavellc",
            githubUsername = "Pavelc4",
            shapeIndex = 30,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Ziyu",
            "Contributor",
            githubUrl = "https://github.com/ziyu4",
            telegramUrl = "https://t.me/ziyu4",
            githubUsername = "ziyu4",
            shapeIndex = 19,
        ),
        TeamMember(
            R.drawable.team_contributor_rio,
            "Rio",
            "Contributor",
            telegramUrl = "https://t.me/hy6nies",
            shapeIndex = 16,
        ),
        TeamMember(
            R.drawable.team_contributor_shimoku,
            "Shimoku",
            "Contributor",
            githubUrl = "https://github.com/shimokuu",
            telegramUrl = "https://t.me/xdshimokuu",
            shapeIndex = 3,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Wil",
            "Tester",
            githubUrl = "https://github.com/Steambot12",
            telegramUrl = "https://t.me/Steambot12",
            githubUsername = "Steambot12",
            shapeIndex = 20,
        ),
        TeamMember(
            R.drawable.logo_a,
            "ᴶᵁᴻᴵ༄",
            "Tester",
            githubUrl = "https://github.com/juns37",
            telegramUrl = "https://t.me/juns37",
            githubUsername = "juns37",
            shapeIndex = 20,
        ),
        TeamMember(
            R.drawable.team_tester_achmad,
            "Achmadh",
            "Tester",
            shapeIndex = 5,
        ),
        TeamMember(
            R.drawable.team_tester_hasan,
            "Hasan",
            "Tester",
            shapeIndex = 16,
        ),
        TeamMember(
            R.drawable.team_tester_reffan,
            "Reffan Lintang M",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_tester_sleep,
            "Adi Suki",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_tester_azhar,
            "Azhar",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.logo_a,
            "NTT Rules",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_sm_tester,
            "Muttahir",
            "Tester",
            shapeIndex = 10,
        ),
    )

@Composable
fun FrostedInfoScreen() {
    val uriHandler = LocalUriHandler.current
    
    val frostedBlobColors = listOf(
        Color(0xFF4A9B8E), 
        Color(0xFF8BA8D8), 
        Color(0xFF6BC4E8)  
    )

    Box(modifier = Modifier.fillMaxSize()) {
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize(),
            colors = frostedBlobColors
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with GlassmorphicCard
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                // Hero Card - ColorOS Style
                item {
                    ColorOSHeroCard()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Device Info Grid (2 columns)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ColorOSInfoCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.PhoneAndroid,
                            label = "App Name",
                            value = stringResource(R.string.app_name_short)
                        )
                        ColorOSInfoCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Storage,
                            label = "Version",
                            value = BuildConfig.VERSION_NAME
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Specifications List
                item {
                    ColorOSSpecItem(
                        label = "Build Type",
                        value = BuildConfig.BUILD_TYPE.uppercase()
                    )
                }

                item {
                    ColorOSSpecItem(
                        label = "License",
                        value = "MIT License",
                        onClick = {
                            uriHandler.openUri(
                                "https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/blob/main/LICENSE"
                            )
                        }
                    )
                }

                item {
                    ColorOSSpecItem(
                        label = "Community",
                        value = "Telegram Channel",
                        onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") }
                    )
                }

                item {
                    ColorOSSpecItem(
                        label = "Website",
                        value = "xtramanagersoftwares.tech",
                        onClick = { uriHandler.openUri("https://xtramanagersoftwares.tech/") }
                    )
                }

                item {
                    ColorOSSpecItem(
                        label = "Copyright",
                        value = "© 2025 XMS"
                    )
                }

                // Team Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Team")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Team Grid
                items(teamMembers.chunked(2)) { rowMembers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowMembers.forEach { member ->
                            Box(modifier = Modifier.weight(1f)) {
                                FrostedTeamMemberCompactCard(member, uriHandler)
                            }
                        }
                        // Fill empty space if odd number
                        if (rowMembers.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// ColorOS Style Hero Card
@Composable
private fun ColorOSHeroCard() {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B4FBB),
                            Color(0xFF3D5AFE),
                            Color(0xFF1E88E5)
                        )
                    )
                )
        ) {
            // Decorative circles (planet-like)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 180.dp, y = (-40).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6B9D).copy(0.6f),
                                Color(0xFFFF6B9D).copy(0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-30).dp, y = 200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4FC3F7).copy(0.5f),
                                Color(0xFF4FC3F7).copy(0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Content - Centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Icon(
                    painter = painterResource(R.drawable.logo_a),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // App name (like ColorOS branding)
                Text(
                    text = stringResource(R.string.app_name_short),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(0.9f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "${BuildConfig.VERSION_NAME} | ${BuildConfig.BUILD_TYPE.uppercase()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom: Status
                Text(
                    text = "Version up to date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ColorOS Style Info Card (2 column grid items)
@Composable
private fun ColorOSInfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    GlassmorphicCard(
        modifier = modifier.height(100.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(0.7f),
                modifier = Modifier.size(24.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ColorOS Style Spec Item (list item)
@Composable
private fun ColorOSSpecItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.7f),
                    textAlign = TextAlign.End
                )
                
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp),
    )
}

// Compact card for grid layout
@Composable
private fun FrostedTeamMemberCompactCard(
    member: TeamMember,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  val memberShape = remember(member.shapeIndex) { ExpressiveShapes.getShape(member.shapeIndex) }
  val hasSocial = member.githubUrl != null || member.telegramUrl != null
  val contentColor = Color.White
  val socialIconTint = contentColor.copy(alpha = 0.8f)

  GlassmorphicCard(
      modifier = Modifier
          .fillMaxWidth()
          .height(140.dp),
      contentPadding = PaddingValues(16.dp)
  ) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar
      Box(
          modifier = Modifier
              .size(80.dp)
              .clip(memberShape)
              .background(contentColor.copy(alpha = 0.2f))
      ) {
        if (member.githubUsername != null) {
          AsyncImage(
              model = "https://github.com/${member.githubUsername}.png",
              contentDescription = "${member.name} avatar",
              placeholder = painterResource(member.imageRes),
              error = painterResource(member.imageRes),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
          )
        } else {
          Image(
              painter = painterResource(member.imageRes),
              contentDescription = "${member.name} avatar",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
          )
        }
      }

      // Info
      Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
      ) {
        Text(
            text = member.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = member.role,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (hasSocial) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
          ) {
            member.githubUrl?.let { url ->
              IconButton(
                  onClick = { uriHandler.openUri(url) },
                  modifier = Modifier.size(32.dp)
              ) {
                Icon(
                    imageVector = SimpleIcons.Github,
                    contentDescription = "GitHub",
                    tint = socialIconTint,
                    modifier = Modifier.size(16.dp),
                )
              }
            }

            member.telegramUrl?.let { url ->
              IconButton(
                  onClick = { uriHandler.openUri(url) },
                  modifier = Modifier.size(32.dp)
              ) {
                Icon(
                    imageVector = SimpleIcons.Telegram,
                    contentDescription = "Telegram",
                    tint = socialIconTint,
                    modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}
