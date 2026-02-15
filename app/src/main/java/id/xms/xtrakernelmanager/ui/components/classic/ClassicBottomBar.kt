package id.xms.xtrakernelmanager.ui.components.classic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.BottomNavItem
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ClassicColors.Surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(64.dp), // Fixed height, standard bottom bar
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val contentColor = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurfaceVariant
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClick = { onNavigate(item.route) },
                            indication = null, // No ripple for cleaner classic look? Or maybe default ripple. Let's keep it simple.
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.label),
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
