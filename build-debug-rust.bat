@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================================================
REM Xtra Kernel Manager - Rust + Debug Build Script with Telegram Progress
REM ============================================================================

echo.
echo ========================================
echo  XKM Debug Build Script (with Rust)
echo ========================================
echo.

set PROJECT_ROOT=%~dp0
set RUST_PROJECT=%PROJECT_ROOT%app\src\main\rust\xkm_native
set JNILIBS_DIR=%PROJECT_ROOT%app\src\main\jniLibs
set LOG_FILE=%PROJECT_ROOT%build_log.txt

REM --- Load Telegram credentials from gradle.properties ---
set TG_BOT_TOKEN=
set TG_CHAT_ID=
set TG_MESSAGE_ID=

for /f "usebackq tokens=1,2 delims==" %%A in ("%PROJECT_ROOT%gradle.properties") do (
    if "%%A"=="telegramBotToken" set "TG_BOT_TOKEN=%%B"
    if "%%A"=="telegramChatId" set "TG_CHAT_ID=%%B"
)

REM --- Initialize log file ---
echo XKM Debug Build Log > "%LOG_FILE%"
echo Started: %DATE% %TIME% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

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

REM --- Send initial Telegram message ---
call :SendTelegramStart "XKM Debug Build Started" "Initializing..." "0"

REM =========================================================================
REM [STEP 1/5] BUILD RUST NATIVE LIBRARY
REM =========================================================================
echo [1/5] Building Rust native library...
echo [1/5] Building Rust native library... >> "%LOG_FILE%"

call :UpdateProgress "Rust Native Library" "Checking Rust installation..." "" "5"

REM Check if Rust is installed
where rustc >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Rust not found! >> "%LOG_FILE%"
    call :BuildFailed "Rust not found! Please install from rustup.rs"
    pause
    exit /b 1
)

call :UpdateProgress "Rust Native Library" "Rust found, checking cargo-ndk..." "" "8"

REM Check if cargo-ndk is installed
where cargo-ndk >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Installing cargo-ndk...
    cargo install cargo-ndk >> "%LOG_FILE%" 2>&1
)

call :UpdateProgress "Rust Native Library" "Adding Android targets..." "" "10"

cd /d "%RUST_PROJECT%"

REM --- Aggressive cleanup of file locks ---
echo Cleaning up potential file locks...
taskkill /f /im rust-analyzer.exe 2>nul
taskkill /f /im ra_proc_macro.exe 2>nul
taskkill /f /im cargo.exe 2>nul
taskkill /f /im rustc.exe 2>nul

REM Wait for processes to fully terminate
timeout /t 2 /nobreak >nul

REM Clean target directory to avoid stale locks
if exist "%RUST_PROJECT%\target\aarch64-linux-android\debug\.cargo-lock" (
    del /f /q "%RUST_PROJECT%\target\aarch64-linux-android\debug\.cargo-lock" 2>nul
)
if exist "%RUST_PROJECT%\target\.cargo-lock" (
    del /f /q "%RUST_PROJECT%\target\.cargo-lock" 2>nul
)

REM --- End cleanup ---

rustup target add aarch64-linux-android >nul 2>&1

call :UpdateProgress "Rust Native Library" "Compiling..." "cargo ndk build" "15"

echo Compiling Rust for Android...
set "RUST_OUTPUT=!PROJECT_ROOT!rust_build.log"

REM Try build with retry
set BUILD_ATTEMPT=1
:RustBuildRetry

cd /d "!RUST_PROJECT!"
cargo ndk -t arm64-v8a -o "!JNILIBS_DIR!" build -j 1
set RUST_EXIT=!ERRORLEVEL!
cd /d "!PROJECT_ROOT!"

if !RUST_EXIT! neq 0 (
    if !BUILD_ATTEMPT! lss 3 (
        echo Rust build failed, retrying in 3 seconds... (Attempt !BUILD_ATTEMPT!/3^)
        call :UpdateProgress "Rust Native Library" "Retry..." "Attempt !BUILD_ATTEMPT! of 3" "18"
        set /a BUILD_ATTEMPT+=1
        
        REM Clean locks again
        taskkill /f /im rust-analyzer.exe 2>nul
        taskkill /f /im cargo.exe 2>nul
        timeout /t 3 /nobreak >nul
        
        REM Try cleaning deps
        if exist "!RUST_PROJECT!\target\aarch64-linux-android\debug\deps" (
            rmdir /s /q "!RUST_PROJECT!\target\aarch64-linux-android\debug\deps" 2>nul
        )
        
        goto :RustBuildRetry
    )
    echo RUST BUILD FAILED after 3 attempts!
    call :BuildFailed "Rust build failed! Close VS Code and try again."
    pause
    exit /b 1
)

