package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.runtime.Immutable

@Immutable
data class TeamMember(
    val imageRes: Int,
    val name: String,
    val role: String,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
    val githubUsername: String? = null,
    val shapeIndex: Int = 0,
)
