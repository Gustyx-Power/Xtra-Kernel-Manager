import org.apache.http.client.methods.HttpPost
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.util.Date
import java.text.SimpleDateFormat
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "2.3.5"
}

// Disable Google Services for debug build (allows different applicationId)
afterEvaluate {
    tasks.matching { it.name == "processDebugGoogleServices" }.configureEach {
        enabled = false
    }
}

android {
    namespace = "id.xms.xtrakernelmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.xms.xtrakernelmanager"
        minSdk = 31
        targetSdk = 36
        versionCode = 3
        versionName = "3.0-Release"

        // Build Parsing
        val buildDate = SimpleDateFormat("yyyy.MM.dd").format(Date())
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")

        vectorDrawables {
            useSupportLibrary = true
        }
        resConfigs("en", "in", "ru")
    }
    
    signingConfigs {
        create("release") {
            storeFile = project.findProperty("myKeystorePath")?.let { file(it) }
            storePassword = project.findProperty("myKeystorePassword") as String?
            keyAlias = project.findProperty("myKeyAlias") as String?
            keyPassword = project.findProperty("myKeyPassword") as String?
        }
    }

    buildTypes {
        debug {
            val gitHash = providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText.get().trim()

            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev-$gitHash"
            // Add debug metadata to help Play Protect identify as development build
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
            buildConfigField("boolean", "ENABLE_ACCESSIBILITY_SERVICE", "true")
            buildConfigField("boolean", "ENABLE_ROOT_FEATURES", "true")
            manifestPlaceholders["appLabel"] = "@string/app_name_short"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            ndk {
                debugSymbolLevel = "NONE"
            }
            // Play Protect compatibility optimizations
            buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
            buildConfigField("boolean", "ENABLE_ACCESSIBILITY_SERVICE", "true")
            buildConfigField("boolean", "ENABLE_ROOT_FEATURES", "false")
            buildConfigField("String", "BUILD_VARIANT", "\"release\"")
            buildConfigField("String", "DISTRIBUTION_CHANNEL", "\"sideload\"")
            manifestPlaceholders["appLabel"] = "@string/app_name_short"
            
            // Additional optimizations for Play Protect
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            isCrunchPngs = true
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        lint.disable.add("NullSafeMutableLiveData")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.properties"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.work:work-runtime-ktx:2.11.1")


    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2026.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.graphics:graphics-shapes:1.1.0")
    
    // Material 3 Expressive
    implementation("androidx.compose.material:material-icons-extended")
    // Simple Icons (Brands)
    implementation("br.com.devsrsouza.compose.icons:simple-icons:1.1.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // LibSu - Root Access
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:service:6.0.0")
    implementation("com.github.topjohnwu.libsu:nio:6.0.0")
    
    // Firebase 
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-database") 
    
    // Google Play Services (for Play Protect compatibility)
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-tasks:18.2.0") 

    // TOML Parser
    implementation("org.tomlj:tomlj:1.1.1")

    // Accompanist (for system UI controller)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    implementation("com.patrykandpatrick.vico:compose-m3:3.0.0-beta.3")

    // Backdrop (Liquid Glass)
    implementation("io.github.kyant0:backdrop:1.0.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    
    // YukiHookAPI - Xposed Module for Banking Hide Mode
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.highcapable.yukihookapi:api:1.3.1")
    ksp("com.highcapable.yukihookapi:ksp-xposed:1.3.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}


abstract class SendTelegramMessageTask : DefaultTask() {
    @get:Input abstract val telegramBotToken: Property<String>
    @get:Input abstract val telegramChatId: Property<String>
    @get:Input abstract val appVersionName: Property<String>
    @get:Input abstract val appPackageName: Property<String>
    @get:Input abstract val appProjectName: Property<String>
    @get:Input @get:Optional abstract val changelog: Property<String>

    init {
        telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
        telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
        appVersionName.convention("")
        appPackageName.convention("")
        appProjectName.convention(project.name)
        changelog.convention(project.findProperty("myChangelog")?.toString() ?: "")
    }

    @TaskAction
    fun sendMessage() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram Bot Token or Chat ID not found. Skipping message.")
            return
        }

        val buildStatus = if (project.gradle.taskGraph.allTasks.any { it.state.failure != null }) "FAILED" else "SUCCESS"
        val currentAppVersion = appVersionName.getOrElse(project.android.defaultConfig.versionName ?: "N/A")
        val currentAppPackage = appPackageName.getOrElse(project.android.defaultConfig.applicationId ?: "N/A")
        val currentProjectName = appProjectName.get()
        val kotlinVersion = project.getKotlinPluginVersion() ?: "N/A"

        fun sendTelegramMessage(text: String, disableNotification: Boolean = false): Int? {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
            val jsonPayload = """{"chat_id":"${telegramChatId.get()}","text":"${text.replace("\"", "\\\"")}","disable_notification":$disableNotification}"""
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                EntityUtils.consumeQuietly(response.entity)
                return "\\\"message_id\\\":(\\d+)".toRegex().find(responseBody)?.groupValues?.get(1)?.toIntOrNull()
            }
        }

        fun editTelegramMessage(messageId: Int, text: String) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/editMessageText"
            val jsonPayload = """{"chat_id":"${telegramChatId.get()}","message_id":$messageId,"text":"${text.replace("\"", "\\\"")}"}"""
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                httpClient.execute(post).entity?.let { EntityUtils.consumeQuietly(it) }
            }
        }

        fun pinTelegramMessage(messageId: Int) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/pinChatMessage"
            val jsonPayload = """{"chat_id":"${telegramChatId.get()}","message_id":$messageId,"disable_notification":true}"""
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                httpClient.execute(post).entity?.let { EntityUtils.consumeQuietly(it) }
            }
        }

        val buildMsgId = sendTelegramMessage("Processing build...", disableNotification = true)
        if (buildMsgId != null) pinTelegramMessage(buildMsgId)

        val javaVersion = JavaVersion.current().toString()
        val gradleVersion = project.gradle.gradleVersion
        val osName = System.getProperty("os.name")
        val osArch = System.getProperty("os.arch")

        val (processor, kernelInfo) = if (osName.contains("Windows", ignoreCase = true)) {
            val proc = try {
                val process = ProcessBuilder("cmd", "/c", "wmic cpu get name")
                    .redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output.lines().drop(1).firstOrNull()?.trim() ?: osArch
            } catch (e: Exception) {
                osArch
            }
            val kernel = try {
                val process = ProcessBuilder("cmd", "/c", "ver")
                    .redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output.trim()
            } catch (e: Exception) {
                "N/A"
            }
            Pair(proc, kernel)
        } else {
            val proc = try {
                val process = ProcessBuilder("cat", "/proc/cpuinfo").redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output.lines().find { it.startsWith("model name") }?.substringAfter(":")?.trim() ?: osArch
            } catch (e: Exception) {
                osArch
            }
            val kernel = try {
                val process = ProcessBuilder("uname", "-r").redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                output.ifEmpty { "N/A" }
            } catch (e: Exception) {
                "N/A"
            }
            Pair(proc, kernel)
        }

        val compileSdkVersion = project.android.compileSdk ?: "N/A"
        val minSdkVersion = project.android.defaultConfig.minSdk ?: "N/A"
        val targetSdkVersionInt = project.android.defaultConfig.targetSdk
        val targetSdkVersionName = when (targetSdkVersionInt) {
            32 -> "12L Snowcone V2"
            33 -> "13 Tiramisu"
            34 -> "14 UpsideDownCake"
            35 -> "15 VanillaIceCream"
            36 -> "16 Baklava"
            else -> "Unknown"
        }

        val buildChangelog = changelog.getOrElse("")

        var message = "[Build Status] ${project.name} - $buildStatus* 🚀\n\n" +
                "- App: $currentProjectName\n" +
                "- Version: $currentAppVersion\n" +
                "- Package: $currentAppPackage\n" +
                "- Time: ${Date()}\n\n" +
                "[Build Environment]\n" +
		"- Device : Lenovo Thinkpad X280\n" +
                "- OS: Microsoft Windows 11\n" +
                "- Kernel: $kernelInfo\n" +
                "- Processor: I5-7200U \n" +
                "- Kotlin: $kotlinVersion\n" +
                "- Java: $javaVersion\n" +
                "- Gradle: $gradleVersion\n\n" +
                "[App SDK Information]\n" +
                "- Min SDK: $minSdkVersion\n" +
                "- Target SDK: $targetSdkVersionInt (Android $targetSdkVersionName)\n"

        if (buildChangelog.isNotBlank()) {
            message += "\nChangelog:\n$buildChangelog\n"
        }

        if (buildMsgId != null) {
            editTelegramMessage(buildMsgId, if (buildStatus == "SUCCESS") "✅ Build finished successfully!" else "❌ Build failed!")
        }

        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
        HttpClients.createDefault().use { httpClient ->
            val post = HttpPost(url)
            val jsonPayload = """{"chat_id":"${telegramChatId.get()}","text":"${message.replace("\"", "\\\"")}"}"""
            post.entity = StringEntity(jsonPayload, "UTF-8")
            post.setHeader("Content-Type", "application/json")
            try {
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    logger.lifecycle("Successfully sent message to Telegram.")
                } else {
                    logger.error("Failed to send message. Status: ${response.statusLine}")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                logger.error("Failed to send message: ${e.message}", e)
            }
        }
    }
}

