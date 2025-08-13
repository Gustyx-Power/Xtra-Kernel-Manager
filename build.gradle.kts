plugins {
    id("com.android.application") version "8.12.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.57" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" apply false
    alias(libs.plugins.kotlin.compose) apply false
}