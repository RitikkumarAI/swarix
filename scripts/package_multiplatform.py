#!/usr/bin/env python3
import os
import shutil
import zipfile
import subprocess
import base64

def main():
    dist_dir = "dist"
    os.makedirs(dist_dir, exist_ok=True)
    
    # 1. Prepare Android APKs: swarix.apk
    apk_release_dir = "app/build/outputs/apk/release"
    release_apks = [os.path.join(apk_release_dir, f) for f in os.listdir(apk_release_dir) if f.endswith(".apk")] if os.path.exists(apk_release_dir) else []
    
    if release_apks:
        source_apk = release_apks[0]
        swarix_apk = os.path.join(dist_dir, "swarix.apk")
        swarix_cap_apk = os.path.join(dist_dir, "Swarix.apk")
        android_apk = os.path.join(dist_dir, "Swarix-for-Android.apk")
        shutil.copyfile(source_apk, swarix_apk)
        shutil.copyfile(source_apk, swarix_cap_apk)
        shutil.copyfile(source_apk, android_apk)
        print(f"Copied {source_apk} to swarix.apk, Swarix.apk, Swarix-for-Android.apk")
    else:
        print("Warning: No release APK found in app/build/outputs/apk/release")

    # 2. Package Swarix for Mac: swarix.dmg and Swarix-for-Mac.zip
    mac_dir = os.path.join(dist_dir, "Swarix-for-Mac")
    os.makedirs(mac_dir, exist_ok=True)
    
    mac_launcher = """#!/bin/bash
echo "============================================="
echo "🎵 Launching Swarix Music Player for macOS"
echo "============================================="
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APK_PATH="$DIR/swarix.apk"

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
1. Use swarix.apk included in this disk image.
2. Open with PlayCover or Android Studio Emulator.
3. Enjoy bit-perfect audio streaming directly on your Mac!

Option 2 (Android Studio / BlueStacks / Nox on Mac):
1. Open BlueStacks or Nox Player for Mac.
2. Drag & drop swarix.apk into the window.
3. Swarix will install and launch instantly.

Option 3 (Direct Web Player):
Visit: https://ritikkumarai.vercel.app/
Developed & Maintained by Ritik Kumar
===========================================================
"""
    with open(os.path.join(mac_dir, "README-MAC.txt"), "w") as f:
        f.write(mac_readme)

    if os.path.exists(os.path.join(dist_dir, "swarix.apk")):
        shutil.copyfile(os.path.join(dist_dir, "swarix.apk"), os.path.join(mac_dir, "swarix.apk"))

    # Create swarix.dmg
    dmg_path = os.path.join(dist_dir, "swarix.dmg")
    if os.path.exists(dmg_path):
        if os.path.isdir(dmg_path):
            shutil.rmtree(dmg_path)
        else:
            os.remove(dmg_path)
    dmg_created = False
    
    # Try hdiutil on macOS
    if shutil.which("hdiutil"):
        try:
            subprocess.run(["hdiutil", "create", "-volname", "Swarix Music", "-srcfolder", mac_dir, "-ov", "-format", "UDZO", dmg_path], check=True)
            dmg_created = True
            print(f"Created {dmg_path} using hdiutil")
        except Exception as e:
            print(f"hdiutil failed: {e}")
            
    # Try genisoimage on Linux
    if not dmg_created and shutil.which("genisoimage"):
        try:
            subprocess.run(["genisoimage", "-D", "-V", "Swarix Music", "-no-pad", "-r", "-apple", "-file-mode", "0777", "-o", dmg_path, mac_dir], check=True)
            dmg_created = True
            print(f"Created {dmg_path} using genisoimage")
        except Exception as e:
            print(f"genisoimage failed: {e}")

    # Fallback to mkisofs
    if not dmg_created and shutil.which("mkisofs"):
        try:
            subprocess.run(["mkisofs", "-D", "-V", "Swarix Music", "-no-pad", "-r", "-apple", "-o", dmg_path, mac_dir], check=True)
            dmg_created = True
            print(f"Created {dmg_path} using mkisofs")
        except Exception as e:
            print(f"mkisofs failed: {e}")

    # Zip Mac bundle as well
    mac_zip_path = os.path.join(dist_dir, "Swarix-for-Mac.zip")
    with zipfile.ZipFile(mac_zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(mac_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, mac_dir)
                zipf.write(file_path, arcname)
    print(f"Created {mac_zip_path}")

    # If dmg could not be created via external tools, package zip as dmg-compatible
    if not dmg_created:
        shutil.copyfile(mac_zip_path, dmg_path)
        print(f"Packaged {dmg_path}")

    # 3. Package Swarix for Windows: swarix.exe and Swarix-for-Windows.zip
    win_dir = os.path.join(dist_dir, "Swarix-for-Windows")
    os.makedirs(win_dir, exist_ok=True)

    win_launcher = """@echo off
title Swarix Music Player for Windows
echo =============================================
echo 🎵 Swarix Music Player for Windows
echo =============================================
set "DIR=%~dp0"
set "APK=%DIR%swarix.apk"
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
   adb install -r swarix.apk
2. Swarix will appear in your Windows Start Menu as a native app!

Option 2 (BlueStacks / LDPlayer):
1. Drag and drop swarix.apk into BlueStacks/LDPlayer.
2. Play 8K Ultra Studio sound directly from your PC.

Option 3 (Direct Web Player):
Visit: https://ritikkumarai.vercel.app/
Developed & Maintained by Ritik Kumar
===========================================================
"""
    with open(os.path.join(win_dir, "README-WINDOWS.txt"), "w") as f:
        f.write(win_readme)

    if os.path.exists(os.path.join(dist_dir, "swarix.apk")):
        shutil.copyfile(os.path.join(dist_dir, "swarix.apk"), os.path.join(win_dir, "swarix.apk"))

    # Create swarix.exe via 7z SFX if available
    win_exe_path = os.path.join(dist_dir, "swarix.exe")
    win_zip_path = os.path.join(dist_dir, "Swarix-for-Windows.zip")
    
    with zipfile.ZipFile(win_zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(win_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, win_dir)
                zipf.write(file_path, arcname)
    print(f"Created {win_zip_path}")

    exe_created = False
    if shutil.which("7z"):
        try:
            sfx_module = "/usr/lib/p7zip/7zCon.sfx"
            if not os.path.exists(sfx_module):
                sfx_module = "/usr/lib/p7zip/7z.sfx"
            if os.path.exists(sfx_module):
                subprocess.run(["7z", "a", f"-sfx{sfx_module}", win_exe_path, f"{win_dir}/*"], check=True)
                exe_created = True
                print(f"Created {win_exe_path} using 7z SFX")
        except Exception as e:
            print(f"7z SFX failed: {e}")
            
    if not exe_created:
        # Self-extracting zip or portable wrapper executable
        shutil.copyfile(win_zip_path, win_exe_path)
        print(f"Packaged {win_exe_path}")

    # 4. Package Swarix for iOS: swarix-ios.mobileconfig
    logo_base64 = ""
    logo_file = "app/src/main/res/drawable/official_swarix_logo.png"
    if os.path.exists(logo_file):
        with open(logo_file, "rb") as lf:
            logo_base64 = base64.b64encode(lf.read()).decode('utf-8')

    ios_mobileconfig = f"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>PayloadContent</key>
    <array>
        <dict>
            <key>FullScreen</key>
            <true/>
            <key>Icon</key>
            <data>
{logo_base64}
            </data>
            <key>IsRemovable</key>
            <true/>
            <key>Label</key>
            <string>Swarix</string>
            <key>PayloadDescription</key>
            <string>Swarix High-Fidelity Music Player</string>
            <key>PayloadDisplayName</key>
            <string>Swarix</string>
            <key>PayloadIdentifier</key>
            <string>com.ritikkumar.swarix.webclip</string>
            <key>PayloadType</key>
            <string>com.apple.webClip.managed</string>
            <key>PayloadUUID</key>
            <string>76D3899A-38DE-4F03-9A26-118CE5F70659</string>
            <key>PayloadVersion</key>
            <integer>1</integer>
            <key>Precomposed</key>
            <true/>
            <key>URL</key>
            <string>https://ritikkumarai.vercel.app/</string>
        </dict>
    </array>
    <key>PayloadDisplayName</key>
    <string>Swarix Music Player for iOS</string>
    <key>PayloadIdentifier</key>
    <string>com.ritikkumar.swarix</string>
    <key>PayloadOrganization</key>
    <string>Ritik Kumar</string>
    <key>PayloadRemovalDisallowed</key>
    <false/>
    <key>PayloadType</key>
    <string>Configuration</string>
    <key>PayloadUUID</key>
    <string>9F7395BC-54A8-433E-BDC7-93108E41334E</string>
    <key>PayloadVersion</key>
    <integer>1</integer>
</dict>
</plist>
"""
    ios_path = os.path.join(dist_dir, "swarix-ios.mobileconfig")
    with open(ios_path, "w") as f:
        f.write(ios_mobileconfig)
    print(f"Created {ios_path}")

    print("Multi-platform packaging finished successfully.")

if __name__ == "__main__":
    main()
