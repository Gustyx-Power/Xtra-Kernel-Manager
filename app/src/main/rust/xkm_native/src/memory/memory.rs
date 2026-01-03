use crate::utils;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryInfo {
    pub total_kb: i64,
    pub available_kb: i64,
    pub free_kb: i64,
    pub cached_kb: i64,
    pub buffers_kb: i64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SwapInfo {
    pub total_kb: i64,
    pub free_kb: i64,
    pub used_kb: i64,
    pub cached_kb: i64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ZramStats {
    pub disksize: i64,
    pub orig_data_size: i64,
    pub compr_data_size: i64,
    pub mem_used_total: i64,
    pub compression_ratio: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemInfoDetailed {
    pub memory: MemoryInfo,
    pub swap: SwapInfo,
    pub zram: ZramStats,
    pub swappiness: i32,
}

/// Read basic memory info from /proc/meminfo
pub fn read_memory_info() -> MemoryInfo {
    let mut total = 0i64;
    let mut available = 0i64;
    let mut free = 0i64;
    let mut cached = 0i64;
    let mut buffers = 0i64;

    let mut buf = [0u8; 4096];
    if let Some(bytes_read) = utils::read_file_libc_buf("/proc/meminfo", &mut buf) {
        if let Ok(content) = std::str::from_utf8(&buf[..bytes_read]) {
            for line in content.lines() {
                let parts: Vec<&str> = line.split_whitespace().collect();
                if parts.len() >= 2 {
                    if let Ok(value) = parts[1].parse::<i64>() {
                        match parts[0] {
                            "MemTotal:" => total = value,
                            "MemAvailable:" => available = value,
                            "MemFree:" => free = value,
                            "Cached:" => cached = value,
                            "Buffers:" => buffers = value,
                            _ => {}
                        }
                    }
                }
            }
        }
    }

    MemoryInfo {
        total_kb: total,
        available_kb: available,
        free_kb: free,
        cached_kb: cached,
        buffers_kb: buffers,
    }
}

/// Read swap information
pub fn read_swap_info() -> SwapInfo {
    let mut total = 0i64;
    let mut free = 0i64;
    let mut cached = 0i64;

    let mut buf = [0u8; 4096];
    if let Some(bytes_read) = utils::read_file_libc_buf("/proc/meminfo", &mut buf) {
        if let Ok(content) = std::str::from_utf8(&buf[..bytes_read]) {
            for line in content.lines() {
                let parts: Vec<&str> = line.split_whitespace().collect();
                if parts.len() >= 2 {
                    if let Ok(value) = parts[1].parse::<i64>() {
                        match parts[0] {
                            "SwapTotal:" => total = value,
                            "SwapFree:" => free = value,
                            "SwapCached:" => cached = value,
                            _ => {}
                        }
                    }
                }
            }
        }
    }

    SwapInfo {
        total_kb: total,
        free_kb: free,
        used_kb: total - free,
        cached_kb: cached,
    }
}

/// Read ZRAM statistics

pub fn read_zram_stats() -> ZramStats {
    let disksize = utils::read_sysfs_int("/sys/block/zram0/disksize", 1000).unwrap_or(0);

    let mut orig_data_size = 0i64;
    let mut compr_data_size = 0i64;
    let mut mem_used_total = 0i64;

    if let Some(mm_stat) = utils::read_sysfs_cached("/sys/block/zram0/mm_stat", 1000) {
        let parts: Vec<&str> = mm_stat.split_whitespace().collect();
        if parts.len() >= 3 {
            orig_data_size = parts[0].parse().unwrap_or(0);
            compr_data_size = parts[1].parse().unwrap_or(0);
            mem_used_total = parts[2].parse().unwrap_or(0);
        }
    }

    let compression_ratio = if compr_data_size > 0 {
        orig_data_size as f32 / compr_data_size as f32
    } else {
        1.0
    };

    ZramStats {
        disksize: disksize.into(),
        orig_data_size,
        compr_data_size,
        mem_used_total,
        compression_ratio,
    }
}

/// Read swappiness value
pub fn read_swappiness() -> i32 {
    utils::read_sysfs_int("/proc/sys/vm/swappiness", 1000).unwrap_or(60) as i32
}

/// Read all memory info in one call
pub fn read_memory_info_detailed() -> MemInfoDetailed {
    MemInfoDetailed {
        memory: read_memory_info(),
        swap: read_swap_info(),
        zram: read_zram_stats(),
        swappiness: read_swappiness(),
    }
}

/// Get available ZRAM compression algorithms
pub fn get_available_zram_algorithms() -> Vec<String> {
    let path = "/sys/block/zram0/comp_algorithm";

    if let Some(content) = utils::read_sysfs_cached(path, 0) {
        return content
            .split_whitespace()
            .map(|s| s.trim_matches(|c| c == '[' || c == ']').to_string())
            .collect();
    }

    vec![]
}

/// Get current ZRAM compression algorithm
pub fn get_current_zram_algorithm() -> String {
    let path = "/sys/block/zram0/comp_algorithm";

    if let Some(content) = utils::read_sysfs_cached(path, 1000) {
        if let Some(start) = content.find('[') {
            if let Some(end) = content.find(']') {
                return content[start + 1..end].to_string();
            }
        }
    }

    "unknown".to_string()
}

/// Get ZRAM device count
pub fn get_zram_device_count() -> i32 {
    for i in 0..8 {
        let path = format!("/sys/block/zram{}/disksize", i);
        if !utils::file_exists(&path) {
            return i;
        }
    }
    8
}

/// Get ZRAM device info for specific device

pub fn read_zram_device_stats(device: i32) -> Option<ZramStats> {
    let disksize_path = format!("/sys/block/zram{}/disksize", device);
    let mm_stat_path = format!("/sys/block/zram{}/mm_stat", device);

    if !utils::file_exists(&disksize_path) {
        return None;
    }

    let disksize = utils::read_sysfs_int(&disksize_path, 1000)?;

    let mm_stat = utils::read_sysfs_cached(&mm_stat_path, 1000)?;
    let parts: Vec<&str> = mm_stat.split_whitespace().collect();

    if parts.len() >= 3 {
        let orig_data_size = parts[0].parse().ok()?;
        let compr_data_size = parts[1].parse().ok()?;
        let mem_used_total = parts[2].parse().ok()?;

        let compression_ratio = if compr_data_size > 0 {
            orig_data_size as f32 / compr_data_size as f32
        } else {
            1.0
        };

        return Some(ZramStats {
            disksize: disksize.into(),
            orig_data_size,
            compr_data_size,
            mem_used_total,
            compression_ratio,
        });
    }

    None
}

/// Get memory pressure percentage
pub fn get_memory_pressure() -> f32 {
    let info = read_memory_info();
    if info.total_kb > 0 {
        let used = info.total_kb - info.available_kb;
        (used as f32 / info.total_kb as f32) * 100.0
    } else {
        0.0
    }
}

/// Get ZRAM compression ratio (quick access)
pub fn get_zram_compression_ratio() -> f32 {
    read_zram_stats().compression_ratio
}

/// Get ZRAM compressed size in bytes
pub fn get_zram_compressed_size() -> i64 {
    read_zram_stats().compr_data_size
}

/// Get ZRAM original data size in bytes (uncompressed data stored in ZRAM)
pub fn get_zram_orig_data_size() -> i64 {
    read_zram_stats().orig_data_size
}

/// Get ZRAM algorithm (alias)
pub fn get_zram_algorithm() -> String {
    get_current_zram_algorithm()
}

/// Get swappiness (alias)
pub fn get_swappiness() -> i32 {
    read_swappiness()
}

/// Read meminfo (alias for backward compatibility)
pub fn read_meminfo() -> MemoryInfo {
    read_memory_info()
}

/// Read ZRAM size (alias for backward compatibility)
pub fn read_zram_size() -> i64 {
    read_zram_stats().disksize
}

/// Get detailed memory info as JSON string
pub fn read_meminfo_detailed() -> String {
    let info = read_memory_info_detailed();
    serde_json::to_string(&info).unwrap_or_else(|_| "{}".to_string())
}
