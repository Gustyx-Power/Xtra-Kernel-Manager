use crate::utils;
use serde::Serialize;
use std::collections::HashMap;
use std::sync::atomic::{AtomicI64, AtomicU64, Ordering};

static PREV_TOTAL: AtomicI64 = AtomicI64::new(0);
static PREV_IDLE: AtomicI64 = AtomicI64::new(0);
static LAST_LOAD_TIME: AtomicU64 = AtomicU64::new(0);
static CACHED_LOAD: AtomicI64 = AtomicI64::new(0); // Store as i64 * 100 for precision


#[derive(Serialize, Debug)]
pub struct ClusterInfo {
    pub cluster_number: i32,
    pub cores: Vec<i32>,
    pub min_freq: i32,
    pub max_freq: i32,
    pub current_min_freq: i32,
    pub current_max_freq: i32,
    pub governor: String,
    pub available_governors: Vec<String>,
    pub policy_path: String,
}

/// Read sysfs file using shared libc utils.
#[inline]
fn read_sysfs(path: &str) -> Option<String> {
    utils::read_file_libc(path)
}

/// Read sysfs file and parse to i32.
#[inline]
fn read_sysfs_int(path: &str) -> Option<i32> {
    read_sysfs(path)?.parse().ok()
}

/// Read sysfs file and parse to i64.
#[inline]
fn read_sysfs_long(path: &str) -> Option<i64> {
    read_sysfs(path)?.parse().ok()
}

/// Read sysfs file and parse to f32.
#[inline]
fn read_sysfs_float(path: &str) -> Option<f32> {
    read_sysfs(path)?.parse().ok()
}


pub fn detect_cpu_clusters() -> Vec<ClusterInfo> {
    let mut clusters = Vec::new();
    let mut available_cores = Vec::new();

    for i in 0..16 {
        let cpu_path = format!("/sys/devices/system/cpu/cpu{}", i);
        if utils::file_exists(&cpu_path) {
            available_cores.push(i);
        }
    }

    if available_cores.is_empty() {
        return clusters;
    }

    let mut core_groups: HashMap<i32, Vec<i32>> = HashMap::new();

    for core in &available_cores {
        let max_freq_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/cpuinfo_max_freq",
            core
        );

        if let Some(max_freq) = read_sysfs_int(&max_freq_path)
            && max_freq > 0 {
                core_groups
                    .entry(max_freq)
                    .or_default()
                    .push(*core);
            }
    }

    let mut sorted_groups: Vec<_> = core_groups.into_iter().collect();
    sorted_groups.sort_by_key(|(freq, _)| *freq);

    for (cluster_index, (max_freq, cores_in_group)) in sorted_groups.into_iter().enumerate() {
        let first_core = cores_in_group[0];
        let base_path = format!("/sys/devices/system/cpu/cpu{}", first_core);


        let min_freq =
            read_sysfs_int(&format!("{}/cpufreq/cpuinfo_min_freq", base_path)).unwrap_or(0);
        let current_min =
            read_sysfs_int(&format!("{}/cpufreq/scaling_min_freq", base_path)).unwrap_or(min_freq);
        let current_max =
            read_sysfs_int(&format!("{}/cpufreq/scaling_max_freq", base_path)).unwrap_or(max_freq);

        
        let governor = read_sysfs(&format!("{}/cpufreq/scaling_governor", base_path))
            .unwrap_or_else(|| "schedutil".to_string());

        
        let available_govs = read_sysfs(&format!(
            "{}/cpufreq/scaling_available_governors",
            base_path
        ))
        .map(|s| s.split_whitespace().map(String::from).collect())
        .unwrap_or_else(|| {
            vec![
                "schedutil".to_string(),
                "performance".to_string(),
                "powersave".to_string(),
            ]
        });

        let policy_path = format!("/sys/devices/system/cpu/cpufreq/policy{}", first_core);

        clusters.push(ClusterInfo {
            cluster_number: cluster_index as i32,
            cores: cores_in_group,
            min_freq: min_freq / 1000, 
            max_freq: max_freq / 1000,
            current_min_freq: current_min / 1000,
            current_max_freq: current_max / 1000,
            governor,
            available_governors: available_govs,
            policy_path,
        });
    }

    clusters
}

