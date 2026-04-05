package id.xms.xtrakernelmanager.ui.screens.donation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R

/**
 * Classic style donation screen - Nebula Core theme
 * "The Luminous Monolith" - High-end editorial dark mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicDonationScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Nebula Core Colors
    val background = Color(0xFF080f11)
    val surfaceContainer = Color(0xFF101b1e)
    val surfaceContainerLowest = Color(0xFF000000)
    val surfaceContainerHighest = Color(0xFF19282c)
    val primaryAccent = Color(0xFFadf4ff)
    val surfaceVariant = Color(0xFF3f484a)
    
    // Luminous glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "luminousGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    TopAppBar(
                        title = { 
                            Text(
                                stringResource(R.string.donation_support_button),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, 
                                    stringResource(R.string.back),
                                    tint = primaryAccent
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp), // Spacing 6 (2rem)
                verticalArrangement = Arrangement.spacedBy(32.dp) // Spacing 10 for white space
            ) {
                // Header Card - Luminous monolith
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Ambient shadow with teal tint
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryAccent.copy(alpha = glowAlpha * 0.2f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.8f
                                )
                            )
                        }
                        .background(
                            color = surfaceContainer,
                            shape = RoundedCornerShape(24.dp) // md corner radius
                        )
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                // Luminous glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryAccent.copy(alpha = glowAlpha * 0.6f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.width * 0.9f
                                )
                            }
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        primaryAccent.copy(alpha = 0.3f),
                                        primaryAccent.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = primaryAccent
                        )
                    }
                    
                    // Editorial hierarchy - headline-sm
                    Text(
                        text = stringResource(R.string.donation_screen_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    
                    // Muted description - body-lg
                    Text(
                        text = stringResource(R.string.donation_screen_description),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
                    )
                }

                // Section header - Editorial asymmetry
                Text(
                    text = stringResource(R.string.donation_choose_platform),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryAccent
                )

                // Platform cards - No dividers, tonal shifts
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PayPal
                    NebulaCorePlatformCard(
                        icon = Icons.Default.Payment,
                        title = stringResource(R.string.donation_paypal),
                        description = stringResource(R.string.donation_paypal_desc),
                        accentColor = Color(0xFF0070BA),
                        surfaceContainer = surfaceContainer,
                        surfaceContainerHighest = surfaceContainerHighest,
                        primaryAccent = primaryAccent,
                        glowAlpha = glowAlpha,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/yourpaypal"))
                            context.startActivity(intent)
                        }
                    )

                    // Buy Me a Coffee
                    NebulaCorePlatformCard(
                        icon = Icons.Default.LocalCafe,
                        title = stringResource(R.string.donation_buymeacoffee),
                        description = stringResource(R.string.donation_buymeacoffee_desc),
                        accentColor = Color(0xFFFFDD00),
                        surfaceContainer = surfaceContainer,
                        surfaceContainerHighest = surfaceContainerHighest,
                        primaryAccent = primaryAccent,
                        glowAlpha = glowAlpha,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/yourusername"))
                            context.startActivity(intent)
                        }
                    )

                    // GitHub Sponsors
                    NebulaCorePlatformCard(
                        icon = Icons.Default.Code,
                        title = stringResource(R.string.donation_github),
                        description = stringResource(R.string.donation_github_desc),
                        accentColor = Color(0xFF6E40C9),
                        surfaceContainer = surfaceContainer,
                        surfaceContainerHighest = surfaceContainerHighest,
                        primaryAccent = primaryAccent,
                        glowAlpha = glowAlpha,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sponsors/yourusername"))
                            context.startActivity(intent)
                        }
                    )

                    // Ko-Fi
                    NebulaCorePlatformCard(
                        icon = Icons.Default.LocalCafe,
                        title = stringResource(R.string.donation_kofi),
                        description = stringResource(R.string.donation_kofi_desc),
                        accentColor = Color(0xFFFF5E5B),
                        surfaceContainer = surfaceContainer,
                        surfaceContainerHighest = surfaceContainerHighest,
                        primaryAccent = primaryAccent,
                        glowAlpha = glowAlpha,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/yourusername"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NebulaCorePlatformCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    surfaceContainer: Color,
    surfaceContainerHighest: Color,
    primaryAccent: Color,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                // Surface shift on hover
                color = if (isHovered) surfaceContainerHighest else surfaceContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .drawBehind {
                    // Subtle glow for icon
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.8f
                    )
                }
                .background(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = primaryAccent.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
    }
}
