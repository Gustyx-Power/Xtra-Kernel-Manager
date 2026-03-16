package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
    
    val iconBackground = if (isDarkTheme) {
        Color(0xFF1E3A8A).copy(alpha = 0.6f)
    } else {
        Color(0xFF3B82F6).copy(alpha = 0.6f)
    }
    
    val settingsBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }
    
    val badgeBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("FrostedHeader"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = id.xms.xtrakernelmanager.R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textColor
                )
                
                if (showVersionBadge) {
                    Surface(
                        color = badgeBackground,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Surface(
            color = settingsBackground,
            shape = CircleShape,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onSettingsClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(id.xms.xtrakernelmanager.R.string.frosted_header_settings),
                    modifier = Modifier.size(20.dp),
                    tint = textColor
                )
            }
        }
    }
}
