use std::fs;

fn main() {
    println!("\n╔════════════════════════════════════════════╗");
    println!("║   XKM Native - Complete Feature Test         ║");
    println!("╚══════════════════════════════════════════════╝\n");

    test_battery();
    test_memory();
    test_gpu_detection();
    test_gpu();
    test_gpu_advanced();
    test_cpu();
    test_cpu_advanced();
    test_system_properties();
    test_disk_io();
    test_available_options();
    test_thermal();
    benchmark();

    println!("\n╔════════════════════════════════════════════╗");
    println!("║   All Tests Completed Successfully!          ║");
    println!("╚══════════════════════════════════════════════╝");
}

fn test_battery() {
    println!("[Battery Tests - Complete Info]");
    println!("─────────────────────────────────────────────");

    let level = read_int("/sys/class/power_supply/battery/capacity").unwrap_or(0);
    let temp = read_int("/sys/class/power_supply/battery/temp").unwrap_or(0);
    let voltage = read_long("/sys/class/power_supply/battery/voltage_now").unwrap_or(0);
    let current = read_long("/sys/class/power_supply/battery/current_now").unwrap_or(0);

    println!("Basic:");
    println!("  Level: {}%", level);
    println!("  Temp: {:.1}°C", temp as f32 / 10.0);
    println!("  Voltage: {:.3}V", voltage as f64 / 1_000_000.0);
    println!("  Current: {}mA", current.abs() / 1000);

    let cycle_paths = [
        "/sys/class/power_supply/bms/cycle_count",
        "/sys/class/power_supply/battery/cycle_count",
        "/sys/class/power_supply/bat/cycle_count",
    ];

    for path in &cycle_paths {
        if let Some(cycles) = read_int(path) {
            println!("\nExtended:");
            println!("  Cycle count: {}", cycles);
            break;
        }
    }

    if let Some(health) = read_string("/sys/class/power_supply/battery/health") {
        println!("  Health: {}", health);
    }

    let design = read_long("/sys/class/power_supply/battery/charge_full_design").unwrap_or(0);
    let current_cap = read_long("/sys/class/power_supply/battery/charge_full").unwrap_or(0);
    if design > 0 && current_cap > 0 {
        let capacity_pct = (current_cap as f64 / design as f64) * 100.0;
        println!(
            "  Capacity: {:.2}% ({} / {} µAh)",
            capacity_pct, current_cap, design
        );
    }

    println!();
}

fn test_memory() {
    println!("        [Memory Tests - Complete Info]       ");
    println!("─────────────────────────────────────────────");

    if let Ok(meminfo) = fs::read_to_string("/proc/meminfo") {
        let mut total = 0i64;
        let mut available = 0i64;
        let mut _free = 0i64;
        let mut cached = 0i64;
        let mut swap_total = 0i64;
        let mut swap_free = 0i64;
        let mut swap_cached = 0i64;

        for line in meminfo.lines() {
            let parts: Vec<&str> = line.split_whitespace().collect();
            if parts.len() >= 2 {
                let value: i64 = parts[1].parse().unwrap_or(0);
                match parts[0] {
                    "MemTotal:" => total = value,
                    "MemAvailable:" => available = value,
                    "MemFree:" => _free = value,
                    "Cached:" => cached = value,
                    "SwapTotal:" => swap_total = value,
                    "SwapFree:" => swap_free = value,
                    "SwapCached:" => swap_cached = value,
                    _ => {}
                }
            }
        }

        println!("Memory:");
        println!("  Total: {} MB", total / 1024);
        println!("  Available: {} MB", available / 1024);
        println!(
            "  Used: {} MB ({:.1}%)",
            (total - available) / 1024,
            ((total - available) as f64 / total as f64) * 100.0
        );
        println!("  Cached: {} MB", cached / 1024);

        println!("\nSwap:");
        println!("  Total: {} MB", swap_total / 1024);
        println!(
            "  Used: {} MB ({:.1}%)",
            (swap_total - swap_free) / 1024,
            ((swap_total - swap_free) as f64 / swap_total as f64) * 100.0
        );
        println!("  Cached: {} MB", swap_cached / 1024);
    }

    println!("\nZRAM:");
    if let Some(disksize) = read_long("/sys/block/zram0/disksize") {
        println!("  Disk size: {} MB", disksize / 1024 / 1024);
    }

    if let Some(algo) = read_string("/sys/block/zram0/comp_algorithm") {
        if let Some(start) = algo.find('[') {
            if let Some(end) = algo.find(']') {
                println!("  Algorithm: {}", &algo[start + 1..end]);
            }
        }
    }

    if let Some(mm_stat) = read_string("/sys/block/zram0/mm_stat") {
        let parts: Vec<&str> = mm_stat.split_whitespace().collect();
        if parts.len() >= 3 {
            if let (Ok(orig), Ok(compr), Ok(mem_used)) = (
                parts[0].parse::<i64>(),
                parts[1].parse::<i64>(),
                parts[2].parse::<i64>(),
            ) {
                let ratio = if compr > 0 {
                    orig as f64 / compr as f64
                } else {
                    1.0
                };
                println!("  Original data: {} MB", orig / 1024 / 1024);
                println!("  Compressed: {} MB", compr / 1024 / 1024);
                println!("  Memory used: {} MB", mem_used / 1024 / 1024);
                println!("  Compression ratio: {:.2}x", ratio);
            }
        }
    }

    if let Some(swappiness) = read_int("/proc/sys/vm/swappiness") {
        println!("\nConfig:");
        println!("  Swappiness: {}", swappiness);
    }

    println!();
}

