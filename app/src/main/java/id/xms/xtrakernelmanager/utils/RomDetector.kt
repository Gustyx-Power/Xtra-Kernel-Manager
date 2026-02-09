package id.xms.xtrakernelmanager.utils

import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager

/**
 * ROM Detection utility for identifying specific ROM types and their capabilities
 */
object RomDetector {
    private const val TAG = "RomDetector"
    
    // Shimoku ROM Properties
    private const val VIP_COMMUNITY_PROP = "persist.sys.oosexgang.vip.community"
    private const val SHIMOKU_ROM_PROP = "ro.build.flavor"
    private const val SHIMOKU_BUILD_PROP = "ro.product.system.brand"
    
    /**
     * Detect if current ROM is Shimoku ROM
     * Checks multiple properties to ensure accurate detection
     */
    suspend fun isShimokuRom(): Boolean {
        return try {
            val vipCommunity = RootManager.getProp(VIP_COMMUNITY_PROP)
            val isVip = vipCommunity.equals("true", ignoreCase = true) || vipCommunity == "1"
            val buildFlavor = RootManager.getProp(SHIMOKU_ROM_PROP)
            val systemBrand = RootManager.getProp(SHIMOKU_BUILD_PROP)
            val touchBoostProp = RootManager.getProp("persist.sys.oosexgang.tch.boost")
            val playIntegrityProp = RootManager.getProp("persist.sys.oosexgang.pi.fix")
            val hasShimokuProps = touchBoostProp.isNotEmpty() || playIntegrityProp.isNotEmpty()
            
            val isShimoku = isVip && hasShimokuProps
            
            Log.d(TAG, "Shimoku ROM Detection:")
            Log.d(TAG, "  VIP Community: $vipCommunity (isVip: $isVip)")
            Log.d(TAG, "  Build Flavor: $buildFlavor")
            Log.d(TAG, "  System Brand: $systemBrand")
            Log.d(TAG, "  Has Shimoku Props: $hasShimokuProps")
            Log.d(TAG, "  Result: $isShimoku")
            
            isShimoku
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting Shimoku ROM: ${e.message}")
            false
        }
    }
    
    suspend fun getRomInfo(): RomInfo {
        return try {
            val isShimoku = isShimokuRom()
            val buildFlavor = RootManager.getProp("ro.build.flavor")
            val systemBrand = RootManager.getProp("ro.product.system.brand")
            val buildVersion = RootManager.getProp("ro.build.version.release")
            
            RomInfo(
                isShimokuRom = isShimoku,
                buildFlavor = buildFlavor,
                systemBrand = systemBrand,
                androidVersion = buildVersion,
                displayName = if (isShimoku) "Shimoku ROM" else "Generic ROM"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ROM info: ${e.message}")
            RomInfo(
                isShimokuRom = false,
                buildFlavor = "Unknown",
                systemBrand = "Unknown",
                androidVersion = "Unknown",
                displayName = "Unknown ROM"
            )
        }
    }
}

data class RomInfo(
    val isShimokuRom: Boolean,
    val buildFlavor: String,
    val systemBrand: String,
    val androidVersion: String,
    val displayName: String
)