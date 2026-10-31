#!/bin/bash
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
