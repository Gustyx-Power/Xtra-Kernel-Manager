package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

/**
 * Liquid Quick Apps Panel - Glassmorphism Light Mode
 */
@Composable
fun LiquidQuickAppsPanel(
    quickApps: List<QuickAppData>,
    onAppClick: (String) -> Unit,
    onAppLongClick: (String) -> Unit,
    onAddAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF007AFF), // iOS Blue
) {
  Column(
      modifier = modifier.fillMaxSize(), 
      verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = "Quick Apps", 
          fontSize = 14.sp, 
          fontWeight = FontWeight.Bold, 
          color = Color(0xFF1E293B)
      )
      Text(
          text = "${quickApps.size} apps", 
          fontSize = 11.sp, 
          color = Color(0xFF64748B)
      )
    }

    Text(
        text = "Tap untuk buka, tahan untuk hapus",
        fontSize = 10.sp,
        color = Color(0xFF64748B).copy(alpha = 0.8f),
    )

    // Apps List
    if (quickApps.isEmpty()) {
      // Empty state
      Box(
          modifier = Modifier.fillMaxWidth().weight(1f), 
          contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
              Icons.Default.Apps,
              contentDescription = null,
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(40.dp),
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              text = "Belum ada quick apps", 
              fontSize = 12.sp, 
              color = Color(0xFF64748B)
          )
          Text(
              text = "Tap + untuk menambahkan",
              fontSize = 10.sp,
              color = Color(0xFF94A3B8),
          )
        }
      }
    } else {
      LazyColumn(
          modifier = Modifier.weight(1f), 
          verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(quickApps) { app ->
          LiquidQuickAppListItem(
              appData = app,
              onClick = { onAppClick(app.packageName) },
              onLongClick = { onAppLongClick(app.packageName) },
              accentColor = accentColor,
          )
        }
      }
    }

    // Add App Button
    LiquidAddQuickAppButton(onClick = onAddAppClick, accentColor = accentColor)
  }
}

/** Liquid Quick App List Item */
@Composable
private fun LiquidQuickAppListItem(
    appData: QuickAppData,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    accentColor: Color,
) {
  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 14.dp,
      backgroundColor = Color.White.copy(alpha = 0.35f),
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Row(
        modifier = Modifier
            .pointerInput(Unit) {
              detectTapGestures(
                  onTap = { onClick() }, 
                  onLongPress = { onLongClick() }
              )
            }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      // App Icon
      GlassmorphicCardLight(
          modifier = Modifier.size(36.dp),
          cornerRadius = 10.dp,
          backgroundColor = Color.White.copy(alpha = 0.4f),
          borderColor = Color.White.copy(alpha = 0.6f),
      ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
          appData.icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                contentDescription = appData.appName,
                modifier = Modifier.size(28.dp),
            )
          } ?: Icon(
              Icons.Default.Android,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp),
          )
        }
      }

      // App Name
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = appData.appName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Tap untuk floating", 
            fontSize = 9.sp, 
            color = Color(0xFF64748B)
        )
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
}

/** Liquid Add Quick App Button */
@Composable
private fun LiquidAddQuickAppButton(onClick: () -> Unit, accentColor: Color) {
  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 14.dp,
      backgroundColor = accentColor.copy(alpha = 0.15f),
      borderColor = accentColor.copy(alpha = 0.5f),
      borderWidth = 2.dp,
  ) {
    Row(
        modifier = Modifier
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
}

/**
 * Liquid App Picker Dialog
 */
@Composable
fun LiquidAppPickerDialog(
    isVisible: Boolean,
    availableApps: List<QuickAppData>,
    currentQuickApps: List<String>,
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color = Color(0xFF007AFF), // iOS Blue
) {
  if (isVisible) {
    Dialog(onDismissRequest = onDismiss) {
      GlassmorphicCardLightGradient(
          modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
          cornerRadius = 24.dp,
          backgroundColor = Color.White.copy(alpha = 0.95f),
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
                color = Color(0xFF1E293B),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
              Icon(
                  Icons.Default.Close, 
                  contentDescription = "Close", 
                  tint = Color(0xFF64748B)
              )
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
                  Text(
                      text = "Semua apps sudah ditambahkan", 
                      fontSize = 12.sp, 
                      color = Color(0xFF64748B)
                  )
                }
              }
            } else {
              items(filteredApps) { app ->
                LiquidAppPickerItem(
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

/** Liquid App Picker Item */
@Composable
private fun LiquidAppPickerItem(
    appData: QuickAppData, 
    onClick: () -> Unit, 
    accentColor: Color
) {
  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 12.dp,
      backgroundColor = Color.White.copy(alpha = 0.5f),
      borderColor = Color.White.copy(alpha = 0.7f),
  ) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      // App Icon
      GlassmorphicCardLight(
          modifier = Modifier.size(40.dp),
          cornerRadius = 10.dp,
          backgroundColor = Color.White.copy(alpha = 0.6f),
          borderColor = Color.White.copy(alpha = 0.8f),
      ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
          appData.icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                contentDescription = appData.appName,
                modifier = Modifier.size(32.dp),
            )
          } ?: Icon(
              Icons.Default.Android,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(28.dp),
          )
        }
      }

      // App Name
      Text(
          text = appData.appName,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF1E293B),
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
}

/** Liquid Remove Quick App Confirmation Dialog */
@Composable
fun LiquidRemoveQuickAppDialog(
    isVisible: Boolean,
    appName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
  if (isVisible) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White.copy(alpha = 0.95f),
        title = { 
          Text(text = "Hapus Quick App?", color = Color(0xFF1E293B)) 
        },
        text = { 
          Text(
              text = "Hapus $appName dari daftar quick apps?", 
              color = Color(0xFF64748B)
          ) 
        },
        confirmButton = {
          TextButton(onClick = onConfirm) { 
            Text("Hapus", color = Color(0xFFEF4444)) 
          }
        },
        dismissButton = { 
          TextButton(onClick = onDismiss) { 
            Text("Batal", color = Color(0xFF64748B)) 
          } 
        },
    )
  }
}