call :UpdateProgress "Rust Native Library" "Complete!" "Built successfully" "30"
echo OK Rust library built successfully!
echo.

REM =========================================================================
REM [STEP 2/5] VERIFY NATIVE LIBRARY
REM =========================================================================
echo [2/5] Verifying native library...

call :UpdateProgress "Verify Library" "Checking arm64-v8a..." "arm64-v8a" "32"

if exist "%JNILIBS_DIR%\arm64-v8a\libxkm_native.so" (
    echo OK arm64-v8a/libxkm_native.so
) else (
    echo ERROR: Native library not found! >> "%LOG_FILE%"
    call :BuildFailed "Native library not found!"
    pause
    exit /b 1
)
echo.

call :UpdateProgress "Verify Library" "Native library verified!" "libxkm_native.so OK" "35"

cd /d "%PROJECT_ROOT%"

REM =========================================================================
REM DEVICE DETECTION
REM =========================================================================
echo Mendeteksi perangkat Android yang terhubung...

set DEVICE_COUNT=0
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
    echo Perangkat terdeteksi: !DEVICE_1! - !MODEL_1! - Android !VERSION_1!
) else (
    echo Beberapa perangkat terdeteksi:
    set /a INDEX=1
    :DisplayDeviceLoop
    if !INDEX! gtr %DEVICE_COUNT% goto :DisplayDeviceDone
    echo !INDEX!. !DEVICE_%INDEX%! - !MODEL_%INDEX%! - Android !VERSION_%INDEX%!
    set /a INDEX+=1
    goto :DisplayDeviceLoop
    :DisplayDeviceDone
    set /p CHOICE="Pilih perangkat (1-%DEVICE_COUNT%): "
    if !CHOICE! lss 1 goto :InvalidChoice
    if !CHOICE! gtr %DEVICE_COUNT% goto :InvalidChoice
    set SELECTED_DEVICE=!DEVICE_%CHOICE%!
    goto :SetSerial
    :InvalidChoice
    echo Pilihan tidak valid.
    set SKIP_INSTALL=1
)

:SetSerial
if %SKIP_INSTALL%==0 set ANDROID_SERIAL=%SELECTED_DEVICE%

REM =========================================================================
REM [STEP 3/5] BUILD ANDROID APK
REM =========================================================================
:BuildStart
echo.
echo [3/5] Building debug APK...
echo.

call :UpdateProgress "Android APK" "Building..." "assembleDebug" "40"

set WORKERS=!NUMBER_OF_PROCESSORS!

call gradlew.bat assembleDebug --parallel --max-workers=!WORKERS! --console=plain
set GRADLE_EXIT=!ERRORLEVEL!

if !GRADLE_EXIT! neq 0 (
    echo GRADLE BUILD FAILED!
    call :BuildFailed "Gradle build failed!"
    pause
    exit /b 1
)

call :UpdateProgress "Android APK" "Complete!" "APK built successfully" "70"
echo OK Debug APK built successfully!
echo.

REM =========================================================================
REM [STEP 4/5] INSTALL APK
REM =========================================================================
set APK_PATH=
for /f "delims=" %%A in ('dir /s /b app\build\outputs\apk\debug\*.apk 2^>nul') do (
    set APK_PATH=%%A
    goto :FoundAPK
)

:FoundAPK
if exist "%APK_PATH%" (
    if %SKIP_INSTALL%==0 (
        echo [4/5] Menginstall APK ke perangkat %SELECTED_DEVICE%...
        call :UpdateProgress "Installing APK" "Installing to device..." "adb install" "75"
        adb -s %SELECTED_DEVICE% install -r "%APK_PATH%" >> "%LOG_FILE%" 2>&1
        echo OK APK berhasil diinstall.
        call :UpdateProgress "Installing APK" "Installation complete!" "Success" "80"
    ) else (
        echo [4/5] Tidak ada perangkat, skip instalasi.
        call :UpdateProgress "Skip Installation" "No device connected" "" "80"
    )
) else (
    echo APK tidak ditemukan!
    call :BuildFailed "APK not found!"
    pause
    exit /b 1
)

