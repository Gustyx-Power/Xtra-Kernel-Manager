use std::ffi::CString;
use std::time::{SystemTime, UNIX_EPOCH};

#[inline]
pub fn read_file_libc(path: &str) -> Option<String> {
    let c_path = CString::new(path).ok()?;

    unsafe {
        let fd = libc::open(c_path.as_ptr(), libc::O_RDONLY);
        if fd < 0 {
            return None;
        }

        let mut buffer = [0u8; 256];
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

#[inline]
pub fn file_exists(path: &str) -> bool {
    let Ok(c_path) = CString::new(path) else {
        return false;
    };

    unsafe { libc::access(c_path.as_ptr(), libc::F_OK) == 0 }
}

#[inline]
pub fn now_millis() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0)
}
