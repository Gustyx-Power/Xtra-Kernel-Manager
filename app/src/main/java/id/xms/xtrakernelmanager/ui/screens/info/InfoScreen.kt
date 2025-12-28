package id.xms.xtrakernelmanager.ui.screens.info

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@Composable
fun InfoScreen() {
  val uriHandler = LocalUriHandler.current
  val sourceUrl = stringResource(R.string.info_source_code_url)
  val plingUrl = stringResource(R.string.info_pling_url)

  // Menggunakan Staggered Grid agar konsisten dengan Home
  LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp), // Bottom padding untuk nav bar
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalItemSpacing = 16.dp,
  ) {
    // --- HEADER APP INFO ---
    item(span = StaggeredGridItemSpan.FullLine) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.padding(vertical = 16.dp),
      ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 16.dp,
            shadowElevation = 8.dp,
        ) {
          Icon(
              painter = painterResource(id = R.drawable.logo_a),
              contentDescription = null,
              modifier = Modifier.size(100.dp).padding(16.dp),
              tint = Color.Unspecified,
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )

          // Versi & Build Type Badge
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(top = 4.dp),
          ) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text("v${BuildConfig.VERSION_NAME}", modifier = Modifier.padding(horizontal = 8.dp))
            }
            Badge(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
              Text(
                  BuildConfig.BUILD_TYPE.uppercase(),
                  modifier = Modifier.padding(horizontal = 8.dp),
              )
            }
          }
        }

        Text(
            text = stringResource(R.string.info_tagline),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
      }
    }

    // --- DEVELOPER CARD ---
    item {
      GlassmorphicCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
              Icon(
                  Icons.Rounded.Person,
                  null,
                  modifier = Modifier.padding(10.dp),
                  tint = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                  text = stringResource(R.string.info_developer_section_title),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
              )
              Text(
                  text = stringResource(R.string.info_developer_name),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            InfoItemCompact(
                Icons.Rounded.Code,
                "License",
                stringResource(R.string.info_license_type),
            )
            InfoItemCompact(Icons.Rounded.Build, "Build", BuildConfig.BUILD_TYPE)
          }
        }
      }
    }

    // --- FEATURES GRID (Modern) ---
    item {
      GlassmorphicCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.info_features),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
          }

          // Grid fitur (2 kolom manual dalam column)
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val features =
                listOf(
                    stringResource(R.string.info_feature_1),
                    stringResource(R.string.info_feature_2),
                    stringResource(R.string.info_feature_3),
                    stringResource(R.string.info_feature_4),
                    stringResource(R.string.info_feature_5),
                )

            features.forEach { feature ->
              Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
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
              fontWeight = FontWeight.Bold,
          )

          FilledTonalButton(
              onClick = { uriHandler.openUri(sourceUrl) },
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(12.dp),
              shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Rounded.Code, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.info_source_code))
          }

          OutlinedButton(
              onClick = { uriHandler.openUri(plingUrl) },
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(12.dp),
              shape = RoundedCornerShape(12.dp),
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
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      ) {
        Text(
            text = stringResource(R.string.info_copyright, "2025"),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Made with ❤️ in Indonesia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
      }
    }
  }
}

// --- KOMPONEN PEMBANTU ---

@Composable
private fun InfoItemCompact(icon: ImageVector, label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(32.dp),
    ) {
      Icon(
          icon,
          null,
          modifier = Modifier.padding(6.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
          label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
  }
}
