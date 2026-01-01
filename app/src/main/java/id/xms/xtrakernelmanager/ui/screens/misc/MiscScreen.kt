package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.usecase.FunctionalRomUseCase
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.ui.screens.misc.section.BatteryInfoSection
import id.xms.xtrakernelmanager.ui.screens.misc.section.DisplaySection
import id.xms.xtrakernelmanager.ui.screens.misc.section.GameControlSection

@Composable
fun MiscScreen(viewModel: MiscViewModel, onNavigateToFunctionalRom: () -> Unit = {}) {
  // Collect Layout Style Preference
  val layoutStyle by viewModel.layoutStyle.collectAsState()

  // Switch between designs
  if (layoutStyle == "material") {
    MaterialMiscScreen(
        viewModel = viewModel,
        onNavigate = { route -> if (route == "functional_rom") onNavigateToFunctionalRom() },
    )
  } else {
    LegacyMiscScreen(viewModel, onNavigateToFunctionalRom)
  }
}

@Composable
fun LegacyMiscScreen(viewModel: MiscViewModel, onNavigateToFunctionalRom: () -> Unit = {}) {
  // Security gate state
  var isVipCommunity by remember { mutableStateOf<Boolean?>(null) }
  var showSecurityWarning by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val useCase = remember { FunctionalRomUseCase() }

  // Check VIP community status on first composition
  LaunchedEffect(Unit) { isVipCommunity = useCase.checkVipCommunity() }

  // Security Warning Dialog
  if (showSecurityWarning) {
    AlertDialog(
        onDismissRequest = { showSecurityWarning = false },
        icon = {
          Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
          )
        },
        title = {
          Text(
              text = stringResource(R.string.security_warning_title),
              style = MaterialTheme.typography.titleLarge,
          )
        },
        text = {
          Text(
              text = stringResource(R.string.security_warning_message),
              style = MaterialTheme.typography.bodyMedium,
          )
        },
        confirmButton = {
          TextButton(onClick = { showSecurityWarning = false }) {
            Text(stringResource(R.string.security_warning_button))
          }
        },
    )
  }

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item { PillCard(text = stringResource(R.string.misc_title)) }

    item { BatteryInfoSection(viewModel) }

    item { GameControlSection(viewModel) }

    item { DisplaySection(viewModel) }

    // Functional ROM Card with Security Gate
    item {
      FunctionalRomCard(
          isVipCommunity = isVipCommunity ?: false,
          isLoading = isVipCommunity == null,
          onClick = {
            if (isVipCommunity == true) {
              onNavigateToFunctionalRom()
            } else {
              showSecurityWarning = true
            }
          },
      )
    }
  }
}

@Composable
private fun FunctionalRomCard(isVipCommunity: Boolean, isLoading: Boolean, onClick: () -> Unit) {
  // Apply blur and alpha when not VIP
  val cardModifier =
      if (!isVipCommunity && !isLoading) {
        Modifier.fillMaxWidth().blur(4.dp).alpha(0.6f)
      } else {
        Modifier.fillMaxWidth()
      }

  GlassmorphicCard(modifier = cardModifier, onClick = onClick, enabled = !isLoading) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Icon(
          imageVector = Icons.Default.Extension,
          contentDescription = null,
          modifier = Modifier.size(48.dp),
          tint = MaterialTheme.colorScheme.primary,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = stringResource(R.string.functional_rom_card_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.functional_rom_card_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
      }
      if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
      } else {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
      }
    }
  }
}
