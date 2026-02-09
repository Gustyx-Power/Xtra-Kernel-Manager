package id.xms.xtrakernelmanager.data.model

enum class HideAccessibilityTab(
    val key: String,
    val displayName: String,
    val description: String
) {
    APPS_TO_HIDE(
        key = "apps_to_hide",
        displayName = "Hide Apps Accessibility",
        description = "Apps to be hidden from accessibility detection"
    ),
    DETECTOR_APPS(
        key = "detector_apps", 
        displayName = "Accessibility detection apps",
        description = "Apps that will be blocked from detecting accessibility services"
    );
    
    companion object {
        fun fromKey(key: String): HideAccessibilityTab? {
            return values().find { it.key == key }
        }
    }
}

data class HideAccessibilityConfig(
    val isEnabled: Boolean = false,
    val currentTab: HideAccessibilityTab = HideAccessibilityTab.APPS_TO_HIDE,
    val appsToHide: Set<String> = emptySet(), 
    val detectorApps: Set<String> = emptySet(), 
    val isLSPosedModuleActive: Boolean = false
)