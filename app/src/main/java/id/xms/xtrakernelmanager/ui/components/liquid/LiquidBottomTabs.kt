package id.xms.xtrakernelmanager.ui.components.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import id.xms.xtrakernelmanager.ui.components.LocalBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.DampedDragAnimation
import id.xms.xtrakernelmanager.ui.components.utils.InteractiveHighlight
import id.xms.xtrakernelmanager.ui.components.utils.LayerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.drawBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.blur
import id.xms.xtrakernelmanager.ui.components.utils.colorControls
import id.xms.xtrakernelmanager.ui.components.utils.lens
import id.xms.xtrakernelmanager.ui.components.utils.Highlight
import id.xms.xtrakernelmanager.ui.components.utils.Shadow
import id.xms.xtrakernelmanager.ui.components.utils.InnerShadow
import id.xms.xtrakernelmanager.ui.components.utils.layerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.rememberCombinedBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.rememberLayerBackdrop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private fun Float.fastCoerceIn(min: Float, max: Float): Float = 
    if (this < min) min else if (this > max) max else this

private fun Int.fastCoerceIn(min: Int, max: Int): Int =
    if (this < min) min else if (this > max) max else this

private fun Float.fastRoundToInt(): Int =
    (this + 0.5f).toInt()

interface LiquidBottomTabsScope : RowScope {
    fun press()
    fun release()
}

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable LiquidBottomTabsScope.() -> Unit
) {
    val backdrop = LocalBackdrop.current
    val isLightTheme = !isSystemInDarkTheme()
    val containerColor =
        if (isLightTheme) Color(0xFFFAFAFA).copy(0.4f)
        else Color(0xFF121212).copy(0.4f)
    val glassBorder = 
        if (isLightTheme) Color.White.copy(alpha = 0.3f) 
        else Color.White.copy(alpha = 0.15f)

    val tabsBackdrop = rememberLayerBackdrop()
    val shape = RoundedCornerShape(percent = 50)

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember {
            mutableIntStateOf(selectedTabIndex())
        }
        var isFromDrag by remember { mutableStateOf(false) }
        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTabIndex().toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    isFromDrag = true
                    currentIndex = targetIndex
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }
        LaunchedEffect(selectedTabIndex) {
            snapshotFlow { selectedTabIndex() }
                .collectLatest { index ->
                    currentIndex = index
                }
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentIndex }
                .drop(1)
                .collectLatest { index ->
                    dampedDragAnimation.animateToValue(index.toFloat())
                    if (isFromDrag) {
                        onTabSelected(index)
                        isFromDrag = false
                    }
                }
        }

        val interactiveHighlight = remember(animationScope) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, offset ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        // Build container modifier
        var containerModifier = Modifier
            .graphicsLayer {
                translationX = panelOffset
            }
            .height(64f.dp)
            .fillMaxWidth()
            .padding(4f.dp)
            .clip(shape)

        if (backdrop != null) {
            containerModifier = containerModifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    colorControls(saturation = 1.0f)
                    blur(8f.dp.toPx())
                    lens(24f.dp.toPx(), 24f.dp.toPx())
                },
                layerBlock = {
                    val progress = dampedDragAnimation.pressProgress
                    val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, progress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = { drawRect(containerColor) }
            )
        } else {
            containerModifier = containerModifier.background(containerColor)
        }

        Row(
            containerModifier
                .border(1.5.dp, glassBorder, shape)
                .then(interactiveHighlight.modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val scope = object : LiquidBottomTabsScope, RowScope by this {
                override fun press() = dampedDragAnimation.press()
                override fun release() = dampedDragAnimation.release()
            }
            scope.content()
        }

        // Hidden tabs layer for combined backdrop effect
        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides {
                lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
            }
        ) {
            var hiddenRowModifier = Modifier
                .clearAndSetSemantics {}
                .alpha(0f)
                .layerBackdrop(tabsBackdrop)
                .graphicsLayer {
                    translationX = panelOffset
                }
                .height(56f.dp)
                .fillMaxWidth()
                .padding(horizontal = 4f.dp)
                .clip(shape)

            if (backdrop != null) {
                hiddenRowModifier = hiddenRowModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        colorControls(saturation = 1.0f)
                        blur(8f.dp.toPx())
                        lens(
                            24f.dp.toPx() * progress,
                            24f.dp.toPx() * progress
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Default.copy(alpha = progress)
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
            }

            Row(
                hiddenRowModifier.then(interactiveHighlight.modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scope = object : LiquidBottomTabsScope, RowScope by this {
                    override fun press() = dampedDragAnimation.press()
                    override fun release() = dampedDragAnimation.release()
                }
                scope.content()
            }
        }

        // Pill indicator
        val pillSurfaceColor = if (isLightTheme) Color.Black.copy(0.1f) else Color.White.copy(0.1f)
        val combinedBackdrop = if (backdrop != null) rememberCombinedBackdrop(backdrop, tabsBackdrop) else null

        var pillModifier = Modifier
            .padding(horizontal = 4f.dp)
            .graphicsLayer {
                translationX =
                    if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                    else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                
                scaleX = dampedDragAnimation.scaleX
                scaleY = dampedDragAnimation.scaleY
                val velocity = dampedDragAnimation.velocity / 10f
                scaleX /= 1f - (velocity * 1.2f).fastCoerceIn(-0.4f, 0.4f)
                scaleY *= 1f - (velocity * 0.4f).fastCoerceIn(-0.2f, 0.2f)
            }
            .then(interactiveHighlight.gestureModifier)
            .then(dampedDragAnimation.getModifier())
            .height(56f.dp)
            .fillMaxWidth(1f / tabsCount)
            .clip(shape)

        if (combinedBackdrop != null) {
            pillModifier = pillModifier.drawBackdrop(
                backdrop = combinedBackdrop,
                shape = { shape },
                effects = {
                    val progress = dampedDragAnimation.pressProgress
                    lens(
                        16f.dp.toPx() * progress,
                        24f.dp.toPx() * progress,
                        chromaticAberration = true
                    )
                },
                highlight = {
                    val progress = dampedDragAnimation.pressProgress
                    Highlight.Default.copy(alpha = progress)
                },
                shadow = {
                    val progress = dampedDragAnimation.pressProgress
                    Shadow(alpha = progress)
                },
                innerShadow = {
                    val progress = dampedDragAnimation.pressProgress
                    InnerShadow(
                        radius = 8f.dp * progress,
                        alpha = progress
                    )
                },
                layerBlock = {},
                onDrawSurface = {
                    val progress = dampedDragAnimation.pressProgress
                    drawRect(pillSurfaceColor, alpha = 1f - progress)
                    drawRect(Color.Black.copy(alpha = 0.03f * progress))
                }
            )
        } else {
            pillModifier = pillModifier.background(pillSurfaceColor)
        }

        Box(pillModifier)
    }
}
