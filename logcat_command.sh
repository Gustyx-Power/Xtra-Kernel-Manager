#!/bin/bash
# Command untuk melihat log dari Process Manager
# Jalankan setelah app di-install dan buka Process Manager screen

echo "=== Monitoring Process Manager Logs ==="
echo "Press Ctrl+C to stop"
echo ""

adb logcat -c  # Clear log
adb logcat | grep -E "(LiquidProcessManager|MaterialProcessManager|ProcessModels|AndroidRuntime|FATAL)"
