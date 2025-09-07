package id.xms.xtrakernelmanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(title: String, onBack: (() -> Unit)? = null) {
    if (onBack != null) {
        TopAppBar(title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
                }
            })
    } else {
        CenterAlignedTopAppBar(title = { Text(title, style = MaterialTheme.typography.headlineSmall) })
    }
}