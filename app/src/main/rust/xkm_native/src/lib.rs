mod cpu;
mod gpu;
mod memory;
mod power;
mod utils;

use jni::objects::{JClass, JString};
use jni::sys::{jfloat, jint, jlong, jstring};
use jni::EnvUnowned;

#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(
    _vm: *mut jni::sys::JavaVM,
    _reserved: *mut std::ffi::c_void,
) -> jint {
    jni::sys::JNI_VERSION_1_6
}

#[inline]
fn create_jstring_safe(env: EnvUnowned, s: String) -> jstring {
    env.with_env(|env| {
        env.new_string(s)
            .unwrap_or_else(|_| env.new_string("").unwrap())
            .into_raw()
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detectCpuClustersNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let clusters = cpu::detect_cpu_clusters();
    let json = serde_json::to_string(&clusters).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreDataNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, cpu::read_core_data())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    cpu::read_cpu_load()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    power::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreTemperatureNative(
    _env: EnvUnowned,
    _class: JClass,
    core: jint,
) -> jfloat {
    cpu::read_core_temperature(core)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getCpuModelNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, cpu::get_cpu_model())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuFreqNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    gpu::read_gpu_freq()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuBusyNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    gpu::read_gpu_busy()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_resetGpuStatsNative(
    _env: EnvUnowned,
    _class: JClass,
) {
    gpu::reset_gpu_stats();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuVendorNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, gpu::get_gpu_vendor().to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuModelNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, gpu::get_gpu_model().to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryLevelNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_battery_level()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryTempNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_battery_temp()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryVoltageNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_battery_voltage_mv()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_drain_rate_ma()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readDrainRateNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_drain_rate_ma()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingNative(
    _env: EnvUnowned,
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
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_wakeup_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readSuspendCountNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_suspend_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let info = memory::read_meminfo();
    let json = serde_json::to_string(&info).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::read_zram_size()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getMemoryPressureNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    memory::get_memory_pressure()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZoneNative(
    _env: EnvUnowned,
    _class: JClass,
    zone: jint,
) -> jfloat {
    power::read_thermal_zone(zone)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalZoneTypeNative(
    env: EnvUnowned,
    _class: JClass,
    zone: jint,
) -> jstring {
    create_jstring_safe(env, power::get_thermal_zone_type(zone))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZonesNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, power::read_thermal_zones())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCycleCountNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    power::read_cycle_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryHealthNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, power::read_battery_health())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCapacityLevelNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    power::read_battery_capacity_level()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressionRatioNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    memory::get_zram_compression_ratio()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressedSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::get_zram_compressed_size()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramOrigDataSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::get_zram_orig_data_size()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramAlgorithmNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, memory::get_zram_algorithm())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSwappinessNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    memory::get_swappiness()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoDetailedNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, memory::read_meminfo_detailed())
}
#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSystemPropertyNative(
    env: EnvUnowned,
    _class: JClass,
    key: JString,
) -> jstring {
    env.with_env(|env| {
        let key_str: String = env.get_string(&key)
            .map(|s| s.into())
            .unwrap_or_default();
        let value = utils::get_system_property(&key_str).unwrap_or_default();
        env.new_string(value)
            .unwrap_or_else(|_| env.new_string("").unwrap())
            .into_raw()
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailableFrequenciesNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let freqs = gpu::get_gpu_available_frequencies();
    let json = serde_json::to_string(&freqs).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailablePoliciesNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let policies = gpu::get_gpu_available_policies();
    let json = serde_json::to_string(&policies).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(env, json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuDriverInfoNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(env, gpu::get_gpu_driver_info())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramDeviceStatsNative(
    env: EnvUnowned,
    _class: JClass,
    device: jint,
) -> jstring {
    if let Some(stats) = memory::read_zram_device_stats(device) {
        let json = serde_json::to_string(&stats).unwrap_or_else(|_| "{}".to_string());
        create_jstring_safe(env, json)
    } else {
        create_jstring_safe(env, "{}".to_string())
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getAvailableZramAlgorithmsNative(
    env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let algos = memory::get_available_zram_algorithms();
    let json = serde_json::to_string(&algos).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(env, json)
}
