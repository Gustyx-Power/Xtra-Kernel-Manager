@echo off
setlocal enabledelayedexpansion

echo Mendeteksi perangkat Android yang terhubung...

REM Get list of connected devices
set DEVICE_COUNT=0
set /a INDEX=0

REM Create temporary file for device list
set TEMP_DEVICES=%TEMP%\devices_list.txt
adb devices > "%TEMP_DEVICES%"

REM Parse device list
for /f "skip=1 tokens=1,2" %%A in ('type "%TEMP_DEVICES%"') do (
    if "%%B"=="device" (
        set /a DEVICE_COUNT+=1
        set DEVICE_!DEVICE_COUNT!=%%A
    )
)

del "%TEMP_DEVICES%"

REM Check if no devices connected
if %DEVICE_COUNT%==0 (
    echo Tidak ada perangkat yang terhubung via USB. Build akan tetap dijalankan tanpa instalasi ke perangkat.
    set SKIP_INSTALL=1
    goto :BuildStart
)

REM Get device information for each connected device
set /a INDEX=1
:DeviceInfoLoop
if !INDEX! gtr %DEVICE_COUNT% goto :DeviceInfoDone
set SERIAL=!DEVICE_%INDEX%!

REM Get model and Android version
for /f "delims=" %%M in ('adb -s !SERIAL! shell getprop ro.product.model') do set MODEL_!INDEX!=%%M
for /f "delims=" %%V in ('adb -s !SERIAL! shell getprop ro.build.version.release') do set VERSION_!INDEX!=%%V

REM Remove carriage returns
set MODEL_!INDEX!=!MODEL_%INDEX%:
=!
set VERSION_!INDEX!=!VERSION_%INDEX%:
=!

set /a INDEX+=1
goto :DeviceInfoLoop

:DeviceInfoDone

REM Handle device selection
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
    
    REM Validate choice
    if !CHOICE! lss 1 goto :InvalidChoice
    if !CHOICE! gtr %DEVICE_COUNT% goto :InvalidChoice
    
    set SELECTED_DEVICE=!DEVICE_%CHOICE%!
    echo Perangkat dipilih: !DEVICE_%CHOICE%! ^| Model: !MODEL_%CHOICE%! ^| Android: !VERSION_%CHOICE%!
    goto :SetSerial
    
    :InvalidChoice
    echo Pilihan tidak valid. Build akan tetap dijalankan tanpa instalasi ke perangkat.
    set SKIP_INSTALL=1
)

:SetSerial
if not defined SKIP_INSTALL (
    set ANDROID_SERIAL=%SELECTED_DEVICE%
)

:BuildStart
REM Get number of processors for parallel build
set WORKERS=%NUMBER_OF_PROCESSORS%

call gradlew.bat assembleDebug --parallel --max-workers=%WORKERS%

REM Find the APK file
set APK_PATH=
for /f "delims=" %%A in ('dir /s /b app\build\outputs\apk\debug\*.apk 2^>nul') do (
    set APK_PATH=%%A
    goto :FoundAPK
)

:FoundAPK
if exist "%APK_PATH%" (
    if not defined SKIP_INSTALL (
        echo Menginstall APK ke perangkat %SELECTED_DEVICE%...
        adb -s %SELECTED_DEVICE% install -r "%APK_PATH%"
        echo APK berhasil diinstall.
    ) else (
        echo Build selesai. Tidak ada perangkat yang terhubung, APK tidak diinstall.
    )
) else (
    echo APK tidak ditemukan. Build mungkin gagal.
)

echo Build debug selesai!
endlocal
