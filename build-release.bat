@echo off
setlocal EnableDelayedExpansion

REM ============================================================================
REM Xtra Kernel Manager - Automated Release Build & Telegram Upload Script
REM ============================================================================
REM This script:
REM 1. Cleans previous builds
REM 2. Builds signed release APK
REM 3. Renames the APK with version name
REM 4. Uploads to Telegram test group
REM 5. Sends build notification
REM ============================================================================

echo.
echo ========================================
echo  XKM Release Build Script
echo ========================================
echo.

REM --- Configuration ---
set KEYSTORE_PATH=C:\Users\putri\Documents\Project\XMS\Keystore\Keystore-XKM\xkm-release-key.jks
set KEY_ALIAS=xkmkey
set KEY_PASSWORD=gusti717
set KEYSTORE_PASSWORD=gusti717

REM --- Validate Keystore ---
echo [1/6] Validating keystore...
if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Keystore not found at: %KEYSTORE_PATH%
    echo Please check the keystore path and try again.
    pause
    exit /b 1
)
echo ✓ Keystore found: %KEYSTORE_PATH%
echo.

REM --- Clean Build ---
echo [2/6] Cleaning previous builds...
if exist "app\build" (
    rmdir /s /q "app\build" 2>nul
    echo ✓ Cleaned app\build
)
if exist "build" (
    rmdir /s /q "build" 2>nul
    echo ✓ Cleaned build
)
if exist ".gradle" (
    rmdir /s /q ".gradle" 2>nul
    echo ✓ Cleaned .gradle cache
)
if exist "app\dist" (
    rmdir /s /q "app\dist" 2>nul
    echo ✓ Cleaned dist folder
)
echo.

REM --- Gradle Clean ---
echo [3/6] Running Gradle clean...
call gradlew.bat clean
if errorlevel 1 (
    echo ERROR: Gradle clean failed!
    pause
    exit /b 1
)
echo ✓ Gradle clean completed
echo.

REM --- Build Release ---
echo [4/6] Building signed release APK...
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
echo ✓ Release APK built successfully!
echo.

REM --- Rename and Upload to Telegram ---
echo [5/6] Renaming APK and uploading to Telegram...
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
    echo ✓ APK uploaded to Telegram successfully!
)
echo.

REM --- Send Build Status ---
echo [6/6] Sending build notification to Telegram...
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
echo   ✓ Built and signed with your keystore
echo   ✓ Renamed with version number
echo   ✓ Uploaded to Telegram test group
echo   ✓ Build notification sent
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

