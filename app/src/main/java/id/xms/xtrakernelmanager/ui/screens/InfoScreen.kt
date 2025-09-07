import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.ui.components.InfoCard
import id.xms.xtrakernelmanager.ui.components.SystemCard
import id.xms.xtrakernelmanager.viewmodel.InfoViewModel

@Composable
fun InfoScreen(vm: InfoViewModel = hiltViewModel()) {
    val kernel by vm.kernel.collectAsState()
    val system by vm.system.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        kernel?.let { item { InfoCard(it, blur = true ) } }
        system?.let { item { SystemCard(it, blur = true ) } }
    }
}