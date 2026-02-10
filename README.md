<h1 align="center">
    Xtra Kernel Manager
</h1>

<p align="center">
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&colorA=363A4F&logo=kotlin&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-7F52FF?style=for-the-badge&colorA=363A4F&logo=jetpack-compose&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Rust-000000?style=for-the-badge&colorA=363A4F&logo=rust&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&colorA=363A4F&logo=android&logoColor=D9E0EE">
    <a href="https://deepwiki.com/Gustyx-Power/Xtra-Kernel-Manager"><img src="https://deepwiki.com/badge.svg" alt="Ask DeepWiki"></a>

</p>

<p align="center">
    <a href="https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/releases">
        <img src="https://img.shields.io/github/v/release/Xtra-Manager-Software/Xtra-Kernel-Manager?style=for-the-badge&logo=github&colorA=363A4F&colorB=F2CDCD&logoColor=D9E0EE">
    </a>
</p>

---

## About

**Xtra Kernel Manager** is a modern, rooted Android application built with **Kotlin** and **Jetpack Compose**, designed for kernel enthusiasts and power users.

It provides **real-time visibility and precise control** over CPU performance, thermal behavior, and power efficiency on custom kernels, while prioritizing **clarity, responsiveness, and minimal system overhead**.

---

## Features

- **Real-time CPU Monitoring**  
  Per-core frequency and temperature monitoring.

- **Thermal Zone Status**  
  Read system thermal zones for advanced debugging.

- **CPU Tuning**  
  Dynamically apply governors such as `performance` or `powersave`  
  via native shell execution using [libsu](https://github.com/topjohnwu/libsu).

- **Material 3 UI**  
  Clean, modern interface built with Jetpack Compose.

- **Fast & Minimal**  
  Lightweight architecture optimized for rooted devices.

---

## Supported Root Managers

- [Magisk](https://github.com/topjohnwu/Magisk)
- [KernelSU](https://github.com/tiann/KernelSU)
- [APatch](https://github.com/bmax121/APatch)

---

## Development Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 31+)
- Rust toolchain
- NDK 26.1.10909125

### Local Development
1. Clone the repository:
   ```bash
   git clone https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager.git
   cd Xtra-Kernel-Manager
   ```

2. Setup configuration files:
   ```bash
   cp local.properties.template local.properties
   cp gradle.properties.template gradle.properties
   ```

3. Edit `local.properties` with your Android SDK path:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   ```

4. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

### Play Protect Compatibility
This project includes optimizations to reduce Play Protect detection:
- Aggressive code obfuscation with ProGuard/R8
- Legitimate app metadata and permissions
- Consistent signing with reproducible builds
- Open source transparency

### Security Notice
- `local.properties` and `gradle.properties` are gitignored for security
- Keystore files are never committed to the repository
- All builds are reproducible and verifiable

---

## Requirements

- **Root Access** – Mandatory for all core features
- **Android Version** – Android 10 (API 29) or above
- **Have a brain** – without understanding what you’re doing is a bad idea

---

## Resources
- [Releases](https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/releases)
- [Issues](https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/issues)

## Credits
- [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI)
- [KavaRef](https://github.com/Kava-Ref)

---

## Community

<p align="left">
    <a href="https://discord.gg/mQYVj4twYZ">
        <img src="https://img.shields.io/badge/Discord-Community-B4BEFE?style=for-the-badge&colorA=363A4F&logo=discord&logoColor=D9E0EE">
    </a>
    <a href="https://t.me/CH_XtraManagerSoftware">
        <img src="https://img.shields.io/badge/Telegram-Community-2CA5E0?style=for-the-badge&colorA=363A4F&logo=telegram&logoColor=D9E0EE">
    </a>
</p>

---

## Stargazers over time

[![Stargazers over time](https://starchart.cc/Gustyx-Power/Xtra-Kernel-Manager.svg?variant=adaptive)](https://starchart.cc/Gustyx-Power/Xtra-Kernel-Manager)

---

## License

Xtra Kernel Manager is open-sourced software licensed under the **MIT License**.  
See the [LICENSE](LICENSE) file for more information.
