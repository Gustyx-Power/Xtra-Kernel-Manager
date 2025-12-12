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
    public static *** w(...);
}

# ===== LibSu (if used) =====
-keep class com.topjohnwu.superuser.** { *; }
-keepclassmembers class com.topjohnwu.superuser.** { *; }

# ===== Remove unused classes =====
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ===== AGGRESSIVE: Remove unused resources metadata =====
-dontwarn com.google.android.material.**
-dontwarn com.google.firebase.**

# ===== Strip debug info =====
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ===== Firebase optimizations =====
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ===== JNI Native =====
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===== Serialization =====
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
