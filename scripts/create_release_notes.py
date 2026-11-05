#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.6 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.6/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.6:
1. **Stop Playback on Recent Tasks Dismissal (Clear from Background)**:
   - Added `onTaskRemoved()` in `MusicPlaybackService` and set `android:stopWithTask="true"`.
   - Swiping away Swarix from Recent Apps immediately pauses and stops ExoPlayer, releases system locks, dismisses notification, and terminates the playback service.
   - When the user clears the app from background, playback stops completely.
2. **Bottom Row Controls Refined (`EQ -- Lyrics -- Share -- Queue`)**:
   - Removed the redundant duplicate DSP button from the bottom row (DSP remains accessible via the top audio mode pill).
   - Positioned the **Share** button in the bottom controls row right after **Lyrics** (`Devices` | `EQ` | `Lyrics` | `Share` | `Queue`).
   - Cleaned up the header row next to the song title so it cleanly contains only **Download (Offline Storage)** and **Favorite (Heart)** with spacious breathing room.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
