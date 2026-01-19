package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCurrentSessionScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } 
    
    val cardColor = Color(0xFF1E1F24) 
    val surfaceColor = Color(0xFF121212)

    Scaffold(
        containerColor = surfaceColor, // Dark background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Sedang berlangsung", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
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
            // 1. Segmented Control
            item {
                SegmentedControl(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    activeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    inactiveColor = cardColor
                )
            }

            // 2. Total Time Header
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Total waktu",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "1j 18m 37d", // Mock
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 3. Main Stats Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatsCard(
                            label = if (selectedTab == 0) "Terisi" else "Dikosongkan",
                            mainValue = "16%",
                            subValue = "715 mAh", 
                             modifier = Modifier.weight(1f),
                             containerColor = cardColor
                        )
                        StatsCard(
                            label = if (selectedTab == 0) "Tingkat pengisian" else "Tingkat pengosongan",
                            mainValue = if (selectedTab == 0) "12.2%/j" else "15.0%/j",
                            subValue = if (selectedTab == 0) "~546 mA" else "~681 mA",
                            modifier = Modifier.weight(1f),
                            containerColor = cardColor
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                         StatsCard(
                            label = "Waktu layar aktif",
                            mainValue = "1j 2m 47d",
                            subValue = null, 
                             modifier = Modifier.weight(1f),
                             containerColor = cardColor
                        )
                         StatsCard(
                            label = "Waktu layar mati",
                            mainValue = "15m 59d",
                            subValue = null, 
                             modifier = Modifier.weight(1f),
                             containerColor = cardColor
                        )
                    }
                }
            }
            
            // 4. Usage Cards
             item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatsCard(
                        label = "Digunakan",
                        mainValue = "4.0%",
                        subValue = "181 mAh",
                        modifier = Modifier.weight(1f),
                        containerColor = cardColor
                    )
                     StatsCard(
                        label = "Digunakan (Laju)",
                        mainValue = "15.0%/j",
                        subValue = "~681 mA",
                        modifier = Modifier.weight(1f),
                        containerColor = cardColor
                    )
                }
            }

            // 5. Additional Info Header
            item {
                Text(
                    text = "Tambahan",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 6. Additional Info Grid
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     DetailedInfoCard(
                         label = "Perkiraan kapasitas",
                         value = "4471 mAh",
                         modifier = Modifier.weight(1f),
                         containerColor = cardColor
                     )
                      DetailedInfoCard(
                         label = "Suhu Maks",
                         value = "38.2°C",
                         modifier = Modifier.weight(1f),
                         containerColor = cardColor
                     )
                }
            }
            
             item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     DetailedInfoCard(
                         label = "Siklus yang digunakan",
                         value = "0.08 siklus",
                         modifier = Modifier.weight(1f),
                         containerColor = cardColor
                     )
                     // Placeholder for 4th item or spacer
                     Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            item {
                 Text(
                    text = "Info pengisi daya",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
             item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     DetailedInfoCard(
                         label = "Jenis pengisi daya",
                         value = "USB",
                         modifier = Modifier.weight(1f),
                         containerColor = cardColor
                     )
                      DetailedInfoCard(
                         label = "Daya pengisian Max",
                         value = "6.4 W",
                         modifier = Modifier.weight(1f),
                         containerColor = cardColor
                     )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SegmentedControl(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeColor: Color,
    inactiveColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Taller for easy tap
    ) {
        // Tab 1
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selectedTab == 0) activeColor else inactiveColor)
                .clickable { onTabSelected(0) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mengisi daya",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        // Tab 2
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selectedTab == 1) activeColor else inactiveColor)
                .clickable { onTabSelected(1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pengosongan",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = if (selectedTab == 1) 1f else 0.7f)
            )
        }
    }
}

@Composable
fun StatsCard(
    label: String,
    mainValue: String,
    subValue: String?,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp), // Expressive corners
        modifier = modifier.aspectRatio(1.2f) // Slightly rectangular/squareish
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Column {
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp), // Big readable number
                    color = Color.White,
                     fontWeight = FontWeight.Normal
                )
                if (subValue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
     Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
             verticalArrangement = Arrangement.Top
        ) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1
                )
                 Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = value,
                 style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                 fontWeight = FontWeight.Normal
            )
        }
     }
}
