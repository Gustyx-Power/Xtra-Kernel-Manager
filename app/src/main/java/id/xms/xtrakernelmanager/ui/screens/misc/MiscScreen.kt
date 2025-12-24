package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.ui.screens.misc.section.BatteryInfoSection
import id.xms.xtrakernelmanager.ui.screens.misc.section.DisplaySection
import id.xms.xtrakernelmanager.ui.screens.misc.section.GameControlSection

@Composable
fun MiscScreen(
    viewModel: MiscViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PillCard(text = stringResource(R.string.misc_title))
        }

        item {
            BatteryInfoSection(viewModel)
        }

        item {
            GameControlSection(viewModel)
        }

        item {
            DisplaySection(viewModel)
        }
    }
}
