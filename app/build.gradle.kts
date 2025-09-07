

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
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "id.xms.xtrakernelmanager"
    compileSdk = 36
    defaultConfig {
        applicationId = "id.xms.xtrakernelmanager"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.4.-Release"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:32.1.3-jre")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.work:work-runtime-ktx:2.10.3")

    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Versi yang aman untuk Kotlin 1.9.24
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.compilercommon)
    kapt("com.google.dagger:hilt-compiler:2.57")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.57")
    kaptTest("com.google.dagger:hilt-compiler:2.57")

    // LibSu & Coil
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")


    // Serialization yang cocok dengan Kotlin 1.9.24
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.3")

    // Guava
    implementation("com.google.guava:guava:32.1.3-jre") {
        exclude(mapOf("group" to "com.google.guava", "module" to "listenablefuture"))
    }
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


    init {
        // Initialize from project properties
        telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
        telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
        // These will be configured by the invoking task or a general build listener
        appVersionName.convention("")
        appPackageName.convention("")
        appProjectName.convention(project.name) // Default to current project name
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

        // Function to escape MarkdownV2 characters
        fun escapeMarkdownV2(text: String): String {
            val escapeChars = charArrayOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
            var result = text
            for (ch in escapeChars) {
                result = result.replace(ch.toString(), "\\" + ch)
            }
            return result
        }

        var message = "[Build Status] ${escapeMarkdownV2(project.name)} \\- $buildStatus* 🚀\n\n" +
                "📦 App: ${escapeMarkdownV2(currentProjectName)}\n" +
                "🏷️ Version: ${escapeMarkdownV2(currentAppVersion)}\n" +
                "🆔 Package: ${escapeMarkdownV2(currentAppPackage)}\n" +
                "📅 Time: ${escapeMarkdownV2(Date().toString())}\n\n" +
                "[Build Environment]\n" +
                "  \\- OS: ${escapeMarkdownV2("$osName ($osArch)")}\n" +
                "  \\- Kernel: ${escapeMarkdownV2(kernelInfo)}\n" +
                "  \\- Processor:\n ${escapeMarkdownV2(processor)}\n" +
                "  \\- RAM:\n Total: ${escapeMarkdownV2(totalRamGb)}\n Free: ${escapeMarkdownV2(freeRamGb)}\n Used: ${escapeMarkdownV2(usedRamGb)}\n" +
                "  \\- Storage:\n Total: ${escapeMarkdownV2(totalStorageGb)}\n Free: ${escapeMarkdownV2(freeStorageGb)}\n" +
                "  \\- Android Studio: ${escapeMarkdownV2("Narwhal Feature Drop")}\n" +
                "  \\- Kotlin: ${escapeMarkdownV2(kotlinVersion)}\n" +
                "  \\- Java: ${escapeMarkdownV2(javaVersion)}\n" +
                "  \\- Gradle (Kotlin DSL): ${escapeMarkdownV2(gradleVersion)}\n\n" +
                "[App SDK Information]\n" +
                "  \\- Min SDK: ${escapeMarkdownV2("$minSdkVersion (Android $minSdkCodename)")}\n" +
                "  \\- Target SDK: ${escapeMarkdownV2("$targetSdkVersionInt (Android $targetSdkVersionName)")}\n"

        if (buildStatus == "FAILED") {
            val failedTasks = project.gradle.taskGraph.allTasks.filter { it.state.failure != null }
            if (failedTasks.isNotEmpty()) {
                val errorDetails = failedTasks.joinToString(separator = "\n") { task ->
                    val taskName = escapeMarkdownV2(task.path)
                    val errorMessage = escapeMarkdownV2(
                        task.state.failure?.message?.lines()?.firstOrNull()?.trim() ?: "No specific error message"
                    )
                    "  \\- Task `$taskName` failed: $errorMessage"
                }
                message += "\n\n⚠️ *Error Details:*\n$errorDetails"
            } else {
                message += "\n\n⚠️ *Build failed. Check build logs for details.*"
            }
        }


        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(15 * 1000)
            .setSocketTimeout(30 * 1000)
            .build()

        HttpClients.custom().setDefaultRequestConfig(requestConfig).build().use { httpClient ->
            val post = HttpPost(url)
            val jsonPayload = """
            {
                "chat_id": "${telegramChatId.get().replace("\"", "\\\"")}",
                "text": "${message.replace("\"", "\\\"")}", // Gunakan message langsung
                "parse_mode": "MarkdownV2"
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
                e.printStackTrace() // logger.error with exception will print stack trace with --stacktrace
            }
        }
    }
}


tasks.register("uploadDebugApkToTelegram", UploadApkToTelegramTask::class) {
    group = "custom"
    description = "Builds and uploads the debug APK to Telegram."
    //dependsOn("assembleDebug")
    dependsOn("assembleRelease")
    //apkFile.set(project.layout.projectDirectory.file("build/outputs/apk/debug/app-debug.apk"))
    apkFile.set(project.layout.projectDirectory.file("release/app-release.apk"))
    telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
    telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
    appVersionName.convention(project.provider { project.android.defaultConfig.versionName ?: "N/A" })
    appName.convention(project.name)
}

// --- Hook tasks into the build lifecycle ---

// Configure the sendTelegramMessage task
project.afterEvaluate {
    tasks.named("sendTelegramMessage", SendTelegramMessageTask::class) {
        appVersionName.set(project.provider { android.defaultConfig.versionName ?: "N/A" })
        appPackageName.set(project.provider { android.defaultConfig.applicationId ?: "N/A" })
        appProjectName.set(project.provider {
            android.namespace?.substringAfterLast('.') ?: project.name
        })
    }
}

// DEFER HOOKING INTO PLUGIN TASKS:
project.afterEvaluate {
    tasks.named("assembleRelease") {
        finalizedBy("sendTelegramMessage")
    } // Add this closing brace

    // Run uploadDebugApkToTelegram after assembleDebug
    tasks.named("assembleRelease") {
        finalizedBy("uploadDebugApkToTelegram")
    }
}