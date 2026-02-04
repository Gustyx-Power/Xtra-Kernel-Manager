use crate::utils;
use once_cell::sync::OnceCell;

static PRIMARY_THERMAL_ZONE: OnceCell<i32> = OnceCell::new();

fn get_primary_thermal_zone() -> i32 {
    *PRIMARY_THERMAL_ZONE.get_or_init(|| {
        for zone in 0..10 {
            let zone_type = get_thermal_zone_type(zone);
            let type_lower = zone_type.to_lowercase();

            if type_lower.contains("cpu") || type_lower.contains("tsens") || type_lower == "pa" {
                return zone;
            }
        }
        0
    })
}

pub fn read_cpu_temperature() -> f32 {
    let zone = get_primary_thermal_zone();
    read_thermal_zone(zone)
}

pub fn read_thermal_zone(zone: i32) -> f32 {
    let path = format!("/sys/class/thermal/thermal_zone{}/temp", zone);

    if let Some(temp) = utils::read_sysfs_float(&path, 500) {
        let temp_c = if temp > 1000.0 { temp / 1000.0 } else { temp };
        if temp_c > 0.0 && temp_c < 150.0 {
            return temp_c;
        }
    }

    0.0
}

pub fn get_thermal_zone_type(zone: i32) -> String {
    let path = format!("/sys/class/thermal/thermal_zone{}/type", zone);
    utils::read_sysfs_cached(&path, 0)
        .unwrap_or_else(|| format!("zone{}", zone))
}

use serde::Serialize;

#[derive(Serialize)]
struct ThermalZoneData {
    name: String,
    temp: f32,
}

pub fn read_thermal_zones() -> String {
    let mut zones = Vec::new();
    for zone in 0..100 {
        let temp = read_thermal_zone(zone);
        if temp > 0.0 {
            let zone_type = get_thermal_zone_type(zone);
            zones.push(ThermalZoneData {
                name: format!("{}:{}", zone, zone_type),
                temp,
            });
        }
    }

    serde_json::to_string(&zones).unwrap_or_else(|_| "[]".to_string())
}
