#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v1.8 (Official Multi-Platform)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/swarix.dmg) | `.dmg` | Native macOS Disk Image |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.8/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v1.8:
1. **10-Billion User Scale Playback Architecture & Zero-Stall Audio**:
   - `WAKE_MODE_NETWORK` + CPU partial wake lock & high-performance wifi locks prevent aggressive OEM battery managers (Xiaomi/MIUI, OnePlus, Samsung, Oppo) from killing background streaming or causing random pauses.
   - Smooth 1-tap playback without stutter or multiple click delays.
2. **Spotify-Level Smart Flow & Linguistic Recommendation**:
   - Strict cultural and language continuity: Punjabi song always flows into Punjabi, Haryanvi into Haryanvi, Bollywood into Bollywood. No random mixed tracks.
3. **Singer-Specific Complete Discography & Instant Playlist**:
   - Searching an artist (e.g. Sidhu Moose Wala, Masoom Sharma, Arijit Singh, Karan Aujla, Diljit Dosanjh) surfaces a dedicated **"This Is {Artist}" Full Discography Playlist Card** (Hit to Classic) with a 1-tap **"Play All Songs"** button and "Save Playlist" option, followed by full individual discography tracks.
4. **Parrot Green + Gold + OLED Black Aesthetics**:
   - Organic smooth gradient blending deep Parrot Green (`#1ED760`), Warm Luxury Gold (`#E5C07B`), and Pure OLED Black (`#050505`).
   - Fixed vertical text squish on DPI scaling (BASS ON/OFF pill now scrolls smoothly horizontally without wrapping).
5. **Real-Time Live Synced Lyrics**:
   - Playback screen's live lyrics card dynamically tracks playback time, glowing the active line in Parrot Green/Gold with soft white upcoming lyrics.
6. **Custom User Profile & Avatar**:
   - Custom username and avatar selection in Settings, reflected across Home, Library, and Profile headers.
7. **Data Saver Mode (64-96 kbps)**:
   - Ultra-fast streaming on low internet / 2G-3G connections with instant zero-buffering playback.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
