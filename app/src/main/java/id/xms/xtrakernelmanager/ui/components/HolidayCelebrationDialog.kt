package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.utils.Holiday
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Celebration dialog for holidays with animations */
@Composable
fun HolidayCelebrationDialog(holiday: Holiday, year: Int, onDismiss: () -> Unit) {
  val context = LocalContext.current

  // Get motivational message based on holiday
  val motivationalMessage =
      remember(holiday, year) {
        when (holiday) {
          Holiday.CHRISTMAS -> getChristmasMotivation(context, year)
          Holiday.NEW_YEAR -> getNewYearMotivation(context, year)
          Holiday.RAMADAN -> getRamadanMotivation(context)
          Holiday.EID_FITR -> getEidFitriMotivation(context)
        }
      }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
  ) {
    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
      // Background confetti/particles animation
      when (holiday) {
        Holiday.CHRISTMAS -> ChristmasAnimation()
        Holiday.NEW_YEAR -> FireworksAnimation()
        Holiday.RAMADAN -> RamadanAnimation()
        Holiday.EID_FITR -> EidFitriAnimation()
      }

      // Main card content
      Card(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          shape = RoundedCornerShape(24.dp),
          colors =
              CardDefaults.cardColors(
                  containerColor =
                      when (holiday) {
                        Holiday.CHRISTMAS -> Color(0xFF1B5E20).copy(alpha = 0.95f)
                        Holiday.NEW_YEAR -> Color(0xFF1A237E).copy(alpha = 0.95f)
                        Holiday.RAMADAN -> Color(0xFF1A237E).copy(alpha = 0.95f)
                        Holiday.EID_FITR -> Color(0xFF2E7D32).copy(alpha = 0.95f) // Green for Eid
                      }
              ),
          elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
      ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Emoji with animation
          AnimatedEmoji(holiday)

          // Main greeting
          Text(
              text =
                  when (holiday) {
                    Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_title)
                    Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_title, year)
                    Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_title)
                    Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_title)
                  },
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center,
          )

          Text(
              text =
                  when (holiday) {
                    Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_subtitle)
                    Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_subtitle)
                    Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_subtitle)
                    Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_subtitle)
                  },
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.9f),
              textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Motivational message
          Text(
              text = motivationalMessage,
              fontSize = 14.sp,
              color = Color.White.copy(alpha = 0.85f),
              textAlign = TextAlign.Center,
              lineHeight = 22.sp,
          )

          Spacer(modifier = Modifier.height(16.dp))

          // OK Button with gradient
          Button(
              onClick = onDismiss,
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor =
                          when (holiday) {
                            Holiday.CHRISTMAS -> Color(0xFFD32F2F)
                            Holiday.NEW_YEAR -> Color(0xFFFFD700)
                            Holiday.RAMADAN -> Color(0xFF4CAF50)
                            Holiday.EID_FITR -> Color(0xFFFFD700) // Gold for Eid
                          }
                  ),
          ) {
            Text(
                text =
                    when (holiday) {
                      Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_button)
                      Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_button)
                      Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_button)
                      Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_button)
                    },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color =
                    when (holiday) {
                      Holiday.CHRISTMAS -> Color.White
                      Holiday.NEW_YEAR -> Color.Black
                      Holiday.RAMADAN -> Color.White
                      Holiday.EID_FITR -> Color.Black
                    },
            )
          }

          // From XKM
          Text(
              text = stringResource(R.string.holiday_from_xkm),
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.6f),
              textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun AnimatedEmoji(holiday: Holiday) {
  val infiniteTransition = rememberInfiniteTransition(label = "emoji")

  val scale by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.2f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(500, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "scale",
      )

  val rotation by
      infiniteTransition.animateFloat(
          initialValue = -10f,
          targetValue = 10f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(300, easing = LinearEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "rotation",
      )

  Text(
      text =
          when (holiday) {
            Holiday.CHRISTMAS -> "🎄🎅🎄"
            Holiday.NEW_YEAR -> "🎆✨🎆"
            Holiday.RAMADAN -> "🌙🕌🌙"
            Holiday.EID_FITR -> "🎉🕌🎉"
          },
      fontSize = (48 * scale).sp,
      modifier = Modifier.offset(x = (rotation / 5).dp),
  )
}

@Composable
private fun ChristmasAnimation() {
  val particles = remember {
    List(30) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat() * 2f - 1f,
          size = Random.nextFloat() * 8f + 4f,
          speed = Random.nextFloat() * 0.5f + 0.3f,
          color =
              listOf(
                      Color(0xFFFF0000), // Red
                      Color(0xFF00FF00), // Green
                      Color(0xFFFFFFFF), // White
                      Color(0xFFFFD700), // Gold
                  )
                  .random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "snow")
  val animProgress by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
          label = "progress",
      )

  Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    particles.forEach { particle ->
      val y = (particle.y + animProgress * particle.speed * 2) % 1.2f
      val x = particle.x + sin(animProgress * 6.28f + particle.x * 10) * 0.02f

      drawCircle(
          color = particle.color.copy(alpha = 0.7f),
          radius = particle.size,
          center = Offset(x = x * size.width, y = y * size.height),
      )
    }
  }
}

