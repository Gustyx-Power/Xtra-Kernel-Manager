package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R

data class Developer(val name: String, val role: String, val githubUsername: String, val drawableResId: Int)

val developers = listOf(
    Developer("Gustyx-Power", "Founder & Developer", "Gustyx-Power", R.drawable.gustyx_power),
    Developer("Pavelc4", "Ui Supports", "pavelc4", R.drawable.pavelc4),
    Developer("Ziyu", "Tuning Supports", "Ziyu", R.drawable.ziyu),
    Developer("Viasco", "MD3 UI Migration", "bimoalfarrabi", R.drawable.viasco)
)

@Composable
fun AboutCard(
    blur: Boolean,
    modifier: Modifier = Modifier,
    githubLink: String = stringResource(R.string.github_link),
    telegramLink: String = stringResource(R.string.telegram_link),
) {
    var showCreditsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.about),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Description
            Text(
                text = stringResource(id = R.string.desc_about),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Action Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Social Links Row
                val uriHandler = LocalUriHandler.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Follow us:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    // Telegram Button
                    IconButton(
                        onClick = { uriHandler.openUri(telegramLink) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = stringResource(id = R.string.telegram),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // GitHub Button
                    IconButton(
                        onClick = { uriHandler.openUri(githubLink) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = stringResource(id = R.string.github),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Credits Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { showCreditsDialog = true },
                        label = {
                            Text(
                                text = stringResource(id = R.string.credits),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showCreditsDialog) {
        AnimatedVisibility(
            visible = showCreditsDialog,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        ) {
            AlertDialog(
                onDismissRequest = { showCreditsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(id = R.string.credits))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        developers.forEach { developer ->
                            DeveloperCreditItem(developer = developer)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showCreditsDialog = false }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
fun DeveloperCreditItem(developer: Developer) {
    val uriHandler = LocalUriHandler.current
    val githubProfileUrl = "https://github.com/${developer.githubUsername}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(githubProfileUrl) },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = developer.drawableResId),
                contentDescription = "${developer.name}'s profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = developer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = developer.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "@${developer.githubUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
