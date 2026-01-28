package id.xms.xtrakernelmanager.ui.screens.misc.material

import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import id.xms.xtrakernelmanager.data.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialGameAppSelectorScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val gameAppsJson by viewModel.gameApps.collectAsState()
  val installedApps by viewModel.installedApps.collectAsState()
  val isLoading by viewModel.isLoadingApps.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var filterMode by remember { mutableStateOf("All") } // All, Added, Not Added
  var showFilterMenu by remember { mutableStateOf(false) }

  // Load apps on enter
  LaunchedEffect(Unit) {
    if (installedApps.isEmpty()) {
      viewModel.loadInstalledApps()
    }
  }

  val filteredApps =
      remember(installedApps, searchQuery, gameAppsJson, filterMode) {
        val sortedApps =
            if (searchQuery.isBlank()) {
              installedApps
            } else {
              installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
              }
            }

        val filteredByMode =
            when (filterMode) {
              "Added" -> sortedApps.filter { viewModel.isGameApp(it.packageName) }
              "Not Added" -> sortedApps.filter { !viewModel.isGameApp(it.packageName) }
              else -> sortedApps
            }

        filteredByMode.sortedWith(
            compareByDescending<AppInfo> { viewModel.isGameApp(it.packageName) }.thenBy { it.label }
        )
      }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
            title = { Text("Add Games", fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            navigationIcon = {
              IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
      },
  ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      // Search Bar & Filter
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search apps...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                ),
        )

        Box {
          FilledTonalIconButton(
              onClick = { showFilterMenu = true },
              colors =
                  IconButtonDefaults.filledTonalIconButtonColors(
                      containerColor =
                          if (filterMode != "All") MaterialTheme.colorScheme.primaryContainer
                          else MaterialTheme.colorScheme.surfaceContainerHigh
                  ),
          ) {
            Icon(
                Icons.Rounded.FilterList,
                contentDescription = "Filter",
                tint =
                    if (filterMode != "All") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            )
          }

          DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                  filterMode = "All"
                  showFilterMenu = false
                },
                leadingIcon = { if (filterMode == "All") Icon(Icons.Rounded.Check, null) },
            )
            DropdownMenuItem(
                text = { Text("Added") },
                onClick = {
                  filterMode = "Added"
                  showFilterMenu = false
                },
                leadingIcon = { if (filterMode == "Added") Icon(Icons.Rounded.Check, null) },
            )
            DropdownMenuItem(
                text = { Text("Not Added") },
                onClick = {
                  filterMode = "Not Added"
                  showFilterMenu = false
                },
                leadingIcon = { if (filterMode == "Not Added") Icon(Icons.Rounded.Check, null) },
            )
          }
        }
      }

      if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(filteredApps, key = { it.packageName }) { app: AppInfo ->
            val isGame = viewModel.isGameApp(app.packageName)

            GameAppItem(
                app = app,
                isAdded = isGame,
                onToggle = { viewModel.toggleGameApp(app.packageName) },
            )
          }

          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }
}

@Composable
fun GameAppItem(app: AppInfo, isAdded: Boolean, onToggle: () -> Unit) {
  Card(
      modifier = Modifier.fillMaxWidth().clickable { onToggle() },
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isAdded) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                  }
          ),
      border =
          if (isAdded) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
          } else null,
  ) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // App icon
      if (app.icon != null) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.large),
        )
      } else {
        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Rounded.Android, null)
        }
      }

      // App info
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = app.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }

      // Checkbox/Switch
      Switch(checked = isAdded, onCheckedChange = { onToggle() })
    }
  }
}