fn test_gpu_detection() {
    println!("      [GPU Detection - Multiple Methods]     ");
    println!("─────────────────────────────────────────────");

    println!("Testing surfaceflinger...");
    let output = std::process::Command::new("dumpsys")
        .arg("SurfaceFlinger")
        .output();

    if let Ok(output) = output {
        let content = String::from_utf8_lossy(&output.stdout);
        for line in content.lines() {
            let line_lower = line.to_lowercase();
            if line_lower.contains("gles")
                || line_lower.contains("renderer")
                || line_lower.contains("vendor")
            {
                println!("  {}", line.trim());
            }
        }
    }

    println!("\nTesting getprop...");
    let props = [
        "ro.hardware.vulkan",
        "ro.hardware.egl",
        "ro.opengles.version",
    ];

    for prop in &props {
        if let Ok(output) = std::process::Command::new("getprop").arg(prop).output() {
            let value = String::from_utf8_lossy(&output.stdout).trim().to_string();
            if !value.is_empty() {
                println!("  {}: {}", prop, value);
            }
        }
    }

    println!();
}

fn test_gpu() {
    println!("        [GPU Tests - Basic Metrics]          ");
    println!("─────────────────────────────────────────────");

    if let Some(raw) = read_string("/sys/class/kgsl/kgsl-3d0/gpuclk") {
        println!("Raw GPU freq: {} (raw)", raw);

        if let Ok(freq_hz) = raw.parse::<i64>() {
            let freq_mhz = freq_hz / 1_000_000;
            println!("Parsed GPU freq: {}MHz", freq_mhz);
        }
    }

    let busy_paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
        "/sys/class/kgsl/kgsl-3d0/gpubusy",
    ];

    for path in &busy_paths {
        if let Some(content) = read_string(path) {
            println!(
                "GPU busy ({}): {}",
                path.split('/').last().unwrap(),
                content
            );
        }
    }

    println!();
}

fn test_gpu_advanced() {
    println!("       [GPU Tests - Advanced Features]       ");
    println!("─────────────────────────────────────────────");

    // Available frequencies
    let freq_paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies",
        "/sys/class/kgsl/kgsl-3d0/devfreq/available_frequencies",
    ];

    for path in &freq_paths {
        if let Some(content) = read_string(path) {
            let freqs: Vec<i32> = content
                .split_whitespace()
                .filter_map(|s| s.parse::<i64>().ok())
                .map(|hz| (hz / 1_000_000) as i32)
                .collect();

            if !freqs.is_empty() {
                println!("Available frequencies: {:?} MHz", freqs);
                println!("  Min: {} MHz", freqs.iter().min().unwrap());
                println!("  Max: {} MHz", freqs.iter().max().unwrap());
                break;
            }
        }
    }

    // GPU temperature
    for zone in 0..20 {
        let type_path = format!("/sys/class/thermal/thermal_zone{}/type", zone);
        if let Some(zone_type) = read_string(&type_path) {
            if zone_type.to_lowercase().contains("gpu") {
                let temp_path = format!("/sys/class/thermal/thermal_zone{}/temp", zone);
                if let Some(temp) = read_int(&temp_path) {
                    let temp_c = temp as f32 / 1000.0;
                    if temp_c > 0.0 && temp_c < 150.0 {
                        println!("GPU Temperature: {:.1}°C", temp_c);
                        break;
                    }
                }
            }
        }
    }

    println!();
}

