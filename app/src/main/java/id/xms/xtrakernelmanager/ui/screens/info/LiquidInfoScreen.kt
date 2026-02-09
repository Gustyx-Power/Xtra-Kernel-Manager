package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember
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
            shapeIndex = 19, // Cookie9
        ),
        TeamMember(
            R.drawable.logo_a,
            "Pavelc4",
            "Founder & UI/UX",
            githubUrl = "https://github.com/Pavelc4",
            telegramUrl = "https://t.me/Pavellc",
            githubUsername = "Pavelc4",
            shapeIndex = 30, // Pixel Circle
        ),
        TeamMember(
            R.drawable.logo_a,
            "Ziyu",
            "Contributor",
            githubUrl = "https://github.com/ziyu4",
            telegramUrl = "https://t.me/ziyu4",
            githubUsername = "ziyu4",
            shapeIndex = 19, // Cookie6
        ),
        TeamMember(
            R.drawable.team_contributor_rio,
            "Rio",
            "Contributor",
            telegramUrl = "https://t.me/hy6nies",
            shapeIndex = 16, // Cookie4
        ),
        TeamMember(
            R.drawable.logo_a,
            "Shimoku",
            "Contributor",
            githubUrl = "https://github.com/shimokuu",
            telegramUrl = "https://t.me/xdshimokuu",
            shapeIndex = 3, // Burst
        ),
        TeamMember(
            R.drawable.logo_a,
            "Wil",
            "Tester",
            githubUrl = "https://github.com/Steambot12",
            telegramUrl = "https://t.me/Steambot12",
            githubUsername = "Steambot12",
            shapeIndex = 20, // Cookie12
        ),
        TeamMember(
            R.drawable.logo_a,
            "ᴶᵁᴻᴵ༄",
            "Tester",
            githubUrl = "https://github.com/juns37",
            telegramUrl = "https://t.me/@juns37",
            githubUsername = "juns37",
            shapeIndex = 20, // Cookie12
        ),
        TeamMember(
            R.drawable.team_tester_achmad,
            "Achmad",
            "Tester",
            shapeIndex = 5, // Square
        ),
        TeamMember(
            R.drawable.team_tester_hasan,
            "Hasan",
            "Tester",
            shapeIndex = 16, // Cookie4
        ),
        TeamMember(
            R.drawable.team_tester_reffan,
            "Reffan Lintang M",
            "Tester",
            shapeIndex = 13, // Gem
        ),
        TeamMember(
            R.drawable.team_sm_tester,
            "Shadow Monarch",
            "Tester",
            shapeIndex = 10, // Ghost
        ),
    )

@Composable
fun LiquidInfoScreen() {
    val uriHandler = LocalUriHandler.current
    
    // Force dark/neon colors for Liquid UI consistency
    val liquidBlobColors = listOf(
        Color(0xFF4A9B8E), 
        Color(0xFF8BA8D8), 
        Color(0xFF6BC4E8)  
    )

    Box(modifier = Modifier.fillMaxSize()) {
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize(),
            colors = liquidBlobColors
        )

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                HeroSection()
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                SectionHeader("Community & Info")
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                GlassmorphicCommunityCard(onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") })
            }

            item {
                GlassmorphicBentoCard(
                    title = "License",
                    subtitle = "MIT License",
                    icon = Icons.Rounded.Gavel,
                    color = Color(0xFF5856D6), // Purple
                    onClick = {
                        uriHandler.openUri(
                            "https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/blob/main/LICENSE"
                        )
                    },
                )
            }

            item {
                GlassmorphicBentoCard(
                    title = "Website",
                    subtitle = "Coming Soon",
                    icon = Icons.Rounded.Language,
                    color = Color(0xFF34C759), // Green
                    onClick = {},
                )
            }

            item {
                GlassmorphicBentoCard(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    icon = Icons.Rounded.Info,
                    color = Color(0xFF007AFF), // Blue
                    onClick = {},
                )
            }

            item {
                GlassmorphicBentoCard(
                    title = "Copyright",
                    subtitle = "© 2025 XMS",
                    icon = Icons.Rounded.Copyright,
                    color = Color(0xFFFF9500), // Orange
                    onClick = {},
                )
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Founders")
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                val founders = teamMembers.filter { it.role.contains("Founder", ignoreCase = true) }
                LiquidTeamCarousel(founders, uriHandler)
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Contributors")
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                val contributors = teamMembers.filter { it.role.contains("Contributor", ignoreCase = true) }
                LiquidTeamCarousel(contributors, uriHandler)
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Testers")
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                val testers = teamMembers.filter { it.role.contains("Tester", ignoreCase = true) }
                LiquidTeamCarousel(testers, uriHandler)
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
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
            color = Color.White
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge("v${BuildConfig.VERSION_NAME}", Color(0xFF007AFF))
            Badge(BuildConfig.BUILD_TYPE.uppercase(), Color(0xFFAF52DE))
        }

        Text(
            text = stringResource(R.string.info_tagline),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.White.copy(0.7f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp),
    )
}

