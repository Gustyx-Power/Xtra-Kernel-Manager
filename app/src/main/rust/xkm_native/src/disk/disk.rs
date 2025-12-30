// src/disk/disk.rs - Disk I/O monitoring

use crate::utils;
use serde::{Deserialize, Serialize};
use std::sync::Mutex;
use once_cell::sync::Lazy;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DiskStats {
    pub read_bytes: u64,
    pub write_bytes: u64,
    pub read_speed: u64,
    pub write_speed: u64,
}

struct DiskState {
    read_sectors: u64,
    write_sectors: u64,
    last_update: std::time::Instant,
}

static DISK_STATE: Lazy<Mutex<DiskState>> = Lazy::new(|| {
    Mutex::new(DiskState {
        read_sectors: 0,
        write_sectors: 0,
        last_update: std::time::Instant::now(),
    })
});

pub fn read_disk_stats() -> DiskStats {
    let devices = ["sda", "mmcblk0", "dm-0"];

    for dev in &devices {
        if let Some(stats) = read_device_stats(dev) {
            return stats;
        }
    }

    DiskStats {
        read_bytes: 0,
        write_bytes: 0,
        read_speed: 0,
        write_speed: 0,
    }
}

fn read_device_stats(device: &str) -> Option<DiskStats> {
    let mut buf = [0u8; 4096];
    let bytes_read = utils::read_file_libc_buf("/proc/diskstats", &mut buf)?;
    let content = std::str::from_utf8(&buf[..bytes_read]).ok()?;

    for line in content.lines() {
        if line.contains(device) {
            let parts: Vec<&str> = line.split_whitespace().collect();

            if parts.len() >= 14 {
                let read_sectors: u64 = parts[5].parse().ok()?;
                let write_sectors: u64 = parts[9].parse().ok()?;

                const SECTOR_SIZE: u64 = 512;
                let read_bytes = read_sectors * SECTOR_SIZE;
                let write_bytes = write_sectors * SECTOR_SIZE;

                let mut state = DISK_STATE.lock().unwrap();
                let now = std::time::Instant::now();
                let elapsed = now.duration_since(state.last_update).as_secs_f64();

                let read_speed = if elapsed > 0.0 && read_sectors >= state.read_sectors {
                    (((read_sectors - state.read_sectors) * SECTOR_SIZE) as f64 / elapsed) as u64
                } else {
                    0
                };

                let write_speed = if elapsed > 0.0 && write_sectors >= state.write_sectors {
                    (((write_sectors - state.write_sectors) * SECTOR_SIZE) as f64 / elapsed) as u64
                } else {
                    0
                };

                state.read_sectors = read_sectors;
                state.write_sectors = write_sectors;
                state.last_update = now;

                return Some(DiskStats {
                    read_bytes,
                    write_bytes,
                    read_speed,
                    write_speed,
                });
            }
        }
    }

    None
}
