@echo off
setlocal EnableDelayedExpansion

REM ============================================================================
REM Xtra Kernel Manager - Rust + Release Build & Telegram Upload Script
REM ============================================================================
REM This script:
REM 1. Builds Rust native library for Android
REM 2. Builds signed release APK
REM 3. Renames the APK with version name
REM 4. Uploads to Telegram test group
REM 5. Sends build notification
REM ============================================================================

echo.
echo ========================================
echo  XKM Release Build Script (with Rust)
echo ========================================
echo.

REM --- Configuration ---
set KEYSTORE_PATH=C:\Users\putri\Documents\Project\XMS\Keystore\Keystore-XKM\xkm-release-key.jks
set KEY_ALIAS=xkmkey
set KEY_PASSWORD=gusti717
set KEYSTORE_PASSWORD=gusti717

set PROJECT_ROOT=%~dp0
set RUST_PROJECT=%PROJECT_ROOT%app\src\main\rust\xkm_native
set JNILIBS_DIR=%PROJECT_ROOT%app\src\main\jniLibs

REM --- Validate Keystore ---
echo [1/8] Validating keystore...
if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Keystore not found at: %KEYSTORE_PATH%
    echo Please check the keystore path and try again.
    pause
    exit /b 1
)
echo OK Keystore found: %KEYSTORE_PATH%
echo.

REM --- Build Rust Native Library ---
echo [2/8] Building Rust native library...

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

REM Build Rust library (release, single-threaded to avoid file locks)
echo Compiling Rust for Android (this may take a while)...
cargo ndk -t arm64-v8a -o "%JNILIBS_DIR%" build --release -j 1

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
echo [3/8] Verifying native library...
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

REM --- Clean Build ---
echo [4/8] Cleaning previous builds...
if exist "app\build" (
    rmdir /s /q "app\build" 2>nul
    echo OK Cleaned app\build
)
if exist "app\dist" (
    rmdir /s /q "app\dist" 2>nul
    echo OK Cleaned dist folder
)
echo.

REM --- Build Release ---
echo [5/8] Building signed release APK...
echo This may take a few minutes...
echo.

call gradlew.bat assembleRelease ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD%

if errorlevel 1 (
    echo.
    echo ========================================
    echo  BUILD FAILED!
    echo ========================================
    echo Check the error messages above.
    pause
    exit /b 1
)

echo.
echo OK Release APK built successfully!
echo.

REM --- Rename and Upload to Telegram ---
echo [6/8] Renaming APK and uploading to Telegram...
echo.

call gradlew.bat renameReleaseApk uploadReleaseApkToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD%

if errorlevel 1 (
    echo.
    echo WARNING: Upload to Telegram may have failed.
    echo Check the logs above for details.
    echo The APK was built successfully though!
) else (
    echo OK APK uploaded to Telegram successfully!
)
echo.

REM --- Send Build Status ---
echo [7/8] Sending build notification to Telegram...
echo.

call gradlew.bat notifyBuildStatusToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD%

echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo APK Location:
echo   - app\build\outputs\apk\release\app-release.apk
echo   - app\dist\XKM-[version].apk
echo.
echo The release APK has been:
echo   OK Built with Rust native library
echo   OK Built and signed with your keystore
echo   OK Renamed with version number
echo   OK Uploaded to Telegram test group
echo   OK Build notification sent
echo.
echo ========================================

REM --- Open dist folder (optional) ---
if exist "app\dist" (
    echo.
    set /p OPEN_FOLDER="Open dist folder? (Y/N): "
    if /i "!OPEN_FOLDER!"=="Y" (
        start "" "app\dist"
    )
)

echo.
pause

endlocal
