use crate::utils;
use std::fs;


pub fn read_thermal_zones() -> String {
    let mut zones_json = String::from("[");
    let mut first = true;

    if let Ok(entries) = fs::read_dir("/sys/class/thermal") {
        for entry in entries.flatten() {
            let path = entry.path();
            if let Some(name) = path.file_name().and_then(|n| n.to_str()) {
                if name.starts_with("thermal_zone") {
                    // Read type (sensor name)
                    let type_path = path.join("type");
                    let temp_path = path.join("temp");

                    if let Some(sensor_type) = utils::read_file_libc(type_path.to_str().unwrap_or("")) 
                       && let Some(temp_str) = utils::read_file_libc(temp_path.to_str().unwrap_or("")) {
                        
                        
                        let temp_val = temp_str.trim().parse::<f32>().unwrap_or(-1.0);
                        if temp_val > -1000.0 { // basic validation
                            let temp_c = temp_val / 1000.0;
                            
                            if !first {
                                zones_json.push(',');
                            }
                            let clean_type = sensor_type.trim();
                            
                            zones_json.push_str(&format!(
                                "{{\"name\":\"{}\",\"temp\":{:.1}}}",
                                clean_type, temp_c
                            ));
                            first = false;
                        }
                    }
                }
            }
        }
    }

    zones_json.push(']');
    zones_json
}
