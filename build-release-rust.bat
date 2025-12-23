@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================================================
REM Xtra Kernel Manager - Rust + Release Build Script with Telegram Progress
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
echo XKM Release Build Log > "%LOG_FILE%"
echo Started: %DATE% %TIME% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

REM --- Send initial Telegram message ---
call :SendTelegramStart "XKM Release Build Started" "Initializing..." "0"

REM =========================================================================
REM [STEP 1/8] VALIDATE KEYSTORE
REM =========================================================================
echo [1/8] Validating keystore...

call :UpdateProgress "Validate Keystore" "Checking keystore file..." "" "2"

if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Keystore not found! >> "%LOG_FILE%"
    call :BuildFailed "Keystore not found at: %KEYSTORE_PATH%"
    pause
    exit /b 1
)
echo OK Keystore found: %KEYSTORE_PATH%
echo.

call :UpdateProgress "Validate Keystore" "Keystore validated!" "OK" "5"

REM =========================================================================
REM [STEP 2/8] BUILD RUST NATIVE LIBRARY
REM =========================================================================
echo [2/8] Building Rust native library...

call :UpdateProgress "Rust Native Library" "Checking Rust installation..." "" "7"

where rustc >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :BuildFailed "Rust not found! Please install from rustup.rs"
    pause
    exit /b 1
)

call :UpdateProgress "Rust Native Library" "Rust found, checking cargo-ndk..." "" "10"

where cargo-ndk >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Installing cargo-ndk...
    cargo install cargo-ndk >> "%LOG_FILE%" 2>&1
)

call :UpdateProgress "Rust Native Library" "Adding Android targets..." "" "12"

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
if exist "%RUST_PROJECT%\target\aarch64-linux-android\release\.cargo-lock" (
    del /f /q "%RUST_PROJECT%\target\aarch64-linux-android\release\.cargo-lock" 2>nul
)
if exist "%RUST_PROJECT%\target\.cargo-lock" (
    del /f /q "%RUST_PROJECT%\target\.cargo-lock" 2>nul
)

REM --- End cleanup ---

rustup target add aarch64-linux-android >nul 2>&1

call :UpdateProgress "Rust Native Library" "Compiling..." "cargo ndk build --release" "15"

echo Compiling Rust for Android (release mode)...

REM Try build with retry
set BUILD_ATTEMPT=1
:RustBuildRetry

cd /d "!RUST_PROJECT!"
cargo ndk -t arm64-v8a -o "!JNILIBS_DIR!" build --release -j 1
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
        if exist "!RUST_PROJECT!\target\aarch64-linux-android\release\deps" (
            rmdir /s /q "!RUST_PROJECT!\target\aarch64-linux-android\release\deps" 2>nul
        )
        
        goto :RustBuildRetry
    )
    echo RUST BUILD FAILED after 3 attempts!
    call :BuildFailed "Rust build failed! Close VS Code and try again."
    pause
    exit /b 1
)

call :UpdateProgress "Rust Native Library" "Complete!" "Built successfully" "25"
echo OK Rust library built successfully!
echo.

REM =========================================================================
REM [STEP 3/8] VERIFY NATIVE LIBRARY
REM =========================================================================
echo [3/8] Verifying native library...

call :UpdateProgress "Verify Library" "Checking arm64-v8a..." "arm64-v8a" "27"

if exist "%JNILIBS_DIR%\arm64-v8a\libxkm_native.so" (
    echo OK arm64-v8a/libxkm_native.so
) else (
    call :BuildFailed "Native library not found!"
    pause
    exit /b 1
)
echo.

call :UpdateProgress "Verify Library" "Native library verified!" "OK" "30"

cd /d "%PROJECT_ROOT%"

REM =========================================================================
REM [STEP 4/8] CLEAN BUILD
REM =========================================================================
echo [4/8] Cleaning previous builds...

call :UpdateProgress "Clean Builds" "Removing old files..." "" "32"

if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist "app\dist" rmdir /s /q "app\dist" 2>nul
echo OK Clean complete
echo.

call :UpdateProgress "Clean Builds" "Clean complete!" "OK" "35"

REM =========================================================================
REM [STEP 5/8] BUILD RELEASE APK
REM =========================================================================
echo [5/8] Building signed release APK...
echo This may take a few minutes...
echo.

call :UpdateProgress "Android Release APK" "Building..." "assembleRelease" "38"

call gradlew.bat assembleRelease --console=plain ^
    -PmyKeystorePath="!KEYSTORE_PATH!" ^
    -PmyKeystorePassword=!KEYSTORE_PASSWORD! ^
    -PmyKeyAlias=!KEY_ALIAS! ^
    -PmyKeyPassword=!KEY_PASSWORD!

set GRADLE_EXIT=!ERRORLEVEL!

if !GRADLE_EXIT! neq 0 (
    echo GRADLE BUILD FAILED!
    call :BuildFailed "Gradle release build failed!"
    pause
    exit /b 1
)

call :UpdateProgress "Android Release APK" "Complete!" "APK built successfully" "65"
echo OK Release APK built successfully!
echo.

REM =========================================================================
REM [STEP 6/8] RENAME APK
REM =========================================================================
echo [6/8] Renaming APK with version...

call :UpdateProgress "Renaming APK" "Renaming..." "Adding version" "70"

call gradlew.bat renameReleaseApk ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

call :UpdateProgress "Renaming APK" "Complete!" "APK renamed" "75"
echo.

REM =========================================================================
REM [STEP 7/8] UPLOAD TO TELEGRAM
REM =========================================================================
echo [7/8] Uploading APK to Telegram...

call :UpdateProgress "Upload to Telegram" "Preparing..." "Uploading APK" "78"

call gradlew.bat uploadReleaseApkToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
    call :UpdateProgress "Upload Warning" "May have failed" "Check logs" "88"
    echo WARNING: Upload may have failed.
) else (
    echo OK APK uploaded to Telegram successfully!
    call :UpdateProgress "Upload to Telegram" "Complete!" "APK uploaded" "92"
)
echo.

REM =========================================================================
REM [STEP 8/8] SEND BUILD NOTIFICATION
REM =========================================================================
echo [8/8] Sending build notification to Telegram...

call :UpdateProgress "Notification" "Sending..." "Notifying users" "95"

call gradlew.bat notifyBuildStatusToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

call :UpdateProgress "Build Complete" "Done!" "All tasks finished" "100"

REM =========================================================================
REM BUILD SUCCESS
REM =========================================================================
echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.

call :SendFinalSuccess

echo APK Location:
echo   - app\build\outputs\apk\release\app-release.apk
echo   - app\dist\XKM-[version].apk
echo.

if exist "app\dist" (
    set /p OPEN_FOLDER="Open dist folder? (Y/N): "
    if /i "!OPEN_FOLDER!"=="Y" start "" "app\dist"
)

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
    -BuildType "Release" ^
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
    -BuildType "Release"

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
        -BuildType "Release"
)

if exist "%LOG_FILE%" (
    echo Sending error log to Telegram...
    curl -s -X POST "https://botapi.arasea.dpdns.org/bot!TG_BOT_TOKEN!/sendDocument" ^
        -F "chat_id=!TG_CHAT_ID!" ^
        -F "document=@%LOG_FILE%" ^
        -F "caption=Release build failed. See log for details." ^
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
    -BuildType "Release"

exit /b 0
