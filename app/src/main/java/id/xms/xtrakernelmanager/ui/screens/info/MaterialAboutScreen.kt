package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAboutScreen() {
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
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding =
            PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
      item(span = StaggeredGridItemSpan.FullLine) { SectionHeader("Community & Info") }

      item(span = StaggeredGridItemSpan.FullLine) {
        CommunityBentoCard(onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") })
      }

      item {
        BentoCard(
            title = "License",
            subtitle = "MIT License",
            icon = Icons.Rounded.Gavel,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = {
              uriHandler.openUri(
                  "https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/blob/main/LICENSE"
              )
            },
        )
      }

      item {
        BentoCard(
            title = "Website",
            subtitle = "Coming Soon",
            icon = Icons.Rounded.Language,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = {},
        )
      }

      item {
        BentoCard(
            title = "Version",
            subtitle = BuildConfig.VERSION_NAME,
            icon = Icons.Rounded.Info,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = {},
        )
      }

      item {
        BentoCard(
            title = "Copyright",
            subtitle = "© 2025 XMS",
            icon = Icons.Rounded.Copyright,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = {},
        )
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader("Founders")
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        val founders = teamMembers.filter { it.role.contains("Founder", ignoreCase = true) }
        TeamCarousel(founders, uriHandler)
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader("Contributors")
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        val contributors = teamMembers.filter { it.role.contains("Contributor", ignoreCase = true) }
        TeamCarousel(contributors, uriHandler)
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader("Testers")
      }

      item(span = StaggeredGridItemSpan.FullLine) {
        val testers = teamMembers.filter { it.role.contains("Tester", ignoreCase = true) }
        TeamCarousel(testers, uriHandler)
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

@Composable
fun CommunityBentoCard(onClick: () -> Unit) {
  val containerColor = MaterialTheme.colorScheme.primaryContainer
  val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
  val iconTint = remember(contentColor) { contentColor.copy(alpha = 0.1f) }
  val subtitleColor = remember(contentColor) { contentColor.copy(alpha = 0.8f) }

  Card(
      onClick = onClick,
      colors = CardDefaults.cardColors(containerColor = containerColor),
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth().height(140.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Icon(
          Icons.Rounded.Groups,
          contentDescription = null,
          modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp),
          tint = iconTint,
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
            color = subtitleColor,
        )
      }
    }
  }
}

@Composable
fun BentoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
  val contentColor = contentColorFor(color)
  val labelColor = remember(contentColor) { contentColor.copy(alpha = 0.7f) }

  Card(
      onClick = onClick,
      colors = CardDefaults.cardColors(containerColor = color),
      shape = RoundedCornerShape(24.dp),
      modifier = Modifier.fillMaxWidth().height(110.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
      Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
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

@Immutable
data class TeamMember(
    val imageRes: Int,
    val name: String,
    val role: String,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
    val githubUsername: String? = null,
    val shapeIndex: Int = 0,
)

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
