package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetupFeatureCard(
  icon: ImageVector,
  title: String,
  description: String
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF1A1F2E),
    tonalElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .background(
            Color(0xFF2D3548),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = Color(0xFF38BDF8),
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF94A3B8),
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
fun SetupPageIndicator(
  currentPage: Int,
  pageCount: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(pageCount) { index ->
      Box(
        modifier = Modifier
          .width(if (index == currentPage) 24.dp else 8.dp)
          .height(8.dp)
          .background(
            color = if (index == currentPage) Color(0xFF38BDF8) else Color(0xFF1E293B),
            shape = RoundedCornerShape(4.dp)
          )
      )
    }
  }
}

@Composable
fun SetupPermissionCard(
  icon: ImageVector,
  title: String,
  description: String,
  isGranted: Boolean,
  onClick: (() -> Unit)?
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF1A1F2E),
    tonalElevation = 2.dp,
    onClick = { onClick?.invoke() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .background(
            if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF94A3B8),
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
      }
      
      Spacer(modifier = Modifier.width(12.dp))
      
      Box(
        modifier = Modifier
          .size(12.dp)
          .background(
            if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
            CircleShape
          )
      )
    }
  }
}


@Composable
fun SetupStyleCard(
  title: String,
  description: String,
  isSelected: Boolean,
  isEnabled: Boolean,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF1A1F2E),
    tonalElevation = 2.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isEnabled) Color.White else Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
          )
          if (!isEnabled) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "(Android 10+)",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B),
              fontSize = 11.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isEnabled) Color(0xFF94A3B8) else Color(0xFF64748B),
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
      }
      
      Spacer(modifier = Modifier.width(12.dp))
      
      Box(
        modifier = Modifier
          .size(24.dp)
          .background(
            if (isSelected) Color(0xFF38BDF8) else Color(0xFF2D3548),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Selected",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
