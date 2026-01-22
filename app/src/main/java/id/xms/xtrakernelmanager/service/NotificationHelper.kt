package id.xms.xtrakernelmanager.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Icon
import id.xms.xtrakernelmanager.R

object NotificationHelper {

  fun generateIcon(
      context: Context,
      type: String,
      level: Int,
      charging: Boolean,
      temp: Int,
      current: Int,
      voltage: Int,
  ): Icon {
    // Handle default battery icon
    if (type == "battery_icon") {
      return Icon.createWithResource(
          context,
          if (charging) R.drawable.ic_battery_charging else R.drawable.ic_battery,
      )
    }

    // Handle dynamic icon generation
    val size = 96 // High resolution for status bar
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint =
        Paint().apply {
          color = Color.WHITE
          isAntiAlias = true
          textAlign = Paint.Align.CENTER
          typeface = Typeface.DEFAULT_BOLD
        }

    val text =
        when (type) {
          "circle_percent" -> "$level"
          "percent_only" -> "$level"
          "temp" -> "${temp / 10}"
          "percent_temp" -> "$level" // We'll draw temp below
          "current" ->
              "${kotlin.math.abs(current)}" // Show absolute value? Usually users want magnitude
          "voltage" -> "${voltage}"
          "power" -> {
            // Calculate Watts: (mV / 1000) * (mA / 1000)
            val watts = (kotlin.math.abs(voltage) / 1000f) * (kotlin.math.abs(current) / 1000f)
            "%.1f".format(watts)
          }
          else -> "$level"
        }

    // Draw Logic
    when (type) {
      "circle_percent" -> {
        // Draw circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        val radius = size / 2f - 4f
        canvas.drawCircle(size / 2f, size / 2f, radius, paint)

        // Draw text inside
        paint.style = Paint.Style.FILL
        paint.textSize = if (text.length > 2) 36f else 48f
        centerText(canvas, paint, text, size / 2f, size / 2f)
      }
      "percent_temp" -> {
        // Draw Level on top
        paint.textSize = 38f // Slightly smaller
        centerText(canvas, paint, "$level", size / 2f, size * 0.35f) // Moved slightly down

        // Draw Temp on bottom
        paint.textSize = 28f
        // Add degree symbol and C if space allows, or just degree
        centerText(canvas, paint, "${temp/10}°", size / 2f, size * 0.75f) // Moved slightly down
      }
      else -> {
        // Just centering the text for other single-value types
        // Adjust text size based on length for consistent "clean" look
        paint.textSize =
            when {
              text.length <= 2 -> 56f // Was 64, reduced for clean look
              text.length == 3 -> 44f // Was 48
              else -> 34f // Was 36
            }
        centerText(canvas, paint, text, size / 2f, size / 2f)
      }
    }

    return Icon.createWithBitmap(bitmap)
  }

  private fun centerText(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    canvas.drawText(text, x, y - bounds.exactCenterY(), paint)
  }
}
