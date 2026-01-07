package id.xms.xtrakernelmanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object ExpressiveShapes {

  private val RadiusLarge = 32.dp
  private val RadiusSmall = 4.dp
  private val RadiusArchTop = 100.dp

  val Squircle = RoundedCornerShape(RadiusLarge)

  val Leaf =
      RoundedCornerShape(
          topStart = RadiusLarge,
          bottomEnd = RadiusLarge,
          topEnd = RadiusSmall,
          bottomStart = RadiusSmall,
      )

  val LeafReverse =
      RoundedCornerShape(
          topStart = RadiusSmall,
          bottomEnd = RadiusSmall,
          topEnd = RadiusLarge,
          bottomStart = RadiusLarge,
      )

  val Arch =
      RoundedCornerShape(
          topStart = RadiusArchTop,
          topEnd = RadiusArchTop,
          bottomStart = 16.dp,
          bottomEnd = 16.dp,
      )

  fun getByIndex(index: Int): Shape {
    return when (index % 4) {
      0 -> Squircle
      1 -> Leaf
      2 -> Arch
      3 -> LeafReverse
      else -> Squircle
    }
  }
}
