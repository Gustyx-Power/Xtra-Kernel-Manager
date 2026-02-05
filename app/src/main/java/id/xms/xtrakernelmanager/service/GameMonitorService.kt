package id.xms.xtrakernelmanager.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.*
import org.json.JSONArray

class GameMonitorService : AccessibilityService() {

  companion object {
    private const val TAG = "GameMonitorService"
    private const val CHANNEL_ID = "game_monitor_channel"
    private const val NOTIFICATION_ID = 2001
  }

  private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private lateinit var preferencesManager: PreferencesManager
  private var enabledGamePackages: Set<String> = emptySet()
  private var bankingModeEnabled: Boolean = false

  // Cache last package to avoid redundant checks/logs
  private var lastPackageName: String = ""

  override fun onServiceConnected() {
    super.onServiceConnected()
    Log.d(TAG, "Accessibility Service Connected")
    
    // CRITICAL: Start foreground immediately to prevent crash
    createNotificationChannel()
    startForegroundImmediately()
    
    preferencesManager = PreferencesManager(applicationContext)

    // Load initial games list and banking mode
    serviceScope.launch { 
      loadGameList()
      loadBankingMode()
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Game Monitor Service",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Monitors game apps for automatic overlay activation"
        setShowBadge(false)
        setSound(null, null)
      }
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun startForegroundImmediately() {
    try {
      val notification = createNotification()
      // Always call startForeground for AccessibilityService to prevent crashes
      startForeground(NOTIFICATION_ID, notification)
      Log.d(TAG, "Started foreground service successfully")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start foreground", e)
    }
  }

  private fun createNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Game Monitor Active")
      .setContentText("Monitoring for game apps")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setOngoing(true)
      .setSilent(true)
      .build()
  }

