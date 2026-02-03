package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.model.PowerAction

@Composable
fun ExpandablePowerFab(onPowerAction: (PowerAction) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val rotation by
      animateFloatAsState(
          targetValue = if (expanded) 45f else 0f,
          animationSpec = tween(durationMillis = 300),
          label = "fab_rotation",
      )

  Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Expanded Menu Items
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
      Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.End,
      ) {
        // Recovery
        FabMenuItem(
            text = stringResource(id.xms.xtrakernelmanager.R.string.power_recovery),
            icon = Icons.Rounded.SettingsBackupRestore,
            onClick = {
              expanded = false
              onPowerAction(PowerAction.Recovery)
            },
            delay = 150,
        )

        // System UI
        FabMenuItem(
            text = stringResource(id.xms.xtrakernelmanager.R.string.power_system_ui),
            icon = Icons.Rounded.Refresh,
            onClick = {
              expanded = false
              onPowerAction(PowerAction.SystemUI)
            },
            delay = 100,
        )

        FabMenuItem(
            text = stringResource(id.xms.xtrakernelmanager.R.string.power_power_off),
            icon = Icons.Rounded.PowerSettingsNew,
            onClick = {
              expanded = false
              onPowerAction(PowerAction.PowerOff)
            },
            delay = 50,
        )

        // Reboot
        FabMenuItem(
            text = stringResource(id.xms.xtrakernelmanager.R.string.power_reboot),
            icon = Icons.Rounded.RestartAlt,
            onClick = {
              expanded = false
              onPowerAction(PowerAction.Reboot)
            },
            delay = 0,
        )
      }
    }

    // Main FAB
    FloatingActionButton(
        onClick = { expanded = !expanded },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        shape = CircleShape,
    ) {
      Icon(
          imageVector = Icons.Rounded.PowerSettingsNew,
          contentDescription = "Power Menu",
          modifier = Modifier.rotate(rotation),
      )
    }
  }
}

@Composable
private fun FabMenuItem(text: String, icon: ImageVector, onClick: () -> Unit, delay: Int) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.clickable { onClick() },
  ) {
    // Label
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = CircleShape,
        shadowElevation = 2.dp,
    ) {
      Text(
          text = text,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }

    // Mini FAB
    Box(
        modifier =
            Modifier.size(48.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}
