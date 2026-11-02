#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.1 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.1/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.1:
1. **Responsive Soundstage Engine & Equalizer Shortcut**:
   - Replaced static test grid with horizontal sliding **Ultra Studio Master Soundstage Engine** with 5 dynamic audio profiles.
   - Added direct **EQ** access button on playback screen for immediate 10-band equalizer tuning.
2. **Fixed Settings & Player Layout Across All Device Screens**:
   - Fixed broken vertical text wrapping in Settings Audio Quality item.
   - Fixed narrow-screen wrapping on tactile bass and DSP status pills.
   - Fixed contrast issue on SearchScreen "Save Playlist" spotlight button.
3. **Persistent Liked Songs / Heart Button**:
   - Tapping the Heart icon instantly persists the track into Room Database / Liked Songs library with zero delay across both local and streamed songs.
4. **Millisecond-Accurate Synced Lyrics**:
   - Aligned word-by-word synced LRC timestamps for top trending tracks (295, So High, Winning Speech, Softly, Apna Bana Le, Chaleya, Tum Kya Mile, etc.) with click-to-seek functionality.
5. **Authentic Artist Portrait Gallery**:
   - Integrated verified, authentic portraits for Sidhu Moose Wala, Arijit Singh, Diljit Dosanjh, Karan Aujla, Masoom Sharma, Shreya Ghoshal, Jubin Nautiyal, and more.
6. **Playlist Management in Library**:
   - Added 1-tap Share Playlist and Delete Playlist with confirmation dialogs.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
