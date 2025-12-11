@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM Xtra Kernel Manager - Rust + Debug Build Script
REM ============================================================================

echo.
echo ========================================
echo  XKM Debug Build Script (with Rust)
echo ========================================
echo.

set PROJECT_ROOT=%~dp0
set RUST_PROJECT=%PROJECT_ROOT%app\src\main\rust\xkm_native
set JNILIBS_DIR=%PROJECT_ROOT%app\src\main\jniLibs

REM --- Ask for Telegram upload ---
set UPLOAD_TO_TELEGRAM=0
set /p UPLOAD_CHOICE="Apakah ingin mengirim APK ke Telegram? (Y/N): "
if /i "!UPLOAD_CHOICE!"=="Y" (
    set UPLOAD_TO_TELEGRAM=1
    echo OK APK akan dikirim ke Telegram setelah build selesai.
) else (
    echo OK APK tidak akan dikirim ke Telegram.
)
echo.

REM --- Build Rust Native Library ---
echo [1/5] Building Rust native library...

REM Check if Rust is installed
where rustc >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Rust not found! Please install Rust from https://rustup.rs
    pause
    exit /b 1
)

REM Check if cargo-ndk is installed
where cargo-ndk >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Installing cargo-ndk...
    cargo install cargo-ndk
)

REM Change to Rust project directory
cd /d "%RUST_PROJECT%"

REM Kill rust-analyzer to prevent file locks
taskkill /f /im rust-analyzer.exe 2>nul
timeout /t 1 /nobreak >nul

REM Add Android targets
rustup target add aarch64-linux-android >nul 2>&1

REM Build Rust library (debug mode, single-threaded)
echo Compiling Rust for Android...
cargo ndk -t arm64-v8a -o "%JNILIBS_DIR%" build -j 1

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Rust build failed!
    echo TIP: Close VS Code and try again
    pause
    exit /b 1
)

echo OK Rust library built successfully!
echo.

REM --- Verify native library ---
echo [2/5] Verifying native library...
if exist "%JNILIBS_DIR%\arm64-v8a\libxkm_native.so" (
    echo OK arm64-v8a/libxkm_native.so
) else (
    echo ERROR: Native library not found!
    pause
    exit /b 1
)
echo.

REM --- Return to project root ---
cd /d "%PROJECT_ROOT%"

REM --- Detect connected devices ---
echo Mendeteksi perangkat Android yang terhubung...

set DEVICE_COUNT=0
set /a INDEX=0
set SKIP_INSTALL=0

set TEMP_DEVICES=%TEMP%\devices_list.txt
adb devices > "%TEMP_DEVICES%"

for /f "skip=1 tokens=1,2" %%A in ('type "%TEMP_DEVICES%"') do (
    if "%%B"=="device" (
        set /a DEVICE_COUNT+=1
        set DEVICE_!DEVICE_COUNT!=%%A
    )
)

del "%TEMP_DEVICES%"

if %DEVICE_COUNT%==0 (
    echo Tidak ada perangkat yang terhubung via USB.
    set SKIP_INSTALL=1
    goto :BuildStart
)

REM Get device info
set /a INDEX=1
:DeviceInfoLoop
if !INDEX! gtr %DEVICE_COUNT% goto :DeviceInfoDone
set SERIAL=!DEVICE_%INDEX%!

for /f "delims=" %%M in ('adb -s !SERIAL! shell getprop ro.product.model') do set MODEL_!INDEX!=%%M
for /f "delims=" %%V in ('adb -s !SERIAL! shell getprop ro.build.version.release') do set VERSION_!INDEX!=%%V

set /a INDEX+=1
goto :DeviceInfoLoop

:DeviceInfoDone

if %DEVICE_COUNT%==1 (
    set SELECTED_DEVICE=!DEVICE_1!
    echo Perangkat terdeteksi: !DEVICE_1! ^| Model: !MODEL_1! ^| Android: !VERSION_1!
) else (
    echo Beberapa perangkat terdeteksi:
    set /a INDEX=1
    :DisplayDeviceLoop
    if !INDEX! gtr %DEVICE_COUNT% goto :DisplayDeviceDone
    echo !INDEX!. !DEVICE_%INDEX%! ^| Model: !MODEL_%INDEX%! ^| Android: !VERSION_%INDEX%!
    set /a INDEX+=1
    goto :DisplayDeviceLoop
    
    :DisplayDeviceDone
    set /p CHOICE="Pilih perangkat (1-%DEVICE_COUNT%): "
    
    if !CHOICE! lss 1 goto :InvalidChoice
    if !CHOICE! gtr %DEVICE_COUNT% goto :InvalidChoice
    
    set SELECTED_DEVICE=!DEVICE_%CHOICE%!
    goto :SetSerial
    
    :InvalidChoice
    echo Pilihan tidak valid. Build akan tetap dijalankan tanpa instalasi.
    set SKIP_INSTALL=1
)

:SetSerial
if %SKIP_INSTALL%==0 (
    set ANDROID_SERIAL=%SELECTED_DEVICE%
)

:BuildStart
echo.
echo [3/5] Building debug APK...
echo.

set WORKERS=%NUMBER_OF_PROCESSORS%
call gradlew.bat assembleDebug --parallel --max-workers=%WORKERS%

if errorlevel 1 (
    echo.
    echo ========================================
    echo  BUILD FAILED!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo OK Debug APK built successfully!
echo.

REM Find APK
set APK_PATH=
for /f "delims=" %%A in ('dir /s /b app\build\outputs\apk\debug\*.apk 2^>nul') do (
    set APK_PATH=%%A
    goto :FoundAPK
)

:FoundAPK
if exist "%APK_PATH%" (
    if %SKIP_INSTALL%==0 (
        echo [4/5] Menginstall APK ke perangkat %SELECTED_DEVICE%...
        adb -s %SELECTED_DEVICE% install -r "%APK_PATH%"
        echo OK APK berhasil diinstall.
    ) else (
        echo [4/5] Tidak ada perangkat, skip instalasi.
    )
) else (
    echo APK tidak ditemukan!
    pause
    exit /b 1
)

echo.

REM --- Upload to Telegram if requested ---
if %UPLOAD_TO_TELEGRAM%==1 (
    echo [5/5] Uploading to Telegram...
    call gradlew.bat renameDebugApk
    call gradlew.bat uploadDebugApkToTelegram
    
    if errorlevel 1 (
        echo WARNING: Upload to Telegram may have failed.
    ) else (
        echo OK APK uploaded to Telegram successfully!
    )
) else (
    echo [5/5] Skipped - Telegram upload not requested.
)

echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo APK Location:
echo   - %APK_PATH%
if %UPLOAD_TO_TELEGRAM%==1 (
    echo   - app\dist\XKM-[version]-debug.apk
)
echo.
echo The debug APK has been:
echo   OK Built with Rust native library
echo   OK Built successfully
if %SKIP_INSTALL%==0 (
    echo   OK Installed to device
)
if %UPLOAD_TO_TELEGRAM%==1 (
    echo   OK Uploaded to Telegram
)
echo.
echo ========================================

echo.
pause
endlocal
