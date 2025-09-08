import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.ui.components.InfoCard
import id.xms.xtrakernelmanager.ui.components.SystemCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import id.xms.xtrakernelmanager.viewmodel.InfoViewModel
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(vm: InfoViewModel = hiltViewModel()) {
    val kernel by vm.kernel.collectAsState()
    val system by vm.system.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val systemUiController = rememberSystemUiController()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorAtElevation = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val topBarContainerColor by remember {
        derivedStateOf {
            lerp(
                surfaceColor,
                surfaceColorAtElevation,
                scrollBehavior.state.overlappedFraction
            )
        }
    }
    val darkTheme = isSystemInDarkTheme()

    LaunchedEffect(topBarContainerColor, darkTheme) {
        systemUiController.setStatusBarColor(
            color = topBarContainerColor,
            darkIcons = !darkTheme
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Information",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            kernel?.let { item { InfoCard(it, blur = false) } }
            system?.let { item { SystemCard(it) } }
        }
    }
}