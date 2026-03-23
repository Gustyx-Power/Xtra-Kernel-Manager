#!/bin/bash

# Configuration
APP_MODULE="app"
PACKAGE_NAME="id.xms.xtrakernelmanager.dev"
MAIN_ACTIVITY=".MainActivity"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting Debug Build Process...${NC}"

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo -e "${RED}Error: ADB not found in PATH. Please install Android Platform Tools.${NC}"
    exit 1
fi

# Check for Waydroid
WAYDROID_AVAILABLE=false
if command -v waydroid &> /dev/null; then
    if waydroid status | grep -q "RUNNING"; then
        WAYDROID_AVAILABLE=true
        echo -e "${GREEN}Waydroid detected and running!${NC}"
    fi
fi

# Get list of all connected ADB devices
DEVICE_LIST=$(adb devices | grep -v "List" | grep "device" | awk '{print $1}')
DEVICE_COUNT=$(echo "$DEVICE_LIST" | grep -v "^$" | wc -l | tr -d ' ')

# Debug output
echo -e "${YELLOW}Debug: Found $DEVICE_COUNT device(s)${NC}"

# Check if any devices are available
if [ "$DEVICE_COUNT" -eq 0 ] && [ "$WAYDROID_AVAILABLE" = false ]; then
    echo -e "${RED}Error: No device connected and Waydroid not running.${NC}"
    echo -e "${YELLOW}Tip: Start Waydroid with 'waydroid session start' or connect an Android device.${NC}"
    exit 1
fi

