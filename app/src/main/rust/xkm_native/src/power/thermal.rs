// src/power/thermal.rs - Fixed
use crate::utils;
use once_cell::sync::OnceCell;

const THERMAL_PATHS: &[&str] = &[
    "/sys/class/thermal/thermal_zone0/temp",
    "/sys/devices/virtual/thermal/thermal_zone0/temp",
    "/sys/class/hwmon/hwmon0/temp1_input",
    "/sys/class/hwmon/hwmon1/temp1_input",
];

static PRIMARY_THERMAL_ZONE: OnceCell<i32> = OnceCell::new();

/// Find primary CPU thermal zone
fn get_primary_thermal_zone() -> i32 {
    *PRIMARY_THERMAL_ZONE.get_or_init(|| {
        // Try to find CPU-related thermal zone
        for zone in 0..10 {
            let zone_type = get_thermal_zone_type(zone); // Return String directly
            let type_lower = zone_type.to_lowercase();

            if type_lower.contains("cpu") ||
               type_lower.contains("tsens") ||
               type_lower == "pa" {
                return zone;
            }
        }
        0 // Fallback to zone 0
    })
}

/// Read CPU temperature (auto-detect best zone)
pub fn read_cpu_temperature() -> f32 {
    let zone = get_primary_thermal_zone();
    read_thermal_zone(zone)
}

/// Read specific thermal zone temperature
pub fn read_thermal_zone(zone: i32) -> f32 {
    let path = format!("/sys/class/thermal/thermal_zone{}/temp", zone);

    if let Some(temp) = utils::read_sysfs_float(&path, 500) {
        let temp_c = if temp > 1000.0 { temp / 1000.0 } else { temp };

        // Filter invalid temps (sensor errors)
        if temp_c > 0.0 && temp_c < 150.0 {
            return temp_c;
        }
    }

    0.0
}

/// Get thermal zone type/name - Returns String directly (not Option)
pub fn get_thermal_zone_type(zone: i32) -> String {
    let path = format!("/sys/class/thermal/thermal_zone{}/type", zone);
    utils::read_sysfs_cached(&path, 0) // Cache permanently
        .unwrap_or_else(|| format!("zone{}", zone))
}

/// List all valid thermal zones
pub fn read_thermal_zones() -> String {
    let mut zones = Vec::new();

    for zone in 0..84 { // Max zones on modern devices
        let temp = read_thermal_zone(zone);
        if temp > 0.0 {
            let zone_type = get_thermal_zone_type(zone);
            zones.push(format!("{}:{}:{:.1}", zone, zone_type, temp));
        }
    }

    zones.join(",")
}

/// Get hottest thermal zone
pub fn get_hottest_zone() -> (i32, f32) {
    let mut hottest_zone = 0;
    let mut hottest_temp = 0.0f32;

    for zone in 0..20 {
        let temp = read_thermal_zone(zone);
        if temp > hottest_temp {
            hottest_temp = temp;
            hottest_zone = zone;
        }
    }

    (hottest_zone, hottest_temp)
}
