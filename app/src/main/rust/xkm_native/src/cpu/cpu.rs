use crate::utils;
use once_cell::sync::{Lazy, OnceCell};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Mutex;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuCluster {
    pub cluster_number: i32,
    pub cores: Vec<i32>,
    pub max_freq: i32,
    pub min_freq: i32,
    #[serde(rename = "current_min_freq")]
    pub cur_min_freq: i32,
    #[serde(rename = "current_max_freq")]
    pub cur_max_freq: i32,
    pub governor: String,
    pub available_governors: Vec<String>,
    pub policy_path: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CoreInfo {
    #[serde(rename = "core")]
    pub core_number: i32,
    pub online: bool,
    #[serde(rename = "freq")]
    pub current_freq: i32,
    pub min_freq: i32,
    pub max_freq: i32,
    pub governor: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuLoadInfo {
    pub total_load: f32,
    pub per_core_load: Vec<f32>,
}

struct CpuStats {
    total_time: Vec<u64>,
    idle_time: Vec<u64>,
}

static CPU_STATS: Lazy<Mutex<CpuStats>> = Lazy::new(|| {
    Mutex::new(CpuStats {
        total_time: vec![0; 16],
        idle_time: vec![0; 16],
    })
});

static CPU_MODEL: OnceCell<String> = OnceCell::new();

/// Detect CPU clusters based on frequency ranges
pub fn detect_cpu_clusters() -> Vec<CpuCluster> {
    let mut clusters: HashMap<(i32, i32), Vec<i32>> = HashMap::new();

    for cpu in 0..16 {
        let policy_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_min_freq",
            cpu
        );

        if !utils::file_exists(&policy_path) {
            break;
        }

        let min_freq = utils::read_sysfs_int(&policy_path, 1000).unwrap_or(0) as i32;
        let max_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq",
            cpu
        );
        let max_freq = utils::read_sysfs_int(&max_path, 1000).unwrap_or(0) as i32;

        if min_freq > 0 && max_freq > 0 {
            clusters
                .entry((min_freq, max_freq))
                .or_insert_with(Vec::new)
                .push(cpu);
        }
    }

    let mut result: Vec<CpuCluster> = clusters
        .into_iter()
        .enumerate()
        .map(|(idx, ((min, max), cores))| {
            let first_core = cores[0];
            let governor_path = format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
                first_core
            );
            let governor = utils::read_sysfs_cached(&governor_path, 1000)
                .unwrap_or_else(|| "unknown".to_string());
            let available_governors = get_available_governors(first_core);

            // Read scaling_min/max_freq (current limits)
            let cur_min_path = format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_min_freq",
                first_core
            );
            let cur_max_path = format!(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq",
                first_core
            );
            let cur_min_freq = utils::read_sysfs_int(&cur_min_path, 1000).unwrap_or(min);
            let cur_max_freq = utils::read_sysfs_int(&cur_max_path, 1000).unwrap_or(max);

            CpuCluster {
                cluster_number: idx as i32,
                cores,
                min_freq: min, // Hardware min
                max_freq: max, // Hardware max
                cur_min_freq,
                cur_max_freq,
                governor,
                available_governors,
                policy_path: format!("/sys/devices/system/cpu/cpu{}/cpufreq", first_core),
            }
        })
        .collect();

    result.sort_by_key(|c| c.min_freq);

    for (idx, cluster) in result.iter_mut().enumerate() {
        cluster.cluster_number = idx as i32;
    }

    result
}

/// Read comprehensive core data as JSON string
pub fn read_core_data() -> String {
    let mut cores = Vec::new();

    for cpu in 0..16 {
        let online_path = format!("/sys/devices/system/cpu/cpu{}/online", cpu);
        let freq_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_cur_freq",
            cpu
        );

        let online = if cpu == 0 {
            true
        } else {
            utils::read_sysfs_int(&online_path, 100).unwrap_or(0) == 1
        };

        if !online && !utils::file_exists(&freq_path) {
            break;
        }

        let current_freq = if online {
            utils::read_sysfs_int(&freq_path, 100).unwrap_or(0) as i32
        } else {
            0
        };

        let min_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_min_freq",
            cpu
        );
        let max_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq",
            cpu
        );
        let gov_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
            cpu
        );

        let core = CoreInfo {
            core_number: cpu,
            online,
            current_freq,
            min_freq: utils::read_sysfs_int(&min_path, 1000).unwrap_or(0) as i32,
            max_freq: utils::read_sysfs_int(&max_path, 1000).unwrap_or(0) as i32,
            governor: utils::read_sysfs_cached(&gov_path, 1000)
                .unwrap_or_else(|| "unknown".to_string()),
        };

        cores.push(core);
    }

    serde_json::to_string(&cores).unwrap_or_else(|_| "[]".to_string())
}

