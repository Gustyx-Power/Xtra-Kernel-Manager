use crate::utils;
use once_cell::sync::{Lazy, OnceCell};
use serde::{Deserialize, Serialize};
use std::process::Command;
use std::sync::Mutex;

#[derive(Debug, Clone, PartialEq)]
pub enum GpuVendor {
    Qualcomm,
    Mali,
    PowerVR,
    Nvidia,
    Unknown,
}

impl std::fmt::Display for GpuVendor {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            GpuVendor::Qualcomm => write!(f, "Qualcomm"),
            GpuVendor::Mali => write!(f, "ARM Mali"),
            GpuVendor::PowerVR => write!(f, "PowerVR"),
            GpuVendor::Nvidia => write!(f, "NVIDIA"),
            GpuVendor::Unknown => write!(f, "Unknown"),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GpuLoad {
    pub frequency_mhz: i32,
    pub busy_percent: i32,
    pub temperature: f32,
    pub throttled: bool,
}

struct GpuBusyStats {
    busy: i64,
    total: i64,
}

static GPU_INFO: OnceCell<(GpuVendor, String)> = OnceCell::new();
static LAST_GPU_BUSY: Lazy<Mutex<Option<GpuBusyStats>>> = Lazy::new(|| Mutex::new(None));

/// Detect GPU vendor and model
fn detect_gpu() -> (GpuVendor, String) {
    // Method 1
    if utils::file_exists("/sys/class/kgsl/kgsl-3d0/gpuclk") {
        if let Some(model) = detect_adreno_model() {
            return (GpuVendor::Qualcomm, model);
        }
        return (GpuVendor::Qualcomm, "Adreno".to_string());
    }

    // Method 2
    let mali_paths = [
        "/sys/class/misc/mali0/device/gpuinfo",
        "/sys/devices/platform/mali/gpuinfo",
        "/dev/mali0",
    ];

    for path in &mali_paths {
        if utils::file_exists(path) {
            if let Some(model) = detect_mali_model() {
                return (GpuVendor::Mali, model);
            }
            return (GpuVendor::Mali, "Mali".to_string());
        }
    }

    // Method 3
    if let Some((vendor, model)) = detect_via_surfaceflinger() {
        return (vendor, model);
    }

    // Method 4
    if let Some((vendor, model)) = detect_via_cpuinfo() {
        return (vendor, model);
    }

    // Method 5
    if let Some((vendor, model)) = detect_via_getprop() {
        return (vendor, model);
    }

    (GpuVendor::Unknown, "Unknown GPU".to_string())
}

fn detect_adreno_model() -> Option<String> {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_model",
        "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_model",
    ];

    for path in &paths {
        if let Some(model) = utils::read_sysfs_cached(path, 0) {
            let model = model.trim();
            if model.to_lowercase().starts_with("adreno") {
                return Some(model.to_string());
            }
            return Some(format!("Adreno {}", model));
        }
    }

    None
}

fn detect_mali_model() -> Option<String> {
    let paths = [
        "/sys/class/misc/mali0/device/gpuinfo",
        "/sys/devices/platform/mali/gpuinfo",
    ];

    for path in &paths {
        if let Some(model) = utils::read_sysfs_cached(path, 0) {
            let model = model.trim();
            if model.to_lowercase().starts_with("mali") {
                return Some(model.to_string());
            }
            return Some(format!("Mali {}", model));
        }
    }

    None
}

fn detect_via_surfaceflinger() -> Option<(GpuVendor, String)> {
    let output = Command::new("dumpsys")
        .arg("SurfaceFlinger")
        .output()
        .ok()?;

    let content = String::from_utf8_lossy(&output.stdout);

    for line in content.lines() {
        let line_lower = line.to_lowercase();

        if line_lower.contains("gles") || line_lower.contains("renderer") {
            if line_lower.contains("adreno") {
                if let Some(model) = extract_adreno_version(&line) {
                    return Some((GpuVendor::Qualcomm, format!("Adreno {}", model)));
                }
                return Some((GpuVendor::Qualcomm, "Adreno".to_string()));
            }

            if line_lower.contains("mali") {
                if let Some(model) = extract_mali_version(&line) {
                    return Some((GpuVendor::Mali, format!("Mali {}", model)));
                }
                return Some((GpuVendor::Mali, "Mali".to_string()));
            }

            if line_lower.contains("powervr") {
                return Some((GpuVendor::PowerVR, "PowerVR".to_string()));
            }

            if line_lower.contains("nvidia") || line_lower.contains("tegra") {
                return Some((GpuVendor::Nvidia, "Tegra".to_string()));
            }
        }
    }

    None
}

