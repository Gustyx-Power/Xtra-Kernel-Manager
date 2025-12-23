#!/bin/bash

# ============================================================================
# Xtra Kernel Manager - Rust + Debug Build Script with Telegram Progress
# Linux/Arch Version
# ============================================================================

set -e

echo ""
echo "========================================"
echo "  XKM Debug Build Script (with Rust)"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
RUST_PROJECT="$PROJECT_ROOT/app/src/main/rust/xkm_native"
JNILIBS_DIR="$PROJECT_ROOT/app/src/main/jniLibs"
LOG_FILE="$PROJECT_ROOT/build_log.txt"

# --- Load Telegram credentials from gradle.properties ---
TG_BOT_TOKEN=""
TG_CHAT_ID=""
TG_MESSAGE_ID=""

if [ -f "$PROJECT_ROOT/gradle.properties" ]; then
    TG_BOT_TOKEN=$(grep "^telegramBotToken=" "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)
    TG_CHAT_ID=$(grep "^telegramChatId=" "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)
fi

# --- Initialize log file ---
echo "XKM Debug Build Log" > "$LOG_FILE"
echo "Started: $(date)" >> "$LOG_FILE"
echo "========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# --- Ask for Telegram upload ---
UPLOAD_TO_TELEGRAM=0
read -p "Apakah ingin mengirim APK ke Telegram? (Y/N): " UPLOAD_CHOICE
if [[ "${UPLOAD_CHOICE^^}" == "Y" ]]; then
    UPLOAD_TO_TELEGRAM=1
    echo -e "${GREEN}OK${NC} APK akan dikirim ke Telegram setelah build selesai."
else
    echo -e "${GREEN}OK${NC} APK tidak akan dikirim ke Telegram."
fi
echo ""

# --- Telegram Functions ---
send_telegram_start() {
    if [ -z "$TG_BOT_TOKEN" ] || [ -z "$TG_CHAT_ID" ]; then
        return 0
    fi

    local TITLE="$1"
    local STATUS="$2"
    local PROGRESS="$3"
    local TIME="$(date +%H:%M:%S)"

    local MESSAGE="🔨 *XKM Debug Build*

📌 *Status:* $STATUS
⏰ *Time:* $TIME
📊 *Progress:* $PROGRESS%

⚙️ Building..."

    local RESPONSE=$(curl -s -X POST "https://botapi.arasea.dpdns.org/bot$TG_BOT_TOKEN/sendMessage" \
        -d "chat_id=$TG_CHAT_ID" \
        -d "text=$MESSAGE" \
        -d "parse_mode=Markdown" 2>/dev/null)
    
    TG_MESSAGE_ID=$(echo "$RESPONSE" | grep -oP '"message_id":\K[0-9]+' | head -1)
}

update_progress() {
    if [ -z "$TG_BOT_TOKEN" ] || [ -z "$TG_CHAT_ID" ] || [ -z "$TG_MESSAGE_ID" ]; then
        return 0
    fi

    local STAGE="$1"
    local STATUS="$2"
    local DETAIL="$3"
    local PROGRESS="$4"
    local TIME="$(date +%H:%M:%S)"

    local PROGRESS_BAR=""
    local FILLED=$((PROGRESS / 5))
    local EMPTY=$((20 - FILLED))
    for ((i=0; i<FILLED; i++)); do PROGRESS_BAR+="█"; done
    for ((i=0; i<EMPTY; i++)); do PROGRESS_BAR+="░"; done

    local MESSAGE="🔨 *XKM Debug Build*

📌 *Stage:* $STAGE
📝 *Status:* $STATUS
💡 *Detail:* $DETAIL
⏰ *Time:* $TIME

[$PROGRESS_BAR] $PROGRESS%"

    curl -s -X POST "https://botapi.arasea.dpdns.org/bot$TG_BOT_TOKEN/editMessageText" \
        -d "chat_id=$TG_CHAT_ID" \
        -d "message_id=$TG_MESSAGE_ID" \
        -d "text=$MESSAGE" \
        -d "parse_mode=Markdown" > /dev/null 2>&1
}

build_failed() {
    echo ""
    echo -e "${RED}BUILD FAILED!${NC}"
    echo "Error: $1"

    if [ -n "$TG_BOT_TOKEN" ] && [ -n "$TG_CHAT_ID" ] && [ -n "$TG_MESSAGE_ID" ]; then
        local TIME="$(date +%H:%M:%S)"
        local MESSAGE="❌ *XKM Debug Build FAILED*

🚫 *Error:* $1
⏰ *Time:* $TIME

Please check the build log for details."

        curl -s -X POST "https://botapi.arasea.dpdns.org/bot$TG_BOT_TOKEN/editMessageText" \
            -d "chat_id=$TG_CHAT_ID" \
            -d "message_id=$TG_MESSAGE_ID" \
            -d "text=$MESSAGE" \
            -d "parse_mode=Markdown" > /dev/null 2>&1

        if [ -f "$LOG_FILE" ]; then
            echo "Sending error log to Telegram..."
            curl -s -X POST "https://botapi.arasea.dpdns.org/bot$TG_BOT_TOKEN/sendDocument" \
                -F "chat_id=$TG_CHAT_ID" \
                -F "document=@$LOG_FILE" \
                -F "caption=Build failed. See log for details." > /dev/null 2>&1
        fi
    fi
}

send_final_success() {
    if [ -z "$TG_BOT_TOKEN" ] || [ -z "$TG_CHAT_ID" ] || [ -z "$TG_MESSAGE_ID" ]; then
        return 0
    fi

    local TIME="$(date +%H:%M:%S)"
    local MESSAGE="✅ *XKM Debug Build SUCCESS*

🎉 Build completed successfully!
⏰ *Finished at:* $TIME

[████████████████████] 100%"

    curl -s -X POST "https://botapi.arasea.dpdns.org/bot$TG_BOT_TOKEN/editMessageText" \
        -d "chat_id=$TG_CHAT_ID" \
        -d "message_id=$TG_MESSAGE_ID" \
        -d "text=$MESSAGE" \
        -d "parse_mode=Markdown" > /dev/null 2>&1
}

# --- Send initial Telegram message ---
send_telegram_start "XKM Debug Build Started" "Initializing..." "0"

# =========================================================================
# [STEP 1/5] BUILD RUST NATIVE LIBRARY
# =========================================================================
echo "[1/5] Building Rust native library..."
echo "[1/5] Building Rust native library..." >> "$LOG_FILE"

update_progress "Rust Native Library" "Checking Rust installation..." "" "5"

# Check if Rust is installed
if ! command -v rustc &> /dev/null; then
    echo "ERROR: Rust not found!" >> "$LOG_FILE"
    build_failed "Rust not found! Please install from rustup.rs"
    exit 1
fi
echo -e "${GREEN}✓${NC} Rust found: $(rustc --version)"

update_progress "Rust Native Library" "Rust found, checking cargo-ndk..." "" "8"

# Check if cargo-ndk is installed
if ! command -v cargo-ndk &> /dev/null; then
    echo -e "${YELLOW}Installing cargo-ndk...${NC}"
    cargo install cargo-ndk >> "$LOG_FILE" 2>&1
fi
echo -e "${GREEN}✓${NC} cargo-ndk ready"

update_progress "Rust Native Library" "Adding Android targets..." "" "10"

# Add Android target
rustup target add aarch64-linux-android > /dev/null 2>&1 || true
echo -e "${GREEN}✓${NC} Android target ready"

update_progress "Rust Native Library" "Compiling..." "cargo ndk build" "15"

echo "Compiling Rust for Android..."

cd "$RUST_PROJECT"

# Try build with retry
BUILD_ATTEMPT=1
MAX_ATTEMPTS=3

while [ $BUILD_ATTEMPT -le $MAX_ATTEMPTS ]; do
    if cargo ndk -t arm64-v8a -o "$JNILIBS_DIR" build 2>&1 | tee -a "$LOG_FILE"; then
        break
    else
        if [ $BUILD_ATTEMPT -lt $MAX_ATTEMPTS ]; then
            echo -e "${YELLOW}Rust build failed, retrying in 3 seconds... (Attempt $BUILD_ATTEMPT/$MAX_ATTEMPTS)${NC}"
            update_progress "Rust Native Library" "Retry..." "Attempt $BUILD_ATTEMPT of $MAX_ATTEMPTS" "18"
            BUILD_ATTEMPT=$((BUILD_ATTEMPT + 1))
            sleep 3
            
            # Clean deps if needed
            if [ -d "$RUST_PROJECT/target/aarch64-linux-android/debug/deps" ]; then
                rm -rf "$RUST_PROJECT/target/aarch64-linux-android/debug/deps" 2>/dev/null || true
            fi
        else
            echo -e "${RED}RUST BUILD FAILED after 3 attempts!${NC}"
            build_failed "Rust build failed!"
            exit 1
        fi
    fi
done

cd "$PROJECT_ROOT"

update_progress "Rust Native Library" "Complete!" "Built successfully" "30"
echo -e "${GREEN}✓${NC} Rust library built successfully!"
echo ""

# =========================================================================
# [STEP 2/5] VERIFY NATIVE LIBRARY
# =========================================================================
echo "[2/5] Verifying native library..."

update_progress "Verify Library" "Checking arm64-v8a..." "arm64-v8a" "32"

if [ -f "$JNILIBS_DIR/arm64-v8a/libxkm_native.so" ]; then
    echo -e "${GREEN}✓${NC} arm64-v8a/libxkm_native.so"
else
    echo "ERROR: Native library not found!" >> "$LOG_FILE"
    build_failed "Native library not found!"
    exit 1
fi
echo ""

update_progress "Verify Library" "Native library verified!" "libxkm_native.so OK" "35"

# =========================================================================
# DEVICE DETECTION
# =========================================================================
echo "Mendeteksi perangkat Android yang terhubung..."

DEVICE_COUNT=0
SKIP_INSTALL=0
SELECTED_DEVICE=""

# Get list of devices
mapfile -t DEVICES < <(adb devices 2>/dev/null | tail -n +2 | grep -E "device$" | awk '{print $1}')
DEVICE_COUNT=${#DEVICES[@]}

if [ $DEVICE_COUNT -eq 0 ]; then
    echo -e "${YELLOW}Tidak ada perangkat yang terhubung via USB.${NC}"
    SKIP_INSTALL=1
else
    # Get device info
    declare -a MODELS
    declare -a VERSIONS
    
    for i in "${!DEVICES[@]}"; do
        SERIAL="${DEVICES[$i]}"
        MODEL=$(adb -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
        VERSION=$(adb -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')
        MODELS[$i]="$MODEL"
        VERSIONS[$i]="$VERSION"
    done

    if [ $DEVICE_COUNT -eq 1 ]; then
        SELECTED_DEVICE="${DEVICES[0]}"
        echo -e "Perangkat terdeteksi: ${BLUE}${DEVICES[0]}${NC} - ${MODELS[0]} - Android ${VERSIONS[0]}"
    else
        echo "Beberapa perangkat terdeteksi:"
        for i in "${!DEVICES[@]}"; do
            echo "  $((i+1)). ${DEVICES[$i]} - ${MODELS[$i]} - Android ${VERSIONS[$i]}"
        done
        
        read -p "Pilih perangkat (1-$DEVICE_COUNT): " CHOICE
        if [[ "$CHOICE" =~ ^[0-9]+$ ]] && [ "$CHOICE" -ge 1 ] && [ "$CHOICE" -le $DEVICE_COUNT ]; then
            SELECTED_DEVICE="${DEVICES[$((CHOICE-1))]}"
        else
            echo -e "${YELLOW}Pilihan tidak valid. Melewati instalasi.${NC}"
            SKIP_INSTALL=1
        fi
    fi
fi

if [ $SKIP_INSTALL -eq 0 ]; then
    export ANDROID_SERIAL="$SELECTED_DEVICE"
fi

# =========================================================================
# [STEP 3/5] BUILD ANDROID APK
# =========================================================================
echo ""
echo "[3/5] Building debug APK..."
echo ""

update_progress "Android APK" "Building..." "assembleDebug" "40"

WORKERS=$(nproc)

if ./gradlew assembleDebug --parallel --max-workers=$WORKERS --console=plain 2>&1 | tee -a "$LOG_FILE"; then
    echo ""
else
    echo -e "${RED}GRADLE BUILD FAILED!${NC}"
    build_failed "Gradle build failed!"
    exit 1
fi

update_progress "Android APK" "Complete!" "APK built successfully" "70"
echo -e "${GREEN}✓${NC} Debug APK built successfully!"
echo ""

# =========================================================================
# [STEP 4/5] INSTALL APK
# =========================================================================
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ] && [ -f "$APK_PATH" ]; then
    if [ $SKIP_INSTALL -eq 0 ]; then
        echo "[4/5] Menginstall APK ke perangkat $SELECTED_DEVICE..."
        update_progress "Installing APK" "Installing to device..." "adb install" "75"
        if adb -s "$SELECTED_DEVICE" install -r "$APK_PATH" >> "$LOG_FILE" 2>&1; then
            echo -e "${GREEN}✓${NC} APK berhasil diinstall."
        else
            echo -e "${YELLOW}⚠${NC} APK gagal diinstall ke perangkat."
        fi
        update_progress "Installing APK" "Installation complete!" "Success" "80"
    else
        echo "[4/5] Tidak ada perangkat, skip instalasi."
        update_progress "Skip Installation" "No device connected" "" "80"
    fi
else
    echo -e "${RED}APK tidak ditemukan!${NC}"
    build_failed "APK not found!"
    exit 1
fi

echo ""

# =========================================================================
# [STEP 5/5] UPLOAD TO TELEGRAM
# =========================================================================
if [ $UPLOAD_TO_TELEGRAM -eq 1 ]; then
    echo "[5/5] Uploading to Telegram..."
    update_progress "Upload to Telegram" "Renaming APK..." "XKM-version-debug.apk" "85"
    ./gradlew renameDebugApk >> "$LOG_FILE" 2>&1 || true
    
    update_progress "Upload to Telegram" "Uploading APK..." "Sending file..." "90"
    if ./gradlew uploadDebugApkToTelegram >> "$LOG_FILE" 2>&1; then
        echo -e "${GREEN}✓${NC} APK uploaded to Telegram successfully!"
        update_progress "Upload to Telegram" "Upload complete!" "Done" "100"
    else
        update_progress "Upload Warning" "Upload may have failed" "Check logs" "95"
    fi
else
    echo "[5/5] Skipped - Telegram upload not requested."
    update_progress "Build Complete" "Telegram upload skipped" "" "100"
fi

# =========================================================================
# BUILD SUCCESS
# =========================================================================
echo ""
echo "========================================"
echo -e "  ${GREEN}BUILD COMPLETED SUCCESSFULLY!${NC}"
echo "========================================"
echo ""

send_final_success

echo "APK Location: $APK_PATH"
echo ""
