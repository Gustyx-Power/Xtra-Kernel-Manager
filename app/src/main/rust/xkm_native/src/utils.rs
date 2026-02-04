use std::collections::HashMap;
use std::ffi::CString;
use std::time::{Duration, Instant};

use once_cell::sync::Lazy;
use parking_lot::RwLock;


#[cfg(unix)]
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

#[cfg(windows)]
#[inline]
pub fn read_file_rustix(path: &str) -> Option<String> {
    read_file_libc(path)
}

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
            buffer.len() as u32,
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
pub fn get_system_property(_key: &str) -> Option<String> {
    #[cfg(unix)]
    {
        use std::ffi::CString;

        unsafe {
            let key_c = CString::new(_key).ok()?;
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
    
    #[cfg(windows)]
    {
        None
    }
}

#[inline]
pub fn read_file_libc_buf(path: &str, buf: &mut [u8]) -> Option<usize> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let bytes_read = libc::read(fd, buf.as_mut_ptr() as *mut libc::c_void, buf.len() as u32);
        libc::close(fd);

        if bytes_read <= 0 {
            None
        } else {
            Some(bytes_read as usize)
        }
    }
}

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

pub fn read_sysfs_cached(path: &str, ttl_ms: u64) -> Option<String> {
    {
        let cache = VALUE_CACHE.read();
        if let Some(cached) = cache.get(path) {
            if cached.timestamp.elapsed() < Duration::from_millis(ttl_ms) {
                return Some(cached.value.clone());
            }
        }
    }
    let value = read_sysfs(path)?;
    {
        let mut cache = VALUE_CACHE.write();
        cache.insert(path.to_string(), CachedValue {
            value: value.clone(),
            timestamp: Instant::now(),
        });
    }

    Some(value)
}

#[inline]
pub fn read_sysfs_int(path: &str, ttl_ms: u64) -> Option<i32> {
    read_sysfs_cached(path, ttl_ms)?.parse().ok()
}

#[inline]
pub fn read_sysfs_float(path: &str, ttl_ms: u64) -> Option<f32> {
    read_sysfs_cached(path, ttl_ms)?.parse().ok()
}

#[inline]
pub fn file_exists(path: &str) -> bool {
    #[cfg(unix)]
    {
        use rustix::fs::{accessat, Access, AtFlags, CWD};
        
        match accessat(CWD, path, Access::EXISTS, AtFlags::empty()) {
            Ok(_) => return true,
            Err(_) => {}
        }
    }

    if let Ok(c_path) = CString::new(path) {
        unsafe {
            #[cfg(unix)]
            return libc::access(c_path.as_ptr(), libc::F_OK) == 0;
            
            #[cfg(windows)]
            return libc::access(c_path.as_ptr(), 0) == 0;
        }
    }

    false
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

        let val1 = read_sysfs_cached(path, 1000);
        let val2 = read_sysfs_cached(path, 1000);

        assert_eq!(val1, val2);
    }

    #[test]
    fn test_file_exists() {
        assert!(file_exists("/sys"));
        assert!(file_exists("/proc"));
        assert!(!file_exists("/sys/nonexistent/path"));
    }

    #[test]
    fn test_typed_readers() {
        if let Some(capacity) = read_sysfs_int("/sys/class/power_supply/battery/capacity", 100) {
            assert!(capacity >= 0 && capacity <= 100);
        }
    }
}
