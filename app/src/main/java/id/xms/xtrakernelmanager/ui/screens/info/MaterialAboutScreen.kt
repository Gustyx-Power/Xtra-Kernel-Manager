package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
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
            R.drawable.team_contributor_shimoku,
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
            telegramUrl = "https://t.me/juns37",
            githubUsername = "juns37",
            shapeIndex = 20, // Cookie12
        ),
        TeamMember(
            R.drawable.team_tester_achmad,
            "Achmadh",
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
            R.drawable.team_tester_sleep,
            "Adi Suki",
            "Tester",
            shapeIndex = 13, // Gem
        ),
        TeamMember(
            R.drawable.team_tester_azhar,
            "Azhar",
            "Tester",
            shapeIndex = 13, // Gem
        ),
        TeamMember(
            R.drawable.logo_a,
            "NTT Rules",
            "Tester",
            shapeIndex = 13, // Gem
        ),
        TeamMember(
            R.drawable.team_sm_tester,
            "Muttahir",
            "Tester",
            shapeIndex = 10, // Ghost
        ),
    )
    
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAboutScreen(
    onNavigateToWebView: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {}
) {
  val uriHandler = LocalUriHandler.current

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "About",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
              )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )
      },
  ) { paddingValues ->
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp // Extra padding for floating bottom bar
        )
    ) {
      // Hero Card - ColorOS Style
      item {
        MaterialColorOSHeroCard()
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Device Info Grid (2 columns)
      item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          MaterialColorOSInfoCard(
              modifier = Modifier.weight(1f),
              icon = Icons.Rounded.PhoneAndroid,
              label = "App Name",
              value = "XKM"
          )
          MaterialColorOSInfoCard(
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
        MaterialColorOSSpecItem(
            label = "Build Type",
            value = BuildConfig.BUILD_TYPE.uppercase()
        )
      }

      item {
        MaterialColorOSSpecItem(
            label = "License",
            value = "MIT License",
            onClick = { onNavigateToLicense() }
        )
      }

      item {
        MaterialColorOSSpecItem(
            label = "Community",
            value = "Telegram Channel",
            onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") }
        )
      }

      item {
        MaterialColorOSSpecItem(
            label = "Website",
            value = "xtramanagersoftwares.tech",
            onClick = { onNavigateToWebView() }
        )
      }

      item {
        MaterialColorOSSpecItem(
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
              TeamMemberCompactCard(member, uriHandler)
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

@Composable
private fun SectionHeader(title: String) {
  Text(
      title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp),
  )
}

// ColorOS Style Hero Card for Material Theme
@Composable
private fun MaterialColorOSHeroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(0.8f),
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
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
                                MaterialTheme.colorScheme.secondary.copy(0.6f),
                                MaterialTheme.colorScheme.secondary.copy(0.3f),
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
                                MaterialTheme.colorScheme.tertiary.copy(0.5f),
                                MaterialTheme.colorScheme.tertiary.copy(0.2f),
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
                    text = "XKM",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Xtra Kernel Manager",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.9f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "${BuildConfig.VERSION_NAME} | ${BuildConfig.BUILD_TYPE.uppercase()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom: Status
                Text(
                    text = "Version up to date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ColorOS Style Info Card (2 column grid items) for Material Theme
@Composable
private fun MaterialColorOSInfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ColorOS Style Spec Item (list item) for Material Theme
@Composable
private fun MaterialColorOSSpecItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun TeamCarousel(
    members: List<TeamMember>,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  LazyRow(
      contentPadding = PaddingValues(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxWidth(),
  ) {
    items(members, key = { it.name }) { member -> TeamMemberCarouselCard(member, uriHandler) }
  }
}

// Compact card for grid layout
@Composable
private fun TeamMemberCompactCard(
    member: TeamMember,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  val memberShape = remember(member.shapeIndex) { ExpressiveShapes.getShape(member.shapeIndex) }
  val hasSocial = member.githubUrl != null || member.telegramUrl != null
  val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

  Card(
      modifier = Modifier
          .fillMaxWidth()
          .height(140.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer
      )
  ) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar
      Box(
          modifier = Modifier
              .size(80.dp)
              .clip(memberShape)
              .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = member.role,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
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
                    tint = iconTint,
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
                    tint = iconTint,
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

@Composable
private fun TeamMemberCarouselCard(
    member: TeamMember,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
  val memberShape = remember(member.shapeIndex) { ExpressiveShapes.getShape(member.shapeIndex) }
  val hasSocial = member.githubUrl != null || member.telegramUrl != null
  val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

  Card(
      modifier = Modifier.width(160.dp).height(240.dp), // Fixed height for consistency
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top, // Change to Top for better control
    ) {
      // Avatar
      Box(
          modifier =
              Modifier.size(90.dp)
                  .clip(memberShape)
                  .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Role
      Text(
          text = member.role,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary,
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
                  tint = iconTint,
                  modifier = Modifier.size(20.dp),
              )
            }
          }

          member.telegramUrl?.let { url ->
            IconButton(onClick = { uriHandler.openUri(url) }, modifier = Modifier.size(40.dp)) {
              Icon(
                  imageVector = SimpleIcons.Telegram,
                  contentDescription = "Telegram",
                  tint = iconTint,
                  modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    }
  }
}
