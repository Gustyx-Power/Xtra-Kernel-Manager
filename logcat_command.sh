#!/bin/bash
# Command untuk melihat log dari Process Manager
# Jalankan setelah app di-install dan buka Process Manager screen

echo "=== Monitoring Process Manager Logs ==="
echo "Press Ctrl+C to stop"
echo ""

adb logcat -c
adb logcat | grep -E "(FrostedProcessManager|MaterialProcessManager|ProcessModels|AndroidRuntime|FATAL)"
