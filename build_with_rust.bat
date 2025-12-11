@echo off
setlocal EnableDelayedExpansion

REM ============================================================
REM XKM Build Script - Builds Rust native library + Android APK
REM ============================================================

set PROJECT_ROOT=%~dp0
set RUST_PROJECT=%PROJECT_ROOT%app\src\main\rust\xkm_native
set JNILIBS_DIR=%PROJECT_ROOT%app\src\main\jniLibs

REM Colors for output
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

echo %BLUE%========================================%NC%
echo %BLUE%   XKM Build Script (Rust + Android)   %NC%
echo %BLUE%========================================%NC%
echo.

REM Check for build type argument
set BUILD_TYPE=debug
if "%1"=="release" set BUILD_TYPE=release
if "%1"=="--release" set BUILD_TYPE=release
if "%1"=="-r" set BUILD_TYPE=release

REM Check for clean argument  
set DO_CLEAN=0
if "%1"=="clean" set DO_CLEAN=1
if "%2"=="clean" set DO_CLEAN=1
if "%1"=="--clean" set DO_CLEAN=1
if "%2"=="--clean" set DO_CLEAN=1

echo %YELLOW%Build type: %BUILD_TYPE%%NC%
echo.

REM ============================================================
REM Step 1: Build Rust Native Library
REM ============================================================
echo %BLUE%[1/3] Building Rust native library...%NC%

REM Check if Rust is installed
where rustc >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Rust not found! Please install Rust from https://rustup.rs%NC%
    exit /b 1
)

REM Check if cargo-ndk is installed
where cargo-ndk >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %YELLOW%Installing cargo-ndk...%NC%
    cargo install cargo-ndk
)

REM Change to Rust project directory
cd /d "%RUST_PROJECT%"

REM Clean if requested or if there were previous errors
if %DO_CLEAN%==1 (
    echo %YELLOW%Cleaning previous build...%NC%
    rmdir /s /q target 2>nul
    timeout /t 2 /nobreak >nul
)

REM Add Android targets if not present
echo %YELLOW%Ensuring Android targets are installed...%NC%
rustup target add aarch64-linux-android >nul 2>&1

REM Kill any processes that might be locking files (like rust-analyzer)
echo %YELLOW%Preparing build environment...%NC%
taskkill /f /im rust-analyzer.exe 2>nul
timeout /t 1 /nobreak >nul

REM Build Rust library for Android (single-threaded to avoid file locks)
echo %YELLOW%Compiling Rust for Android (this may take a while)...%NC%

if "%BUILD_TYPE%"=="release" (
    cargo ndk -t arm64-v8a -o "%JNILIBS_DIR%" build --release -j 1
) else (
    cargo ndk -t arm64-v8a -o "%JNILIBS_DIR%" build -j 1
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo %RED%ERROR: Rust build failed!%NC%
    echo %YELLOW%TIP: Try running with 'clean' flag: build_with_rust.bat clean%NC%
    echo %YELLOW%TIP: Close VS Code or other IDEs and try again%NC%
    echo %YELLOW%TIP: Add exclusion for project folder in Windows Defender%NC%
    exit /b 1
)

echo %GREEN%Rust library built successfully!%NC%
echo.

REM ============================================================
REM Step 2: Verify .so files
REM ============================================================
echo %BLUE%[2/3] Verifying native libraries...%NC%

if exist "%JNILIBS_DIR%\arm64-v8a\libxkm_native.so" (
    echo   %GREEN%OK%NC% arm64-v8a/libxkm_native.so
) else (
    echo   %RED%MISSING%NC% arm64-v8a/libxkm_native.so
    echo %RED%ERROR: Native library not found!%NC%
    exit /b 1
)

echo.

REM ============================================================
REM Step 3: Build Android APK
REM ============================================================
echo %BLUE%[3/3] Building Android APK...%NC%

cd /d "%PROJECT_ROOT%"

if "%BUILD_TYPE%"=="release" (
    call gradlew.bat assembleRelease --warning-mode none
) else (
    call gradlew.bat assembleDebug --warning-mode none
)

if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Android build failed!%NC%
    exit /b 1
)

echo.
echo %GREEN%========================================%NC%
echo %GREEN%   Build completed successfully!       %NC%
echo %GREEN%========================================%NC%
echo.

if "%BUILD_TYPE%"=="release" (
    echo APK: %PROJECT_ROOT%app\build\outputs\apk\release\app-release.apk
) else (
    echo APK: %PROJECT_ROOT%app\build\outputs\apk\debug\app-debug.apk
)

endlocal
