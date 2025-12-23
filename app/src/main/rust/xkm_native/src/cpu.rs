use serde::Serialize;
use std::collections::HashMap;
use std::fs;
use std::path::Path;

/// CPU Cluster information
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

/// Read a sysfs file and return its contents as a string
fn read_sysfs_file(path: &str) -> Option<String> {
    fs::read_to_string(path).ok().map(|s| s.trim().to_string())
}

/// Read a sysfs file and parse as integer
fn read_sysfs_int(path: &str) -> Option<i32> {
    read_sysfs_file(path)?.parse().ok()
}

/// Read a sysfs file and parse as long
fn read_sysfs_long(path: &str) -> Option<i64> {
    read_sysfs_file(path)?.parse().ok()
}

/// Read a sysfs file and parse as float
fn read_sysfs_float(path: &str) -> Option<f32> {
    read_sysfs_file(path)?.parse().ok()
}

/// Detect all CPU clusters by grouping cores with same max frequency
pub fn detect_cpu_clusters() -> Vec<ClusterInfo> {
    let mut clusters = Vec::new();
    let mut available_cores = Vec::new();
    
    // Find all available CPU cores (0-15)
    for i in 0..16 {
        let cpu_path = format!("/sys/devices/system/cpu/cpu{}", i);
        if Path::new(&cpu_path).exists() {
            available_cores.push(i);
        }
    }
    
    if available_cores.is_empty() {
        return clusters;
    }
    
    // Group cores by max frequency
    let mut core_groups: HashMap<i32, Vec<i32>> = HashMap::new();
    
    for core in &available_cores {
        let max_freq_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/cpuinfo_max_freq",
            core
        );
        
        if let Some(max_freq) = read_sysfs_int(&max_freq_path) {
            if max_freq > 0 {
                core_groups.entry(max_freq).or_insert_with(Vec::new).push(*core);
            }
        }
    }
    
    // Sort groups by max frequency and create cluster info
    let mut sorted_groups: Vec<_> = core_groups.into_iter().collect();
    sorted_groups.sort_by_key(|(freq, _)| *freq);
    
    for (cluster_index, (max_freq, cores_in_group)) in sorted_groups.into_iter().enumerate() {
        let first_core = cores_in_group[0];
        let base_path = format!("/sys/devices/system/cpu/cpu{}", first_core);
        
        // Read frequency info
        let min_freq = read_sysfs_int(&format!("{}/cpufreq/cpuinfo_min_freq", base_path))
            .unwrap_or(0);
        let current_min = read_sysfs_int(&format!("{}/cpufreq/scaling_min_freq", base_path))
            .unwrap_or(min_freq);
        let current_max = read_sysfs_int(&format!("{}/cpufreq/scaling_max_freq", base_path))
            .unwrap_or(max_freq);
        
        // Read governor
        let governor = read_sysfs_file(&format!("{}/cpufreq/scaling_governor", base_path))
            .unwrap_or_else(|| "schedutil".to_string());
        
        // Read available governors
        let available_govs = read_sysfs_file(&format!(
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
            min_freq: min_freq / 1000,      // Convert kHz to MHz
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

/// Read battery current in milliamps
/// Positive = charging, Negative = discharging
pub fn read_battery_current() -> i32 {
    let paths = [
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/bms/current_now",
        "/sys/class/power_supply/Battery/current_now",
        "/sys/class/power_supply/main/current_now",
    ];
    
    for path in &paths {
        if let Some(value) = read_sysfs_long(path) {
            if value != 0 {
                // Convert microamps to milliamps
                return (value / 1000) as i32;
            }
        }
    }
    
    0
}

/// Read CPU load percentage from /proc/stat
pub fn read_cpu_load() -> f32 {
    let stat = match read_sysfs_file("/proc/stat") {
        Some(s) => s,
        None => return 0.0,
    };
    
    // Find the "cpu " line (total CPU stats)
    let cpu_line = match stat.lines().find(|line| line.starts_with("cpu ")) {
        Some(line) => line,
        None => return 0.0,
    };
    
    // Parse values: user, nice, system, idle, iowait, irq, softirq, steal
    let values: Vec<i64> = cpu_line
        .split_whitespace()
        .skip(1) // Skip "cpu" label
        .filter_map(|s| s.parse().ok())
        .collect();
    
    if values.len() < 4 {
        return 0.0;
    }
    
    let idle = values[3];
    let total: i64 = values.iter().sum();
    
    if total > 0 {
        ((total - idle) as f32 / total as f32) * 100.0
    } else {
        0.0
    }
}

/// Read CPU temperature from thermal zones
pub fn read_cpu_temperature() -> f32 {
    let thermal_paths = [
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp",
        "/sys/class/hwmon/hwmon0/temp1_input",
        "/sys/class/hwmon/hwmon1/temp1_input",
    ];
    
    for path in &thermal_paths {
        if let Some(temp) = read_sysfs_float(path) {
            if temp > 0.0 {
                // Some devices report in millidegrees, some in degrees
                return if temp > 1000.0 { temp / 1000.0 } else { temp };
            }
        }
    }
    
    0.0
}
