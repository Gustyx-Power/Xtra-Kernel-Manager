package id.xms.xtrakernelmanager.ui.components.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch

/**
 * Simplified Liquid Toggle inspired by backdrop catalog
 * Uses simple animation without complex backdrop effects
 */
@Composable
fun LiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
    val trackColor = if (isLightTheme) Color(0xFF787878).copy(0.2f) else Color(0xFF787880).copy(0.36f)
    
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val density = LocalDensity.current
    val dragWidth = with(density) { 24f.dp.toPx() }  // Increased for better movement
    
    val scope = rememberCoroutineScope()
    val fraction = remember { Animatable(if (checked) 1f else 0f) }
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(checked) {
        fraction.animateTo(
            targetValue = if (checked) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        // Track
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .drawBehind {
                    drawRect(lerp(trackColor, accentColor, fraction.value))
                }
                .size(51f.dp, 31f.dp)  // iOS-like proportions
        )
        
        // Thumb
        Box(
            Modifier
                .graphicsLayer {
                    val padding = 2f.dp.toPx()
                    translationX = if (isLtr) 
                        lerp(padding, padding + dragWidth, fraction.value)
                    else 
                        lerp(-padding, -(padding + dragWidth), fraction.value)
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .semantics {
                    role = Role.Switch
                }
                .pointerInput(enabled, checked) {  // Add checked as key
                    if (enabled) {
                        detectTapGestures(
                            onPress = {
                                scope.launch {
                                    scale.animateTo(0.9f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                                }
                                tryAwaitRelease()
                                scope.launch {
                                    scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                                }
                            },
                            onTap = {
                                onCheckedChange(!checked)
                            }
                        )
                    }
                }
                .clip(RoundedCornerShape(50))
                .drawBehind {
                    drawRect(Color.White)
                }
                .size(27f.dp, 27f.dp)  // Perfect circle thumb
        )
    }
}
