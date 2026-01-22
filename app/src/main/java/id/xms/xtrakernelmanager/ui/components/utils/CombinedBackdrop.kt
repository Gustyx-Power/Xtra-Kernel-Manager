package id.xms.xtrakernelmanager.ui.components.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density

@Composable
fun rememberCombinedBackdrop(
    backdrop1: LayerBackdrop,
    backdrop2: LayerBackdrop
): LayerBackdrop {
    return remember(backdrop1, backdrop2) {
        Combined2Backdrops(backdrop1, backdrop2)
    }
}

@Immutable
private class Combined2Backdrops(
    val backdrop1: LayerBackdrop,
    val backdrop2: LayerBackdrop
) : LayerBackdrop(backdrop1.graphicsLayer, backdrop1.onDraw) {

    override val isCoordinatesDependent: Boolean =
        backdrop1.isCoordinatesDependent || backdrop2.isCoordinatesDependent

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        with(backdrop1) { drawBackdrop(density, coordinates, layerBlock) }
        with(backdrop2) { drawBackdrop(density, coordinates, layerBlock) }
    }
}
