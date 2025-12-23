mod cpu;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jstring;

/// JNI function to detect CPU clusters
/// Returns JSON string with cluster information
#[no_mangle]
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

/// JNI function to read battery current
/// Returns current in milliamps (positive = charging, negative = discharging)
#[no_mangle]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: JNIEnv,
    _class: JClass,
) -> i32 {
    cpu::read_battery_current()
}

/// JNI function to read CPU load
/// Returns CPU load percentage (0.0 - 100.0)
#[no_mangle]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: JNIEnv,
    _class: JClass,
) -> f32 {
    cpu::read_cpu_load()
}

/// JNI function to read CPU temperature
/// Returns temperature in Celsius
#[no_mangle]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
) -> f32 {
    cpu::read_cpu_temperature()
}
