package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import id.xms.xtrakernelmanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySizeScreen(
    onNavigateBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get current smallest width in dp
    val currentSmallestWidth = remember {
        val config = context.resources.configuration
        config.smallestScreenWidthDp
    }
    
    val currentDPI = remember {
        context.resources.displayMetrics.densityDpi
    }
    
    var selectedWidth by remember { mutableStateOf(currentSmallestWidth) }
    var customWidth by remember { mutableStateOf(currentSmallestWidth.toString()) }
    var isApplying by remember { mutableStateOf(false) }
    var useCustom by remember { mutableStateOf(false) }
    
    // Preset smallest width values (in dp)
    val presetWidths = listOf(
        320 to "320 dp (Large UI)",
        360 to "360 dp (Default)",
        411 to "411 dp (Compact)",
        480 to "480 dp (More Compact)",
        540 to "540 dp (Very Compact)",
        600 to "600 dp (Tablet-like)"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Display Size",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Column {
                            Text(
                                text = "Current: $currentSmallestWidth dp ($currentDPI DPI)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Higher value = Smaller UI (More content)\nLower value = Larger UI (Easier to tap)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            item {
                 Text(
                    text = "Select Density",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .padding(vertical = 8.dp),
                    ) {
                        presetWidths.forEach { (width, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = !useCustom && selectedWidth == width,
                                        onClick = { useCustom = false; selectedWidth = width },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = !useCustom && selectedWidth == width, onClick = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = useCustom,
                                    onClick = { useCustom = true },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = useCustom, onClick = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { customWidth = it.filter { char -> char.isDigit() }; useCustom = true },
                                label = { Text("Custom width (dp)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            isApplying = true
                            try {
                                val targetWidth = if (useCustom) customWidth.toIntOrNull() ?: selectedWidth else selectedWidth
                                
                                if (targetWidth < 200 || targetWidth > 900) {
                                    android.widget.Toast.makeText(context, "Please enter a valid width (200-900)", android.widget.Toast.LENGTH_SHORT).show()
                                    isApplying = false
                                    return@launch
                                }

                                withContext(Dispatchers.IO) {
                                    val displayMetrics = context.resources.displayMetrics
                                    val smallestPixels = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                    val newDensity = (smallestPixels.toFloat() / targetWidth.toFloat() * 160).toInt()
                                    id.xms.xtrakernelmanager.utils.RootShell.execute("wm density $newDensity")
                                }
                                android.widget.Toast.makeText(context, "Applied! Screen may flicker.", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            } finally {
                                isApplying = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isApplying
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Applying...")
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Changes")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    id.xms.xtrakernelmanager.utils.RootShell.execute("wm density reset")
                                }
                                android.widget.Toast.makeText(context, "Reset to default!", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Reset to Default")
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
