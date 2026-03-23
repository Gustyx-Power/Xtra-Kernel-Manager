#!/bin/bash

# Configuration
APP_MODULE="app"
PACKAGE_NAME="id.xms.xtrakernelmanager.dev"
MAIN_ACTIVITY=".MainActivity"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting Debug Build Process for macOS...${NC}"

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

# Check for connected devices
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)

# Determine installation target
INSTALL_TARGET=""
if [ "$WAYDROID_AVAILABLE" = true ] && [ "$DEVICE_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}Multiple targets available:${NC}"
    echo "1) ADB Device ($DEVICE_COUNT device(s) connected)"
    echo "2) Waydroid"
    read -p "Select target (1/2): " choice
    case $choice in
        1) INSTALL_TARGET="adb" ;;
        2) INSTALL_TARGET="waydroid" ;;
        *) echo -e "${RED}Invalid choice. Defaulting to ADB.${NC}"; INSTALL_TARGET="adb" ;;
    esac
elif [ "$WAYDROID_AVAILABLE" = true ]; then
    echo -e "${GREEN}Using Waydroid as target...${NC}"
    INSTALL_TARGET="waydroid"
elif [ "$DEVICE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}Device found. Proceeding with build...${NC}"
    INSTALL_TARGET="adb"
else
    echo -e "${RED}Error: No device connected and Waydroid not running.${NC}"
    echo -e "${YELLOW}Tip: Start Waydroid with 'waydroid session start' or connect an Android device.${NC}"
    exit 1
fi

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
    
    # Build for arm64-v8a (common for modern devices) - adjust targets as needed
    cargo ndk -t arm64-v8a -o ../../jniLibs build --release
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Rust Build Failed!${NC}"
        # cd back before exiting? simpler to just exit, shell context ends
        exit 1
    fi
    
    cd "$CURRENT_DIR"
    echo -e "${GREEN}Rust Build Successful!${NC}"
else
    echo -e "${RED}Error: Rust directory not found at $RUST_DIR${NC}"
    exit 1
fi

# specialized clean before build if needed, generally allow incremental
# ./gradlew clean 

# Build Debug APK
echo -e "${YELLOW}Building Debug APK...${NC}"
./gradlew :$APP_MODULE:assembleDebug

if [ $? -ne 0 ]; then
    echo -e "${RED}Build Failed!${NC}"
    exit 1
fi

echo -e "${GREEN}Build Successful!${NC}"

# Find the generated APK
APK_PATH=$(find $APP_MODULE/build/outputs/apk/debug -name "*-debug.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    # Fallback to standard name if wildcard fails or verify path
    APK_PATH="$APP_MODULE/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Error: APK not found at $APK_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}Installing APK ($APK_PATH)...${NC}"

if [ "$INSTALL_TARGET" = "waydroid" ]; then
    # Install to Waydroid
    echo -e "${YELLOW}Installing to Waydroid...${NC}"
    waydroid app install "$APK_PATH"
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Waydroid Installation Failed!${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Installed Successfully to Waydroid!${NC}"
    
    echo -e "${YELLOW}Launching App in Waydroid...${NC}"
    waydroid app launch "$PACKAGE_NAME"
    
    echo -e "${GREEN}Done!${NC}"
    echo -e "${YELLOW}Tip: Use 'waydroid show-full-ui' to see the Waydroid window.${NC}"
else
    # Install to ADB device
    APK_NAME=$(basename "$APK_PATH")
    
    # Detect if device is an emulator
    IS_EMULATOR=$(adb shell getprop ro.kernel.qemu 2>/dev/null)
    DEVICE_MODEL=$(adb shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    
    if [ "$IS_EMULATOR" = "1" ] || [[ "$DEVICE_MODEL" == *"SDK"* ]] || [[ "$DEVICE_MODEL" == *"Emulator"* ]]; then
        echo -e "${GREEN}AVD Emulator detected: $DEVICE_MODEL${NC}"
        echo -e "${YELLOW}Installing via standard adb install...${NC}"
        
        adb install -r -d "$APK_PATH"
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}Installation Failed!${NC}"
            exit 1
        fi
    else
        # Physical device - use root method
        echo -e "${GREEN}Physical device detected: $DEVICE_MODEL${NC}"
        REMOTE_PATH="/data/local/tmp/$APK_NAME"
        
        echo -e "${YELLOW}Pushing APK to device...${NC}"
        adb push "$APK_PATH" "$REMOTE_PATH"
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}Push Failed!${NC}"
            exit 1
        fi
        
        echo -e "${YELLOW}Installing via pm install (root)...${NC}"
        adb shell "su -c 'pm install -r -d $REMOTE_PATH'"
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}Installation Failed!${NC}"
            # Cleanup temp file
            adb shell "rm -f $REMOTE_PATH"
            exit 1
        fi
        
        # Cleanup temp file
        adb shell "rm -f $REMOTE_PATH"
    fi
    
    echo -e "${GREEN}Installed Successfully!${NC}"
    
    echo -e "${YELLOW}Launching App...${NC}"
    # Try to launch using monkey (generic) or specific intent
    adb shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1
    
    echo -e "${GREEN}Done!${NC}"
fi
