package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialGameSpaceScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
    onAddGames: () -> Unit,
) {
  val gameApps by viewModel.gameApps.collectAsState()
  val appCount = try { JSONArray(gameApps).length() } catch (e: Exception) { 0 }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        LargeTopAppBar(
            title = {
                Text(
                    "Game Space",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
              }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            ),
        )
      },
  ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

      item { Spacer(modifier = Modifier.height(8.dp)) }

      // 1. Game Library Section
      item {
          GameSpaceSection(
              title = "Game Library",
              content = {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Column {
                          Text(
                              text = "$appCount Games",
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.Bold,
                              color = Color.White
                          )
                          Text(
                              text = "Registered in Game Space",
                              style = MaterialTheme.typography.bodySmall,
                              color = Color.Gray
                          )
                      }
                      
                      Button(
                          onClick = onAddGames,
                          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                      ) {
                          Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Text("Add")
                      }
                  }
              }
          )
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }

      // 2. Notifications Section
      item {
        GameSpaceSection(
            title = "Notifications",
            content = {
                GameSpaceSwitchRow(
                    title = "Call overlay",
                    subtitle = "Show minimal call overlay to answer/reject calls",
                    icon = Icons.Rounded.Call,
                    checked = viewModel.callOverlay.collectAsState().value,
                    onCheckedChange = { viewModel.setCallOverlay(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                GameSpaceSwitchRow(
                    title = "Danmaku Notification mode",
                    subtitle = "Show notification as danmaku aka bullet comments while game is active",
                    icon = Icons.Rounded.Comment,
                    checked = viewModel.danmakuMode.collectAsState().value,
                    onCheckedChange = { viewModel.setDanmakuMode(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                val callAction = viewModel.inGameCallAction.collectAsState().value
                GameSpaceExpandableRow(
                    title = "In-game call",
                    subtitle = when(callAction) {
                        "no_action" -> "No action"
                        "answer" -> "Auto Answer"
                        "reject" -> "Auto Reject"
                        else -> "No action"
                    },
                    icon = Icons.Rounded.PhoneInTalk,
                    options = listOf(
                        "no_action" to "No action",
                        "answer" to "Auto Answer",
                        "reject" to "Auto Reject"
                    ),
                    selectedOption = callAction,
                    onOptionSelected = { viewModel.setInGameCallAction(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val ringerMode = viewModel.inGameRingerMode.collectAsState().value
                GameSpaceExpandableRow(
                    title = "In-game ringer mode",
                    subtitle = when(ringerMode) {
                        "no_change" -> "Do not change"
                        "silent" -> "Silent"
                        "vibrate" -> "Vibrate"
                        else -> "Do not change"
                    },
                    icon = Icons.Rounded.VolumeUp,
                    options = listOf(
                        "no_change" to "Do not change",
                        "silent" to "Silent",
                        "vibrate" to "Vibrate"
                    ),
                    selectedOption = ringerMode,
                    onOptionSelected = { viewModel.setInGameRingerMode(it) }
                )
            }
        )
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }

      // 3. Display & Gestures Section
      item {
        GameSpaceSection(
            title = "Display & Gestures",
            content = {
                GameSpaceSwitchRow(
                    title = "Disable auto-brightness",
                    subtitle = "Keep brightness settled while in-game",
                    icon = Icons.Rounded.BrightnessAuto,
                    checked = viewModel.disableAutoBrightness.collectAsState().value,
                    onCheckedChange = { viewModel.setDisableAutoBrightness(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                GameSpaceSwitchRow(
                    title = "Disable three fingers swipe gesture",
                    subtitle = "Temporary disable three fingers swipe gesture while in-game",
                    icon = Icons.Rounded.Gesture,
                    checked = viewModel.disableThreeFingerSwipe.collectAsState().value,
                    onCheckedChange = { viewModel.setDisableThreeFingerSwipe(it) }
                )
            }
        )
      }

      item { Spacer(modifier = Modifier.height(32.dp)) }
    }
  }
}

@Composable
fun GameSpaceSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun GameSpaceSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF2D2E36)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.LightGray,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = Color.Gray
            )
        )
    }
}

@Composable
fun GameSpaceExpandableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<Pair<String, String>>, // key -> label
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D2E36)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            

        }
        
        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2D2E36),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { (key, label) ->
                        val isSelected = (key == selectedOption)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) 
                                    else Color.Transparent
                                )
                                .clickable { 
                                    onOptionSelected(key)
                                    expanded = false 
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