fn detect_via_cpuinfo() -> Option<(GpuVendor, String)> {
    let content = std::fs::read_to_string("/proc/cpuinfo").ok()?;
    let content_lower = content.to_lowercase();

    if content_lower.contains("adreno") {
        return Some((GpuVendor::Qualcomm, "Adreno".to_string()));
    }

    if content_lower.contains("mali") {
        return Some((GpuVendor::Mali, "Mali".to_string()));
    }

    None
}

fn detect_via_getprop() -> Option<(GpuVendor, String)> {
    // Try native property getter (faster)
    if let Some(vulkan_hw) = utils::get_system_property("ro.hardware.vulkan") {
        if vulkan_hw.to_lowercase().contains("adreno") {
            return Some((GpuVendor::Qualcomm, "Adreno".to_string()));
        }

        if vulkan_hw.to_lowercase().contains("mali") {
            return Some((GpuVendor::Mali, "Mali".to_string()));
        }
    }

    None
}

fn extract_adreno_version(s: &str) -> Option<String> {
    let words: Vec<&str> = s.split_whitespace().collect();

    for (i, word) in words.iter().enumerate() {
        if word.to_lowercase().contains("adreno") {
            for j in 1..4 {
                if i + j < words.len() {
                    let candidate = words[i + j].trim_matches(|c: char| !c.is_numeric());

                    if let Ok(num) = candidate.parse::<u32>() {
                        if num >= 200 && num <= 900 {
                            return Some(num.to_string());
                        }
                    }
                }
            }
        }
    }

    None
}

fn extract_mali_version(s: &str) -> Option<String> {
    let words: Vec<&str> = s.split(&[' ', '-'][..]).collect();

    for (i, word) in words.iter().enumerate() {
        if word.to_lowercase().contains("mali") {
            if i + 1 < words.len() {
                let version = words[i + 1].trim();
                if !version.is_empty() {
                    return Some(version.to_string());
                }
            }
        }
    }

    None
}

pub fn get_gpu_vendor() -> GpuVendor {
    GPU_INFO.get_or_init(detect_gpu).0.clone()
}

pub fn get_gpu_model() -> String {
    GPU_INFO.get_or_init(detect_gpu).1.clone()
}

pub fn read_gpu_freq() -> i32 {
    let vendor = get_gpu_vendor();

    match vendor {
        GpuVendor::Qualcomm => read_adreno_freq(),
        GpuVendor::Mali => read_mali_freq(),
        _ => 0,
    }
}

fn read_adreno_freq() -> i32 {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/gpuclk",
        "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
        "/sys/class/kgsl/kgsl-3d0/clock_mhz",
    ];

    for path in &paths {
        if let Some(freq) = utils::read_sysfs_int(path, 200) {
            if freq > 1_000_000 {
                return (freq / 1_000_000) as i32;
            } else if freq > 1000 {
                return (freq / 1000) as i32;
            } else {
                return freq as i32;
            }
        }
    }

    0
}

fn read_mali_freq() -> i32 {
    let paths = [
        "/sys/class/misc/mali0/device/clock",
        "/sys/devices/platform/mali/clock",
    ];

    for path in &paths {
        if let Some(freq) = utils::read_sysfs_int(path, 200) {
            if freq > 1_000_000 {
                return (freq / 1_000_000) as i32;
            }
            return freq as i32;
        }
    }

    0
}

pub fn read_gpu_busy() -> i32 {
    let vendor = get_gpu_vendor();

    match vendor {
        GpuVendor::Qualcomm => read_adreno_busy(),
        GpuVendor::Mali => read_mali_busy(),
        _ => 0,
    }
}

fn read_adreno_busy() -> i32 {
    if let Some(busy) = utils::read_sysfs_int("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage", 200) {
        return busy as i32;
    }

    // Method 2: Cumulative counters (Needs delta calculation)
    // Method 2: Cumulative counters (Needs delta calculation)
    // Use uncached read to ensure we get fresh counters every time
    if let Some(content) = utils::read_sysfs("/sys/class/kgsl/kgsl-3d0/gpubusy") {
        let parts: Vec<&str> = content.split_whitespace().collect();
        if parts.len() >= 2 {
            if let (Ok(curr_busy), Ok(curr_total)) =
                (parts[0].parse::<i64>(), parts[1].parse::<i64>())
            {
                let mut last_processed = LAST_GPU_BUSY.lock().unwrap();

                if let Some(last) = &*last_processed {
                    let delta_busy;
                    let delta_total;

                    // Check for reset/overflow
                    if curr_total < last.total {
                        // Reset: Treat current values as the delta
                        delta_busy = curr_busy;
                        delta_total = curr_total;
                    } else {
                        delta_busy = curr_busy.saturating_sub(last.busy);
                        delta_total = curr_total.saturating_sub(last.total);
                    }

                    // Update state
                    *last_processed = Some(GpuBusyStats {
                        busy: curr_busy,
                        total: curr_total,
                    });

                    if delta_total > 0 {
                        // Calculate percentage
                        let load = (delta_busy * 100) / delta_total;
                        return load.min(100) as i32;
                    } else {
                        // No change in cycles = 0% load
                        return 0;
                    }
                } else {
                    // First run, initialize state
                    *last_processed = Some(GpuBusyStats {
                        busy: curr_busy,
                        total: curr_total,
                    });
                }
            }
        }
    }

    0
}

