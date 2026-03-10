package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.BuildConfig

@Composable
fun FrostedHeader(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(id.xms.xtrakernelmanager.R.string.frosted_header_title),
    showVersionBadge: Boolean = true
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    val badgeBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    val badgeBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("FrostedHeader"),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        onClick = onSettingsClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
                
                if (showVersionBadge) {
                    Surface(
                        color = badgeBackground,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = textColor
                        )
                    }
                }
            }

            Surface(
                color = badgeBackground,
                shape = CircleShape,
                modifier = Modifier.size(32.dp).clickable(onClick = onSettingsClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id.xms.xtrakernelmanager.R.string.frosted_header_settings),
                        modifier = Modifier.size(18.dp),
                        tint = textColor
                    )
                }
            }
        }
    }
}
