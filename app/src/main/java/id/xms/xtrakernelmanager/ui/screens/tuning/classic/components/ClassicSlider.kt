package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlin.math.roundToInt

@Composable
fun ClassicSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueFinished: (() -> Unit)? = null,
    valueDisplay: String,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueFinished,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = ClassicColors.Primary,
                activeTrackColor = ClassicColors.Primary,
                inactiveTrackColor = ClassicColors.SurfaceVariant
            )
        )
    }
}
