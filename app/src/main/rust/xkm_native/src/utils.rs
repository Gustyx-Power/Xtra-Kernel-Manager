use std::collections::HashMap;
use std::ffi::CString;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

use once_cell::sync::Lazy;
use parking_lot::RwLock;


/// Primary: rustix (safe, fast)
#[inline]
pub fn read_file_rustix(path: &str) -> Option<String> {
    use rustix::fs::{openat, CWD, OFlags, Mode};
    use rustix::io::read as rustix_read;

    let fd = openat(CWD, path, OFlags::RDONLY, Mode::empty()).ok()?;
    let mut buffer = [0u8; 512];
    let bytes_read = rustix_read(&fd, &mut buffer).ok()?;

    String::from_utf8(buffer[..bytes_read].to_vec())
        .ok()
        .map(|s| s.trim().to_string())
}

/// Fallback: libc (raw performance)
#[inline]
pub fn read_file_libc(path: &str) -> Option<String> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let mut buffer = [0u8; 512];
        let bytes_read = libc::read(
            fd,
            buffer.as_mut_ptr() as *mut libc::c_void,
            buffer.len(),
        );

        libc::close(fd);

        if bytes_read <= 0 {
            return None;
        }

        String::from_utf8(buffer[..bytes_read as usize].to_vec())
            .ok()
            .map(|s| s.trim().to_string())
    }
}
/// Get system property (100x faster than shell getprop)
pub fn get_system_property(key: &str) -> Option<String> {
    use std::ffi::CString;

    unsafe {
        let key_c = CString::new(key).ok()?;
        let mut value = [0u8; 92]; // PROP_VALUE_MAX

        let len = libc::__system_property_get(
            key_c.as_ptr(),
            value.as_mut_ptr() as *mut libc::c_char,
        );

        if len > 0 {
            std::str::from_utf8(&value[..len as usize])
                .ok()
                .map(|s| s.to_string())
        } else {
            None
        }
    }
}

/// Get multiple properties at once (batch optimization)
pub fn get_system_properties(keys: &[&str]) -> Vec<(String, String)> {
    keys.iter()
        .filter_map(|key| {
            get_system_property(key).map(|val| (key.to_string(), val))
        })
        .collect()
}


/// Buffer read for large files (tetap libc - fastest)
#[inline]
pub fn read_file_libc_buf(path: &str, buf: &mut [u8]) -> Option<usize> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let bytes_read = libc::read(fd, buf.as_mut_ptr() as *mut libc::c_void, buf.len());
        libc::close(fd);

        if bytes_read <= 0 {
            None
        } else {
            Some(bytes_read as usize)
        }
    }
}

/// Smart read: rustix first, libc fallback
#[inline]
pub fn read_sysfs(path: &str) -> Option<String> {
    read_file_rustix(path).or_else(|| read_file_libc(path))
}

struct CachedValue {
    value: String,
    timestamp: Instant,
}

static VALUE_CACHE: Lazy<RwLock<HashMap<String, CachedValue>>> =
    Lazy::new(|| RwLock::new(HashMap::with_capacity(64)));

static DEFAULT_TTL: Lazy<RwLock<u64>> = Lazy::new(|| RwLock::new(300));

pub fn read_sysfs_cached(path: &str, ttl_ms: u64) -> Option<String> {
    // Fast read path
    {
        let cache = VALUE_CACHE.read();
        if let Some(cached) = cache.get(path) {
            if cached.timestamp.elapsed() < Duration::from_millis(ttl_ms) {
                return Some(cached.value.clone());
            }
        }
    }

    // Cache miss - read fresh
    let value = read_sysfs(path)?;

    // Update cache
    {
        let mut cache = VALUE_CACHE.write();
        cache.insert(path.to_string(), CachedValue {
            value: value.clone(),
            timestamp: Instant::now(),
        });
    }

    Some(value)
}

// Typed readers
#[inline]
pub fn read_sysfs_int(path: &str, ttl_ms: u64) -> Option<i32> {
    read_sysfs_cached(path, ttl_ms)?.parse().ok()
}

#[inline]
pub fn read_sysfs_long(path: &str, ttl_ms: u64) -> Option<i64> {
    read_sysfs_cached(path, ttl_ms)?.parse().ok()
}

#[inline]
pub fn read_sysfs_float(path: &str, ttl_ms: u64) -> Option<f32> {
    read_sysfs_cached(path, ttl_ms)?.parse().ok()
}