@Composable
fun GlassmorphicCommunityCard(onClick: () -> Unit) {
    val isLight = !isSystemInDarkTheme()
    val containerColor = Color(0xFF007AFF) // Material Primary Container-ish but blue
    val contentColor = Color.White

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(containerColor.copy(alpha = 0.85f))
        ) {
            Icon(
                Icons.Rounded.Groups,
                contentDescription = null,
                modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp),
                tint = contentColor.copy(alpha = 0.2f),
            )

            Column(modifier = Modifier.padding(20.dp).align(Alignment.TopStart)) {
                Icon(
                    Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Join Community",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Text(
                    "Get help & updates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun GlassmorphicBentoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    val isLight = !isSystemInDarkTheme()
    val contentColor = Color.White

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth().height(110.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.85f))
        ) {
            // Decorative background icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 16.dp),
                tint = contentColor.copy(alpha = 0.15f)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                     modifier = Modifier
                         .size(32.dp)
                         .clip(RoundedCornerShape(8.dp))
                         .background(Color.White.copy(0.2f)),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(icon, null, modifier = Modifier.size(18.dp), tint = contentColor)
                 }
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.8f),
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}


@Composable
private fun LiquidTeamCarousel(
    members: List<TeamMember>,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  LazyRow(
      contentPadding = PaddingValues(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxWidth(),
  ) {
    items(members, key = { it.name }) { member -> LiquidTeamMemberCard(member, uriHandler) }
  }
}

@Composable
private fun LiquidTeamMemberCard(
    member: TeamMember,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  val memberShape = remember(member.shapeIndex) { ExpressiveShapes.getShape(member.shapeIndex) }
  val hasSocial = member.githubUrl != null || member.telegramUrl != null
  val isLight = !isSystemInDarkTheme()

  // Determine card color based on role
  val cardColor =
      when {
        member.role.contains("Founder", ignoreCase = true) -> Color(0xFF007AFF) // Blue
        member.role.contains("Contributor", ignoreCase = true) -> Color(0xFF34C759) // Green
        member.role.contains("Tester", ignoreCase = true) -> Color(0xFFFF2D55) // Pink
        else -> Color(0xFF8E8E93) // Gray
      }

  val contentColor = Color.White
  val socialIconTint = contentColor.copy(alpha = 0.8f)

  GlassmorphicCard(
      modifier = Modifier.width(160.dp).height(240.dp),
      contentPadding = PaddingValues(0.dp)
  ) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(cardColor.copy(alpha = 0.85f))
    ) {
      Column(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Top,
      ) {
            // Avatar
            Box(
                modifier =
                    Modifier.size(90.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                color = contentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Role
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            // Social Icons
            if (hasSocial) {
              Spacer(modifier = Modifier.weight(1f))
              Row(
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
              ) {
                member.githubUrl?.let { url ->
                  IconButton(onClick = { uriHandler.openUri(url) }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = SimpleIcons.Github,
                        contentDescription = "GitHub",
                        tint = socialIconTint,
                        modifier = Modifier.size(20.dp),
                    )
                  }
                }

                member.telegramUrl?.let { url ->
                  IconButton(onClick = { uriHandler.openUri(url) }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = SimpleIcons.Telegram,
                        contentDescription = "Telegram",
                        tint = socialIconTint,
                        modifier = Modifier.size(20.dp),
                    )
                  }
                }
              }
            }
      }
    }
  }
}
