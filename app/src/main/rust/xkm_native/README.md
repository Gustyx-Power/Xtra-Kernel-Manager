# Testing XKM Native on Android

This guide explains how to compile the Rust native tests and run them directly on an Android device via ADB.

## Prerequisites

- **Rust Toolchain**: `stable` (edition 2024 recommended)
- **Target Architecture**: `aarch64-linux-android`
- **Android Device**: Rooted, connected via ADB
- **ADB**: Installed and in your PATH

## Build & Run Instructions

To compile the `android_test` binary and execute it on your device, run the following commands in the terminal:

```bash
# 1. Build the binary for Android AArch64
cargo build --release --bin android_test --target aarch64-linux-android

# 2. Push the binary to the device's temporary directory
adb push target/aarch64-linux-android/release/android_test /data/local/tmp/

# 3. Execute the binary as root (su)
adb shell su -c "/data/local/tmp/android_test"
```

> [!NOTE]
> Make sure your device screen is on and you grant Root access if prompted on the device screen.

## Troubleshooting

- **Target not found**: If `cargo` complains about the target, install it via `rustup target add aarch64-linux-android`.
- **Permission denied**: Ensure you are running `adb shell su -c ...` to execute as root, and that the file has execution permissions (`chmod +x` might be needed if push doesn't preserve them, though typically `adb push` does fine for binaries).
