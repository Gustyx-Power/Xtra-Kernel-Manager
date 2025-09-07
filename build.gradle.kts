plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.dagger.hilt.android") version "2.57" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.25" apply false
    alias(libs.plugins.kotlin.compose) apply false
}