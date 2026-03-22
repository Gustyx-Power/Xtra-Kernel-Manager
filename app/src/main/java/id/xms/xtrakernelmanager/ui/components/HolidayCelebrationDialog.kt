package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun HolidayCelebrationDialog(holiday: Holiday, year: Int, onDismiss: () -> Unit) {
  val context = LocalContext.current

  val motivationalMessage =
      remember(holiday, year) {
        when (holiday) {
          Holiday.CHRISTMAS -> getChristmasMotivation(context, year)
          Holiday.NEW_YEAR -> getNewYearMotivation(context, year)
          Holiday.RAMADAN -> getRamadanMotivation(context)
          Holiday.EID_FITR -> getEidFitriMotivation(context)
        }
      }

  val (gradientColors, icon, accentColor) = when (holiday) {
    Holiday.CHRISTMAS -> Triple(
      listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)),
      Icons.Rounded.Celebration,
      Color(0xFFD32F2F)
    )
    Holiday.NEW_YEAR -> Triple(
      listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)),
      Icons.Rounded.AutoAwesome,
      Color(0xFFFFD700)
    )
    Holiday.RAMADAN -> Triple(
      listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2)),
      Icons.Rounded.NightlightRound,
      Color(0xFFFFD700)
    )
    Holiday.EID_FITR -> Triple(
      listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047)),
      Icons.Rounded.Celebration,
      Color(0xFFFFD700)
    )
  }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(
        dismissOnBackPress = true, 
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
      ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.7f))
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      when (holiday) {
        Holiday.CHRISTMAS -> ChristmasAnimation()
        Holiday.NEW_YEAR -> FireworksAnimation()
        Holiday.RAMADAN -> RamadanAnimation()
        Holiday.EID_FITR -> EidFitriAnimation()
      }

      Card(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
          shape = RoundedCornerShape(32.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color.White
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
      ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .background(
                Brush.verticalGradient(gradientColors)
              ),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(80.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                AnimatedIcon(icon, accentColor)
              }

              Text(
                  text = when (holiday) {
                    Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_title_clean)
                    Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_title_clean, year)
                    Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_title_clean)
                    Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_title_clean)
                  },
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  textAlign = TextAlign.Center,
              )
            }
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Text(
                text = when (holiday) {
                  Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_subtitle_clean)
                  Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_subtitle_clean)
                  Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_subtitle_clean)
                  Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_subtitle_clean)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
            )

            Divider(
              modifier = Modifier.width(60.dp),
              thickness = 3.dp,
              color = accentColor
            )

            Text(
                text = motivationalMessage,
                fontSize = 15.sp,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = gradientColors[1]
                ),
            ) {
              Text(
                  text = when (holiday) {
                    Holiday.CHRISTMAS -> stringResource(R.string.holiday_christmas_button_clean)
                    Holiday.NEW_YEAR -> stringResource(R.string.holiday_newyear_button_clean)
                    Holiday.RAMADAN -> stringResource(R.string.holiday_ramadan_button_clean)
                    Holiday.EID_FITR -> stringResource(R.string.holiday_eid_fitr_button_clean)
                  },
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
              )
            }

            Text(
                text = stringResource(R.string.holiday_from_xkm),
                fontSize = 13.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AnimatedIcon(icon: ImageVector, tintColor: Color) {
  val infiniteTransition = rememberInfiniteTransition(label = "icon")

  val scale by infiniteTransition.animateFloat(
      initialValue = 0.9f,
      targetValue = 1.1f,
      animationSpec = infiniteRepeatable(
          animation = tween(1000, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
      ),
      label = "scale",
  )

  Icon(
    imageVector = icon,
    contentDescription = null,
    modifier = Modifier.size((48 * scale).dp),
    tint = Color.White
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
          color = listOf(
            Color(0xFFFF0000),
            Color(0xFF00FF00),
            Color(0xFFFFFFFF),
            Color(0xFFFFD700),
          ).random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "snow")
  val animProgress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
      label = "progress",
  )

  Canvas(modifier = Modifier.fillMaxSize()) {
    particles.forEach { particle ->
      val y = (particle.y + animProgress * particle.speed * 2) % 1.2f
      val x = particle.x + sin(animProgress * 6.28f + particle.x * 10) * 0.02f

      drawCircle(
          color = particle.color.copy(alpha = 0.6f),
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
          color = listOf(
            Color(0xFFFFD700),
            Color(0xFFFF6B6B),
            Color(0xFF4ECDC4),
            Color(0xFFFFE66D),
            Color(0xFF95E1D3),
            Color(0xFFDDA0DD),
          ).random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
  val animProgress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
      label = "progress",
  )

  Canvas(modifier = Modifier.fillMaxSize()) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    particles.forEachIndexed { index, particle ->
      val angle = (index.toFloat() / particles.size) * 6.28f
      val radius = animProgress * size.minDimension * 0.4f * particle.speed
      val alpha = (1f - animProgress).coerceIn(0f, 0.7f)

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
  val particles = remember {
    List(40) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat(),
          size = Random.nextFloat() * 4f + 2f,
          speed = Random.nextFloat() * 0.5f + 0.3f,
          color = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFF8E1),
            Color(0xFFFFFFFF),
            Color(0xFFFFC107),
          ).random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "ramadan")
  val animProgress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing)),
      label = "progress",
  )

  Canvas(modifier = Modifier.fillMaxSize()) {
    particles.forEach { particle ->
      val twinkle = (sin(animProgress * 6.28f + particle.x * 20) + 1f) / 2f
      val alpha = 0.3f + twinkle * 0.4f

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
  val particles = remember {
    List(60) {
      Particle(
          x = Random.nextFloat(),
          y = Random.nextFloat() * 2f - 0.5f,
          size = Random.nextFloat() * 6f + 3f,
          speed = Random.nextFloat() * 0.8f + 0.4f,
          color = listOf(
            Color(0xFFFFD700),
            Color(0xFF4CAF50),
            Color(0xFFFFFFFF),
            Color(0xFFFFC107),
            Color(0xFF8BC34A),
            Color(0xFFFF9800),
          ).random(),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "eid")
  val animProgress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
      label = "progress",
  )

  Canvas(modifier = Modifier.fillMaxSize()) {
    particles.forEach { particle ->
      val y = (particle.y + animProgress * particle.speed) % 1.5f
      val wave = sin(animProgress * 6.28f * 2 + particle.x * 15) * 0.03f
      val x = particle.x + wave
      val rotate = animProgress * 360f * particle.speed

      drawCircle(
          color = particle.color.copy(alpha = 0.7f),
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
