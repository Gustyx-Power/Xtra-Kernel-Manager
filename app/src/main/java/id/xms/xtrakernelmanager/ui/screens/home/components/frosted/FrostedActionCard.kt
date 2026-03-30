package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
fun FrostedActionCard(
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = true // XKM is always dark mode
    
    val cardBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val cardBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardBackground)
            .border(0.8.dp, cardBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
         Icon(icon, null, tint = color.copy(alpha = 0.8f))
    }
}
