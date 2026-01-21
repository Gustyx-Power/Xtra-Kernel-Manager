package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun DashboardProfileCard(viewModel: TuningViewModel) {
  var expanded by remember { mutableStateOf(false) }
  val selectedProfile by viewModel.selectedProfile.collectAsState()
  val profiles = viewModel.availableProfiles

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
      shape = RoundedCornerShape(32.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
  ) {
    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        // Icon Bubble
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
        ) {
          Box(contentAlignment = Alignment.Center) {
            val icon =
                when (selectedProfile) {
                  "Performance" -> Icons.Rounded.Bolt
                  "Powersave" -> Icons.Rounded.Eco
                  "Battery" -> Icons.Rounded.BatteryFull
                  else -> Icons.Rounded.Speed // Balance
                }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = selectedProfile,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
          Text(
              text = "Active Profile",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
          )
        }
      }

      // Expanded List
      AnimatedVisibility(visible = expanded) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f),
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            profiles.forEach { profile ->
              val isSelected = profile == selectedProfile
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .clip(RoundedCornerShape(14.dp))
                          .clickable {
                            viewModel.applyGlobalProfile(profile)
                            expanded = false
                          }
                          .background(
                              if (isSelected)
                                  MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)
                              else Color.Transparent
                          )
                          .padding(vertical = 12.dp, horizontal = 16.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                    text = profile,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(
                            alpha = if (isSelected) 1f else 0.8f
                        ),
                )
              }
            }
          }
        }
      }
    }
  }
}
