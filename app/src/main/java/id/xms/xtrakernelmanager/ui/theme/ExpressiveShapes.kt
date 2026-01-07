package id.xms.xtrakernelmanager.ui.theme

import android.graphics.Path
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ExpressiveShapes {

  private val RadiusLarge = 32.dp
  private val RadiusSmall = 4.dp
  private val RadiusArchTop = 100.dp

  // ===== BASIC SHAPES =====
  
  // Circle
  val Circle = RoundedCornerShape(50) // Full circle
  
  // Square (rounded)
  val Square = RoundedCornerShape(28.dp)
  
  // Slanted (asymmetric rounded corners)
  val Slanted = RoundedCornerShape(
      topStart = 36.dp,
      topEnd = 12.dp,
      bottomStart = 12.dp,
      bottomEnd = 36.dp
  )
  
  // Arch (semicircle top)
  val Arch = RoundedCornerShape(
      topStart = 100.dp,
      topEnd = 100.dp,
      bottomStart = 16.dp,
      bottomEnd = 16.dp
  )
  
  // Semicircle (half circle on right)
  val Semicircle = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      
      moveTo(0f, 0f)
      lineTo(width * 0.6f, 0f)
      
      // Curved right side
      cubicTo(
          width * 0.85f, 0f,
          width, height * 0.15f,
          width, height * 0.5f
      )
      cubicTo(
          width, height * 0.85f,
          width * 0.85f, height,
          width * 0.6f, height
      )
      
      lineTo(0f, height)
      close()
  }
  
  // Oval (horizontal)
  val Oval = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radiusX = width / 2f
      val radiusY = height / 2.5f
      
      moveTo(radiusX, height / 2f - radiusY)
      
      // Approximate oval with bezier curves
      cubicTo(
          radiusX + radiusX * 0.55f, height / 2f - radiusY,
          width, height / 2f - radiusY * 0.45f,
          width, height / 2f
      )
      cubicTo(
          width, height / 2f + radiusY * 0.45f,
          radiusX + radiusX * 0.55f, height / 2f + radiusY,
          radiusX, height / 2f + radiusY
      )
      cubicTo(
          radiusX - radiusX * 0.55f, height / 2f + radiusY,
          0f, height / 2f + radiusY * 0.45f,
          0f, height / 2f
      )
      cubicTo(
          0f, height / 2f - radiusY * 0.45f,
          radiusX - radiusX * 0.55f, height / 2f - radiusY,
          radiusX, height / 2f - radiusY
      )
      close()
  }
  
  // Pill (capsule)
  val Pill = RoundedCornerShape(50)
  
  // Triangle (rounded)
  val Triangle = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2.2f
      val polygon = RoundedPolygon(
          numVertices = 3,
          radius = radius,
          rounding = CornerRounding(radius * 0.25f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Arrow (rounded triangle pointing right)
  val Arrow = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      
      moveTo(0f, height * 0.2f)
      lineTo(width * 0.6f, height * 0.2f)
      lineTo(width * 0.6f, 0f)
      lineTo(width, height * 0.5f)
      lineTo(width * 0.6f, height)
      lineTo(width * 0.6f, height * 0.8f)
      lineTo(0f, height * 0.8f)
      close()
  }
  
  // Fan (like semicircle but bottom left)
  val Fan = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      
      moveTo(0f, 0f)
      lineTo(width, 0f)
      cubicTo(
          width, height * 0.4f,
          width * 0.6f, height,
          0f, height
      )
      close()
  }

  // Diamond (rounded)
  val Diamond = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon(
          numVertices = 4,
          radius = radius,
          rounding = CornerRounding(radius * 0.25f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Clamshell (hexagon horizontal)
  val Clamshell = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon(
          numVertices = 6,
          radius = radius,
          rounding = CornerRounding(radius * 0.2f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Pentagon (rounded)
  val Pentagon = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon(
          numVertices = 5,
          radius = radius,
          rounding = CornerRounding(radius * 0.22f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Gem (hexagon - alternative)
  val Gem = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon(
          numVertices = 6,
          radius = radius,
          rounding = CornerRounding(radius * 0.18f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }

  
  // Very Sunny (10-point star)
  val VerySunny = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 10,
          radius = radius,
          innerRadius = radius * 0.75f,
          rounding = CornerRounding(radius * 0.15f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Sunny (8-point star)
  val Sunny = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 8,
          radius = radius,
          innerRadius = radius * 0.78f,
          rounding = CornerRounding(radius * 0.18f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Cookie4 (4-sided cookie)
  val Cookie4 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 4,
          radius = radius,
          innerRadius = radius * 0.85f,
          rounding = CornerRounding(radius * 0.3f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Cookie6 (6-sided cookie)
  val Cookie6 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 6,
          radius = radius,
          innerRadius = radius * 0.88f,
          rounding = CornerRounding(radius * 0.25f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Cookie7 (7-sided cookie)
  val Cookie7 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 7,
          radius = radius,
          innerRadius = radius * 0.88f,
          rounding = CornerRounding(radius * 0.22f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Cookie9 (9-sided cookie)
  val Cookie9 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 9,
          radius = radius,
          innerRadius = radius * 0.88f,
          rounding = CornerRounding(radius * 0.2f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Cookie12 (12-sided cookie)
  val Cookie12 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 12,
          radius = radius,
          innerRadius = radius * 0.9f,
          rounding = CornerRounding(radius * 0.18f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Clover4 (4-leaf clover)
  val Clover4 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 4,
          radius = radius,
          innerRadius = radius * 0.5f,
          rounding = CornerRounding(radius * 0.4f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Clover8 (8-leaf clover)
  val Clover8 = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 8,
          radius = radius,
          innerRadius = radius * 0.5f,
          rounding = CornerRounding(radius * 0.35f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }

  // Burst (16-point sharp star)
  val Burst = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 16,
          radius = radius,
          innerRadius = radius * 0.65f,
          rounding = CornerRounding(radius * 0.05f), // Sharp
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Soft Burst
  val SoftBurst = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 16,
          radius = radius,
          innerRadius = radius * 0.7f,
          rounding = CornerRounding(radius * 0.12f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Boom (Sharp 20-point)
  val Boom = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 20,
          radius = radius,
          innerRadius = radius * 0.6f,
          rounding = CornerRounding(radius * 0.03f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Soft Boom
  val SoftBoom = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 24,
          radius = radius,
          innerRadius = radius * 0.75f,
          rounding = CornerRounding(radius * 0.1f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }

  
  // Flower (rounded petals)
  val Flower = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 8,
          radius = radius,
          innerRadius = radius * 0.6f,
          rounding = CornerRounding(radius * 0.5f), // Very rounded
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Puffy
  val Puffy = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 12,
          radius = radius,
          innerRadius = radius * 0.7f,
          rounding = CornerRounding(radius * 0.45f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }
  
  // Puffy Diamond
  val PuffyDiamond = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = size.minDimension / 2f
      val polygon = RoundedPolygon.star(
          numVerticesPerRadius = 4,
          radius = radius,
          innerRadius = radius * 0.65f,
          rounding = CornerRounding(radius * 0.5f),
          centerX = width / 2f,
          centerY = height / 2f
      )
      val path = Path()
      polygon.toPath(path)
      addPath(path.asComposePath())
  }

  
  // Pixelated Circle
  val PixelCircle = GenericShape { size, _ ->
      val w = size.width
      val step = w / 10f
      
      moveTo(step * 3, 0f)
      lineTo(step * 7, 0f)
      lineTo(step * 7, step)
      lineTo(step * 9, step)
      lineTo(step * 9, step * 3)
      lineTo(step * 10, step * 3)
      lineTo(step * 10, step * 7)
      lineTo(step * 9, step * 7)
      lineTo(step * 9, step * 9)
      lineTo(step * 7, step * 9)
      lineTo(step * 7, step * 10)
      lineTo(step * 3, step * 10)
      lineTo(step * 3, step * 9)
      lineTo(step, step * 9)
      lineTo(step, step * 7)
      lineTo(0f, step * 7)
      lineTo(0f, step * 3)
      lineTo(step, step * 3)
      lineTo(step, step)
      lineTo(step * 3, step)
      close()
  }
  
  // Heart
  val Heart = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      
      moveTo(width / 2f, height * 0.35f)
      
      // Left curve
      cubicTo(
          width / 2f, height * 0.25f,
          width * 0.25f, height * 0.1f,
          width * 0.2f, height * 0.3f
      )
      cubicTo(
          width * 0.1f, height * 0.5f,
          width * 0.2f, height * 0.7f,
          width / 2f, height * 0.95f
      )
      
      // Right curve
      cubicTo(
          width * 0.8f, height * 0.7f,
          width * 0.9f, height * 0.5f,
          width * 0.8f, height * 0.3f
      )
      cubicTo(
          width * 0.75f, height * 0.1f,
          width / 2f, height * 0.25f,
          width / 2f, height * 0.35f
      )
      
      close()
  }
  
  // Ghost (rounded bottom with wavy edge)
  val Ghost = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      
      // Top half (circle)
      moveTo(width * 0.5f, 0f)
      cubicTo(
          width * 0.2f, 0f,
          0f, height * 0.2f,
          0f, height * 0.45f
      )
      
      // Left side
      lineTo(0f, height * 0.8f)
      
      // Wavy bottom
      cubicTo(
          0f, height * 0.95f,
          width * 0.15f, height,
          width * 0.25f, height * 0.9f
      )
      cubicTo(
          width * 0.35f, height * 0.8f,
          width * 0.4f, height,
          width * 0.5f, height
      )
      cubicTo(
          width * 0.6f, height,
          width * 0.65f, height * 0.8f,
          width * 0.75f, height * 0.9f
      )
      cubicTo(
          width * 0.85f, height,
          width, height * 0.95f,
          width, height * 0.8f
      )
      
      // Right side
      lineTo(width, height * 0.45f)
      cubicTo(
          width, height * 0.2f,
          width * 0.8f, 0f,
          width * 0.5f, 0f
      )
      
      close()
  }
  
  // Bun (like pill but softer)
  val Bun = GenericShape { size, _ ->
      val width = size.width
      val height = size.height
      val radius = height / 2f
      
      moveTo(radius, 0f)
      lineTo(width - radius, 0f)
      
      // Right semicircle
      cubicTo(
          width - radius * 0.45f, 0f,
          width, radius * 0.45f,
          width, radius
      )
      cubicTo(
          width, radius + radius * 0.55f,
          width - radius * 0.45f, height,
          width - radius, height
      )
      
      lineTo(radius, height)
      
      // Left semicircle
      cubicTo(
          radius * 0.55f, height,
          0f, radius + radius * 0.55f,
          0f, radius
      )
      cubicTo(
          0f, radius * 0.45f,
          radius * 0.55f, 0f,
          radius, 0f
      )
      
      close()
  }

  val ContributorShape = Sunny

  // Updated getShape dengan ALL shapes dari Material Design palette
  fun getShape(index: Int): Shape {
      return when(index) {
          0 -> Circle
          1 -> Square
          2 -> Slanted
          3 -> Arch
          4 -> Semicircle
          5 -> Oval
          6 -> Pill
          7 -> Triangle
          8 -> Arrow
          9 -> Fan
          10 -> Diamond
          11 -> Clamshell
          12 -> Pentagon
          13 -> Gem
          14 -> VerySunny
          15 -> Sunny
          16 -> Cookie4
          17 -> Cookie6
          18 -> Cookie7
          19 -> Cookie9
          20 -> Cookie12
          21 -> Clover4
          22 -> Clover8
          23 -> Burst
          24 -> SoftBurst
          25 -> Boom
          26 -> SoftBoom
          27 -> Flower
          28 -> Puffy
          29 -> PuffyDiamond
          30 -> Ghost
          31 -> PixelCircle
          32 -> Heart
          33 -> Bun
          else -> Circle
      }
  }

  fun getByIndex(index: Int): Shape {
    return getShape(index)
  }
  
  // Helper function untuk random shape (opsional)
  fun getRandomShape(): Shape {
      return getShape((0..33).random())
  }
}
