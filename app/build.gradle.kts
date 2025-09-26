import org.apache.http.client.methods.HttpPost
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.util.Date
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import kotlin.text.substringAfterLast


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "id.xms.xtrakernelmanager"
    compileSdk = 36
    defaultConfig {
        applicationId = "id.xms.xtrakernelmanager"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.5-TestRelease"
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
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        lint.disable.add("NullSafeMutableLiveData")


    }
    kotlin {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:33.5.0-jre")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.5")

    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Room Database
    implementation("androidx.room:room-runtime:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-work:1.3.0")
    ksp("androidx.hilt:hilt-compiler:1.3.0") // KSP processor for Hilt-WorkManager
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kspTest("com.google.dagger:hilt-compiler:2.57.2")

    // LibSu & Coil
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-drawablepainter:0.37.3")

    // Serialization for Kotlin 2.0.0
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.5")

    // Guava
    implementation("com.google.guava:guava:33.5.0-jre") {
        exclude(mapOf("group" to "com.google.guava", "module" to "listenablefuture"))
    }

    // Firebase BOM (Bill of Materials) for version management
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    // Add Firebase Analytics (core dependency, you can add more as needed)
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Add other Firebase dependencies as needed, e.g.:
    // implementation("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-firestore-ktx")
    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-common-ktx:21.0.0")
}


tasks.register("sendTelegramMessage", SendTelegramMessageTask::class) {
    group = "custom"
    description = "Sends a build status message to Telegram."
    // Properties will be set via convention mapping or direct assignment
}

abstract class SendTelegramMessageTask : DefaultTask() {

    @get:Input
    abstract val telegramBotToken: Property<String>

    @get:Input
    abstract val telegramChatId: Property<String>

    @get:Input
    abstract val appVersionName: Property<String>

    @get:Input
    abstract val appPackageName: Property<String>

    @get:Input
    abstract val appProjectName: Property<String>

    @get:Input
    @get:Optional
    abstract val changelog: Property<String>



    init {
        // Initialize from project properties
        telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
        telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
        // These will be configured by the invoking task or a general build listener
        appVersionName.convention("")
        appPackageName.convention("")
        appProjectName.convention(project.name) // Default to current project name
        changelog.convention(project.findProperty("myChangelog")?.toString() ?: "")

    }