abstract class UploadApkToTelegramTask : DefaultTask() {
    @get:Input abstract val telegramBotToken: Property<String>
    @get:Input abstract val telegramChatId: Property<String>
    @get:InputFile abstract val apkFile: RegularFileProperty
    @get:Input abstract val appVersionName: Property<String>
    @get:Input abstract val appName: Property<String>

    @TaskAction
    fun uploadApk() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram credentials not found. Skipping APK upload.")
            return
        }

        val currentApkFile = apkFile.get().asFile
        if (!currentApkFile.exists()) {
            logger.error("APK not found at ${currentApkFile.absolutePath}")
            return
        }

        val fileSizeMb = currentApkFile.length() / (1024.0 * 1024.0)
        logger.lifecycle("Uploading APK: ${currentApkFile.name} (${"%.2f".format(fileSizeMb)} MB)")

        if (fileSizeMb > 199) {
            logger.error("APK size exceeds 200MB limit. Skipping upload.")
            return
        }

        val caption = "📦 New Test Release: ${appName.get()} v${appVersionName.get()}\n" +
                "Build time: ${Date()}\n" +
                "File: ${currentApkFile.name} (${"%.2f".format(fileSizeMb)} MB)"

        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendDocument"
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(30 * 1000)
            .setSocketTimeout(5 * 60 * 1000)
            .build()

        HttpClients.custom().setDefaultRequestConfig(requestConfig).build().use { httpClient ->
            val post = HttpPost(url)
            val entityBuilder = MultipartEntityBuilder.create()
            entityBuilder.addTextBody("chat_id", telegramChatId.get())
            entityBuilder.addTextBody("caption", caption, org.apache.http.entity.ContentType.TEXT_PLAIN.withCharset("UTF-8"))
            entityBuilder.addPart("document", FileBody(currentApkFile))
            post.entity = entityBuilder.build()

            try {
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    logger.lifecycle("Successfully uploaded APK to Telegram.")
                } else {
                    logger.error("Failed to upload APK. Status: ${response.statusLine}")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                logger.error("Failed to upload APK: ${e.message}", e)
            }
        }
    }
}

