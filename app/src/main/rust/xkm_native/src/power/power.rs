use std::ffi::CString;
use std::process::Command;
use std::sync::atomic::{AtomicI64, AtomicU64, Ordering};
use std::time::{SystemTime, UNIX_EPOCH};


static LAST_BATTERY_UA: AtomicI64 = AtomicI64::new(0);
static LAST_SAMPLE_TIME: AtomicU64 = AtomicU64::new(0);

fn read_file_libc(path: &str) -> Option<String> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let mut buffer = [0u8; 128];
        let bytes_read = libc::read(fd, buffer.as_mut_ptr() as *mut libc::c_void, buffer.len());

        libc::close(fd);

        if bytes_read <= 0 {
            return None;
        }

        String::from_utf8(buffer[..bytes_read as usize].to_vec())
            .ok()
            .map(|s| s.trim().to_string())
    }
}


fn read_file_root(path: &str) -> Option<String> {
    let output = Command::new("su")
        .arg("-c")
        .arg(format!("cat {}", path))
        .output()
        .ok()?;

    if output.status.success() {
        String::from_utf8(output.stdout)
            .ok()
            .map(|s| s.trim().to_string())
    } else {
        None
    }
}

fn read_sysfs(path: &str) -> Option<String> {
    read_file_libc(path).or_else(|| read_file_root(path))
}


fn now_millis() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0)
}


fn read_battery_current_ua() -> i64 {
    let paths = [
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/bms/current_now",
        "/sys/class/power_supply/Battery/current_now",
    ];

    for path in &paths {
        if let Some(content) = read_sysfs(path)
            && let Ok(value) = content.parse::<i64>()
            && value != 0
        {
            return value;
        }
    }

    0
}


pub fn read_battery_level() -> i32 {
    let paths = [
        "/sys/class/power_supply/battery/capacity",
        "/sys/class/power_supply/bms/capacity",
        "/sys/class/power_supply/Battery/capacity",
    ];

    for path in &paths {
        if let Some(content) = read_sysfs(path)
            && let Ok(level) = content.parse::<i32>()
            && level >= 0 && level <= 100
        {
            return level;
        }
    }

    0
}


pub fn read_drain_rate_ma() -> i32 {
    let current_ua = read_battery_current_ua();
    let now = now_millis();


    LAST_BATTERY_UA.store(current_ua, Ordering::Relaxed);
    LAST_SAMPLE_TIME.store(now, Ordering::Relaxed);

    (-(current_ua / 1000)) as i32
}


pub fn read_wakeup_count() -> i32 {
    if let Some(content) = read_sysfs("/sys/power/wakeup_count")
        && let Ok(count) = content.parse::<i32>()
    {
        return count;
    }
    0
}

pub fn read_suspend_count() -> i32 {
    if let Some(content) = read_sysfs("/sys/kernel/debug/suspend_stats")
        && let Some(line) = content.lines().find(|l| l.contains("success:"))
    {
        if let Some(num) = line.split_whitespace().last()
            && let Ok(count) = num.parse::<i32>()
        {
            return count;
        }
    }
    0
}


pub fn read_suspend_time_secs() -> i64 {
    if let Some(content) = read_sysfs("/proc/stat") {
        for line in content.lines() {
            if line.starts_with("cpu ") {
                let values: Vec<&str> = line.split_whitespace().collect();
                if values.len() > 4 {
                    if let Ok(idle_jiffies) = values[4].parse::<i64>() {
                        return idle_jiffies / 100;
                    }
                }
            }
        }
    }
    0
}

pub fn is_charging() -> bool {
    let paths = [
        "/sys/class/power_supply/battery/status",
        "/sys/class/power_supply/bms/status",
        "/sys/class/power_supply/Battery/status",
    ];

    for path in &paths {
        if let Some(status) = read_sysfs(path) {
            let status_lower = status.to_lowercase();
            if status_lower.contains("charging") || status_lower.contains("full") {
                return true;
            }
            if status_lower.contains("discharging") || status_lower.contains("not") {
                return false;
            }
        }
    }

    false
}

pub fn read_battery_temp() -> i32 {
    let paths = [
        "/sys/class/power_supply/battery/temp",
        "/sys/class/power_supply/bms/temp",
        "/sys/class/power_supply/Battery/temp",
    ];

    for path in &paths {
        if let Some(content) = read_sysfs(path)
            && let Ok(temp) = content.parse::<i32>()
        {
            return temp; 
        }
    }

    0
}

pub fn read_battery_voltage_mv() -> i32 {
    let paths = [
        "/sys/class/power_supply/battery/voltage_now",
        "/sys/class/power_supply/bms/voltage_now",
        "/sys/class/power_supply/Battery/voltage_now",
    ];

    for path in &paths {
        if let Some(content) = read_sysfs(path)
            && let Ok(voltage) = content.parse::<i64>()
        {
            return (voltage / 1000) as i32;
        }
    }

    0
}
