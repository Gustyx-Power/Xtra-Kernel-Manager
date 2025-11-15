# ===== AGGRESSIVE SHRINKING =====
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ===== Keep Application class =====
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ===== Kotlin =====
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

# ===== Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===== Compose =====
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===== Data classes & Models =====
-keep class id.xms.xtrakernelmanager.data.model.** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.data.model.** { *; }

# ===== Remove logging in release =====
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ===== LibSu (if used) =====
-keep class com.topjohnwu.superuser.** { *; }
-keepclassmembers class com.topjohnwu.superuser.** { *; }

# ===== Remove unused classes =====
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
