use crate::utils;
use std::os::unix::io::RawFd;
use std::sync::Once;

static mut CAPACITY_FD: RawFd = -1;
static mut TEMP_FD: RawFd = -1;
static mut VOLTAGE_FD: RawFd = -1;
static mut CURRENT_FD: RawFd = -1;

static INIT: Once = Once::new();

#[inline]
fn ensure_init() {
    INIT.call_once(|| unsafe {
        CAPACITY_FD = libc::open(
            b"/sys/class/power_supply/battery/capacity\0".as_ptr() as *const libc::c_char,
            libc::O_RDONLY,
        );
        TEMP_FD = libc::open(
            b"/sys/class/power_supply/battery/temp\0".as_ptr() as *const libc::c_char,
            libc::O_RDONLY,
        );
        VOLTAGE_FD = libc::open(
            b"/sys/class/power_supply/battery/voltage_now\0".as_ptr() as *const libc::c_char,
            libc::O_RDONLY,
        );
        CURRENT_FD = libc::open(
            b"/sys/class/power_supply/battery/current_now\0".as_ptr() as *const libc::c_char,
            libc::O_RDONLY,
        );
    });
}

#[inline(always)]
unsafe fn read_fd_int(fd: RawFd) -> Option<i64> {
    if fd < 0 {
        return None;
    }

    let mut buf = [0u8; 32];

    unsafe {
        libc::lseek(fd, 0, libc::SEEK_SET);
    }

    let n = unsafe { libc::read(fd, buf.as_mut_ptr() as *mut libc::c_void, buf.len()) };

    if n <= 0 {
        return None;
    }

    unsafe {
        std::str::from_utf8_unchecked(&buf[..n as usize])
            .trim()
            .parse()
            .ok()
    }
}

/// Read battery level percentage
#[inline(always)]
pub fn read_battery_level() -> i32 {
    ensure_init();

    unsafe {
        if let Some(level) = read_fd_int(CAPACITY_FD) {
            return level as i32;
        }
    }

    // Fallback
    utils::read_sysfs_int("/sys/class/power_supply/battery/capacity", 500).unwrap_or(0) as i32
}

/// Read battery temperature
#[inline(always)]
pub fn read_battery_temp() -> i32 {
    ensure_init();

    unsafe {
        if let Some(temp) = read_fd_int(TEMP_FD) {
            return temp as i32;
        }
    }

    utils::read_sysfs_int("/sys/class/power_supply/battery/temp", 500).unwrap_or(0) as i32
}

/// Read battery voltage in millivolts
#[inline(always)]
pub fn read_battery_voltage_mv() -> i32 {
    ensure_init();

    unsafe {
        if let Some(voltage_uv) = read_fd_int(VOLTAGE_FD) {
            return (voltage_uv / 1000) as i32;
        }
    }

    let voltage_uv =
        utils::read_sysfs_int("/sys/class/power_supply/battery/voltage_now", 500).unwrap_or(0);
    (voltage_uv / 1000) as i32
}

/// Read battery drain rate in milliamps
#[inline(always)]
pub fn read_drain_rate_ma() -> i32 {
    let raw_value = unsafe {
        if let Some(val) = read_fd_int(CURRENT_FD) {
            val
        } else {
            utils::read_sysfs_int("/sys/class/power_supply/battery/current_now", 500)
                .unwrap_or(0)
                .into()
        }
    };

    let abs_val = raw_value.abs();

    if abs_val < 10000 {
        abs_val as i32
    } else {
        (abs_val / 1000) as i32
    }
}

/// Check if battery is charging
pub fn is_charging() -> bool {
    let path = "/sys/class/power_supply/battery/status";
    if let Some(status) = utils::read_sysfs_cached(path, 500) {
        status.contains("Charging")
    } else {
        false
    }
}

/// Read wakeup count
pub fn read_wakeup_count() -> i32 {
    utils::read_sysfs_int("/sys/power/wakeup_count", 1000).unwrap_or(0) as i32
}

/// Read suspend count
pub fn read_suspend_count() -> i32 {
    utils::read_sysfs_int("/sys/kernel/debug/suspend_stats/success", 1000).unwrap_or(0) as i32
}

/// Read battery cycle count
pub fn read_cycle_count() -> i32 {
    let paths = [
        "/sys/class/power_supply/bms/cycle_count",
        "/sys/class/power_supply/battery/cycle_count",
        "/sys/class/power_supply/bat/cycle_count",
    ];

    for path in &paths {
        if let Some(cycles) = utils::read_sysfs_int(path, 1000) {
            return cycles as i32;
        }
    }

    -1
}

/// Read battery health status
pub fn read_battery_health() -> String {
    let path = "/sys/class/power_supply/battery/health";
    utils::read_sysfs_cached(path, 1000).unwrap_or_else(|| "Unknown".to_string())
}

/// Read battery capacity level
pub fn read_battery_capacity_level() -> f32 {
    let design_path = "/sys/class/power_supply/battery/charge_full_design";
    let current_path = "/sys/class/power_supply/battery/charge_full";

    let design = utils::read_sysfs_int(design_path, 5000).unwrap_or(0) as f32;
    let current = utils::read_sysfs_int(current_path, 5000).unwrap_or(0) as f32;

    if design > 0.0 && current > 0.0 {
        let capacity = (current / design) * 100.0;
        if capacity >= 50.0 && capacity <= 100.0 {
            return capacity;
        }
    }

    100.0
}