fn test_cpu() {
    println!(" [CPU Tests - Basic Metrics]");
    println!("─────────────────────────────────────────────");

    let mut online_count = 0;
    let mut total_freq = 0i64;

    for cpu in 0..8 {
        let online_path = format!("/sys/devices/system/cpu/cpu{}/online", cpu);
        let freq_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_cur_freq",
            cpu
        );

        let is_online = if cpu == 0 {
            true
        } else {
            read_int(&online_path).unwrap_or(0) == 1
        };

        if is_online {
            if let Some(freq) = read_int(&freq_path) {
                println!("CPU{}: {}MHz", cpu, freq / 1000);
                online_count += 1;
                total_freq += freq as i64;
            }
        }
    }

    if online_count > 0 {
        println!("\nAverage freq: {}MHz", total_freq / 1000 / online_count);
        println!("Online cores: {}", online_count);
    }

    println!();
}

fn test_cpu_advanced() {
    println!(" [CPU Tests - Advanced Features]");
    println!("─────────────────────────────────────────────");

    // CPU Load (requires /proc/stat parsing)
    if let Ok(stat) = fs::read_to_string("/proc/stat") {
        for line in stat.lines() {
            if line.starts_with("cpu ") {
                let parts: Vec<&str> = line.split_whitespace().collect();
                if parts.len() >= 5 {
                    println!(
                        "CPU stats available: user={}, nice={}, system={}, idle={}",
                        parts[1], parts[2], parts[3], parts[4]
                    );
                }
                break;
            }
        }
    }

    // CPU Governors
    println!("\nCPU Governors:");
    for cpu in 0..2 {
        let gov_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
            cpu
        );
        if let Some(gov) = read_string(&gov_path) {
            println!("  CPU{}: {}", cpu, gov);
        }

        let avail_path = format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_available_governors",
            cpu
        );
        if let Some(govs) = read_string(&avail_path) {
            println!("    Available: {}", govs);
            break;
        }
    }

    // CPU Frequencies
    println!("\nCPU Frequencies:");
    let freq_path = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    if let Some(freqs) = read_string(freq_path) {
        let freq_list: Vec<i32> = freqs
            .split_whitespace()
            .filter_map(|s| s.parse::<i32>().ok())
            .map(|khz| khz / 1000)
            .collect();

        if !freq_list.is_empty() {
            println!("  CPU0 available: {:?} MHz", freq_list);
            println!("    Min: {} MHz", freq_list.iter().min().unwrap());
            println!("    Max: {} MHz", freq_list.iter().max().unwrap());
        }
    }

    println!();
}

fn test_system_properties() {
    println!("System Properties - Native Access]");
    println!("─────────────────────────────────────────────");

    let props = vec![
        "ro.build.version.release",
        "ro.build.version.sdk",
        "ro.product.manufacturer",
        "ro.product.model",
        "ro.hardware",
        "ro.board.platform",
    ];

    for prop in props {
        if let Ok(output) = std::process::Command::new("getprop").arg(prop).output() {
            let value = String::from_utf8_lossy(&output.stdout).trim().to_string();
            if !value.is_empty() {
                println!("  {}: {}", prop, value);
            }
        }
    }

    println!();
}

fn test_disk_io() {
    println!(" [Disk I/O - Statistics]");
    println!("─────────────────────────────────────────────");

    if let Ok(diskstats) = fs::read_to_string("/proc/diskstats") {
        let devices = ["sda", "mmcblk0", "dm-0"];

        for dev in &devices {
            for line in diskstats.lines() {
                if line.contains(dev) && !line.contains(&format!("{}p", dev)) {
                    let parts: Vec<&str> = line.split_whitespace().collect();
                    if parts.len() >= 14 {
                        let read_sectors: i64 = parts[5].parse().unwrap_or(0);
                        let write_sectors: i64 = parts[9].parse().unwrap_or(0);

                        let read_mb = (read_sectors * 512) / 1024 / 1024;
                        let write_mb = (write_sectors * 512) / 1024 / 1024;

                        println!("Device: {}", dev);
                        println!("  Total reads: {} MB", read_mb);
                        println!("  Total writes: {} MB", write_mb);
                        break;
                    }
                }
            }
        }
    }

    println!();
}

fn test_thermal() {
    println!(" [Thermal Tests - Valid Zones Only]");
    println!("─────────────────────────────────────────────");

    let mut valid_zones = 0;

    for zone in 0..20 {
        let temp_path = format!("/sys/class/thermal/thermal_zone{}/temp", zone);
        let type_path = format!("/sys/class/thermal/thermal_zone{}/type", zone);

        if let Some(temp) = read_int(&temp_path) {
            let temp_c = temp as f32 / 1000.0;

            if temp_c > -50.0 && temp_c < 150.0 {
                let zone_type = read_string(&type_path).unwrap_or_else(|| "unknown".to_string());

                println!("Zone {}: {:.1}°C ({})", zone, temp_c, zone_type);
                valid_zones += 1;
            }
        }
    }

    println!("\nValid thermal zones: {}", valid_zones);
    println!();
}

