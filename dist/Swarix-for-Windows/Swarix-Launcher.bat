@echo off
title Swarix Music Player for Windows
echo =============================================
echo 🎵 Swarix Music Player for Windows
echo =============================================
set "DIR=%~dp0"
set "APK=%DIR%Swarix-for-Android.apk"
echo Checking Windows Subsystem for Android (WSA) or ADB...
where adb >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Installing Swarix to WSA/Emulator...
    adb install -r "%APK%"
    adb shell monkey -p com.aistudio.swarix.musply -c android.intent.category.LAUNCHER 1
) else (
    echo Opening Swarix Web Player...
    start https://ritikkumarai.vercel.app/
)
