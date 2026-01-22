package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.components.utils.drawBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.blur
import id.xms.xtrakernelmanager.ui.components.utils.colorControls
import id.xms.xtrakernelmanager.ui.components.utils.lens
import id.xms.xtrakernelmanager.ui.components.utils.DampedDragAnimation

@Composable
fun LiquidBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        val backdrop = LocalBackdrop.current
        val isDark = isSystemInDarkTheme()
        val shape = RoundedCornerShape(percent = 50) 
        val localDensity = androidx.compose.ui.platform.LocalDensity.current

        var containerModifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(64.dp) 
            .clip(shape)

        val containerColor = if (isDark) Color(0xFF121212).copy(alpha = 0.4f) else Color(0xFFFAFAFA).copy(alpha = 0.4f)
        val fallbackColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        val glassBorder = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)

        if (backdrop != null) {
             containerModifier = containerModifier.drawBackdrop(
                 backdrop = backdrop,
                 shape = { shape },
                 effects = {
                      colorControls(saturation = 1.0f)
                      blur(8.dp.toPx())
                      lens(24.dp.toPx(), 24.dp.toPx())
                 },
                 onDrawSurface = { drawRect(containerColor) }
             )
        } else {
            containerModifier = containerModifier.background(fallbackColor)
        }
        
        // Determine current index
        val selectedIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: 0
        val selectedIndexFloat = selectedIndex.toFloat()

        BoxWithConstraints(
            modifier = containerModifier
                .background(if (backdrop == null) fallbackColor else Color.Transparent)
                .border(1.5.dp, glassBorder, shape),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidth = maxWidth
            val itemWidth = totalWidth / items.size
            val trackWidthPx = with(localDensity) { totalWidth.toPx() }
            
            // Animation State
            val animationScope = rememberCoroutineScope()
            // We use a local state to drive the animation target
            var targetIndex by remember { mutableFloatStateOf(selectedIndexFloat) }
            
            // Sync with external route changes
            LaunchedEffect(selectedIndexFloat) {
                targetIndex = selectedIndexFloat
            }

            val dampedDragAnimation = remember(animationScope) {
                DampedDragAnimation(
                    animationScope = animationScope,
                    initialValue = selectedIndexFloat,
                    valueRange = 0f..(items.size - 1).toFloat(),
                    visibilityThreshold = 0.01f,
                    initialScale = 1f,
                    pressedScale = 1.05f, // Subtle scale up
                    onDragStarted = {},
                    onDragStopped = {
                        // Snap logic on release
                         val nearestIndex = value.roundToInt().coerceIn(0, items.size - 1)
                         // Navigate only if changed
                         if (nearestIndex != selectedIndex) {
                             onNavigate(items[nearestIndex].route)
                         } else {
                             // Snap back to current if didn't change enough
                             animateToValue(nearestIndex.toFloat())
                         }
                    },
                    onDrag = { _, dragAmount ->
                        // Calculate delta in "index units"
                        // dragAmount.x is pixels. 
                        // 1.0 index = itemWidth pixels = (trackWidthPx / items.size)
                        val itemWidthPx = trackWidthPx / items.size
                        val deltaIndex = dragAmount.x / itemWidthPx
                        
                        // Update the internal animation value instantly (dragging)
                        // We use a "hack": update targetValue to (current + delta) and animate immediately? 
                        // DampedDragAnimation usually expects `onValueChange` to drive it.
                        // Here, we can manipulate `targetIndex`? 
                        // Actually, looking at LiquidSlider:
                        // val delta = ... 
                        // onValueChange(targetValue + delta)
                        // And LaunchedEffect syncs DampedDragAnimation.updateValue(value)
                        
                        targetIndex = (targetIndex + deltaIndex).coerceIn(0f, (items.size - 1).toFloat())
                    }
                )
            }
            
            // Sync DampedDragAnimation with our local targetIndex
            LaunchedEffect(targetIndex) {
                // Use updateValue to smoothly spring to the new target (or track drag)
                dampedDragAnimation.updateValue(targetIndex)
            }
            // IMPORTANT: Also sync if selectedIndex changes externally (handled by first LaunchedEffect but we need to update targetIndex)

            // 1. The Glass Pill
            val pillShape = RoundedCornerShape(50)
            
            // Calculate translation from physics value
            val currentPillIndex = dampedDragAnimation.value
            val itemWidthPx = with(localDensity) { itemWidth.toPx() }

            // Pill colors - WHITE for dark theme, BLACK for light theme (matching catalog)
            val pillSurfaceColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
            
            // Build pill modifier - use graphicsLayer for translation like catalog does
            var pillModifier = Modifier
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    // Use translationX for smooth animated movement
                    translationX = currentPillIndex * itemWidthPx
                }
                .then(dampedDragAnimation.modifier) // Gesture modifier BEFORE drawBackdrop
            
            if (backdrop != null) {
                pillModifier = pillModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { pillShape },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        lens(
                             refractionHeight = 10.dp.toPx() * progress,
                             refractionAmount = 14.dp.toPx() * progress,
                             chromaticAberration = true
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        id.xms.xtrakernelmanager.ui.components.utils.Highlight.Default.copy(alpha = progress)
                    },
                    shadow = {
                        val progress = dampedDragAnimation.pressProgress
                        id.xms.xtrakernelmanager.ui.components.utils.Shadow(alpha = progress)
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        id.xms.xtrakernelmanager.ui.components.utils.InnerShadow(
                            radius = 8.dp * progress,
                            alpha = progress
                        )
                    },
                    layerBlock = {
                        // Velocity-based Squish & Stretch
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        val stretch = (velocity * 0.75f).coerceIn(-0.2f, 0.2f)
                        val squash = (velocity * 0.25f).coerceIn(-0.2f, 0.2f)
                        scaleX /= 1f - stretch
                        scaleY *= 1f - squash
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        // Subtle white overlay at rest (glass look), darker when pressed
                        drawRect(pillSurfaceColor, alpha = 1f - progress)
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
            }

            // Pill box with explicit size matching catalog
            Box(
                modifier = pillModifier
                    .height(56.dp)
                    .fillMaxWidth(1f / items.size)
            )

            // 2. The Items
            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                     val isSelected = index == selectedIndex
                     LiquidDockNavItem(
                         item = item,
                         isSelected = isSelected,
                         textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                         onClick = { onNavigate(item.route) }, 
                         modifier = Modifier.weight(1f)
                     )
                }
            }
        }
    }
}


@Composable
private fun LiquidDockNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    textColor: Color, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, 
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.label),
            tint = textColor, // Use calculated color
            modifier = Modifier.size(26.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        AnimatedVisibility(visible = isSelected) {
            Text(
                text = stringResource(item.label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
