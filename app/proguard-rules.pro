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
# -keep class androidx.** { *; } <-- Removed for shrinking

# ===== Kotlin =====
-dontwarn kotlin.**
-dontwarn kotlinx.**
# -keep class kotlin.** { *; }  <-- Removed for shrinking
-keep class kotlin.Metadata { *; }

# Keep Kotlin reflection for data classes
-keep class kotlin.reflect.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Prevent stripping of parameter names (important for data classes)
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*, MethodParameters

# ===== Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===== Compose =====
# -keep class androidx.compose.** { *; } <-- Removed for shrinking
-dontwarn androidx.compose.**

# ===== Data classes & Models =====
-keep class id.xms.xtrakernelmanager.data.model.** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.data.model.** { *; }

# Keep all fields and methods in data classes (critical for native interop)
-keepclassmembers class id.xms.xtrakernelmanager.data.model.** {
    <fields>;
    <methods>;
}

# Prevent field name obfuscation for classes used with native code
-keepnames class id.xms.xtrakernelmanager.data.model.CPUInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.CoreInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.ClusterInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.GPUInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.SystemInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.BatteryInfo { *; }
-keepnames class id.xms.xtrakernelmanager.data.model.PowerInfo { *; }

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

# ===== Repository classes (critical for system data reading) =====
-keep class id.xms.xtrakernelmanager.data.repository.** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.data.repository.** {
    <fields>;
    <methods>;
}

# ===== UseCase classes (critical for system control) =====
-keep class id.xms.xtrakernelmanager.domain.usecase.** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.domain.usecase.** {
    <fields>;
    <methods>;
}

# ===== RootManager (critical for root operations) =====
-keep class id.xms.xtrakernelmanager.domain.root.RootManager { *; }
-keepclassmembers class id.xms.xtrakernelmanager.domain.root.RootManager {
    <fields>;
    <methods>;
}

# ===== Remove unused classes =====
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ===== JSON Parsing (org.json) =====
# Keep JSON classes used by NativeLib for parsing native responses
-keep class org.json.** { *; }
-keepclassmembers class org.json.** {
    <fields>;
    <methods>;
}
-dontwarn org.json.**

# ===== TOML Parser =====
-keep class org.tomlj.** { *; }
-keepclassmembers class org.tomlj.** { *; }
-dontwarn org.tomlj.**

# Keep TomlConfigManager and related classes
-keep class id.xms.xtrakernelmanager.data.preferences.TomlConfigManager { *; }
-keepclassmembers class id.xms.xtrakernelmanager.data.preferences.TomlConfigManager {
    <fields>;
    <methods>;
}

# ===== YukiHookAPI & KavaRef =====
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn com.highcapable.yukihookapi.**
-dontwarn com.highcapable.kavaref.**
-keep class com.highcapable.yukihookapi.** { *; }
-keep class com.highcapable.kavaref.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keep class id.xms.xtrakernelmanager.xposed.** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.xposed.** { *; }

# ===== AGGRESSIVE: Remove unused resources metadata =====
-dontwarn com.google.android.material.**
-dontwarn com.google.firebase.**

# ===== Strip debug info =====
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ===== Firebase optimizations =====
# -keep class com.google.firebase.** { *; } <-- Removed for shrinking
-dontwarn com.google.firebase.**

# ===== JNI Native =====
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep NativeLib and all its inner classes/data classes
-keep class id.xms.xtrakernelmanager.domain.native.NativeLib { *; }
-keep class id.xms.xtrakernelmanager.domain.native.NativeLib$** { *; }
-keepclassmembers class id.xms.xtrakernelmanager.domain.native.NativeLib$** {
    <fields>;
    <methods>;
}

# Prevent obfuscation of data classes used by native code
-keepnames class id.xms.xtrakernelmanager.domain.native.NativeLib$CoreData { *; }
-keepnames class id.xms.xtrakernelmanager.domain.native.NativeLib$ThermalZone { *; }
-keepnames class id.xms.xtrakernelmanager.domain.native.NativeLib$MemInfo { *; }

