package id.xms.xtrakernelmanager.ui.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.CompositionLocalProvider
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard


@Composable
fun FrostedDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    backgroundColor: Color? = null
) {
    val isLightTheme = !isSystemInDarkTheme()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphicCard(
                modifier = modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                val contentColor = if (isLightTheme) {
                    Color.Black.copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.95f)
                }

                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp
                            ),
                            color = contentColor,
                            modifier = Modifier.padding(28.dp, 24.dp, 28.dp, 12.dp)
                        )

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 12.dp, 24.dp, 12.dp)
                        ) {
                            content()
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 12.dp, 24.dp, 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dismiss button (if provided)
                            dismissButton?.let {
                                Box(modifier = Modifier.weight(1f)) {
                                    it()
                                }
                            }

                            // Confirm button
                            Box(modifier = Modifier.weight(1f)) {
                                confirmButton()
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FrostedDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties()
) {
    val isLightTheme = !isSystemInDarkTheme()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphicCard(
                modifier = modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                val contentColor = if (isLightTheme) {
                    Color.Black.copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.95f)
                }

                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title
                        Box(
                            modifier = Modifier.padding(28.dp, 24.dp, 28.dp, 12.dp)
                        ) {
                            title()
                        }

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 12.dp, 24.dp, 12.dp)
                        ) {
                            content()
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 12.dp, 24.dp, 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dismiss button (if provided)
                            dismissButton?.let {
                                Box(modifier = Modifier.weight(1f)) {
                                    it()
                                }
                            }

                            // Confirm button
                            Box(modifier = Modifier.weight(1f)) {
                                confirmButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val backgroundColor = if (isPrimary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    }

    val textColor = if (isPrimary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        LocalContentColor.current
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            color = textColor
        )
    }
}
