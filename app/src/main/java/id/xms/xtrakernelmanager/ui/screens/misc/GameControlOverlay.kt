package id.xms.xtrakernelmanager.ui.screens.misc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class GameControlOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 100
        }

        overlayView = ComposeView(context).apply {
            setContent {
                OverlayContent(
                    onClose = { hide() }
                )
            }
        }

        windowManager.addView(overlayView, params)
    }

    fun hide() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }
}

@Composable
private fun OverlayContent(
    onClose: () -> Unit
) {
    var fps by remember { mutableIntStateOf(60) }
    var cpuFreq by remember { mutableIntStateOf(2400) }
    var cpuLoad by remember { mutableFloatStateOf(45.5f) }
    var gpuLoad by remember { mutableFloatStateOf(32.8f) }
    var batteryTemp by remember { mutableFloatStateOf(35.2f) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Simulate real-time updates
        while (true) {
            kotlinx.coroutines.delay(1000)
            fps = Random.nextInt(50, 61)
            cpuFreq = Random.nextInt(1800, 2841)
            cpuLoad = Random.nextFloat() * 60f + 20f  // 20f to 80f
            gpuLoad = Random.nextFloat() * 50f + 15f  // 15f to 65f
            batteryTemp = Random.nextFloat() * 10f + 30f  // 30f to 40f
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(12.dp)
    ) {
        if (isExpanded) {
            ExpandedOverlay(
                fps = fps,
                cpuFreq = cpuFreq,
                cpuLoad = cpuLoad,
                gpuLoad = gpuLoad,
                batteryTemp = batteryTemp,
                onCollapse = { isExpanded = false },
                onClose = onClose
            )
        } else {
            CompactOverlay(
                fps = fps,
                onExpand = { isExpanded = true }
            )
        }
    }
}

@Composable
private fun CompactOverlay(
    fps: Int,
    onExpand: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onExpand() }
    ) {
        Text(
            text = "FPS: $fps",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExpandedOverlay(
    fps: Int,
    cpuFreq: Int,
    cpuLoad: Float,
    gpuLoad: Float,
    batteryTemp: Float,
    onCollapse: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.width(200.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Performance",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                Text(
                    text = "−",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { onCollapse() }
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OverlayStat("FPS", fps.toString(), getFpsColor(fps))
        OverlayStat("CPU", "$cpuFreq MHz", Color.Cyan)
        OverlayStat("CPU Load", "${cpuLoad.toInt()}%", Color.Yellow)
        OverlayStat("GPU Load", "${gpuLoad.toInt()}%", Color.Magenta)
        OverlayStat("Battery", "${batteryTemp.toInt()}°C", getTempColor(batteryTemp))
    }
}

@Composable
private fun OverlayStat(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getFpsColor(fps: Int): Color = when {
    fps >= 55 -> Color.Green
    fps >= 45 -> Color.Yellow
    else -> Color.Red
}

private fun getTempColor(temp: Float): Color = when {
    temp < 35 -> Color.Green
    temp < 40 -> Color.Yellow
    else -> Color.Red
}
