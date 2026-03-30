package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicSettingsContent(
    preferencesManager: PreferencesManager,
    currentLayout: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isAndroid10Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ClassicColors.Surface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onNavigateBack,
                        color = ClassicColors.SurfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ClassicColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ClassicColors.Primary
                    )
                }
            }
        },
        containerColor = ClassicColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Section Header
            AnimatedComponent(visible = isVisible, delayMillis = 100) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_appearance_uppercase),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = ClassicColors.Primary.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = stringResource(R.string.settings_interface_style),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_interface_style_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClassicColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Theme Options
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                AnimatedComponent(visible = isVisible, delayMillis = 200) {
                    ClassicThemeCard(
                        title = stringResource(R.string.settings_layout_material),
                        subtitle = stringResource(R.string.settings_layout_material_desc),
                        isSelected = currentLayout == "material",
                        isEnabled = isAndroid10Plus,
                        previewContent = { MaterialPreview() },
                        onClick = {
                            if (isAndroid10Plus) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    preferencesManager.setLayoutStyle("material")
                                    onNavigateBack()
                                }
                            }
                        }
                    )
                }

                AnimatedComponent(visible = isVisible, delayMillis = 300) {
                    ClassicThemeCard(
                        title = stringResource(R.string.settings_layout_frosted),
                        subtitle = stringResource(R.string.settings_layout_frosted_desc),
                        isSelected = currentLayout == "liquid",
                        isEnabled = isAndroid10Plus,
                        previewContent = { FrostedPreview() },
                        onClick = {
                            if (isAndroid10Plus) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    preferencesManager.setLayoutStyle("liquid")
                                    onNavigateBack()
                                }
                            }
                        }
                    )
                }

                AnimatedComponent(visible = isVisible, delayMillis = 400) {
                    ClassicThemeCard(
                        title = stringResource(R.string.settings_layout_classic),
                        subtitle = stringResource(R.string.settings_layout_classic_desc),
                        isSelected = currentLayout == "classic",
                        isEnabled = true,
                        previewContent = { ClassicPreview() },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                preferencesManager.setLayoutStyle("classic")
                                onNavigateBack()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ClassicThemeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    previewContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = ClassicColors.Primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .graphicsLayer {
                            shadowElevation = 30f
                        }
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            )
            .clickable(enabled = isEnabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) 
            ClassicColors.SurfaceContainerHighest 
        else 
            ClassicColors.SurfaceContainer,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isSelected) 
                            ClassicColors.Primary 
                        else 
                            ClassicColors.OnSurface,
                        fontSize = 20.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) 
                            ClassicColors.Primary.copy(alpha = 0.7f) 
                        else 
                            ClassicColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                // Radio Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isSelected) 
                                ClassicColors.Primary 
                            else 
                                Color.Transparent,
                            CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) 
                                ClassicColors.Primary 
                            else 
                                ClassicColors.Outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Visual Preview
            previewContent()
        }
    }
}

@Composable
private fun MaterialPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ClassicColors.SurfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ClassicColors.Primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .padding(16.dp)
                .width(48.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ClassicColors.Primary.copy(alpha = 0.4f))
        )
        
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 32.dp)
                .width(96.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ClassicColors.OutlineVariant)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ClassicColors.Primary)
        )
    }
}

@Composable
private fun FrostedPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ClassicColors.SurfaceContainerLowest)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4A9B8E).copy(alpha = 0.4f),
                            Color(0xFF8BA8D8).copy(alpha = 0.3f),
                            Color(0xFF6BC4E8).copy(alpha = 0.35f)
                        )
                    )
                )
        )
        
        // Glassmorphic element
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(200.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@Composable
private fun ClassicPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ClassicColors.SurfaceContainerLowest)
            .border(
                width = 1.dp,
                color = ClassicColors.OutlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClassicColors.SurfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ClassicColors.OnSurfaceVariant.copy(alpha = 0.4f))
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f))
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun AnimatedComponent(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMillis.toLong())
            startAnimation = true
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    val translationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 30f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "translationY"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translationY
            }
    ) {
        content()
    }
}
