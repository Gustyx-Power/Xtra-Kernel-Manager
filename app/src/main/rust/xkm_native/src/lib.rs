mod cpu;
mod gpu;
mod memory;
mod power;
mod utils;

use jni::objects::JClass;
use jni::sys::{jboolean, jstring};
use jni::JNIEnv;

// ============================================================================
// CPU Module JNI
// ============================================================================

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detectCpuClustersNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let result = cpu::detect_cpu_clusters();

    let json = match serde_json::to_string(&result) {
        Ok(json) => json,
        Err(_) => "[]".to_string(),
    };

    env.new_string(json)
        .expect("Failed to create Java string")
        .into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    cpu::read_battery_current()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: JNIEnv,
    _class: JClass,
) -> f32 {
    cpu::read_cpu_load()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
) -> f32 {
    cpu::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreDataNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let json = cpu::read_core_data();
    env.new_string(json)
        .expect("Failed to create Java string")
        .into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuFreqNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    gpu::read_gpu_freq()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuBusyNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    gpu::read_gpu_busy()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryLevelNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_battery_level()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readDrainRateNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_drain_rate_ma()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readWakeupCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_wakeup_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readSuspendCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_suspend_count()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingNative(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    if power::is_charging() {
        1
    } else {
        0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryTempNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_battery_temp()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryVoltageNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    power::read_battery_voltage_mv()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let info = memory::read_meminfo();

    let json = match serde_json::to_string(&info) {
        Ok(json) => json,
        Err(_) => "{}".to_string(),
    };

    env.new_string(json)
        .expect("Failed to create Java string")
        .into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> i64 {
    memory::read_zram_size()
}
