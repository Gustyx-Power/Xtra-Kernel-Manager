package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    amplitude: Dp = 4.dp,
    frequency: Float = 0.06f,
    strokeWidth: Dp = 4.dp
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = { Spacer(Modifier.size(20.dp)) },
        track = { sliderState ->
            WavyTrack(
                sliderState = sliderState,
                colors = colors,
                enabled = enabled,
                amplitude = amplitude,
                frequency = frequency,
                strokeWidth = strokeWidth
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WavyTrack(
    sliderState: SliderState,
    colors: SliderColors,
    enabled: Boolean,
    amplitude: Dp,
    frequency: Float,
    strokeWidth: Dp
) {
    val activeTrackColor = colors.activeTrackColor
    val inactiveTrackColor = colors.inactiveTrackColor
    
    val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp) // Height to accommodate waves
    ) {
        val width = size.width
        val height = size.height
        val midHeight = height / 2f
        val waveAmplitude = amplitude.toPx()
        val strokePx = strokeWidth.toPx()

        val progressWidth = width * fraction

        // Draw Inactive (Straight)
        if (fraction < 1f) {
            drawLine(
                color = inactiveTrackColor,
                start = Offset(progressWidth, midHeight),
                end = Offset(width, midHeight),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }

        // Draw Active (Wavy)
        if (fraction > 0f) {
            val path = Path()
            path.moveTo(0f, midHeight)

            var currentX = 0f
            while (currentX <= progressWidth) {
                val y = midHeight + waveAmplitude * sin(currentX * frequency)
                path.lineTo(currentX, y)
                currentX += 1f // 1 pixel step
            }
            
            drawPath(
                path = path,
                color = activeTrackColor,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}
