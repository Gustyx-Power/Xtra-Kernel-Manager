package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun ExpandableNetworkCard(
    viewModel: TuningViewModel,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClickNav: () -> Unit,
    topRightContent: @Composable () -> Unit = {},
) {
  // Animation
  val height by
      animateDpAsState(targetValue = if (expanded) 420.dp else 120.dp, label = "net_height")

  Box(
      modifier =
          Modifier.fillMaxWidth().height(height).clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
          ) {
            onExpandChange(!expanded)
          }
  ) {
    if (!expanded) {
      // COLLAPSED STATE (Simple Card)
      Card(
          modifier = Modifier.fillMaxSize(),
          shape = RoundedCornerShape(24.dp),
          colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Icon(
              Icons.Rounded.Router,
              null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
          )
          Column {
            Text(
                "Network",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Transmission Control Protocol",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      // EXPANDED STATE (L-SHAPE LAYOUT)

      val cornerRadius = 24.dp
      val gap = 24.dp
      val cutoutHeight = 120.dp // Match DashboardNavCard height
      val themeColor = MaterialTheme.colorScheme.secondaryContainer

      Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) // Placeholder for Left L-Arm

          // TOP RIGHT SLOT
          Box(modifier = Modifier.weight(1f).height(cutoutHeight)) { topRightContent() }
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        // The L-Shape Background
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val colW = w / 2 - gap.toPx() / 2 // Width of left column
          val ch = cutoutHeight.toPx() + gap.toPx()
          val cr = cornerRadius.toPx()
          val ir = cornerRadius.toPx() // Match inverted radius with card corner radius

          val path =
              Path().apply {
                // Start Top-Left
                moveTo(0f, cr)
                quadraticTo(0f, 0f, cr, 0f)

                // Top Edge to Top-Right Corner of Left Leg
                lineTo(colW - cr, 0f)
                quadraticTo(colW, 0f, colW, cr)

                // Going Down the inner vertical edge
                lineTo(colW, ch - ir)

                arcTo(
                    rect =
                        androidx.compose.ui.geometry.Rect(
                            left = colW,
                            top = ch - 2 * ir,
                            right = colW + 2 * ir,
                            bottom = ch,
                        ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false,
                )

                // Top Edge of Bottom-Right Section
                lineTo(w - cr, ch)
                quadraticTo(w, ch, w, ch + cr)

                // Right Edge
                lineTo(w, h - cr)
                quadraticTo(w, h, w - cr, h)

                // Bottom Edge
                lineTo(cr, h)
                quadraticTo(0f, h, 0f, h - cr)

                close()
              }

          drawPath(path, color = themeColor)
        }

        // CONTENT LAYOUT
        // We overlay content on the L-shape
        // CONTENT LAYOUT
        // We overlay content on the L-shape
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), // Unified padding
            verticalArrangement = Arrangement.Top,
        ) {
          // TOP SECTION: Left Content (Header + Status) | Right Spacer (Memory Card)
          Row(modifier = Modifier.fillMaxWidth()) {
            // Left Side Container
            Column(modifier = Modifier.weight(1f)) {
              // Header
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier.size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                  Icon(
                      Icons.Rounded.Router,
                      null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(20.dp),
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Network",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Status Badge
              val networkStatus by viewModel.networkStatus.collectAsState()
              Surface(
                  shape = RoundedCornerShape(50),
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), // Glassy feel
                  modifier = Modifier.wrapContentSize(),
              ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                  // Green Status Dot (Visual Indicator)
                  Box(
                      modifier =
                          Modifier.size(6.dp)
                              .clip(CircleShape)
                              .background(
                                  androidx.compose.ui.graphics.Color(0xFF4CAF50)
                              ) // Material Green
                  )

                  Spacer(modifier = Modifier.width(8.dp))

                  Text(
                      networkStatus,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                      fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }

            // Right Side Gap
            Spacer(modifier = Modifier.width(20.dp))

            // Matches Memory Card Height + visual alignment
            Spacer(modifier = Modifier.weight(1f).height(cutoutHeight))
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Hostname UI (Horizontal Strip) - Full Width Row 2
          val currentHostname by viewModel.currentHostname.collectAsState()
          var editHostnameDialog by remember { mutableStateOf(false) }

          Surface(
              onClick = { editHostnameDialog = true }, // Make whole card clickable
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceContainer, // Darker contrast
              modifier = Modifier.fillMaxWidth().height(60.dp), // Fixed height strip
          ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Rounded.Smartphone,
                    null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Hostname",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    currentHostname.ifEmpty { "android" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
              }

              Icon(
                  Icons.Rounded.Edit,
                  null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          // Dialog
          if (editHostnameDialog) {
            var input by remember { mutableStateOf(currentHostname) }
            AlertDialog(
                onDismissRequest = { editHostnameDialog = false },
                title = { Text("Set Hostname") },
                text = {
                  OutlinedTextField(
                      value = input,
                      onValueChange = { input = it },
                      label = { Text("Hostname") },
                      singleLine = true,
                      shape = RoundedCornerShape(12.dp),
                  )
                },
                confirmButton = {
                  TextButton(
                      onClick = {
                        viewModel.setHostname(input)
                        editHostnameDialog = false
                      }
                  ) {
                    Text("Save")
                  }
                },
                dismissButton = {
                  TextButton(onClick = { editHostnameDialog = false }) { Text("Cancel") }
                },
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // BOTTOM SECTION: TCP + Private DNS (Row spanning full width)
          val currentTCP by viewModel.currentTCPCongestion.collectAsState()
          val availableTCP by viewModel.availableTCPCongestion.collectAsState()
          var expandedDropdown by remember { mutableStateOf(false) }
          var tcpBoxWidth by remember { mutableStateOf(0) }

          val currentDNS by viewModel.currentDNS.collectAsState()
          val availableDNS = viewModel.availableDNS
          var expandedDNSDropdown by remember { mutableStateOf(false) }
          var dnsBoxWidth by remember { mutableStateOf(0) }

          Row(
              modifier = Modifier.fillMaxWidth().weight(1f), // Take remaining height
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // TCP Congestion UI
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp),
                  verticalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      Icons.Rounded.Wifi,
                      null,
                      modifier = Modifier.size(20.dp),
                      tint = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      "TCP",
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                  )
                }

                Box(
                    modifier =
                        Modifier.fillMaxWidth().onGloballyPositioned { tcpBoxWidth = it.size.width }
                ) {
                  OutlinedButton(
                      onClick = { expandedDropdown = true },
                      modifier = Modifier.fillMaxWidth().height(48.dp),
                      shape = RoundedCornerShape(12.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp),
                      colors =
                          ButtonDefaults.outlinedButtonColors(
                              containerColor = MaterialTheme.colorScheme.surfaceContainer,
                              contentColor = MaterialTheme.colorScheme.onSurface,
                          ),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                  ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                          text = currentTCP.ifEmpty { "?" },
                          style = MaterialTheme.typography.bodyMedium,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.weight(1f),
                      )
                      Icon(
                          if (expandedDropdown) Icons.Rounded.KeyboardArrowUp
                          else Icons.Rounded.KeyboardArrowDown,
                          null,
                          modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  DropdownMenu(
                      expanded = expandedDropdown,
                      onDismissRequest = { expandedDropdown = false },
                      modifier = Modifier.width(with(LocalDensity.current) { tcpBoxWidth.toDp() }),
                  ) {
                    availableTCP.forEach { tcp ->
                      DropdownMenuItem(
                          text = { Text(tcp) },
                          onClick = {
                            viewModel.setTCPCongestion(tcp)
                            expandedDropdown = false
                          },
                      )
                    }
                  }
                }
              }
            }

            // Private DNS UI
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp),
                  verticalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      Icons.Rounded.Security,
                      null,
                      modifier = Modifier.size(18.dp),
                      tint = MaterialTheme.colorScheme.secondary,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      "DNS",
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.secondary,
                      maxLines = 1,
                  )
                }

                Box(
                    modifier =
                        Modifier.fillMaxWidth().onGloballyPositioned { dnsBoxWidth = it.size.width }
                ) {
                  OutlinedButton(
                      onClick = { expandedDNSDropdown = true },
                      modifier = Modifier.fillMaxWidth().height(48.dp),
                      shape = RoundedCornerShape(12.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp),
                      colors =
                          ButtonDefaults.outlinedButtonColors(
                              containerColor = MaterialTheme.colorScheme.surfaceContainer,
                              contentColor = MaterialTheme.colorScheme.onSurface,
                          ),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                  ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                          text = currentDNS.ifEmpty { "Auto" },
                          style = MaterialTheme.typography.bodyMedium,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.weight(1f),
                      )
                      Icon(
                          if (expandedDNSDropdown) Icons.Rounded.KeyboardArrowUp
                          else Icons.Rounded.KeyboardArrowDown,
                          null,
                          modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  DropdownMenu(
                      expanded = expandedDNSDropdown,
                      onDismissRequest = { expandedDNSDropdown = false },
                      modifier = Modifier.width(with(LocalDensity.current) { dnsBoxWidth.toDp() }),
                  ) {
                    availableDNS.forEach { (name, hostname) ->
                      DropdownMenuItem(
                          text = { Text(name) },
                          onClick = {
                            viewModel.setPrivateDNS(name, hostname)
                            expandedDNSDropdown = false
                          },
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
