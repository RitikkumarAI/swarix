#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.4 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.4/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.4:
1. **Fix "Swarix keeps stopping" Crash & Background Auto-Pause**:
   - Fixed foreground service lifecycle in `MusicPlaybackService` by immediately invoking `ServiceCompat.startForeground` with `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.
   - Android will never terminate the service with `ForegroundServiceDidNotStartInTimeException`.
   - Prevented background auto-pause on Xiaomi (MIUI / HyperOS), Samsung, OnePlus, and other aggressive battery management OEM devices.
2. **Lock Screen & Notification Panel Playback Screen**:
   - High-fidelity MediaStyle notification with `NotificationCompat.VISIBILITY_PUBLIC`.
   - Lock screen & notification shade display live album artwork, song title, artist, seekbar, and working 1-tap playback controls (Previous, Play/Pause, Next).
   - Silent notification channel with zero chime/vibration on track changes.
3. **World-Best Original Sound Quality (320 kbps Extreme Studio Master)**:
   - All online and catalog tracks default to 320 kbps Lossless Hi-Res dynamic bitstream.
   - Transparent fallback pipeline (320kbps -> 160kbps -> 96kbps) ensures uninterrupted playback on fluctuating mobile networks.
4. **1-Tap Direct Download to Local Storage**:
   - Added direct offline download button in the Now Playing screen.
   - Downloaded songs are saved directly to device storage with zero mobile data requirement for subsequent plays.
   - Connected downloaded tracks directly to "Your Library -> Offline Music" and "Downloads" screen so all offline tracks are clearly visible and playable.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