# Keep ALL native methods explicitly (mapped from Rust JNI functions)
-keepclassmembers class id.xms.xtrakernelmanager.domain.native.NativeLib {
    # CPU Module
    *** detectCpuClustersNative();
    *** readCoreDataNative();
    *** readCpuLoadNative();
    *** readCpuTemperatureNative();
    *** readCoreTemperatureNative(int);
    *** getCpuModelNative();
    
    # GPU Module
    *** readGpuFreqNative();
    *** readGpuBusyNative();
    *** resetGpuStatsNative();
    *** getGpuVendorNative();
    *** getGpuModelNative();
    *** getGpuAvailableFrequenciesNative();
    *** getGpuAvailablePoliciesNative();
    *** getGpuDriverInfoNative();
    
    # Power/Battery Module
    *** readBatteryLevelNative();
    *** readBatteryTempNative();
    *** readBatteryVoltageNative();
    *** readBatteryCurrentNative();
    *** readDrainRateNative();
    *** isChargingNative();
    *** readWakeupCountNative();
    *** readSuspendCountNative();
    *** readCycleCountNative();
    *** readBatteryHealthNative();
    *** readBatteryCapacityLevelNative();
    
    # Thermal Module
    *** readThermalZoneNative(int);
    *** getThermalZoneTypeNative(int);
    *** readThermalZonesNative();
    
    # Memory Module
    *** readMemInfoNative();
    *** readMemInfoDetailedNative();
    *** getMemoryPressureNative();
    
    # ZRAM Module
    *** readZramSizeNative();
    *** getZramCompressionRatioNative();
    *** getZramCompressedSizeNative();
    *** getZramOrigDataSizeNative();
    *** getZramAlgorithmNative();
    *** getAvailableZramAlgorithmsNative();
    *** readZramDeviceStatsNative(int);
    *** getSwappinessNative();
    
    # System Utils
    *** getSystemPropertyNative(java.lang.String);
    
    # Public wrapper methods (Kotlin)
    *** detectCpuClusters();
    *** readBatteryCurrent();
    *** readCpuLoad();
    *** readCpuTemperature();
    *** readCoreData();
    *** readGpuFreq();
    *** readGpuBusy();
    *** resetGpuStats();
    *** readBatteryLevel();
    *** readDrainRate();
    *** readWakeupCount();
    *** readSuspendCount();
    *** isCharging();
    *** readBatteryTemp();
    *** readBatteryVoltage();
    *** readMemInfo();
    *** readZramSize();
    *** getSystemProperty(java.lang.String);
    *** getGpuVendor();
    *** getGpuModel();
    *** readCycleCount();
    *** readBatteryHealth();
    *** readBatteryCapacityLevel();
    *** getZramCompressionRatio();
    *** getZramCompressedSize();
    *** getZramOrigDataSize();
    *** getZramAlgorithm();
    *** getSwappiness();
    *** getMemoryPressure();
    *** getAvailableZramAlgorithms();
    *** getGpuAvailableFrequencies();
    *** getGpuAvailablePolicies();
    *** getGpuDriverInfo();
    *** readThermalZones();
    *** getVulkanVersion(android.content.Context);
}

# ===== JNI/Rust for AAB Packages =====
# Keep all native methods and their declaring classes
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep all classes that contain native methods (critical for Rust JNI)
-keepclasseswithmembers class * {
    native <methods>;
}

# Prevent stripping of JNI method signatures and parameter names
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,MethodParameters

# Keep all native library loading code (static initializers)
-keepclassmembers class * {
    <clinit>();
}

# Keep System.loadLibrary and System.load calls
-keepclassmembers class java.lang.System {
    public static void loadLibrary(java.lang.String);
    public static void load(java.lang.String);
}

# Keep all classes that load native libraries
-keepclasseswithmembers class * {
    static <methods>;
}

# Prevent obfuscation of field names accessed from native code
-keepclassmembers class * {
    @com.sun.jna.* *;
}

# Keep all JNI registration methods (for dynamic JNI registration)
-keepclassmembers class * {
    *** JNI_OnLoad(...);
    *** JNI_OnUnload(...);
}

# Keep all classes referenced from native code (Rust FFI)
-keep class ** {
    native <methods>;
}

# Prevent removal of empty constructors (needed for JNI object creation)
-keepclassmembers class * {
    public <init>();
}

# Keep all enums (often used in JNI/Rust interop)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Additional AAB-specific rules for native libraries
-keep class **.R$* { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Ensure native library extraction works correctly in AAB
-keep class com.android.vending.** { *; }
-keep class com.google.android.play.** { *; }

# ===== Serialization =====
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== Kotlinx Serialization =====
# Keep all @Serializable annotated classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their members
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all classes with @Serializable annotation
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    <fields>;
    <methods>;
}

# Prevent field name obfuscation for serializable classes
-keepnames @kotlinx.serialization.Serializable class * { *; }

# ===== Play Protect Compatibility =====
# Obfuscate sensitive method names that might trigger detection
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

# Keep Google Play Services classes to appear more legitimate
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep standard Android classes
-keep class android.support.** { *; }
# -keep class androidx.** { *; } <-- Removed for shrinking

# Rename sensitive native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Remove potentially suspicious strings
-adaptresourcefilenames **.properties,**.xml,**.json
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# Additional Play Protect optimizations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Obfuscate root-related method names
-keepclassmembers class * {
    *** *root*(...);
    *** *su*(...);
    *** *superuser*(...);
}

# Keep legitimate Android components
-keep class * extends android.app.Activity
-keep class * extends android.app.Fragment
-keep class * extends androidx.fragment.app.Fragment

# Remove debug and development traces
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Optimize for smaller APK size (less suspicious)
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''