fn benchmark() {
    println!("⚡ [Performance Benchmark]");
    println!("─────────────────────────────────────────────");

    use std::time::Instant;

    let iterations = 100;

    let start = Instant::now();
    for _ in 0..iterations {
        let _ = read_int("/sys/class/power_supply/battery/capacity");
    }
    let battery_time = start.elapsed();

    let start = Instant::now();
    for _ in 0..iterations {
        let _ = fs::read_to_string("/proc/meminfo");
    }
    let memory_time = start.elapsed();

    let start = Instant::now();
    for _ in 0..iterations {
        let _ = read_string("/sys/class/kgsl/kgsl-3d0/gpuclk");
    }
    let gpu_time = start.elapsed();

    let start = Instant::now();
    for _ in 0..iterations {
        let _ = read_int("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
    }
    let cpu_time = start.elapsed();

    let start = Instant::now();
    for _ in 0..iterations {
        let _ = read_string("/sys/block/zram0/mm_stat");
    }
    let zram_time = start.elapsed();

    println!(
        "Battery read ({} iterations): {:?}",
        iterations, battery_time
    );
    println!("  Avg: {:?}/read", battery_time / iterations);

    println!("Memory read ({} iterations): {:?}", iterations, memory_time);
    println!("  Avg: {:?}/read", memory_time / iterations);

    println!("GPU read ({} iterations): {:?}", iterations, gpu_time);
    println!("  Avg: {:?}/read", gpu_time / iterations);

    println!("CPU read ({} iterations): {:?}", iterations, cpu_time);
    println!("  Avg: {:?}/read", cpu_time / iterations);

    println!("ZRAM read ({} iterations): {:?}", iterations, zram_time);
    println!("  Avg: {:?}/read", zram_time / iterations);
}

fn test_available_options() {
    println!("📋 [Available System Options]");
    println!("─────────────────────────────────────────────");

    // ZRAM algorithms
    println!("ZRAM Compression:");
    if let Some(algos) = read_string("/sys/block/zram0/comp_algorithm") {
        let current = algos
            .chars()
            .skip_while(|&c| c != '[')
            .skip(1)
            .take_while(|&c| c != ']')
            .collect::<String>();

        let available: Vec<String> = algos
            .split_whitespace()
            .map(|s| s.trim_matches(|c| c == '[' || c == ']').to_string())
            .collect();

        if !current.is_empty() {
            println!("  Current: {}", current);
        }
        println!("  Available: {:?}", available);
    }

    // CPU Governors
    println!("\nCPU Governors:");
    if let Some(govs) = read_string("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor") {
        println!("  Current: {}", govs);
    }

    if let Some(available) =
        read_string("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors")
    {
        let gov_list: Vec<&str> = available.split_whitespace().collect();
        println!("  Available: {:?}", gov_list);
    }

    // CPU Frequency Driver
    if let Some(driver) = read_string("/sys/devices/system/cpu/cpu0/cpufreq/scaling_driver") {
        println!("  Driver: {}", driver);
    }

    // GPU Policies
    println!("\nGPU Power Policy:");
    let gpu_policy_paths = [
        "/sys/class/kgsl/kgsl-3d0/devfreq/governor",
        "/sys/class/kgsl/kgsl-3d0/governor",
    ];

    for path in &gpu_policy_paths {
        if let Some(policy) = read_string(path) {
            println!("  Current: {}", policy);
            break;
        }
    }

    let gpu_avail_paths = [
        "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors",
        "/sys/class/kgsl/kgsl-3d0/available_governors",
    ];

    for path in &gpu_avail_paths {
        if let Some(available) = read_string(path) {
            let policies: Vec<&str> = available.split_whitespace().collect();
            println!("  Available: {:?}", policies);
            break;
        }
    }

    // ZRAM Devices
    println!("\nZRAM Devices:");
    for i in 0..4 {
        let path = format!("/sys/block/zram{}/disksize", i);
        if let Some(size) = read_long(&path) {
            if size > 0 {
                println!("  zram{}: {} MB", i, size / 1024 / 1024);
            }
        }
    }

    println!();
}

// Helper functions
fn read_string(path: &str) -> Option<String> {
    fs::read_to_string(path).ok().map(|s| s.trim().to_string())
}

fn read_int(path: &str) -> Option<i32> {
    read_string(path)?.parse().ok()
}

fn read_long(path: &str) -> Option<i64> {
    read_string(path)?.parse().ok()
}
