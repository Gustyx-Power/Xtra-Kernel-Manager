package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicPowerMenu(onPowerAction: (PowerAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
            text = "Power Menu",
            style = MaterialTheme.typography.titleMedium,
            color = ClassicColors.Secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val actions = PowerAction.values().toList().chunked(2)
            actions.forEach { rowActions ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowActions.forEach { action ->
                        Button(
                            onClick = { onPowerAction(action) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClassicColors.Surface,
                                contentColor = ClassicColors.OnSurface
                            )
                        ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = ClassicColors.Secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(action.getLocalizedLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                    if (rowActions.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
