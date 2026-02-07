package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

// --- ADAPTIVE COLORS FOR LIQUID THEME ---

/**
 * Returns adaptive text color based on system theme
 * - ACCESSIBILITY UPDATE: Now always returns Black to match the forced White Glassmorphic cards.
 */
@Composable
fun adaptiveTextColor(): Color {
    return Color.Black
}

/**
 * Returns adaptive text color with alpha based on system theme
 * - ACCESSIBILITY UPDATE: Now always returns Black with alpha to match the forced White Glassmorphic cards.
 */
@Composable
fun adaptiveTextColor(alpha: Float): Color {
    return Color.Black.copy(alpha = alpha)
}

/**
 * Returns adaptive surface color based on system theme
 * - ACCESSIBILITY UPDATE: Now always returns Black with alpha (shadow-like) to match the forced White Glassmorphic cards.
 */
@Composable
fun adaptiveSurfaceColor(alpha: Float = 0.1f): Color {
    return Color.Black.copy(alpha = alpha)
}

// --- SHARED CONTAINER ---

@Composable
fun LiquidSharedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = modifier.animateContentSize(),
        onClick = onClick,
        contentPadding = contentPadding,
        content = content
    )
}

// --- SHARED BATTERY COMPONENTS ---

@Composable
fun LiquidBatterySilhouette(level: Float, isCharging: Boolean, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp, 4.dp)
                .background(Color.Gray, MaterialTheme.shapes.extraSmall)
        )
        Box(
            modifier = Modifier
                .size(50.dp, 80.dp)
                .border(4.dp, Color.Gray, MaterialTheme.shapes.medium)
                .padding(4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(level)
                    .background(color, MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
fun LiquidBatteryStatusChip(text: String) {
    Surface(color = adaptiveSurfaceColor(0.1f), shape = MaterialTheme.shapes.small) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = adaptiveTextColor(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun LiquidBatteryStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(adaptiveSurfaceColor(0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveTextColor()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = adaptiveTextColor(0.6f)
            )
        }
    }
}