static PATH_CACHE: Lazy<RwLock<HashMap<String, String>>> =
    Lazy::new(|| RwLock::new(HashMap::with_capacity(16)));

pub fn discover_first_valid(candidates: &[&str]) -> Option<String> {
    let cache_key = format!("{:?}", candidates);

    // Check cache
    {
        let cache = PATH_CACHE.read();
        if let Some(path) = cache.get(&cache_key) {
            return Some(path.clone());
        }
    }

    // Discovery
    for path in candidates {
        if file_exists(path) {
            let path_str = path.to_string();

            // Cache permanently
            let mut cache = PATH_CACHE.write();
            cache.insert(cache_key, path_str.clone());

            return Some(path_str);
        }
    }

    None
}

#[inline]
pub fn file_exists(path: &str) -> bool {
    use rustix::fs::{accessat, Access, AtFlags, CWD};

    // Try rustix first - Use AtFlags::empty() explicitly
    match accessat(CWD, path, Access::EXISTS, AtFlags::empty()) {
        Ok(_) => return true,
        Err(_) => {}
    }

    // Fallback: libc
    if let Ok(c_path) = CString::new(path) {
        unsafe {
            return libc::access(c_path.as_ptr(), libc::F_OK) == 0;
        }
    }

    false
}

#[inline]
pub fn now_millis() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0)
}


pub fn clear_value_cache() {
    VALUE_CACHE.write().clear();
}

pub fn clear_path_cache() {
    PATH_CACHE.write().clear();
}

pub fn set_default_cache_ttl(ttl_ms: u64) {
    *DEFAULT_TTL.write() = ttl_ms;
}

pub fn get_cache_size() -> usize {
    VALUE_CACHE.read().len()
}


use std::sync::atomic::{AtomicU64, Ordering};

static READ_SUCCESS: AtomicU64 = AtomicU64::new(0);
static READ_FAILURE: AtomicU64 = AtomicU64::new(0);

pub fn track_read_success() {
    READ_SUCCESS.fetch_add(1, Ordering::Relaxed);
}

pub fn track_read_failure() {
    READ_FAILURE.fetch_add(1, Ordering::Relaxed);
}

pub fn get_read_stats() -> (u64, u64) {
    (
        READ_SUCCESS.load(Ordering::Relaxed),
        READ_FAILURE.load(Ordering::Relaxed),
    )
}

pub fn reset_read_stats() {
    READ_SUCCESS.store(0, Ordering::Relaxed);
    READ_FAILURE.store(0, Ordering::Relaxed);
}

pub fn init_caches() {
    // Pre-allocate cache capacity
    let _ = VALUE_CACHE.read();
    let _ = PATH_CACHE.read();
}

pub fn clear_caches() {
    clear_value_cache();
    clear_path_cache();
}


#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_read_battery_capacity() {
        let path = "/sys/class/power_supply/battery/capacity";
        if let Some(value) = read_sysfs(path) {
            println!("Battery: {}%", value);
            assert!(!value.is_empty());
        }
    }

    #[test]
    fn test_caching() {
        let path = "/sys/class/power_supply/battery/capacity";

        // First read (cache miss)
        let val1 = read_sysfs_cached(path, 1000);

        // Second read (cache hit - should be instant)
        let val2 = read_sysfs_cached(path, 1000);

        assert_eq!(val1, val2);
    }

    #[test]
    fn test_path_discovery() {
        let candidates = [
            "/sys/class/power_supply/battery",
            "/sys/class/power_supply/bms",
            "/sys/class/power_supply/Battery",
        ];

        if let Some(path) = discover_first_valid(&candidates) {
            println!("Battery path: {}", path);
            assert!(file_exists(&path));
        }
    }

    #[test]
    fn test_file_exists() {
        assert!(file_exists("/sys"));
        assert!(file_exists("/proc"));
        assert!(!file_exists("/sys/nonexistent/path"));
    }

    #[test]
    fn test_typed_readers() {
        // Test int reader
        if let Some(capacity) = read_sysfs_int("/sys/class/power_supply/battery/capacity", 100) {
            assert!(capacity >= 0 && capacity <= 100);
        }

        // Test long reader
        if let Some(voltage) = read_sysfs_long("/sys/class/power_supply/battery/voltage_now", 100) {
            assert!(voltage > 0);
        }
    }
}
