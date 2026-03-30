package id.xms.xtrakernelmanager.ui.components.classic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        color = ClassicColors.Surface.copy(alpha = 0.6f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                if (isSelected) {
                    // Selected item with pill background
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                        color = ClassicColors.Primary,
                        modifier = Modifier.clickable(
                            onClick = { onNavigate(item.route) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.label),
                                tint = ClassicColors.OnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(item.label).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                } else {
                    // Unselected item
                    Column(
                        modifier = Modifier
                            .clickable(
                                onClick = { onNavigate(item.route) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.label),
                            tint = ClassicColors.OnSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(item.label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}
