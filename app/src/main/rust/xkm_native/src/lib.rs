mod cpu;
mod gpu;
mod memory;
mod power;
mod utils;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jfloat, jint, jlong, jstring};
use jni::JNIEnv;

// Helper: Safe JString creation
#[inline]
fn create_jstring_safe(env: &JNIEnv, s: String) -> jstring {
    env.new_string(s)
        .unwrap_or_else(|_| env.new_string("").unwrap())
        .into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detectCpuClustersNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let clusters = cpu::detect_cpu_clusters();
    let json = serde_json::to_string(&clusters).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreDataNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, cpu::read_core_data())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    cpu::read_cpu_load()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    power::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
    core: jint,
) -> jfloat {
    cpu::read_core_temperature(core)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getCpuModelNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, cpu::get_cpu_model())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuFreqNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    gpu::read_gpu_freq()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuBusyNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    gpu::read_gpu_busy()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuVendorNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, gpu::get_gpu_vendor().to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuModelNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, gpu::get_gpu_model().to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryLevelNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_battery_level()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryTempNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_battery_temp()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryVoltageNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_battery_voltage_mv()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_drain_rate_ma()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readDrainRateNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_drain_rate_ma()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    if power::is_charging() {
        1
    } else {
        0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readWakeupCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_wakeup_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readSuspendCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_suspend_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let info = memory::read_meminfo();
    let json = serde_json::to_string(&info).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    (memory::read_zram_size() / 1024) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getMemoryPressureNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    memory::get_memory_pressure()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZoneNative(
    _env: JNIEnv,
    _class: JClass,
    zone: jint,
) -> jfloat {
    power::read_thermal_zone(zone)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalZoneTypeNative(
    env: JNIEnv,
    _class: JClass,
    zone: jint,
) -> jstring {
    create_jstring_safe(&env, power::get_thermal_zone_type(zone))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZonesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, power::read_thermal_zones())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCycleCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    power::read_cycle_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryHealthNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, power::read_battery_health())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCapacityLevelNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    power::read_battery_capacity_level()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressionRatioNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    memory::get_zram_compression_ratio()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressedSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    memory::get_zram_compressed_size() as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramAlgorithmNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, memory::get_zram_algorithm())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSwappinessNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    memory::get_swappiness()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoDetailedNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, memory::read_meminfo_detailed())
}
#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSystemPropertyNative(
    mut env: JNIEnv,
    _class: JClass,
    key: JString,
) -> jstring {
    let key_str: String = env.get_string(&key).map(|s| s.into()).unwrap_or_default();
    let value = utils::get_system_property(&key_str).unwrap_or_default();
    create_jstring_safe(&env, value)
}