  private fun createBankingModeNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("XKM - Banking Mode")
      .setContentText("Accessibility disabled for banking security")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setOngoing(true)
      .setSilent(true)
      .build()
  }

  private var stopJob: Job? = null

  // Whitelist of system packages that shouldn't unwantedly kill the overlay
  private val ignoredPackages =
      setOf(
          "com.android.systemui",
          "android",
          "com.google.android.inputmethod.latin", // Gboard
          "com.google.android.permissioncontroller",
          "com.android.permissioncontroller",
          "com.google.android.packageinstaller",
          "com.android.packageinstaller",
          "com.google.android.gms",
          "com.google.android.play.games",
          "com.android.vending",
          "com.google.android.webview",
          "com.xiaomi.xmsf", // Xiaomi service framework
          "com.miui.securitycenter", // MIUI Security
          "id.xms.xtrakernelmanager", // Self
          "id.xms.xtrakernelmanager.dev", // Self (debug)
      )

  // Banking apps whitelist - disable overlay completely when these apps are active
  private val bankingPackages = setOf(
      // Indonesian Banks
      "com.bni.mobile", // BNI Mobile Banking
      "com.bri.brimo", // BRImo
      "com.bca", // BCA mobile
      "com.bca.mybca", // myBCA
      "com.mandiri.mandirionline", // Mandiri Online
      "com.bankmandiri.livin", // Livin' by Mandiri
      "com.bankmandiri.livin.merchant", // Livin' Merchant
      "id.co.bankbkemobile.digitalbank", // SeaBank
      "com.cimbniaga.mobile.cimbgo", // CIMB Go
      "com.danamon.dbmobile", // D-Bank Pro
      "com.btpn.wow", // Jenius
      "com.ocbc.mobile", // OCBC mobile
      "com.maybank2u.m2umobile", // Maybank2u
      "com.permatabank.mobile", // PermataBank Mobile
      "com.panin.android.bankingnew", // Panin Mobile
      "com.hsbc.hsbcindonesia", // HSBC Indonesia
      "com.uob.mighty", // UOB Mighty
      "com.standardchartered.mobile.id", // SC Mobile Indonesia
      "com.citibank.mobile.id", // Citi Mobile
      
      // E-Wallets & Payment
      "com.gojek.app", // Gojek
      "com.grab.passenger", // Grab
      "ovo.id", // OVO
      "com.dana.id", // DANA
      "com.telkom.mwallet", // LinkAja
      "com.shopee.id", // ShopeePay
      "com.tokopedia.tkpd", // Tokopedia
      "com.bukalapak.android", // Bukalapak
      "com.lazada.android", // Lazada
      "com.blibli.mobile", // Blibli
      
      // International Banks
      "com.chase.sig.android", // Chase
      "com.bankofamerica.mobile", // Bank of America
      "com.wellsfargo.mobile.android", // Wells Fargo
      "com.citi.citimobile", // Citibank
      "com.usbank.mobilebanking", // US Bank
      "com.capitalone.bank", // Capital One
      "com.ally.MobileBanking", // Ally Bank
      "com.schwab.mobile", // Charles Schwab
      "com.fidelity.android", // Fidelity
      "com.etrade.mobilepro.activity", // E*TRADE
      "com.paypal.android.p2pmobile", // PayPal
      "com.venmo", // Venmo
      "com.squareup.cash", // Cash App
      "com.coinbase.android", // Coinbase
      "com.robinhood.android", // Robinhood
      
      // European Banks
      "com.revolut.revolut", // Revolut
      "com.n26.gk", // N26
      "com.starlingbank.android", // Starling Bank
      "com.monzo.monzo", // Monzo
      "uk.co.santander.santanderUK", // Santander UK
      "com.barclays.android.barclaysmobilebanking", // Barclays
      "com.rbs.mobile.android.natwest", // NatWest
      "uk.co.hsbc.hsbcukmobilebanking", // HSBC UK
      "com.lloydsbank.businessmobile", // Lloyds Bank
      
      // Asian Banks
      "com.dbs.sg.dbsmbanking", // DBS Singapore
      "com.ocbc.mobile", // OCBC Singapore
      "com.uob.mighty.sg", // UOB Singapore
      "hk.com.hsbc.hsbchkmobilebanking", // HSBC Hong Kong
      "com.sc.mobilebanking.hk", // Standard Chartered HK
      "com.hangseng.rbmobile", // Hang Seng Bank
      "jp.co.smbc.direct", // SMBC Japan
      "jp.co.netbk.smartplus", // Sumitomo Mitsui
      "com.mizuho_bk.smart", // Mizuho Bank
      
      // Crypto & Investment
      "com.binance.dev", // Binance
      "com.crypto.multiwallet", // Crypto.com
      "com.kucoin.android", // KuCoin
      "com.bittrex.trade", // Bittrex
      "com.kraken.trade", // Kraken
      "com.gemini.android.app", // Gemini
      "com.blockfolio.blockfolio", // FTX (Blockfolio)
      "com.plaid.link", // Plaid
      "com.mint", // Mint
      "com.personalcapital.pcapandroid", // Personal Capital
      "com.ynab.evergreen.app", // YNAB
      
      // Government & Official Apps
      "com.kemenkeu.djp", // DJP Online (Indonesia Tax)
      "id.go.bpjsketenagakerjaan.mobile", // BPJS Ketenagakerjaan
      "id.go.bpjskesehatan.mobile", // BPJS Kesehatan
      "com.pertamina.myperta", // MyPertamina
      "com.pln.mobile", // PLN Mobile
      "com.telkom.indihome", // IndiHome
      "com.irs2goapp", // IRS2Go (US Tax)
      "gov.irs", // IRS Official
      "com.ssa.mobile.android.app", // Social Security (US)
      "uk.gov.hmrc.ptcalc", // HMRC (UK Tax)
  )

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null) return

    // If banking mode is enabled, ignore all events
    if (bankingModeEnabled) {
      Log.d(TAG, "Banking mode enabled - ignoring accessibility events")
      return
    }

    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      val packageName = event.packageName?.toString() ?: return

      if (packageName == lastPackageName) return
      lastPackageName = packageName

      Log.d(TAG, "Window changed: $packageName")

      // PRIORITY 1: Banking apps - immediately disable overlay and stop monitoring
      if (bankingPackages.contains(packageName)) {
        Log.d(TAG, "Banking app detected: $packageName. Disabling overlay completely.")
        stopJob?.cancel()
        stopJob = null
        stopGameOverlay()
        // Optionally disable the accessibility service temporarily
        disableServiceTemporarily()
        return
      }

      if (enabledGamePackages.contains(packageName)) {
        Log.d(TAG, "Game detected: $packageName. Ensuring Overlay is ON.")
        stopJob?.cancel()
        stopJob = null
        startGameOverlay()
      } else if (ignoredPackages.contains(packageName) || packageName.contains("inputmethod")) {
        // Ignore system UI, keyboards, Google services
        Log.d(TAG, "Ignoring system/transient package: $packageName")
        stopJob?.cancel()
        stopJob = null
      } else {
        // Potential exit - Stopping immediately as requested
        Log.d(TAG, "Non-game package detected: $packageName. Stopping overlay.")

        stopJob?.cancel()
        stopJob = null
        stopGameOverlay()
      }
    }
  }

  override fun onInterrupt() {
    Log.d(TAG, "Accessibility Service Interrupted")
  }

  private suspend fun loadGameList() {
    try {
      preferencesManager.getGameApps().collect { json ->
        enabledGamePackages = parseEnabledGamePackages(json)
        Log.d(TAG, "Updated game list: ${enabledGamePackages.size} games")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error collecting game list", e)
    }
  }

  private suspend fun loadBankingMode() {
    try {
      preferencesManager.getBankingModeEnabled().collect { enabled ->
        bankingModeEnabled = enabled
        Log.d(TAG, "Banking mode: ${if (enabled) "ENABLED" else "DISABLED"}")
        
        // Update notification based on banking mode
        if (enabled) {
          val bankingNotification = createBankingModeNotification()
          val notificationManager = getSystemService(NotificationManager::class.java)
          notificationManager.notify(NOTIFICATION_ID, bankingNotification)
        } else {
          val normalNotification = createNotification()
          val notificationManager = getSystemService(NotificationManager::class.java)
          notificationManager.notify(NOTIFICATION_ID, normalNotification)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error collecting banking mode", e)
    }
  }

  private fun parseEnabledGamePackages(json: String): Set<String> {
    return try {
      val jsonArray = JSONArray(json)
      val packages = mutableSetOf<String>()
      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.opt(i)
        when (item) {
          is String -> {
            // Old format: simple string
            packages.add(item)
          }
          is org.json.JSONObject -> {
            // New format: JSON object with enabled flag
            val enabled = item.optBoolean("enabled", true)
            if (enabled) {
              packages.add(item.optString("packageName"))
            }
          }
        }
      }
      packages
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse game apps: ${e.message}")
      emptySet()
    }
  }

  private fun startGameOverlay() {
    try {
      val intent = Intent(applicationContext, GameOverlayService::class.java)
      startService(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start overlay service", e)
    }
  }

  private fun stopGameOverlay() {
    try {
      val intent = Intent(applicationContext, GameOverlayService::class.java)
      stopService(intent)
    } catch (e: Exception) {
      // Ignore if not running
      Log.e(TAG, "Failed to stop overlay service", e)
    }
  }

  private fun disableServiceTemporarily() {
    try {
      // Send broadcast to temporarily disable the service
      val intent = Intent("id.xms.xtrakernelmanager.BANKING_APP_DETECTED")
      intent.setPackage(packageName)
      sendBroadcast(intent)
      
      Log.d(TAG, "Banking app detected - service temporarily disabled")
      
      // Update notification to show banking mode
      val bankingNotification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("XKM - Banking Mode")
        .setContentText("Accessibility disabled for banking security")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setSilent(true)
        .build()
      
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.notify(NOTIFICATION_ID, bankingNotification)
      
    } catch (e: Exception) {
      Log.e(TAG, "Failed to disable service temporarily", e)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    stopGameOverlay()
    Log.d(TAG, "Service Destroyed")
    
    // Try to restart service if killed by system
    try {
      val restartIntent = Intent(applicationContext, GameMonitorService::class.java)
      restartIntent.setPackage(packageName)
      sendBroadcast(restartIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send restart broadcast", e)
    }
  }
  
  override fun onUnbind(intent: Intent?): Boolean {
    Log.d(TAG, "Service Unbound - attempting to stay alive")
    // Return true to indicate we want onRebind to be called
    return true
  }
  
  override fun onRebind(intent: Intent?) {
    super.onRebind(intent)
    Log.d(TAG, "Service Rebound")
    startForegroundImmediately()
  }
}
