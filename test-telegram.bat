@echo off
setlocal EnableDelayedExpansion

REM ============================================================================
REM XKM Telegram Connection Test Script
REM ============================================================================
REM Tests if your Telegram bot can send messages to the test group
REM ============================================================================

echo.
echo ========================================
echo  Telegram Connection Test
echo ========================================
echo.

echo Testing Telegram bot connection...
echo This will send a test message to your group.
echo.

call gradlew.bat notifyBuildStatusToTelegram ^
    -PmyKeystorePath="C:\Users\putri\Documents\Project\XMS\Keystore\Keystore-XKM\xkm-release-key.jks" ^
    -PmyKeystorePassword=gusti717 ^
    -PmyKeyAlias=xkmkey ^
    -PmyKeyPassword=gusti717 ^
    -Pchangelog="🧪 Telegram connection test - If you see this, the bot is working!"

if errorlevel 1 (
    echo.
    echo ========================================
    echo  CONNECTION TEST FAILED!
    echo ========================================
    echo.
    echo Possible issues:
    echo   - No internet connection
    echo   - Wrong bot token in gradle.properties
    echo   - Wrong chat ID in gradle.properties
    echo   - Bot blocked by Telegram
    echo.
    echo Check gradle.properties for:
    echo   telegramBotToken=YOUR_BOT_TOKEN
    echo   telegramChatId=YOUR_CHAT_ID
    echo.
) else (
    echo.
    echo ========================================
    echo  CONNECTION TEST SUCCESSFUL!
    echo ========================================
    echo.
    echo Check your Telegram test group for the message.
    echo If you received it, everything is working correctly!
    echo.
)

pause

