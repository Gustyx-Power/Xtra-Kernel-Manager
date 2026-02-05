package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialTempTile(
    modifier: Modifier = Modifier,
    cpuTemp: Int,
    gpuTemp: Int,
    pmicTemp: Int,
    thermalTemp: Int,
    color: Color,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    // Data class for page content
    data class TempPage(
        val label: String,
        val value: Int
    )

    val pages = listOf(
        TempPage(stringResource(id.xms.xtrakernelmanager.R.string.material_temp_cpu), cpuTemp),
        TempPage(stringResource(id.xms.xtrakernelmanager.R.string.material_temp_gpu), gpuTemp),
        TempPage(stringResource(id.xms.xtrakernelmanager.R.string.material_temp_pmic), pmicTemp),
        TempPage(stringResource(id.xms.xtrakernelmanager.R.string.material_temp_thermal), thermalTemp)
    )

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (Restored)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Thermostat,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape,
                ) {
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.material_temp_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Value
                    Text(
                        text = "${item.value}°C",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Label
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Pager Indicator
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val indicatorColor = if (pagerState.currentPage == iteration) color else color.copy(alpha = 0.3f)
                    val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                            .height(6.dp)
                            .width(width)
                    )
                }
            }
        }
    }
}