fn read_mali_busy() -> i32 {
    let paths = [
        "/sys/class/misc/mali0/device/utilization",
        "/sys/devices/platform/mali/utilization",
    ];

    for path in &paths {
        if let Some(busy) = utils::read_sysfs_int(path, 200) {
            return busy as i32;
        }
    }

    0
}

/// Get GPU available frequencies (for UI sliders/charts)
pub fn get_gpu_available_frequencies() -> Vec<i32> {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies",
        "/sys/class/kgsl/kgsl-3d0/devfreq/available_frequencies",
    ];

    for path in &paths {
        if let Some(content) = utils::read_sysfs_cached(path, 0) {
            let freqs: Vec<i32> = content
                .split_whitespace()
                .filter_map(|s| s.parse::<i64>().ok())
                .map(|hz| {
                    if hz > 1_000_000 {
                        (hz / 1_000_000) as i32
                    } else if hz > 1000 {
                        (hz / 1000) as i32
                    } else {
                        hz as i32
                    }
                })
                .collect();

            if !freqs.is_empty() {
                return freqs;
            }
        }
    }

    vec![]
}

/// Get GPU min/max frequency range
pub fn get_gpu_freq_range() -> (i32, i32) {
    let freqs = get_gpu_available_frequencies();
    if freqs.is_empty() {
        return (0, 0);
    }

    let min = *freqs.iter().min().unwrap_or(&0);
    let max = *freqs.iter().max().unwrap_or(&0);
    (min, max)
}

/// Get comprehensive GPU load info (frequency + busy + temp + throttling)
pub fn read_gpu_load() -> GpuLoad {
    let freq = read_gpu_freq();
    let busy = read_gpu_busy();
    let temp = read_gpu_temperature();

    // Check if throttled (current freq < 90% of max)
    let (_, max_freq) = get_gpu_freq_range();
    let throttled = max_freq > 0 && freq > 0 && freq < max_freq * 9 / 10;

    GpuLoad {
        frequency_mhz: freq,
        busy_percent: busy,
        temperature: temp,
        throttled,
    }
}

/// Read GPU temperature from thermal zones
fn read_gpu_temperature() -> f32 {
    let zone_names = ["gpu", "gpuss", "gpu0", "gpu1"];

    for zone in 0..20 {
        let type_path = format!("/sys/class/thermal/thermal_zone{}/type", zone);
        if let Some(zone_type) = utils::read_sysfs_cached(&type_path, 0) {
            let zone_lower = zone_type.to_lowercase();

            for name in &zone_names {
                if zone_lower.contains(name) {
                    let temp_path = format!("/sys/class/thermal/thermal_zone{}/temp", zone);
                    if let Some(temp) = utils::read_sysfs_int(&temp_path, 500) {
                        let temp_c = temp as f32 / 1000.0;
                        if temp_c > 0.0 && temp_c < 150.0 {
                            return temp_c;
                        }
                    }
                }
            }
        }
    }

    0.0
}

/// Get GPU power policy (if available)
pub fn get_gpu_power_policy() -> String {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/devfreq/governor",
        "/sys/class/kgsl/kgsl-3d0/governor",
    ];

    for path in &paths {
        if let Some(policy) = utils::read_sysfs_cached(path, 1000) {
            return policy;
        }
    }

    "unknown".to_string()
}

/// Get available GPU power policies
pub fn get_gpu_available_policies() -> Vec<String> {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors",
        "/sys/class/kgsl/kgsl-3d0/available_governors",
    ];

    for path in &paths {
        if let Some(content) = utils::read_sysfs_cached(path, 0) {
            return content.split_whitespace().map(|s| s.to_string()).collect();
        }
    }

    vec![]
}

/// Get GPU driver info
pub fn get_gpu_driver_info() -> String {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_model",
        "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_model",
    ];

    for path in &paths {
        if let Some(model) = utils::read_sysfs_cached(path, 0) {
            return model;
        }
    }

    "unknown".to_string()
}
