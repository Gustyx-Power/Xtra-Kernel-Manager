#!/bin/bash

# Configuration
APP_MODULE="app"
PACKAGE_NAME="id.xms.xtrakernelmanager.dev"
MAIN_ACTIVITY=".MainActivity" # Adjust if your main activity has a different name or path relative to package

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🚀 Starting Debug Build Process for macOS...${NC}"

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo -e "${RED}❌ Error: ADB not found in PATH. Please install Android Platform Tools.${NC}"
    exit 1
fi

# Check for connected devices
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ Error: No device connected. Please connect your Android device.${NC}"
    exit 1
fi

echo -e "${GREEN}📱 Device found. Proceeding with build...${NC}"

# Setup NDK Path
if [ -z "$ANDROID_NDK_HOME" ]; then
    # Prioritize specific versions we know work (r26, r25)
    if [ -d "/usr/local/share/android-commandlinetools/ndk/26.1.10909125" ]; then
        export ANDROID_NDK_HOME="/usr/local/share/android-commandlinetools/ndk/26.1.10909125"
        echo -e "${GREEN}✅ Auto-configured NDK (r26) at $ANDROID_NDK_HOME${NC}"
    elif [ -d "/usr/local/share/android-commandlinetools/ndk-bundle" ]; then
        export ANDROID_NDK_HOME="/usr/local/share/android-commandlinetools/ndk-bundle"
        echo -e "${GREEN}✅ Auto-configured NDK (bundle) at $ANDROID_NDK_HOME${NC}"
    elif [ -d "$HOME/Library/Android/sdk/ndk-bundle" ]; then
        export ANDROID_NDK_HOME="$HOME/Library/Android/sdk/ndk-bundle"
        echo -e "${GREEN}✅ Auto-configured NDK at $ANDROID_NDK_HOME${NC}"
    fi
fi

# Rust Build
echo -e "${YELLOW}🦀 Building Rust Native Library (Release)...${NC}"

# Check for Cargo
if ! command -v cargo &> /dev/null; then
    echo -e "${RED}❌ Error: Rust/Cargo not found. Please install Rust.${NC}"
    exit 1
fi

# Check/Install cargo-ndk
if ! command -v cargo-ndk &> /dev/null; then
    echo -e "${YELLOW}⚠️ cargo-ndk not found. Installing...${NC}"
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
        echo -e "${RED}❌ Rust Build Failed!${NC}"
        # cd back before exiting? simpler to just exit, shell context ends
        exit 1
    fi
    
    cd "$CURRENT_DIR"
    echo -e "${GREEN}✅ Rust Build Successful!${NC}"
else
    echo -e "${RED}❌ Error: Rust directory not found at $RUST_DIR${NC}"
    exit 1
fi

# specialized clean before build if needed, generally allow incremental
# ./gradlew clean 

# Build Debug APK
echo -e "${YELLOW}🔨 Building Debug APK...${NC}"
./gradlew :$APP_MODULE:assembleDebug

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build Failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build Successful!${NC}"

# Find the generated APK
APK_PATH=$(find $APP_MODULE/build/outputs/apk/debug -name "*-debug.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    # Fallback to standard name if wildcard fails or verify path
    APK_PATH="$APP_MODULE/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ Error: APK not found at $APK_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Installing APK ($APK_PATH)...${NC}"
adb install -r "$APK_PATH"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Installation Failed!${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Installed Successfully!${NC}"
    
    echo -e "${YELLOW}🚀 Launching App...${NC}"
    # Try to launch using monkey (generic) or specific intent
    adb shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1
    
    echo -e "${GREEN}✨ Done!${NC}"
fi
