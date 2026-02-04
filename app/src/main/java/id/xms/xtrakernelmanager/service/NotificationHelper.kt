package id.xms.xtrakernelmanager.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
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
  ): IconCompat {
    // Handle default battery icon
    if (type == "battery_icon") {
      return IconCompat.createWithResource(
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
          "current" -> {
             val absCurrent = kotlin.math.abs(current)
             val sign = if (current > 0) "+ " else if (current < 0) "- " else ""
             "$sign$absCurrent"
          }
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

    return IconCompat.createWithBitmap(bitmap)
  }

  // Native Icon version for Android O+ to avoid IconCompat conversion issues
  @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.M)
  fun generateNativeIcon(
      context: Context,
      type: String,
      level: Int,
      charging: Boolean,
      temp: Int,
      current: Int,
      voltage: Int,
  ): android.graphics.drawable.Icon {
    // Handle default battery icon
    if (type == "battery_icon") {
      return android.graphics.drawable.Icon.createWithResource(
          context,
          if (charging) R.drawable.ic_battery_charging else R.drawable.ic_battery,
      )
    }

    // Handle dynamic icon generation
    // Use standard 72x72 for compat (approx 24dp xxhdpi)
    val size = 72 
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
          "percent_temp" -> "$level" 
          "current" -> {
             val absCurrent = kotlin.math.abs(current)
             val sign = if (current > 0) "+ " else if (current < 0) "- " else ""
             "$sign$absCurrent"
          }
          "voltage" -> "${voltage}"
          "power" -> {
            val watts = (kotlin.math.abs(voltage) / 1000f) * (kotlin.math.abs(current) / 1000f)
            "%.1f".format(watts)
          }
          else -> "$level"
        }

    // Reuse draw logic (duplicated here for simplicity/independence from Compat types)
    when (type) {
      "circle_percent" -> {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f // Reduced for smaller size
        val radius = size / 2f - 4f
        canvas.drawCircle(size / 2f, size / 2f, radius, paint)
        paint.style = Paint.Style.FILL
        paint.textSize = if (text.length > 2) 28f else 36f
        centerText(canvas, paint, text, size / 2f, size / 2f)
      }
      "percent_temp" -> {
        paint.textSize = 28f
        centerText(canvas, paint, "$level", size / 2f, size * 0.35f)
        paint.textSize = 22f
        centerText(canvas, paint, "${temp/10}°", size / 2f, size * 0.75f)
      }
      else -> {
        paint.textSize =
            when {
              text.length <= 2 -> 42f
              text.length == 3 -> 34f
              else -> 28f
            }
        centerText(canvas, paint, text, size / 2f, size / 2f)
      }
    }
    val icon = android.graphics.drawable.Icon.createWithBitmap(bitmap)
    // Force tint to white to ensure it acts as a mask on some ROMs
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        icon.setTint(Color.WHITE)
    }
    return icon
  }

  private fun centerText(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    canvas.drawText(text, x, y - bounds.exactCenterY(), paint)
  }
}
