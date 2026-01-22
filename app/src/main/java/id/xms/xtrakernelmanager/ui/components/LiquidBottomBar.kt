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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens

@Composable
fun LiquidBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        val backdrop = LocalBackdrop.current
        val isDark = isSystemInDarkTheme()
        val shape = RoundedCornerShape(percent = 50) // Capsule shape

        // Base modifier for the glass container
        var containerModifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(88.dp) // Adjusted height
            .clip(shape)

        // Manual fallback color
        val fallbackColor = if (isDark) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        }

        val glassBorder = if (isDark) {
            Color.White.copy(alpha = 0.2f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }
        
        // Container Glass Effect
        if (backdrop != null) {
             containerModifier = containerModifier.drawBackdrop(
                 backdrop = backdrop,
                 shape = { shape },
                 effects = {
                      colorControls(
                              saturation = 0.7f,
                              brightness = 0.15f
                      )
                      blur(20.dp.toPx())
                      lens(
                              refractionHeight = 32.dp.toPx(),
                              refractionAmount = 48.dp.toPx(),
                              chromaticAberration = true,
                              depthEffect = true
                      )
                 }
             )
        } else {
            containerModifier = containerModifier.background(fallbackColor)
        }
        
        // State for drag
        var selectedIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: 0
        // Use a state for the drag offset
        var dragOffset by remember { mutableFloatStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }

        BoxWithConstraints(
            modifier = containerModifier
                .background(if (backdrop == null) fallbackColor else Color.Transparent)
                .border(1.5.dp, glassBorder, shape),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidth = maxWidth
            val itemWidth = totalWidth / items.size
            
            // Calculate target offset based on selection or drag
            val targetOffset = if (isDragging) {
                // Determine base offset for current index + drag
                (selectedIndex * itemWidth.value) + dragOffset
            } else {
                (selectedIndex * itemWidth.value).toFloat()
            }
            
            // Animate offset when not dragging
            val animatedOffset by animateFloatAsState(
                targetValue = if (isDragging) (selectedIndex * itemWidth.value) + dragOffset else (selectedIndex * itemWidth.value),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.7f),
                label = "pillOffset"
            )

            // 1. The Glass Pill (Background - Interactive)
            val pillShape = RoundedCornerShape(50)
            var pillModifier = Modifier
                .offset(x = (if (isDragging) (selectedIndex * itemWidth.value) + dragOffset else animatedOffset).dp)
                .width(itemWidth)
                .padding(4.dp)
                .fillMaxHeight()
                .clip(pillShape)
                .draggable( // Draggable moves to the pill itself!
                    orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                    state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
                        isDragging = true
                        dragOffset += (delta / localDensity.density)
                    },
                    onDragStopped = {
                         isDragging = false
                         // Snap logic
                         val finalOffset = (selectedIndex * itemWidth.value) + dragOffset
                         val newIndex = (finalOffset / itemWidth.value).roundToInt().coerceIn(0, items.size - 1)
                         if (newIndex != selectedIndex) {
                              onNavigate(items[newIndex].route)
                         }
                         dragOffset = 0f
                    }
                )

            // Pill Glass Effect ("Solid Glass")
            val pillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            if (backdrop != null) {
                pillModifier = pillModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { pillShape },
                    effects = {
                        colorControls(saturation = 1.0f, brightness = 0.3f)
                        blur(10.dp.toPx())
                        lens(
                             refractionHeight = 24.dp.toPx(),
                             refractionAmount = 16.dp.toPx(),
                             chromaticAberration = true,
                             depthEffect = true
                        )
                    }
                )
            } else {
                pillModifier = pillModifier.background(pillColor)
            }

            Box(
                modifier = pillModifier
                    .background(pillColor)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), pillShape)
            )

            // 2. The Items (Foreground - Clickable)
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
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
