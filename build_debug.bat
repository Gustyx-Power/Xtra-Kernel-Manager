@echo off
setlocal EnableDelayedExpansion

:: Configuration
set "APP_MODULE=app"
set "PACKAGE_NAME=id.xms.xtrakernelmanager.dev"
set "MAIN_ACTIVITY=.MainActivity"

:: Colors (Simulated with simple tags as raw ANSI is tricky in BAT without setup)
echo [INFO] Starting Debug Build Process for Windows...

:: Check if ADB is available
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] ADB not found in PATH. Please install Android Platform Tools.
    exit /b 1
)

:: Check for connected devices
set "DEVICE_FOUND=0"
for /f "tokens=*" %%i in ('adb devices ^| findstr "device" ^| findstr /v "List"') do (
    set "DEVICE_FOUND=1"
)

if "!DEVICE_FOUND!"=="0" (
    echo [ERROR] No device connected. Please connect your Android device.
    exit /b 1
)

echo [INFO] Device found. Proceeding with build...

:: Setup NDK Path
if "%ANDROID_NDK_HOME%"=="" (
    :: Try to auto-detect
    if exist "%LOCALAPPDATA%\Android\Sdk\ndk" (
        :: Just pick the first directory found in NDK folder if possible, or set base
        :: For simplicity warning the user if specific version needed
        echo [WARN] ANDROID_NDK_HOME not set. Trying to locate...
        for /d %%D in ("%LOCALAPPDATA%\Android\Sdk\ndk\*") do (
            set "ANDROID_NDK_HOME=%%D"
            goto :NdkFound
        )
    )
)

:NdkFound
if "%ANDROID_NDK_HOME%"=="" (
    echo [WARN] Could not auto-detect ANDROID_NDK_HOME. Rust build might fail if not in PATH.
) else (
    echo [INFO] Using NDK at: !ANDROID_NDK_HOME!
    set "NDK_HOME=!ANDROID_NDK_HOME!"
)

:: Rust Build
echo [INFO] Building Rust Native Library (Release)...

:: Check for Cargo
where cargo >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Rust/Cargo not found. Please install Rust.
    exit /b 1
)

:: Check/Install cargo-ndk
where cargo-ndk >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARN] cargo-ndk not found. Installing...
    cargo install cargo-ndk
)

:: Build Rust Lib
set "CURRENT_DIR=%CD%"
set "RUST_DIR=%APP_MODULE%\src\main\rust\xkm_native"

if exist "%RUST_DIR%" (
    cd /d "%RUST_DIR%"
    
    echo [INFO] Building Rust for arm64-v8a...
    :: Using 'call' if cargo executes another batch, though usually it's an exe
    cargo ndk -t arm64-v8a -o ..\..\jniLibs build --release
    
    if !errorlevel! neq 0 (
        echo [ERROR] Rust Build Failed!
        cd /d "%CURRENT_DIR%"
        exit /b 1
    )
    
    cd /d "%CURRENT_DIR%"
    echo [INFO] Rust Build Successful!
) else (
    echo [ERROR] Rust directory not found at %RUST_DIR%
    exit /b 1
)

:: Build Debug APK
echo [INFO] Building Debug APK...
call gradlew.bat :%APP_MODULE%:assembleDebug

if %errorlevel% neq 0 (
    echo [ERROR] Build Failed!
    exit /b 1
)

echo [INFO] Build Successful!

:: Find the generated APK
:: Bash used 'find', for batch we predict the path or use dir
set "APK_PATH=%APP_MODULE%\build\outputs\apk\debug\app-debug.apk"

:: Try to find if explicit path doesn't exist (basic wildcard search fallback)
if not exist "%APK_PATH%" (
    for /r "%APP_MODULE%\build\outputs\apk\debug" %%F in (*-debug.apk) do (
        set "APK_PATH=%%F"
        goto :ApkFound
    )
)

:ApkFound
if not exist "%APK_PATH%" (
    echo [ERROR] APK not found at %APK_PATH%
    exit /b 1
)

echo [INFO] Installing APK (%APK_PATH%)...

:: ColorOS 16 fix: Use push + pm install
for %%F in ("%APK_PATH%") do set "APK_NAME=%%~nxF"
set "REMOTE_PATH=/data/local/tmp/!APK_NAME!"

echo [INFO] Pushing APK to device...
adb push "%APK_PATH%" "%REMOTE_PATH%"

if %errorlevel% neq 0 (
    echo [ERROR] Push Failed!
    exit /b 1
)

echo [INFO] Installing via pm install (root)...
adb shell "su -c 'pm install -r -d %REMOTE_PATH%'"

if %errorlevel% neq 0 (
    echo [ERROR] Installation Failed!
    adb shell "rm -f %REMOTE_PATH%"
    exit /b 1
) else (
    echo [INFO] Installed Successfully!
    adb shell "rm -f %REMOTE_PATH%"
    
    echo [INFO] Launching App...
    adb shell monkey -p %PACKAGE_NAME% -c android.intent.category.LAUNCHER 1
    
    echo [INFO] Done!
)

endlocal
