use std::ffi::CString;
use std::process::Command;

const KGSL_GPUCLK: &str = "/sys/class/kgsl/kgsl-3d0/gpuclk";
const KGSL_CLOCK_MHZ: &str = "/sys/class/kgsl/kgsl-3d0/clock_mhz";
const MALI_GPU_CLOCK: &str = "/sys/kernel/gpu/gpu_clock";
const MALI_CLOCK: &str = "/sys/devices/platform/mali.0/clock";

fn read_file_libc(path: &str) -> Option<String> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let mut buffer = [0u8; 64];
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
    if let Some(content) = read_file_libc(path) {
        return Some(content);
    }

    read_file_root(path)
}

fn parse_freq_to_mhz(value: &str) -> Option<i32> {
    let freq: i64 = value.parse().ok()?;

    if freq <= 0 {
        None
    } else if freq > 1_000_000 {
        Some((freq / 1_000_000) as i32)
    } else if freq > 1_000 {
        Some((freq / 1_000) as i32)
    } else {
        Some(freq as i32)
    }
}

pub fn read_gpu_freq() -> i32 {
    let paths = [KGSL_GPUCLK, KGSL_CLOCK_MHZ, MALI_GPU_CLOCK, MALI_CLOCK];

    for path in &paths {
        if let Some(content) = read_sysfs(path)
            && let Some(freq_mhz) = parse_freq_to_mhz(&content) {
                return freq_mhz;
            }
    }

    0
}

pub fn read_gpu_busy() -> i32 {
    // Try gpu_busy_percentage first (Qualcomm, format: "2 %" or just "2")
    if let Some(content) = read_sysfs("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage") {
        // Extract just the number (handles "2 %" or "2")
        let num_str = content.split_whitespace().next().unwrap_or("0");
        if let Ok(busy) = num_str.parse::<i32>() {
            return busy;
        }
    }

    // Fallback: gpubusy format "busy_time total_time" - calculate percentage
    if let Some(content) = read_sysfs("/sys/class/kgsl/kgsl-3d0/gpubusy") {
        let parts: Vec<&str> = content.split_whitespace().collect();
        if parts.len() >= 2
            && let (Ok(busy), Ok(total)) = (parts[0].parse::<i64>(), parts[1].parse::<i64>())
                && total > 0 {
                    return ((busy * 100) / total) as i32;
                }
    }

    0
}