# Display available devices
echo -e "${BLUE}=== Available Devices ===${NC}"
if [ "$DEVICE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}ADB Devices ($DEVICE_COUNT):${NC}"
    while IFS= read -r device_id; do
        if [ -n "$device_id" ]; then
            DEVICE_MODEL=$(adb -s "$device_id" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
            IS_EMULATOR=$(adb -s "$device_id" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')
            
            if [ "$IS_EMULATOR" = "1" ] || [[ "$DEVICE_MODEL" == *"SDK"* ]] || [[ "$DEVICE_MODEL" == *"Emulator"* ]]; then
                echo -e "  ${BLUE}[AVD]${NC} $device_id - $DEVICE_MODEL"
            else
                echo -e "  ${GREEN}[Physical]${NC} $device_id - $DEVICE_MODEL"
            fi
        fi
    done <<< "$DEVICE_LIST"
fi

if [ "$WAYDROID_AVAILABLE" = true ]; then
    echo -e "${GREEN}Waydroid: Running${NC}"
fi
echo -e "${BLUE}=========================${NC}"

# Setup NDK Path
if [ -z "$ANDROID_NDK_HOME" ]; then
    # Prioritize specific versions we know work (r26, r25)
    if [ -d "/usr/local/share/android-commandlinetools/ndk/26.1.10909125" ]; then
        export ANDROID_NDK_HOME="/usr/local/share/android-commandlinetools/ndk/26.1.10909125"
        echo -e "${GREEN}Auto-configured NDK (r26) at $ANDROID_NDK_HOME${NC}"
    elif [ -d "/usr/local/share/android-commandlinetools/ndk-bundle" ]; then
        export ANDROID_NDK_HOME="/usr/local/share/android-commandlinetools/ndk-bundle"
        echo -e "${GREEN}Auto-configured NDK (bundle) at $ANDROID_NDK_HOME${NC}"
    elif [ -d "$HOME/Library/Android/sdk/ndk-bundle" ]; then
        export ANDROID_NDK_HOME="$HOME/Library/Android/sdk/ndk-bundle"
        echo -e "${GREEN}Auto-configured NDK at $ANDROID_NDK_HOME${NC}"
    fi
fi

# Rust Build
echo -e "${YELLOW}Building Rust Native Library (Release)...${NC}"

# Check for Cargo
if ! command -v cargo &> /dev/null; then
    echo -e "${RED}Error: Rust/Cargo not found. Please install Rust.${NC}"
    exit 1
fi

# Check/Install cargo-ndk
if ! command -v cargo-ndk &> /dev/null; then
    echo -e "${YELLOW}cargo-ndk not found. Installing...${NC}"
    cargo install cargo-ndk
fi

# Build Rust Lib
# Store current directory
CURRENT_DIR=$(pwd)
RUST_DIR="$APP_MODULE/src/main/rust/xkm_native"

if [ -d "$RUST_DIR" ]; then
    cd "$RUST_DIR"
    
    # Build for arm64-v8a (for physical devices) and x86_64 (for emulators)
    echo -e "${YELLOW}  Building for arm64-v8a (physical devices)...${NC}"
    cargo ndk -t arm64-v8a -o ../../jniLibs build --release
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Rust Build Failed for arm64-v8a!${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}  Building for x86_64 (emulators)...${NC}"
    cargo ndk -t x86_64 -o ../../jniLibs build --release
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Rust Build Failed for x86_64!${NC}"
        exit 1
    fi
    
    cd "$CURRENT_DIR"
    echo -e "${GREEN}Rust Build Successful for all architectures!${NC}"
else
    echo -e "${RED}Error: Rust directory not found at $RUST_DIR${NC}"
    exit 1
fi

# Build Debug APK
echo -e "${YELLOW}Building Debug APK...${NC}"
./gradlew :$APP_MODULE:assembleDebug

if [ $? -ne 0 ]; then
    echo -e "${RED}Build Failed!${NC}"
    exit 1
fi

echo -e "${GREEN}Build Successful!${NC}"

# Verify APKs are generated
APK_COUNT=$(find $APP_MODULE/build/outputs/apk/debug -name "*-debug.apk" | grep -v "unaligned" | wc -l)
echo -e "${BLUE}Found $APK_COUNT APK variant(s)${NC}"

if [ "$APK_COUNT" -eq 0 ]; then
    echo -e "${RED}Error: No APK files found!${NC}"
    exit 1
fi

echo -e "${YELLOW}Installing APK to all devices...${NC}"
APK_NAME_BASE=$(basename "$APP_MODULE/build/outputs/apk/debug/app-debug.apk" .apk)

# Track installation results
INSTALL_SUCCESS=0
INSTALL_FAILED=0

# Function to get appropriate APK for device
get_apk_for_device() {
    local device_id=$1
    local is_emulator=$(adb -s "$device_id" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')
    local device_model=$(adb -s "$device_id" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    local device_abi=$(adb -s "$device_id" shell getprop ro.product.cpu.abi 2>/dev/null | tr -d '\r')
    
    local apk_path=""
    
    # Determine architecture
    if [ "$is_emulator" = "1" ] || [[ "$device_model" == *"SDK"* ]] || [[ "$device_model" == *"Emulator"* ]]; then
        # Emulator - usually x86_64
        if [[ "$device_abi" == *"x86_64"* ]]; then
            apk_path=$(find $APP_MODULE/build/outputs/apk/debug -name "*x86_64-debug.apk" | grep -v "unaligned" | head -n 1)
        elif [[ "$device_abi" == *"x86"* ]]; then
            apk_path=$(find $APP_MODULE/build/outputs/apk/debug -name "*x86-debug.apk" | grep -v "unaligned" | grep -v "x86_64" | head -n 1)
        fi
    else
        # Physical device - usually arm64-v8a
        if [[ "$device_abi" == *"arm64"* ]] || [[ "$device_abi" == *"aarch64"* ]]; then
            apk_path=$(find $APP_MODULE/build/outputs/apk/debug -name "*arm64-v8a-debug.apk" | grep -v "unaligned" | head -n 1)
        elif [[ "$device_abi" == *"armeabi"* ]]; then
            apk_path=$(find $APP_MODULE/build/outputs/apk/debug -name "*armeabi-v7a-debug.apk" | grep -v "unaligned" | head -n 1)
        fi
    fi
    
    # Fallback to universal APK if specific architecture not found
    if [ -z "$apk_path" ] || [ ! -f "$apk_path" ]; then
        apk_path=$(find $APP_MODULE/build/outputs/apk/debug -name "*universal-debug.apk" -o -name "app-debug.apk" | grep -v "unaligned" | head -n 1)
    fi
    
    echo "$apk_path"
}

# Function to install to a specific ADB device
install_to_device() {
    local device_id=$1
    local device_model=$(adb -s "$device_id" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    local is_emulator=$(adb -s "$device_id" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')
    local device_abi=$(adb -s "$device_id" shell getprop ro.product.cpu.abi 2>/dev/null | tr -d '\r')
    
    # Get appropriate APK for this device
    local apk_path=$(get_apk_for_device "$device_id")
    
    if [ -z "$apk_path" ] || [ ! -f "$apk_path" ]; then
        echo -e "${RED}✗ No suitable APK found for $device_model (ABI: $device_abi)${NC}"
        return 1
    fi
    
    local apk_name=$(basename "$apk_path")
    echo -e "${BLUE}Installing to: $device_model ($device_id)${NC}"
    echo -e "${BLUE}  Using APK: $apk_name (ABI: $device_abi)${NC}"
    
    if [ "$is_emulator" = "1" ] || [[ "$device_model" == *"SDK"* ]] || [[ "$device_model" == *"Emulator"* ]]; then
        # AVD Emulator
        echo -e "${YELLOW}  Using standard adb install for emulator...${NC}"
        INSTALL_OUTPUT=$(adb -s "$device_id" install -r -d "$apk_path" 2>&1)
        
        if echo "$INSTALL_OUTPUT" | grep -q "Success"; then
            echo -e "${GREEN}✓ Installed successfully to $device_model${NC}"
            adb -s "$device_id" shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
            return 0
        else
            echo -e "${RED}✗ Installation failed to $device_model${NC}"
            echo -e "${RED}  Error: $INSTALL_OUTPUT${NC}"
            return 1
        fi
    else
        # Physical device - use root method
        local remote_path="/data/local/tmp/$apk_name"
        
        echo -e "${YELLOW}  Pushing APK to device...${NC}"
        if ! adb -s "$device_id" push "$apk_path" "$remote_path" 2>&1; then
            echo -e "${RED}✗ Failed to push APK to $device_model${NC}"
            return 1
        fi
        
        echo -e "${YELLOW}  Installing via pm install (requires root)...${NC}"
        INSTALL_OUTPUT=$(adb -s "$device_id" shell "su -c 'pm install -r -d $remote_path'" 2>&1)
        
        if echo "$INSTALL_OUTPUT" | grep -q "Success"; then
            echo -e "${GREEN}✓ Installed successfully to $device_model${NC}"
            adb -s "$device_id" shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
            adb -s "$device_id" shell "rm -f $remote_path" > /dev/null 2>&1
            return 0
        else
            echo -e "${RED}✗ Installation failed to $device_model${NC}"
            echo -e "${RED}  Error: $INSTALL_OUTPUT${NC}"
            echo -e "${YELLOW}  Tip: Make sure root access is granted in Magisk/KernelSU${NC}"
            adb -s "$device_id" shell "rm -f $remote_path" > /dev/null 2>&1
            return 1
        fi
    fi
}

# Install to all ADB devices
if [ "$DEVICE_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}Debug: Processing $DEVICE_COUNT device(s)...${NC}"
    
    # Convert to array for reliable iteration
    mapfile -t DEVICE_ARRAY <<< "$DEVICE_LIST"
    
    echo -e "${YELLOW}Debug: Device array has ${#DEVICE_ARRAY[@]} elements${NC}"
    
    for device_id in "${DEVICE_ARRAY[@]}"; do
        # Trim whitespace
        device_id=$(echo "$device_id" | tr -d '[:space:]')
        
        if [ -n "$device_id" ]; then
            echo -e "${YELLOW}Debug: Installing to device_id: '$device_id'${NC}"
            if install_to_device "$device_id"; then
                ((INSTALL_SUCCESS++))
            else
                ((INSTALL_FAILED++))
            fi
        else
            echo -e "${YELLOW}Debug: Skipping empty device_id${NC}"
        fi
    done
fi

# Install to Waydroid if available
if [ "$WAYDROID_AVAILABLE" = true ]; then
    echo -e "${BLUE}Installing to: Waydroid${NC}"
    if waydroid app install "$APK_PATH" 2>&1 | grep -q -i "success\|installed"; then
        echo -e "${GREEN}✓ Installed successfully to Waydroid${NC}"
        waydroid app launch "$PACKAGE_NAME" > /dev/null 2>&1
        ((INSTALL_SUCCESS++))
    else
        echo -e "${RED}✗ Installation failed to Waydroid${NC}"
        ((INSTALL_FAILED++))
    fi
fi

echo -e "${GREEN}==========================${NC}"
if [ $INSTALL_FAILED -eq 0 ]; then
    echo -e "${GREEN}All installations completed successfully! ($INSTALL_SUCCESS/$((INSTALL_SUCCESS + INSTALL_FAILED)))${NC}"
else
    echo -e "${YELLOW}Installations completed with errors: $INSTALL_SUCCESS succeeded, $INSTALL_FAILED failed${NC}"
fi
echo -e "${GREEN}==========================${NC}"