val renameReleaseApk by tasks.registering(Copy::class) {
    group = "custom"
    description = "Renames release APK (supports ABI splits)"
    val versionName = android.defaultConfig.versionName ?: "unknown"
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        // Support both universal and arm64-v8a APK names
        include("app-release.apk")
        include("app-arm64-v8a-release.apk")
    }
    into(layout.projectDirectory.dir("dist"))
    rename { "XKM-$versionName.apk" }
}

val uploadReleaseApkToTelegram by tasks.registering(UploadApkToTelegramTask::class) {
    group = "custom"
    description = "Uploads renamed APK to Telegram"
    val versionName = android.defaultConfig.versionName ?: "unknown"
    apkFile.set(layout.projectDirectory.file("dist/XKM-$versionName.apk"))
    telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
    telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appName.convention(project.name)
    mustRunAfter(renameReleaseApk)
}

val notifyBuildStatusToTelegram by tasks.registering(SendTelegramMessageTask::class) {
    group = "custom"
    description = "Sends build status to Telegram"
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appPackageName.convention(project.provider { android.defaultConfig.applicationId ?: "N/A" })
    appProjectName.convention(project.provider { android.namespace?.substringAfterLast('.') ?: project.name })
}

// Debug APK Tasks
val renameDebugApk by tasks.registering(Copy::class) {
    group = "custom"
    description = "Renames debug APK (supports ABI splits)"
    val versionName = android.defaultConfig.versionName ?: "unknown"
    from(layout.buildDirectory.dir("outputs/apk/debug")) {
        // Support both universal and arm64-v8a APK names
        include("app-debug.apk")
        include("app-arm64-v8a-debug.apk")
    }
    into(layout.projectDirectory.dir("dist"))
    rename { "XKM-$versionName-debug.apk" }
}

val uploadDebugApkToTelegram by tasks.registering(UploadApkToTelegramTask::class) {
    group = "custom"
    description = "Uploads debug APK to Telegram"
    val versionName = android.defaultConfig.versionName ?: "unknown"
    apkFile.set(layout.projectDirectory.file("dist/XKM-$versionName-debug.apk"))
    telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
    telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appName.convention("${project.name} (Debug)")
    mustRunAfter(renameDebugApk)
}

tasks.register("buildAndPublish") {
    group = "custom"
    description = "Build, rename, upload APK, and notify"
    dependsOn(tasks.named("assembleRelease"))
    renameReleaseApk.get().mustRunAfter(tasks.named("assembleRelease"))
    uploadReleaseApkToTelegram.get().mustRunAfter(renameReleaseApk)
    notifyBuildStatusToTelegram.get().mustRunAfter(uploadReleaseApkToTelegram)
    finalizedBy(renameReleaseApk, uploadReleaseApkToTelegram, notifyBuildStatusToTelegram)
}
