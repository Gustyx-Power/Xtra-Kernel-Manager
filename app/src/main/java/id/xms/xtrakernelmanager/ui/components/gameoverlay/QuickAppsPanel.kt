package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap

/** Quick App Data Model */
data class QuickAppData(val packageName: String, val appName: String, val icon: Drawable?)

/**
 * Quick Apps Panel
 *
 * Panel showing configured quick apps with:
 * - App list with icons
 * - Launch on tap
 * - Remove on long press
 * - Add app button
 */
@Composable
fun QuickAppsPanel(
    quickApps: List<QuickAppData>,
    onAppClick: (String) -> Unit,
    onAppLongClick: (String) -> Unit,
    onAddAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = "Quick Apps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
      Text(text = "${quickApps.size} apps", fontSize = 11.sp, color = Color.Gray)
    }

    Text(
        text = "Tap untuk buka, tahan untuk hapus",
        fontSize = 10.sp,
        color = Color.Gray.copy(alpha = 0.7f),
    )

    // Apps List
    if (quickApps.isEmpty()) {
      // Empty state
      Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
              Icons.Default.Apps,
              contentDescription = null,
              tint = Color.Gray,
              modifier = Modifier.size(40.dp),
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Belum ada quick apps", fontSize = 12.sp, color = Color.Gray)
          Text(
              text = "Tap + untuk menambahkan",
              fontSize = 10.sp,
              color = Color.Gray.copy(alpha = 0.7f),
          )
        }
      }
    } else {
      LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(quickApps) { app ->
          QuickAppListItem(
              appData = app,
              onClick = { onAppClick(app.packageName) },
              onLongClick = { onAppLongClick(app.packageName) },
              accentColor = accentColor,
          )
        }
      }
    }

    // Add App Button
    AddQuickAppButton(onClick = onAddAppClick, accentColor = accentColor)
  }
}

/** Quick App List Item */
@Composable
private fun QuickAppListItem(
    appData: QuickAppData,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    accentColor: Color,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF1A1A1A))
              .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
              .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongClick() })
              }
              .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // App Icon
    Box(
        modifier =
            Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center,
    ) {
      appData.icon?.let { drawable ->
        Image(
            bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
            contentDescription = appData.appName,
            modifier = Modifier.size(28.dp),
        )
      }
          ?: Icon(
              Icons.Default.Android,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp),
          )
    }

    // App Name
    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = appData.appName,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
      Text(text = "Tap untuk floating", fontSize = 9.sp, color = Color.Gray)
    }

    // Launch indicator
    Icon(
        Icons.Default.OpenInNew,
        contentDescription = "Launch",
        tint = accentColor,
        modifier = Modifier.size(18.dp),
    )
  }
}

/** Add Quick App Button */
@Composable
private fun AddQuickAppButton(onClick: () -> Unit, accentColor: Color) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(accentColor.copy(alpha = 0.1f))
              .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .clickable { onClick() }
              .padding(12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        Icons.Default.Add,
        contentDescription = "Add App",
        tint = accentColor,
        modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = "Tambah Quick App",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = accentColor,
    )
  }
}

/**
 * App Picker Dialog
 *
 * Dialog for selecting apps to add to quick apps
 */
@Composable
fun AppPickerDialog(
    isVisible: Boolean,
    availableApps: List<QuickAppData>,
    currentQuickApps: List<String>,
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  if (isVisible) {
    Dialog(onDismissRequest = onDismiss) {
      Card(
          modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
          shape = RoundedCornerShape(16.dp),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Header
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = "Pilih Aplikasi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Apps List
          LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val filteredApps = availableApps.filter { it.packageName !in currentQuickApps }

            if (filteredApps.isEmpty()) {
              item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                  Text(text = "Semua apps sudah ditambahkan", fontSize = 12.sp, color = Color.Gray)
                }
              }
            } else {
              items(filteredApps) { app ->
                AppPickerItem(
                    appData = app,
                    onClick = {
                      onAppSelected(app.packageName)
                      onDismiss()
                    },
                    accentColor = accentColor,
                )
              }
            }
          }
        }
      }
    }
  }
}

/** App Picker Item */
@Composable
private fun AppPickerItem(appData: QuickAppData, onClick: () -> Unit, accentColor: Color) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF2A2A2A))
              .clickable { onClick() }
              .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // App Icon
    Box(
        modifier =
            Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF3A3A3A)),
        contentAlignment = Alignment.Center,
    ) {
      appData.icon?.let { drawable ->
        Image(
            bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
            contentDescription = appData.appName,
            modifier = Modifier.size(32.dp),
        )
      }
          ?: Icon(
              Icons.Default.Android,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(28.dp),
          )
    }

    // App Name
    Text(
        text = appData.appName,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )

    // Add icon
    Icon(
        Icons.Default.AddCircle,
        contentDescription = "Add",
        tint = accentColor,
        modifier = Modifier.size(24.dp),
    )
  }
}

/** Remove Quick App Confirmation Dialog */
@Composable
fun RemoveQuickAppDialog(
    isVisible: Boolean,
    appName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
  if (isVisible) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text(text = "Hapus Quick App?", color = Color.White) },
        text = { Text(text = "Hapus $appName dari daftar quick apps?", color = Color.Gray) },
        confirmButton = {
          TextButton(onClick = onConfirm) { Text("Hapus", color = Color(0xFFF44336)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } },
    )
  }
}
