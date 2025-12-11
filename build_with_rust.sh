#!/bin/bash

# ============================================================
# XKM Build Script - Builds Rust native library + Android APK
# ============================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
RUST_PROJECT="$PROJECT_ROOT/app/src/main/rust/xkm_native"
JNILIBS_DIR="$PROJECT_ROOT/app/src/main/jniLibs"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   XKM Build Script (Rust + Android)   ${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# Check for build type argument
BUILD_TYPE="debug"
if [[ "$1" == "release" || "$1" == "--release" || "$1" == "-r" ]]; then
    BUILD_TYPE="release"
fi

echo -e "${YELLOW}Build type: $BUILD_TYPE${NC}"
echo

# ============================================================
# Step 1: Build Rust Native Library
# ============================================================
echo -e "${BLUE}[1/3] Building Rust native library...${NC}"

# Check if Rust is installed
if ! command -v rustc &> /dev/null; then
    echo -e "${RED}ERROR: Rust not found! Please install Rust from https://rustup.rs${NC}"
    exit 1
fi

# Check if cargo-ndk is installed
if ! command -v cargo-ndk &> /dev/null; then
    echo -e "${YELLOW}Installing cargo-ndk...${NC}"
    cargo install cargo-ndk
fi

# Change to Rust project directory
cd "$RUST_PROJECT"

# Add Android targets if not present
echo -e "${YELLOW}Ensuring Android targets are installed...${NC}"
rustup target add aarch64-linux-android 2>/dev/null || true
rustup target add armv7-linux-androideabi 2>/dev/null || true
rustup target add x86_64-linux-android 2>/dev/null || true

# Build Rust library for Android
echo -e "${YELLOW}Compiling Rust for Android targets...${NC}"

if [[ "$BUILD_TYPE" == "release" ]]; then
    cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o "$JNILIBS_DIR" build --release
else
    cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o "$JNILIBS_DIR" build
fi

echo -e "${GREEN}Rust library built successfully!${NC}"
echo

# ============================================================
# Step 2: Verify .so files
# ============================================================
echo -e "${BLUE}[2/3] Verifying native libraries...${NC}"

LIBS_OK=1

if [[ -f "$JNILIBS_DIR/arm64-v8a/libxkm_native.so" ]]; then
    echo -e "  ${GREEN}✓${NC} arm64-v8a/libxkm_native.so"
else
    echo -e "  ${RED}✗${NC} arm64-v8a/libxkm_native.so MISSING"
    LIBS_OK=0
fi

if [[ -f "$JNILIBS_DIR/armeabi-v7a/libxkm_native.so" ]]; then
    echo -e "  ${GREEN}✓${NC} armeabi-v7a/libxkm_native.so"
else
    echo -e "  ${RED}✗${NC} armeabi-v7a/libxkm_native.so MISSING"
    LIBS_OK=0
fi

if [[ -f "$JNILIBS_DIR/x86_64/libxkm_native.so" ]]; then
    echo -e "  ${GREEN}✓${NC} x86_64/libxkm_native.so"
else
    echo -e "  ${RED}✗${NC} x86_64/libxkm_native.so MISSING"
    LIBS_OK=0
fi

if [[ $LIBS_OK -eq 0 ]]; then
    echo -e "${RED}ERROR: Some native libraries are missing!${NC}"
    exit 1
fi

echo

# ============================================================
# Step 3: Build Android APK
# ============================================================
echo -e "${BLUE}[3/3] Building Android APK...${NC}"

cd "$PROJECT_ROOT"

if [[ "$BUILD_TYPE" == "release" ]]; then
    ./gradlew assembleRelease --warning-mode none
else
    ./gradlew assembleDebug --warning-mode none
fi

echo
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Build completed successfully!       ${NC}"
echo -e "${GREEN}========================================${NC}"
echo

if [[ "$BUILD_TYPE" == "release" ]]; then
    echo "APK: $PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
else
    echo "APK: $PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
fi
