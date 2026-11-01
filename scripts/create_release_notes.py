#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v1.7 (Official Multi-Platform)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/swarix.dmg) | `.dmg` | Native macOS Disk Image |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.7/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v1.7:
1. **High-Speed In-App Updater (OTA)**:
   - Optimized 64KB chunk streaming and throttled non-blocking progress dispatch. Completely eliminates download freezes, stalls, or mid-transfer cancellation.
   - Live download size counter (`MB / Total MB`) with automatic APK installer handoff.
2. **100% Offline Mode & Device Music Vault**:
   - Dedicated "Offline" chip on Home screen and "Offline Music" tab in Library.
   - Scan device storage for local MP3, FLAC, and WAV audio with 0ms latency and zero data usage.
   - Automatic background caching of played tracks for offline playback.
3. **Official Standalone Web Player (GitHub Pages)**:
   - Dedicated browser player hosted at `https://ritikkumarai.github.io/swarix/` with rotating vinyl animation, real-time frequency visualizer, synced lyrics, and direct device download cards.
4. **Last-Played Track Mini Player Persistence**:
   - Automatically restores your previous session onto the bottom mini player on app launch so you can resume immediately.
5. **Developer Portfolio & Official Credits**:
   - Official maintainer credits link to `https://ritikkumarai.vercel.app/` without interfering with web music playback.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