/// Read CPU load (total + per-core)
pub fn read_cpu_load_detailed() -> CpuLoadInfo {
    let mut per_core_load = Vec::new();

    let mut buf = [0u8; 4096];
    if let Some(bytes_read) = utils::read_file_libc_buf("/proc/stat", &mut buf) {
        if let Ok(content) = std::str::from_utf8(&buf[..bytes_read]) {
            let mut total_load = 0.0f32;
            let mut cpu_index = 0;

            let mut stats = CPU_STATS.lock().unwrap();

            for line in content.lines() {
                if line.starts_with("cpu") && !line.starts_with("cpu ") {
                    let parts: Vec<&str> = line.split_whitespace().collect();

                    if parts.len() >= 5 {
                        let user: u64 = parts[1].parse().unwrap_or(0);
                        let nice: u64 = parts[2].parse().unwrap_or(0);
                        let system: u64 = parts[3].parse().unwrap_or(0);
                        let idle: u64 = parts[4].parse().unwrap_or(0);

                        let total = user + nice + system + idle;

                        let load = if cpu_index < stats.total_time.len() {
                            let total_diff = total.saturating_sub(stats.total_time[cpu_index]);
                            let idle_diff = idle.saturating_sub(stats.idle_time[cpu_index]);

                            if total_diff > 0 {
                                100.0 - (idle_diff as f32 / total_diff as f32 * 100.0)
                            } else {
                                0.0
                            }
                        } else {
                            0.0
                        };

                        per_core_load.push(load.max(0.0).min(100.0));

                        if cpu_index < stats.total_time.len() {
                            stats.total_time[cpu_index] = total;
                            stats.idle_time[cpu_index] = idle;
                        } else {
                            stats.total_time.push(total);
                            stats.idle_time.push(idle);
                        }

                        cpu_index += 1;
                    }
                }
            }

            if !per_core_load.is_empty() {
                total_load = per_core_load.iter().sum::<f32>() / per_core_load.len() as f32;
            }

            return CpuLoadInfo {
                total_load,
                per_core_load,
            };
        }
    }

    CpuLoadInfo {
        total_load: 0.0,
        per_core_load: vec![],
    }
}

/// Quick total CPU load
pub fn read_cpu_load() -> f32 {
    read_cpu_load_detailed().total_load
}

/// Read per-core temperature
pub fn read_core_temperature(core: i32) -> f32 {
    let paths = [
        format!("/sys/class/hwmon/hwmon1/temp{}_input", core + 1),
        format!("/sys/devices/virtual/thermal/thermal_zone{}/temp", core),
    ];

    for path in &paths {
        if let Some(temp) = utils::read_sysfs_float(path, 500) {
            let temp_c = if temp > 1000.0 { temp / 1000.0 } else { temp };
            if temp_c > 0.0 && temp_c < 150.0 {
                return temp_c;
            }
        }
    }

    0.0
}

/// Get CPU model name
pub fn get_cpu_model() -> String {
    CPU_MODEL
        .get_or_init(|| {
            let mut buf = [0u8; 2048];
            if let Some(bytes_read) = utils::read_file_libc_buf("/proc/cpuinfo", &mut buf) {
                if let Ok(content) = std::str::from_utf8(&buf[..bytes_read]) {
                    for line in content.lines() {
                        if line.starts_with("Hardware") {
                            if let Some(model) = line.split(':').nth(1) {
                                return model.trim().to_string();
                            }
                        }
                        if line.starts_with("Processor") {
                            if let Some(model) = line.split(':').nth(1) {
                                return model.trim().to_string();
                            }
                        }
                    }
                }
            }
            "Unknown".to_string()
        })
        .clone()
}

/// Get available CPU governors
pub fn get_available_governors(cpu: i32) -> Vec<String> {
    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_available_governors",
        cpu
    );

    if let Some(content) = utils::read_sysfs_cached(&path, 0) {
        return content.split_whitespace().map(|s| s.to_string()).collect();
    }

    vec![]
}

/// Get current governor for CPU
pub fn get_current_governor(cpu: i32) -> String {
    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
        cpu
    );
    utils::read_sysfs_cached(&path, 1000).unwrap_or_else(|| "unknown".to_string())
}

/// Get available CPU frequencies
pub fn get_available_frequencies(cpu: i32) -> Vec<i32> {
    let path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_available_frequencies",
        cpu
    );

    if let Some(content) = utils::read_sysfs_cached(&path, 0) {
        return content
            .split_whitespace()
            .filter_map(|s| s.parse::<i32>().ok())
            .map(|khz| khz / 1000)
            .collect();
    }

    vec![]
}

/// Get available CPU scaling governors (system-wide)
pub fn get_system_available_governors() -> Vec<String> {
    // Try CPU0 first
    let governors = get_available_governors(0);
    if !governors.is_empty() {
        return governors;
    }

    // Fallback: try other CPUs
    for cpu in 1..8 {
        let governors = get_available_governors(cpu);
        if !governors.is_empty() {
            return governors;
        }
    }

    vec![]
}

/// Get CPU frequency scaling driver
pub fn get_cpu_freq_driver() -> String {
    let path = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_driver";
    utils::read_sysfs_cached(path, 0).unwrap_or_else(|| "unknown".to_string())
}

/// Get CPU frequency policy (per-cluster info)
pub fn get_cpu_policy_info(cpu: i32) -> Option<CpuPolicyInfo> {
    let base = format!("/sys/devices/system/cpu/cpu{}/cpufreq", cpu);

    if !utils::file_exists(&format!("{}/scaling_governor", base)) {
        return None;
    }

    Some(CpuPolicyInfo {
        cpu,
        governor: utils::read_sysfs_cached(&format!("{}/scaling_governor", base), 1000)?,
        min_freq: utils::read_sysfs_int(&format!("{}/scaling_min_freq", base), 1000)? as i32 / 1000,
        max_freq: utils::read_sysfs_int(&format!("{}/scaling_max_freq", base), 1000)? as i32 / 1000,
        cur_freq: utils::read_sysfs_int(&format!("{}/scaling_cur_freq", base), 100)? as i32 / 1000,
    })
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuPolicyInfo {
    pub cpu: i32,
    pub governor: String,
    pub min_freq: i32,
    pub max_freq: i32,
    pub cur_freq: i32,
}