echo.

REM =========================================================================
REM [STEP 5/5] UPLOAD TO TELEGRAM
REM =========================================================================
if %UPLOAD_TO_TELEGRAM%==1 (
    echo [5/5] Uploading to Telegram...
    call :UpdateProgress "Upload to Telegram" "Renaming APK..." "XKM-version-debug.apk" "85"
    call gradlew.bat renameDebugApk >> "%LOG_FILE%" 2>&1
    
    call :UpdateProgress "Upload to Telegram" "Uploading APK..." "Sending file..." "90"
    call gradlew.bat uploadDebugApkToTelegram >> "%LOG_FILE%" 2>&1
    
    if errorlevel 1 (
        call :UpdateProgress "Upload Warning" "Upload may have failed" "Check logs" "95"
    ) else (
        echo OK APK uploaded to Telegram successfully!
        call :UpdateProgress "Upload to Telegram" "Upload complete!" "Done" "100"
    )
) else (
    echo [5/5] Skipped - Telegram upload not requested.
    call :UpdateProgress "Build Complete" "Telegram upload skipped" "" "100"
)

REM =========================================================================
REM BUILD SUCCESS
REM =========================================================================
echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.

call :SendFinalSuccess

echo APK Location: %APK_PATH%
echo.
pause
endlocal
exit /b 0

REM =========================================================================
REM TELEGRAM FUNCTIONS (using external PowerShell helper)
REM =========================================================================

:SendTelegramStart
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0

set "TITLE=%~1"
set "STATUS=%~2"
set "PROGRESS=%~3"

set TG_RESPONSE=%TEMP%\tg_msgid.txt

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "start" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -Title "!TITLE!" ^
    -Stage "!STATUS!" ^
    -Progress !PROGRESS! ^
    -Time "%TIME%" ^
    -BuildType "Debug" ^
    -ResponseFile "%TG_RESPONSE%"

if exist "%TG_RESPONSE%" (
    set /p TG_MESSAGE_ID=<"%TG_RESPONSE%"
    del "%TG_RESPONSE%" 2>nul
)
exit /b 0

:UpdateProgress
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0
if "!TG_MESSAGE_ID!"=="" exit /b 0

set "STAGE=%~1"
set "STATUS=%~2"
set "DETAIL=%~3"
set "PROGRESS=%~4"

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "update" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -MessageId "!TG_MESSAGE_ID!" ^
    -Stage "!STAGE!" ^
    -Status "!STATUS!" ^
    -Detail "!DETAIL!" ^
    -Progress !PROGRESS! ^
    -Time "%TIME%" ^
    -BuildType "Debug"

exit /b 0

:BuildFailed
echo.
echo BUILD FAILED!
echo Error: %~1

if "!TG_BOT_TOKEN!"=="" goto :SkipTGError
if "!TG_CHAT_ID!"=="" goto :SkipTGError

if not "!TG_MESSAGE_ID!"=="" (
    powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
        -Action "failed" ^
        -BotToken "!TG_BOT_TOKEN!" ^
        -ChatId "!TG_CHAT_ID!" ^
        -MessageId "!TG_MESSAGE_ID!" ^
        -ErrorMsg "%~1" ^
        -Time "%TIME%" ^
        -BuildType "Debug"
)

if exist "%LOG_FILE%" (
    echo Sending error log to Telegram...
    curl -s -X POST "https://botapi.arasea.dpdns.org/bot!TG_BOT_TOKEN!/sendDocument" ^
        -F "chat_id=!TG_CHAT_ID!" ^
        -F "document=@%LOG_FILE%" ^
        -F "caption=Build failed. See log for details." ^
        > nul 2>&1
)

:SkipTGError
exit /b 0

:SendFinalSuccess
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0
if "!TG_MESSAGE_ID!"=="" exit /b 0

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "success" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -MessageId "!TG_MESSAGE_ID!" ^
    -Time "%TIME%" ^
    -BuildType "Debug"

exit /b 0