@Composable
private fun FireworksAnimation() {
  val particles = remember {
    List(50) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat(),
          size = Random.nextFloat() * 6f + 2f,
          speed = Random.nextFloat() * 2f + 1f,
          color =
              listOf(
                      Color(0xFFFFD700), // Gold
                      Color(0xFFFF6B6B), // Pink-red
                      Color(0xFF4ECDC4), // Cyan
                      Color(0xFFFFE66D), // Yellow
                      Color(0xFF95E1D3), // Mint
                      Color(0xFFDDA0DD), // Plum
                  )
                  .random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
  val animProgress by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
          label = "progress",
      )

  Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    particles.forEachIndexed { index, particle ->
      val angle = (index.toFloat() / particles.size) * 6.28f
      val radius = animProgress * size.minDimension * 0.4f * particle.speed
      val alpha = (1f - animProgress).coerceIn(0f, 0.8f)

      val x = centerX + cos(angle) * radius * particle.x
      val y = centerY + sin(angle) * radius * particle.y

      drawCircle(
          color = particle.color.copy(alpha = alpha),
          radius = particle.size * (1f - animProgress * 0.5f),
          center = Offset(x, y),
      )
    }
  }
}

@Composable
private fun RamadanAnimation() {
  // Stars and crescent moon animation
  val particles = remember {
    List(40) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat(),
          size = Random.nextFloat() * 4f + 2f,
          speed = Random.nextFloat() * 0.5f + 0.3f,
          color =
              listOf(
                      Color(0xFFFFD700), // Gold
                      Color(0xFFFFF8E1), // Light gold
                      Color(0xFFFFFFFF), // White
                      Color(0xFFFFC107), // Amber
                  )
                  .random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "ramadan")
  val animProgress by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing)),
          label = "progress",
      )

  Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    // Twinkling stars effect
    particles.forEach { particle ->
      val twinkle = (sin(animProgress * 6.28f + particle.x * 20) + 1f) / 2f
      val alpha = 0.3f + twinkle * 0.5f

      drawCircle(
          color = particle.color.copy(alpha = alpha),
          radius = particle.size * (0.8f + twinkle * 0.4f),
          center = Offset(x = particle.x * size.width, y = particle.y * size.height),
      )
    }
  }
}

@Composable
private fun EidFitriAnimation() {
  // Festive celebration with confetti and sparkles
  val particles = remember {
    List(60) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat() * 2f - 0.5f,
          size = Random.nextFloat() * 6f + 3f,
          speed = Random.nextFloat() * 0.8f + 0.4f,
          color =
              listOf(
                      Color(0xFFFFD700), // Gold
                      Color(0xFF4CAF50), // Green
                      Color(0xFFFFFFFF), // White
                      Color(0xFFFFC107), // Amber
                      Color(0xFF8BC34A), // Light green
                      Color(0xFFFF9800), // Orange
                  )
                  .random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "eid")
  val animProgress by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
          label = "progress",
      )

  Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    // Falling confetti with wave motion
    particles.forEach { particle ->
      val y = (particle.y + animProgress * particle.speed) % 1.5f
      val wave = sin(animProgress * 6.28f * 2 + particle.x * 15) * 0.03f
      val x = particle.x + wave
      val rotate = animProgress * 360f * particle.speed

      drawCircle(
          color = particle.color.copy(alpha = 0.8f),
          radius = particle.size * (0.7f + sin(rotate * 0.01f) * 0.3f),
          center = Offset(x = x * size.width, y = y * size.height),
      )
    }
  }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val color: Color,
)

private fun getChristmasMotivation(context: android.content.Context, year: Int): String {
  val messages =
      listOf(
          context.getString(R.string.holiday_christmas_motivation_1),
          context.getString(R.string.holiday_christmas_motivation_2),
          context.getString(R.string.holiday_christmas_motivation_3),
          context.getString(R.string.holiday_christmas_motivation_4),
      )
  return messages.random()
}

private fun getNewYearMotivation(context: android.content.Context, year: Int): String {
  val messages =
      listOf(
          context.getString(R.string.holiday_newyear_motivation_1, year),
          context.getString(R.string.holiday_newyear_motivation_2, year),
          context.getString(R.string.holiday_newyear_motivation_3, year),
          context.getString(R.string.holiday_newyear_motivation_4, year),
      )
  return messages.random()
}

private fun getRamadanMotivation(context: android.content.Context): String {
  val messages =
      listOf(
          context.getString(R.string.holiday_ramadan_motivation_1),
          context.getString(R.string.holiday_ramadan_motivation_2),
          context.getString(R.string.holiday_ramadan_motivation_3),
          context.getString(R.string.holiday_ramadan_motivation_4),
      )
  return messages.random()
}

private fun getEidFitriMotivation(context: android.content.Context): String {
  val messages =
      listOf(
          context.getString(R.string.holiday_eid_fitr_motivation_1),
          context.getString(R.string.holiday_eid_fitr_motivation_2),
          context.getString(R.string.holiday_eid_fitr_motivation_3),
          context.getString(R.string.holiday_eid_fitr_motivation_4),
      )
  return messages.random()
}