    @TaskAction
    fun sendMessage() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram Bot Token or Chat ID not found in gradle.properties. Skipping message.")
            return
        }

        val buildStatus = if (project.gradle.taskGraph.allTasks.any { it.state.failure != null }) {
            "FAILED"
        } else {
            "SUCCESS"
        }

        val currentAppVersion = appVersionName.getOrElse(project.android.defaultConfig.versionName ?: "N/A")
        val currentAppPackage = appPackageName.getOrElse(project.android.defaultConfig.applicationId ?: "N/A")
        val currentProjectName = appProjectName.get()

        val kotlinVersion = project.getKotlinPluginVersion() ?: "N/A"
        // --- Progress message for build ---
        fun sendTelegramMessage(text: String, disableNotification: Boolean = false): Int? {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"text\": \"${text.replace("\"", "\\\"")}\",\n  \"disable_notification\": $disableNotification\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                EntityUtils.consumeQuietly(response.entity)
                val idRegex = "\\\"message_id\\\":(\\d+)".toRegex()
                return idRegex.find(responseBody)?.groupValues?.get(1)?.toIntOrNull()
            }
        }
        fun editTelegramMessage(messageId: Int, text: String) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/editMessageText"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"message_id\": $messageId,\n  \"text\": \"${text.replace("\"", "\\\"")}\"\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                EntityUtils.consumeQuietly(response.entity)
            }
        }
        fun pinTelegramMessage(messageId: Int) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/pinChatMessage"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"message_id\": $messageId,\n  \"disable_notification\": true\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                EntityUtils.consumeQuietly(response.entity)
            }
        }

        val buildMsgId = sendTelegramMessage("Processing build...", disableNotification = true)
        if (buildMsgId != null) pinTelegramMessage(buildMsgId)

        val javaVersion = JavaVersion.current().toString()
        val gradleVersion = project.gradle.gradleVersion
        // val osName = System.getProperty("os.name")
        val osName = try {
            val process = ProcessBuilder("cat", "/etc/os-release")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            val prettyNameLine = output.lines().find { it.startsWith("NAME=") }
            prettyNameLine?.substringAfter("=")?.removeSurrounding("\"") ?: System.getProperty("os.name")
        } catch (e: Exception) {
            logger.warn("Could not read /etc/os-release: ${e.message}")
            System.getProperty("os.name") // Fallback
        }
        val osArch = System.getProperty("os.arch")
        val processor = try {
            val process = ProcessBuilder("cat", "/proc/cpuinfo")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.lines().find { it.startsWith("model name") }?.substringAfter(":")?.trim() ?: osArch
        } catch (e: Exception) {
            logger.warn("Could not read /proc/cpuinfo: ${e.message}")
            osArch // Fallback
        }

        // SDK Info
        val compileSdkVersion = project.android.compileSdk ?: "N/A"
        val minSdkVersion = project.android.defaultConfig.minSdk ?: "N/A"
        val targetSdkVersionInt = project.android.defaultConfig.targetSdk
        val targetSdkVersionName = when (targetSdkVersionInt) {
            29 -> "10 [QuinceTart]" // Android 10
            30 -> "11 [RedVelvet]" // Android 11
            31 -> "12 [Snowcone]" // Android 12
            32 -> "12L [Snowcone V2]" // Android 12L
            33 -> "13 [Tiramisu]" // Android 13
            34 -> "14 [UpsideDownCake]" // Android 14
            35 -> "15 [VanillaIceCream]" // Android 15
            36 -> "16 [Baklava]" // Android 16
            else -> "Unknown"
        }
        val minSdkCodename = when (minSdkVersion) {
            // Assuming minSdkVersion is an Int or can be converted to one for this when
            29 -> "10 [QuinceTart]" // Android 10
            30 -> "11 [RedVelvet]" // Android 11
            31 -> "12 [Snowcone]" // Android 12
            32 -> "12L [Snowcone V2]" // Android 12L
            33 -> "13 [Tiramisu]" // Android 13
            34 -> "14 [UpsideDownCake]" // Android 14
            35 -> "15 [VanillaIceCream]" // Android 15
            36 -> "16 [Baklava]" // Android 16
            else -> "Unknown"
        }

        val (totalRamGb, freeRamGb, usedRamGb) = try {
            val process = ProcessBuilder("cat", "/proc/meminfo")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            val lines = output.lines()
            val memTotalKb = lines.find { it.startsWith("MemTotal:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: 0L
            val memFreeKb = lines.find { it.startsWith("MemFree:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: 0L
            // MemAvailable is generally a better indicator of free memory
            val memAvailableKb = lines.find { it.startsWith("MemAvailable:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: memFreeKb

            val totalGb = memTotalKb / (1024.0 * 1024.0)
            val freeGb = memAvailableKb / (1024.0 * 1024.0)
            val usedGb = totalGb - freeGb

            Triple(String.format("%.2f GB", totalGb), String.format("%.2f GB", freeGb), String.format("%.2f GB", usedGb))

        } catch (e: Exception) {
            logger.warn("Could not read /proc/meminfo: ${e.message}")
            Triple("N/A", "N/A", "N/A") // Fallback
        }

        val (totalStorageGb, freeStorageGb) = try {
            val process = ProcessBuilder("df", "-h", "/")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            val lines = output.lines()
            // Example df output: /dev/sda1       100G   50G   50G  50% /
            val relevantLine = lines.find { it.contains(" /") }
            val parts = relevantLine?.split("\\s+".toRegex())
            val total = parts?.getOrNull(1) ?: "N/A"
            val free = parts?.getOrNull(3) ?: "N/A"
            Pair(total, free)
        } catch (e: Exception) {
            logger.warn("Could not read disk usage: ${e.message}")
            Pair("N/A", "N/A") // Fallback
        }

        val kernelInfo = try {
            val process = ProcessBuilder("uname", "-r")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output.ifEmpty { "N/A" }
        } catch (e: Exception) {
            logger.warn("Could not read kernel info: ${e.message}")
            "N/A" // Fallback
        }

        val buildChangelog = changelog.getOrElse("")

        var message = "[Build Status] ${project.name} - $buildStatus* 🚀\n\n" +
                "📦 App: ${currentProjectName}\n" +
                "🏷️ Version: ${currentAppVersion}\n" +
                "🆔 Package: ${currentAppPackage}\n" +
                "📅 Time: ${Date().toString()}\n\n" +
                "[Build Environment]\n" +
                "OS: $osName ($osArch)\n" +
                "Kernel: $kernelInfo\n" +
                "Processor:\n $processor\n" +
                "RAM:\n Total: $totalRamGb\n Free: $freeRamGb\n Used: $usedRamGb\n" +
                "Storage:\n Total: $totalStorageGb\n Free: $freeStorageGb\n" +
                "Android Studio: Narwhal Feature Drop\n" +
                "Kotlin: $kotlinVersion\n" +
                "Java: $javaVersion\n" +
                "Gradle (Kotlin DSL): $gradleVersion\n\n" +
                "[App SDK Information]\n" +
                "Min SDK: $minSdkVersion (Android $minSdkCodename)\n" +
                "Target SDK: $targetSdkVersionInt (Android $targetSdkVersionName)\n"


        if (buildChangelog.isNotBlank()) {
            message += "\nChangelog:\n$buildChangelog\n"
        }

        if (buildStatus == "FAILED") {
            val failedTasks = project.gradle.taskGraph.allTasks.filter { it.state.failure != null }
            if (failedTasks.isNotEmpty()) {
                val errorDetails = failedTasks.joinToString(separator = "\n") { task ->
                    val taskName = task.path
                    val errorMessage =
                        task.state.failure?.message?.lines()?.firstOrNull()?.trim() ?: "No specific error message"
                    "  - Task `$taskName` failed: $errorMessage"
                }
                message += "\n\n⚠️ Error Details:\n$errorDetails"
            } else {
                message += "\n\n⚠️ Build failed. Check build logs for details."
            }
        }


        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(15 * 1000)
            .setSocketTimeout(30 * 1000)
            .build()



        if (buildMsgId != null) {
            editTelegramMessage(buildMsgId, if (buildStatus == "SUCCESS") "✅ Build finished successfully!" else "❌ Build failed!")
        }

        HttpClients.custom().setDefaultRequestConfig(requestConfig).build().use { httpClient ->
            val post = HttpPost(url)
            val jsonPayload = """
            {
                "chat_id": "${telegramChatId.get().replace("\"", "\\\"")}",
                "text": "${message.replace("\"", "\\\"")}"
            }
            """.trimIndent()
            post.entity = StringEntity(jsonPayload, "UTF-8")
            post.setHeader("Content-Type", "application/json")

            try {
                logger.info("Sending build status message to Telegram...")
                logger.info("Payload: $jsonPayload") // Tambahkan log untuk payload
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    logger.lifecycle("Successfully sent message to Telegram. Response: $responseBody")
                } else {
                    logger.error("Failed to send message to Telegram. Status: ${response.statusLine}, Body: $responseBody. Payload: $jsonPayload")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                logger.error("Failed to send message to Telegram: ${e.message}", e)
            }
        }
    }
}

abstract class UploadApkToTelegramTask : DefaultTask() {
    @get:Input abstract val telegramBotToken: Property<String>

    @get:Input
    abstract val telegramChatId: Property<String>

    @get:InputFile
    abstract val apkFile: RegularFileProperty

    @get:Input
    abstract val appVersionName: Property<String>

    @get:Input
    abstract val appName: Property<String>

    @TaskAction
    fun uploadApk() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram Bot Token or Chat ID not found. Skipping APK upload.")
            return
        }

        val currentApkFile = apkFile.get().asFile
        if (!currentApkFile.exists()) {
            project.logger.error("Release APK not found at ${currentApkFile.absolutePath}. Ensure assembleRelease has run.")
            return
        }

        val fileSizeMb = currentApkFile.length() / (1024.0 * 1024.0)
        logger.lifecycle("Attempting to upload APK: ${currentApkFile.name} (Size: ${"%.2f".format(fileSizeMb)} MB)")

        // Telegram Bot API limit can be up to 2GB for bots, but typically 50MB for general use without special setup.
        if (fileSizeMb > 199) { // Check slightly less than 200MB to be safe
            project.logger.error("APK size (${"%.2f".format(fileSizeMb)} MB) exceeds Telegram's typical 200MB limit. Skipping upload.")
            // Consider how to trigger SendTelegramMessageTask if needed, as direct task execution from another is not standard.
            // One option is to ensure SendTelegramMessageTask runs via task dependencies or lifecycle hooks.
            return
        }

        // Hilangkan // jika sudah ingin release
        // val caption = "📦 New Release build: ${appName.get()} v${appVersionName.get()}\n" +
        //"Build time: ${Date()}\n" +
        // "File: ${currentApkFile.name} (${"%.2f".format(fileSizeMb)} MB)"

        // Tambahkan // jika sudah tidak ingin menggunakan test release
        val caption = "📦 New Test Release build: ${appName.get()} v${appVersionName.get()}\n" +
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
                project.logger.info("Uploading APK to Telegram...")
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    project.logger.lifecycle("Successfully uploaded APK to Telegram. Response: $responseBody")
                } else if (response.statusLine.statusCode == 400 && responseBody.contains("can't parse entities")) {
                    project.logger.error("Failed to upload APK to Telegram due to Markdown parsing error. Status: ${response.statusLine}, Body: $responseBody. Caption was: $caption")
                } else {
                    project.logger.error("Failed to upload APK to Telegram. Status: ${response.statusLine}, Body: $responseBody")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                project.logger.error("Failed to upload APK to Telegram: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}


// Task to rename/copy app-release.apk to XtraKernelManager-versionName.apk after assembleRelease
// 1. Task untuk me-rename/menyalin app-release.apk
val renameReleaseApk by tasks.registering(Copy::class) {
    group = "custom"
    description = "Renames/copies app-release.apk to a custom name."

    val versionName = android.defaultConfig.versionName ?: "unknown-version"

    // Sumber file APK dari direktori build
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
    }

    // Direktori tujuan (misalnya 'dist' di root proyek)
    into(layout.projectDirectory.dir("dist"))

    // Mengganti nama file
    rename { "XtraKernelManager-$versionName.apk" }
}

// 2. Task untuk mengunggah APK yang sudah diganti namanya
val uploadReleaseApkToTelegram by tasks.registering(UploadApkToTelegramTask::class) {
    group = "custom"
    description = "Uploads the renamed release APK to Telegram."

    // --- INI BAGIAN UTAMA YANG DIPERBAIKI ---
    // Daripada mencoba menghubungkan output task secara dinamis,
    // kita secara eksplisit menunjuk ke LOKASI FILE yang kita tahu akan dibuat.
    val versionName = android.defaultConfig.versionName ?: "unknown-version"
    val outputApkPath = layout.projectDirectory.file("dist/XtraKernelManager-$versionName.apk")

    // .set() sekarang menerima referensi file yang valid dan "lazy".
    apkFile.set(outputApkPath)
    // -----------------------------------------

    // Konfigurasi lainnya tetap sama
    telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
    telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appName.convention(project.name)
    mustRunAfter(renameReleaseApk)
}

// 3. Task untuk mengirim notifikasi status build
val notifyBuildStatusToTelegram by tasks.registering(SendTelegramMessageTask::class) {
    group = "custom"
    description = "Sends the final build status to Telegram."


    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appPackageName.convention(project.provider { android.defaultConfig.applicationId ?: "N/A" })
    appProjectName.convention(project.provider { android.namespace?.substringAfterLast('.') ?: project.name })
}

// 4. Task Utama untuk menggabungkan semua langkah menjadi satu pipeline
tasks.register("buildAndPublish") {
    group = "custom"
    description = "Builds, renames, uploads the APK, and sends a notification."

    // Menetapkan urutan eksekusi yang BENAR.
    // Jika salah satu gagal, yang berikutnya tidak akan berjalan.
    dependsOn(tasks.named("assembleRelease"))

    // Rantai tugas dengan `mustRunAfter` untuk memastikan urutan yang ketat
    renameReleaseApk.get().mustRunAfter(tasks.named("assembleRelease"))
    uploadReleaseApkToTelegram.get().mustRunAfter(renameReleaseApk)
    notifyBuildStatusToTelegram.get().mustRunAfter(uploadReleaseApkToTelegram)

    // Menjadikan semua task ini sebagai dependensi dari buildAndPublish
    finalizedBy(
        renameReleaseApk,
        uploadReleaseApkToTelegram,
        notifyBuildStatusToTelegram
    )
}