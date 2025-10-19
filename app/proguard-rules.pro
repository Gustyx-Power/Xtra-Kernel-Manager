# Add project specific ProGuard rules here.

# LibSu
-keep class com.topjohnwu.superuser.** { *; }
-keepclassmembers class * extends com.topjohnwu.superuser.Shell.Initializer {
    <init>(...);
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class id.xms.xtrakernelmanager.**$$serializer { *; }
-keepclassmembers class id.xms.xtrakernelmanager.** {
    *** Companion;
}
-keepclasseswithmembers class id.xms.xtrakernelmanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Keep data classes
-keep class id.xms.xtrakernelmanager.data.model.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
