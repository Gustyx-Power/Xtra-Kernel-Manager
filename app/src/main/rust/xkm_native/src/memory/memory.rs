use crate::utils;
use serde::Serialize;

#[derive(Serialize, Debug, Default)]
pub struct MemInfo {
    pub total_kb: i64,
    pub available_kb: i64,
    pub free_kb: i64,
    pub cached_kb: i64,
    pub buffers_kb: i64,
    pub swap_total_kb: i64,
    pub swap_free_kb: i64,
}

pub fn read_meminfo() -> MemInfo {
    let mut info = MemInfo::default();

    let mut buf = [0u8; 2048];
    let Some(bytes) = utils::read_file_libc_buf("/proc/meminfo", &mut buf) else {
        return info;
    };

    let Ok(content) = std::str::from_utf8(&buf[..bytes]) else {
        return info;
    };

    for line in content.lines() {
        let mut parts = line.split_whitespace();
        let Some(key) = parts.next() else { continue };
        let Some(value) = parts.next() else { continue };
        let Ok(val) = value.parse::<i64>() else {
            continue;
        };

        match key {
            "MemTotal:" => info.total_kb = val,
            "MemAvailable:" => info.available_kb = val,
            "MemFree:" => info.free_kb = val,
            "Cached:" => info.cached_kb = val,
            "Buffers:" => info.buffers_kb = val,
            "SwapTotal:" => info.swap_total_kb = val,
            "SwapFree:" => info.swap_free_kb = val,
            _ => {}
        }
    }

    info
}

pub fn read_zram_size() -> i64 {
    utils::read_file_libc("/sys/block/zram0/disksize")
        .and_then(|s| s.parse().ok())
        .unwrap_or(0)
}
