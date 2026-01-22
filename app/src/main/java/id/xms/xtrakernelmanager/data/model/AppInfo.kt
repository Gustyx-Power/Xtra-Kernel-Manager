package id.xms.xtrakernelmanager.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    var isSelected: Boolean = false,
)
