package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val isDark = isSystemInDarkTheme()

    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    val borderColor = MaterialTheme.colorScheme.onSurface.copy(
        alpha = if (isDark) 0.35f else 0.15f
    )

    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = 14.dp,
            shape = shape,
            clip = false
        )

    val elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = baseModifier,
            shape = shape,
            colors = cardColors,
            enabled = enabled,
            elevation = elevation,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = baseModifier,
            shape = shape,
            colors = cardColors,
            elevation = elevation,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}


@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        content = content
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}