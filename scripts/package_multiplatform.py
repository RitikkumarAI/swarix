#!/usr/bin/env python3
import os
import shutil
import zipfile

def main():
    dist_dir = "dist"
    os.makedirs(dist_dir, exist_ok=True)
    
    # 1. Prepare Android APKs
    apk_release_dir = "app/build/outputs/apk/release"
    release_apks = [os.path.join(apk_release_dir, f) for f in os.listdir(apk_release_dir) if f.endswith(".apk")] if os.path.exists(apk_release_dir) else []
    
    if release_apks:
        source_apk = release_apks[0]
        swarix_apk = os.path.join(dist_dir, "Swarix.apk")
        android_apk = os.path.join(dist_dir, "Swarix-for-Android.apk")
        shutil.copyfile(source_apk, swarix_apk)
        shutil.copyfile(source_apk, android_apk)
        print(f"Copied {source_apk} to {swarix_apk} and {android_apk}")
    else:
        print("Warning: No release APK found in app/build/outputs/apk/release")

    # 2. Package Swarix for Mac
    mac_dir = os.path.join(dist_dir, "Swarix-for-Mac")
    os.makedirs(mac_dir, exist_ok=True)
    
    mac_launcher = """#!/bin/bash
echo "============================================="
echo "🎵 Launching Swarix Music Player for macOS"
echo "============================================="
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APK_PATH="$DIR/Swarix-for-Android.apk"

if command -v scrcpy &> /dev/null; then
    echo "Starting Swarix via scrcpy..."
    scrcpy --stay-awake &
elif command -v adb &> /dev/null; then
    echo "Installing and launching on connected device/emulator..."
    adb install -r "$APK_PATH"
    adb shell monkey -p com.aistudio.swarix.musply -c android.intent.category.LAUNCHER 1
else
    echo "Opening Swarix Web Player in Browser..."
    open "https://ritikkumarai.vercel.app/"
fi
"""
    with open(os.path.join(mac_dir, "Swarix-Launcher.command"), "w") as f:
        f.write(mac_launcher)
    os.chmod(os.path.join(mac_dir, "Swarix-Launcher.command"), 0o755)

    mac_readme = """===========================================================
🍏 SWARIX FOR MAC (macOS Installation & Usage Guide)
===========================================================

Option 1 (Apple Silicon M1/M2/M3/M4 with PlayCover or Sideloadly):
1. Download Swarix-for-Android.apk included in this folder.
2. Open with PlayCover or Android Studio Emulator.
3. Enjoy bit-perfect audio streaming directly on your Mac!

Option 2 (Android Studio / BlueStacks / Nox on Mac):
1. Open BlueStacks or Nox Player for Mac.
2. Drag & drop Swarix-for-Android.apk into the window.
3. Swarix will install and launch instantly.

Option 3 (Web Player & Portfolio):
Visit: https://ritikkumarai.vercel.app/
Developed & Maintained by Ritik Kumar
===========================================================
"""
    with open(os.path.join(mac_dir, "README-MAC.txt"), "w") as f:
        f.write(mac_readme)

    if os.path.exists(os.path.join(dist_dir, "Swarix-for-Android.apk")):
        shutil.copyfile(os.path.join(dist_dir, "Swarix-for-Android.apk"), os.path.join(mac_dir, "Swarix-for-Android.apk"))

    # Zip Mac bundle
    mac_zip_path = os.path.join(dist_dir, "Swarix-for-Mac.zip")
    with zipfile.ZipFile(mac_zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(mac_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, dist_dir)
                zipf.write(file_path, arcname)
    print(f"Created {mac_zip_path}")

    # 3. Package Swarix for Windows
    win_dir = os.path.join(dist_dir, "Swarix-for-Windows")
    os.makedirs(win_dir, exist_ok=True)

    win_launcher = """@echo off
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
"""
    with open(os.path.join(win_dir, "Swarix-Launcher.bat"), "w") as f:
        f.write(win_launcher)

    win_readme = """===========================================================
🖥️ SWARIX FOR WINDOWS (Installation & Usage Guide)
===========================================================

Option 1 (Windows Subsystem for Android - WSA):
1. Double-click Swarix-Launcher.bat or run:
   adb install -r Swarix-for-Android.apk
2. Swarix will appear in your Windows Start Menu as a native app!

Option 2 (BlueStacks / LDPlayer):
1. Drag and drop Swarix-for-Android.apk into BlueStacks/LDPlayer.
2. Play 8K Ultra Studio sound directly from your PC.

Option 3 (Direct Web Player):
Visit: https://ritikkumarai.vercel.app/
Developed & Maintained by Ritik Kumar
===========================================================
"""
    with open(os.path.join(win_dir, "README-WINDOWS.txt"), "w") as f:
        f.write(win_readme)

    if os.path.exists(os.path.join(dist_dir, "Swarix-for-Android.apk")):
        shutil.copyfile(os.path.join(dist_dir, "Swarix-for-Android.apk"), os.path.join(win_dir, "Swarix-for-Android.apk"))

    # Zip Windows bundle
    win_zip_path = os.path.join(dist_dir, "Swarix-for-Windows.zip")
    with zipfile.ZipFile(win_zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(win_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, dist_dir)
                zipf.write(file_path, arcname)
    print(f"Created {win_zip_path}")

if __name__ == "__main__":
    main()
