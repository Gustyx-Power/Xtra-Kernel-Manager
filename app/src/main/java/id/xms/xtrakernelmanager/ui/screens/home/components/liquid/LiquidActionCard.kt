package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun LiquidActionCard(
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .border(1.dp, backgroundColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
         Icon(icon, null, tint = color.copy(alpha = 0.8f))
    }
}
