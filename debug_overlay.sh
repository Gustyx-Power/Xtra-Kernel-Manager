#!/bin/bash

# Debug script for Game Overlay auto-start issue
# This script helps diagnose why the overlay isn't appearing automatically

echo "=========================================="
echo "XKM Game Overlay Debug Script v2"
echo "=========================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected via ADB"
    exit 1
fi

echo "✅ Device connected"
echo ""

# 1. Check if AppProfileService is running
echo "1. Checking AppProfileService status..."
SERVICE_RUNNING=$(adb shell "ps -A | grep -i appprofile")
if [ -z "$SERVICE_RUNNING" ]; then
    echo "❌ AppProfileService is NOT running"
    echo "   Attempting to start service..."
    adb shell "am startservice id.xms.xtrakernelmanager/.service.AppProfileService"
    sleep 2
    SERVICE_RUNNING=$(adb shell "ps -A | grep -i appprofile")
    if [ -z "$SERVICE_RUNNING" ]; then
        echo "❌ Failed to start service - check logcat for errors"
    else
        echo "✅ Service started successfully"
    fi
else
    echo "✅ AppProfileService is running:"
    echo "$SERVICE_RUNNING"
fi
echo ""

# 2. Check for service notification
echo "2. Checking for Per-App Profile notification..."
NOTIF=$(adb shell "dumpsys notification | grep -A 5 'Per-App Profile' | head -10")
if [ -z "$NOTIF" ]; then
    echo "❌ No Per-App Profile notification found"
else
    echo "✅ Per-App Profile notification exists"
fi
echo ""

# 3. Check Usage Stats permission
echo "3. Checking Usage Stats permission..."
USAGE_PERM=$(adb shell "appops get id.xms.xtrakernelmanager GET_USAGE_STATS")
if echo "$USAGE_PERM" | grep -q "allow"; then
    echo "✅ Usage Stats permission granted"
else
    echo "❌ Usage Stats permission NOT granted"
    echo "   Attempting to grant..."
    adb shell "appops set id.xms.xtrakernelmanager GET_USAGE_STATS allow"
fi
echo ""

# 4. Check Overlay permission
echo "4. Checking Overlay permission..."
OVERLAY_PERM=$(adb shell "appops get id.xms.xtrakernelmanager SYSTEM_ALERT_WINDOW")
if echo "$OVERLAY_PERM" | grep -q "allow"; then
    echo "✅ Overlay permission granted"
else
    echo "❌ Overlay permission NOT granted"
    echo "   Attempting to grant..."
    adb shell "appops set id.xms.xtrakernelmanager SYSTEM_ALERT_WINDOW allow"
fi
echo ""

# 5. Check registered profiles
echo "5. Checking registered app profiles..."
PROFILES=$(adb shell "cat /data/data/id.xms.xtrakernelmanager/shared_prefs/xkm_preferences.xml 2>/dev/null | grep 'app_profiles'")
if [ -z "$PROFILES" ]; then
    echo "❌ No profiles found in preferences"
else
    echo "✅ Profiles found in preferences"
    # Extract package names
    echo "$PROFILES" | sed 's/&quot;/"/g' | grep -o '"packageName":"[^"]*"' | sed 's/"packageName":"//g' | sed 's/"//g' | while read pkg; do
        echo "   - $pkg"
    done
fi
echo ""

# 6. Check if Chrome is registered
echo "6. Checking if Chrome is registered..."
CHROME_PROFILE=$(echo "$PROFILES" | grep -i "chrome")
if [ -z "$CHROME_PROFILE" ]; then
    echo "❌ Chrome is NOT registered in profiles"
else
    echo "✅ Chrome is registered"
fi
echo ""

# 7. Check current foreground app
echo "7. Checking current foreground app..."
FOREGROUND=$(adb shell "dumpsys activity activities | grep 'mResumedActivity' | head -1")
echo "$FOREGROUND"
echo ""

# 8. Check recent logs
echo "8. Recent AppProfileService logs (last 30 lines)..."
adb logcat -d -s AppProfileService:V | tail -30
echo ""

echo "=========================================="
echo "9. Starting live log monitoring..."
echo "   Press Ctrl+C to stop"
echo "   Now open Chrome to test..."
echo "=========================================="
echo ""
adb logcat -c  # Clear logcat
adb logcat -s AppProfileService:V GameOverlayService:V