pub fn read_core_data() -> String {
    let mut cores_data = Vec::new();
    
    for core in 0..16 {
        let base_path = format!("/sys/devices/system/cpu/cpu{}", core);
        if !utils::file_exists(&base_path) {
            continue;
        }
        
        let is_online = if core == 0 {
            true
        } else {
            read_sysfs_int(&format!("{}/online", base_path)).unwrap_or(1) == 1
        };
        
        let (freq, governor) = if is_online {
            let freq = read_sysfs_int(&format!("{}/cpufreq/scaling_cur_freq", base_path))
                .unwrap_or(0) / 1000; 
            let gov = read_sysfs(&format!("{}/cpufreq/scaling_governor", base_path))
                .unwrap_or_else(|| "unknown".to_string());
            (freq, gov)
        } else {
            (0, "offline".to_string())
        };
        
        cores_data.push(format!(
            r#"{{"core":{},"online":{},"freq":{},"governor":"{}"}}"#,
            core, is_online, freq, governor
        ));
    }
    
    format!("[{}]", cores_data.join(","))
}

pub fn read_battery_current() -> i32 {
    let paths = [
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/bms/current_now",
        "/sys/class/power_supply/Battery/current_now",
        "/sys/class/power_supply/main/current_now",
    ];

    for path in &paths {
        if let Some(value) = read_sysfs_long(path)
            && value != 0 {
                return (value / 1000) as i32;
            }
    }

    0
}

pub fn read_cpu_load() -> f32 {
    let now_ms = utils::now_millis();
    
    let last_time = LAST_LOAD_TIME.load(Ordering::Relaxed);
    
    // If called within 100ms, return cached value
    if now_ms > 0 && last_time > 0 && now_ms - last_time < 100 {
        return CACHED_LOAD.load(Ordering::Relaxed) as f32 / 100.0;
    }
    
    // Read current /proc/stat values
    let (total, idle) = match read_proc_stat_values() {
        Some(v) => v,
        None => return CACHED_LOAD.load(Ordering::Relaxed) as f32 / 100.0,
    };
    
    // Get previous values
    let prev_total = PREV_TOTAL.load(Ordering::Relaxed);
    let prev_idle = PREV_IDLE.load(Ordering::Relaxed);
    
    // Calculate load if we have previous data
    let load = if prev_total > 0 {
        let total_diff = total - prev_total;
        let idle_diff = idle - prev_idle;
        
        if total_diff > 0 {
            ((total_diff - idle_diff) as f32 / total_diff as f32) * 100.0
        } else {
            CACHED_LOAD.load(Ordering::Relaxed) as f32 / 100.0
        }
    } else {
        0.0
    };
    
    PREV_TOTAL.store(total, Ordering::Relaxed);
    PREV_IDLE.store(idle, Ordering::Relaxed);
    LAST_LOAD_TIME.store(now_ms, Ordering::Relaxed);
    CACHED_LOAD.store((load * 100.0) as i64, Ordering::Relaxed);
    
    load
}


fn read_proc_stat_values() -> Option<(i64, i64)> {
    let mut buffer = [0u8; 512];
    let bytes = utils::read_file_libc_buf("/proc/stat", &mut buffer)?;
    
    let content = std::str::from_utf8(&buffer[..bytes]).ok()?;
    let cpu_line = content.lines().find(|line| line.starts_with("cpu "))?;
    
    let values: Vec<i64> = cpu_line
        .split_whitespace()
        .skip(1)
        .filter_map(|s| s.parse().ok())
        .collect();
    
    if values.len() < 4 {
        return None;
    }
    
    // user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice
    // idle = idle + iowait
    let idle = values[3] + values.get(4).unwrap_or(&0);
    let total: i64 = values.iter().sum();
    
    Some((total, idle))
}

pub fn read_cpu_temperature() -> f32 {
    let thermal_paths = [
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp",
        "/sys/class/hwmon/hwmon0/temp1_input",
        "/sys/class/hwmon/hwmon1/temp1_input",
    ];

    for path in &thermal_paths {
        if let Some(temp) = read_sysfs_float(path)
            && temp > 0.0 {
                return if temp > 1000.0 { temp / 1000.0 } else { temp };
            }
    }

    0.0
}
