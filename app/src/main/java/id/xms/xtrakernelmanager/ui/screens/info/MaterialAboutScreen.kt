package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
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
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Github
import compose.icons.simpleicons.Telegram
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAboutScreen() {
  val uriHandler = LocalUriHandler.current
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  val teamMembers = remember {
    listOf(
        TeamMember(
            R.drawable.team_dev_gustyx,
            "Gustyx-Power",
            "Founder & Dev",
            githubUrl = "https://github.com/Gustyx-Power",
            telegramUrl = "https://t.me/GustyxPower",
        ),
        TeamMember(
            R.drawable.team_dev_dimsvel,
            "Pavelc4",
            "Founder & UI/UX",
            githubUrl = "https://github.com/Pavelc4",
            telegramUrl = "https://t.me/Pavellc",
        ),
        TeamMember(
            R.drawable.team_contributor_pandu,
            "Ziyu",
            "Contributor",
            githubUrl = "https://github.com/ziyu4",
            telegramUrl = "https://t.me/ziyu4",
        ),
        TeamMember(
            R.drawable.team_contributor_shimoku,
            "Shimoku",
            "Contributor",
        ),
        TeamMember(
            R.drawable.team_tester_wil,
            "Wil",
            "Tester",
            githubUrl = "https://github.com/Steambot12",
            telegramUrl = "https://t.me/Steambot12",
        ),
        TeamMember(R.drawable.team_tester_achmad, "Achmad", "Tester"),
        TeamMember(R.drawable.team_tester_hasan, "Hasan", "Tester"),
    )
  }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar( // Left-aligned title
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
        columns = StaggeredGridCells.Adaptive(150.dp), // Bento Grid Base
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding =
            PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
      item(span = StaggeredGridItemSpan.FullLine) { SectionHeader("Our Team") }

      // --- TEAM MEMBERS (Horizontal Carousel) ---
      item(span = StaggeredGridItemSpan.FullLine) { TeamCarousel(teamMembers) }

      // --- BENTO GRID INFO SECTION ---
      item(span = StaggeredGridItemSpan.FullLine) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionHeader("Community & Info")
      }

      // 1. Join Community (Prominent)
      item(span = StaggeredGridItemSpan.FullLine) {
        CommunityBentoCard(onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") })
      }

      // 2. License
      item {
        BentoCard(
            title = "License",
            subtitle = "MIT License",
            icon = Icons.Rounded.Gavel,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = {
              uriHandler.openUri(
                  "https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/blob/256c019bcff5754d27cdc8db4fd049d30866f63c/LICENSE"
              )
            },
        )
      }

      // 3. Website
      item {
        BentoCard(
            title = "Website",
            subtitle = "Coming Soon",
            icon = Icons.Rounded.Language, // Globe icon
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = { /* TODO */ },
        )
      }

      // 4. Version (Full width or half?) - Let's do a compact row?
      // Actually let's do more squares.
      item {
        BentoCard(
            title = "Version",
            subtitle = "${BuildConfig.VERSION_NAME}",
            icon = Icons.Rounded.Info,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { /* TODO */ },
        )
      }

      // 5. Copyright
      item {
        BentoCard(
            title = "Copyright",
            subtitle = "© 2025",
            icon = Icons.Rounded.Copyright,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = { /* TODO */ },
        )
      }
    }
  }
}

// HeroHeader removed

@Composable
private fun ActionRow(onGithubClick: () -> Unit, onPlingClick: () -> Unit) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    ActionButton(
        text = "GitHub",
        icon = Icons.Rounded.Code,
        color = MaterialTheme.colorScheme.primary,
        onClick = onGithubClick,
        modifier = Modifier.weight(1f),
    )
    ActionButton(
        text = "Pling",
        icon = Icons.Rounded.Download,
        color = MaterialTheme.colorScheme.tertiary,
        onClick = onPlingClick,
        modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      onClick = onClick,
      shape = CircleShape,
      color = color,
      modifier = modifier.height(56.dp),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
      Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onPrimary,
      )
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
      modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
  )
}

// --- NEW BENTO COMPONENTS ---

@Composable
fun CommunityBentoCard(onClick: () -> Unit) {
  Card(
      onClick = onClick,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth().height(140.dp), // Prominent height
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background decoration
      Icon(
          Icons.Rounded.Groups,
          contentDescription = null,
          modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
      )

      Column(modifier = Modifier.padding(20.dp).align(Alignment.TopStart)) {
        Icon(
            Icons.Rounded.Groups,
            null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Join Community",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            "Get help & updates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
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
  Card(
      onClick = onClick,
      colors = CardDefaults.cardColors(containerColor = color),
      shape = RoundedCornerShape(24.dp),
      modifier = Modifier.fillMaxWidth().height(110.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
      Icon(icon, null, tint = contentColorFor(color), modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.weight(1f))
      Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = contentColorFor(color).copy(alpha = 0.7f),
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColorFor(color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

data class TeamMember(
    val imageRes: Int,
    val name: String,
    val role: String,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
)

@Composable
private fun TeamCarousel(members: List<TeamMember>) {
  androidx.compose.foundation.lazy.LazyRow(
      contentPadding = PaddingValues(horizontal = 4.dp), // Align with grid padding visually
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxWidth(),
  ) {
    items(members.size) { index -> TeamMemberCarouselCard(members[index]) }
  }
}

@Composable
private fun TeamMemberCarouselCard(member: TeamMember) {
  val uriHandler = LocalUriHandler.current
  Card(
      modifier = Modifier.width(160.dp).height(240.dp), // Tall, prominent cards
      shape = RoundedCornerShape(28.dp), // Uniform, modern corners
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, // Revert to Center for alignment without icons
    ) {
      // Large Avatar
      Box(
          modifier =
              Modifier.size(90.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceContainerHigh)
      ) {
        Image(
            painter = painterResource(member.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Name
      Text(
          member.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )

      // Role
      Text(
          member.role,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )

      // Social Icons (Conditionally visible)
      if (member.githubUrl != null || member.telegramUrl != null) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          member.githubUrl?.let { url ->
            Icon(
                imageVector = SimpleIcons.Github,
                contentDescription = "GitHub",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp).clickable { uriHandler.openUri(url) },
            )
          }

          member.telegramUrl?.let { url ->
            Icon(
                imageVector = SimpleIcons.Telegram,
                contentDescription = "Telegram",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp).clickable { uriHandler.openUri(url) },
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      } else {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
