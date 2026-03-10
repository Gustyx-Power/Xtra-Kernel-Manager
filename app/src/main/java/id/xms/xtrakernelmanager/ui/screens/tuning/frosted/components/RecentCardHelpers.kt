package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Helper function to get display resolution
@Composable
fun getDisplayResolution(): String {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels
    
    // Get the larger dimension as height (portrait orientation standard)
    val displayWidth = minOf(width, height)
    val displayHeight = maxOf(width, height)
    
    return "${displayWidth}x${displayHeight}"
}

// Helper function to get max refresh rate
@Composable
fun getMaxRefreshRate(): Int {
    val context = LocalContext.current
    val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
    
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            val display = context.display
            val mode = display?.mode
            mode?.refreshRate?.toInt() ?: 60
        } else {
            // Android 10 and below
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            display.refreshRate.toInt()
        }
    } catch (e: Exception) {
        60 // Default fallback
    }
}

// Helper function to determine process node based on SOC
fun getProcessNode(platform: String, chipname: String): String {
    return when {
        // Snapdragon 8 Gen 3 - 4nm
        platform.contains("pineapple") || chipname.contains("8650") || chipname.contains("sm8650") -> "4nm"
        
        // Snapdragon 8 Gen 2 - 4nm
        platform.contains("kalama") || chipname.contains("8550") || chipname.contains("sm8550") -> "4nm"
        
        // Snapdragon 8 Gen 1 - 4nm
        platform.contains("taro") || chipname.contains("8475") || chipname.contains("sm8475") -> "4nm"
        platform.contains("waipio") || chipname.contains("8450") || chipname.contains("sm8450") -> "4nm"
        
        // Snapdragon 888 - 5nm
        platform.contains("lahaina") || chipname.contains("888") || chipname.contains("sm8350") -> "5nm"
        
        // Snapdragon 870/865 - 7nm
        platform.contains("kona") || chipname.contains("865") || chipname.contains("870") || chipname.contains("sm8250") -> "7nm"
        
        // Snapdragon 855 - 7nm
        platform.contains("msmnile") || chipname.contains("855") || chipname.contains("sm8150") -> "7nm"
        
        // Snapdragon 845 - 10nm
        platform.contains("sdm845") || chipname.contains("845") -> "10nm"
        
        // Snapdragon 7 series Gen 3 - 4nm
        platform.contains("parrot") || chipname.contains("7s") && chipname.contains("gen3") -> "4nm"
        
        // Snapdragon 7 series Gen 2 - 4nm
        chipname.contains("7+") && chipname.contains("gen2") -> "4nm"
        chipname.contains("7") && chipname.contains("gen2") -> "4nm"
        
        // Snapdragon 7 series Gen 1 - 4nm
        chipname.contains("7+") && chipname.contains("gen1") -> "4nm"
        chipname.contains("7") && chipname.contains("gen1") -> "4nm"
        
        // Snapdragon 6 series - 4nm/6nm
        chipname.contains("6") && chipname.contains("gen1") -> "4nm"
        chipname.contains("695") || chipname.contains("690") -> "6nm"
        chipname.contains("680") || chipname.contains("685") -> "6nm"
        
        // MediaTek Dimensity 9000 series - 4nm
        chipname.contains("mt6985") || chipname.contains("9300") -> "4nm"
        chipname.contains("mt6983") || chipname.contains("9200") -> "4nm"
        chipname.contains("mt6893") || chipname.contains("9000") -> "4nm"
        
        // MediaTek Dimensity 8000 series - 4nm/6nm
        chipname.contains("mt6895") || chipname.contains("8200") -> "4nm"
        chipname.contains("mt6891") || chipname.contains("8100") -> "5nm"
        chipname.contains("mt6877") || chipname.contains("8050") -> "6nm"
        
        // MediaTek Dimensity 7000 series - 4nm/6nm
        chipname.contains("mt6879") || chipname.contains("7050") -> "6nm"
        chipname.contains("mt6878") || chipname.contains("7200") -> "4nm"
        
        // MediaTek Dimensity 6000 series - 6nm/7nm
        chipname.contains("mt6833") || chipname.contains("6020") -> "7nm"
        chipname.contains("mt6835") || chipname.contains("6080") -> "6nm"
        
        // Exynos 2400/2200 - 4nm
        chipname.contains("s5e9945") || chipname.contains("2400") -> "4nm"
        chipname.contains("s5e9925") || chipname.contains("2200") -> "4nm"
        
        // Exynos 2100/990 - 5nm/7nm
        chipname.contains("s5e9840") || chipname.contains("2100") -> "5nm"
        chipname.contains("s5e9830") || chipname.contains("990") -> "7nm"
        
        // Tensor G3/G2/G1 - 4nm/5nm
        chipname.contains("gs201") || chipname.contains("tensor") && chipname.contains("g3") -> "4nm"
        chipname.contains("gs101") || chipname.contains("tensor") && chipname.contains("g2") -> "5nm"
        chipname.contains("tensor") -> "5nm"
        
        // Default fallback
        else -> "N/A"
    }
}
