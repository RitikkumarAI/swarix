#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.7 (Audiophile Edition - Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.7/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.7:
1. 🎧 **Audio & Sound Enhancements (Audiophile Experience)**:
   - **Crossfade & Gapless Playback**: 2s–6s smooth crossfade slider with seamless transitions to eliminate silence between tracks.
   - **Auto Volume Normalization (Loudness Equalizer)**: Equalizes track loudness to prevent sudden volume spikes.
   - **Custom EQ Presets (Save & Load)**: Save and delete user custom EQ presets with names, plus new presets: *Bass Heavy*, *Pure Vocals*, *EDM Party*, *Acoustic*, *Rock*, and *Treble Boost*.
2. 🎵 **Playback & Discovery (Smart Features)**:
   - **Infinite Radio / Smart Autoplay**: When the queue finishes, automatically finds and plays matching top hits by the same singer or genre seamlessly.
   - **Lyric Story Card Generator**: Select 1–4 lyric lines and export an aesthetic album-art + quotes card directly to Instagram Stories / WhatsApp Status with 4 luxury themes.
   - **Quick Sleep Timer on Player**: 1-tap sleep timer button on Now Playing top bar with live countdown badge (`🌙 24m`).
3. 💾 **Library & Offline Management**:
   - **1-Tap Batch Playlist & Album Download**: One-click offline download button in playlist and artist screens with real-time progress.
   - **Phone Local Music Auto-Scanner**: Dedicated "Device Storage" category in Library to auto-detect and play MP3/FLAC files stored on your phone with a quick "Scan Music" button.
   - **Tactile Queue Reordering**: Easily reorder upcoming tracks in the play queue with move up/down controls.
4. 🎨 **UI & Visual Delight**:
   - **Dynamic Ambient Blur (Mesh Gradient)**: Subtle animated glowing aura extracted from the album artwork dominant colors behind the Now Playing screen.
   - **Mini Player Swipe Gestures**: Smooth horizontal swipe left/right on MiniPlayer to skip to next/previous song with tactile haptics.
   - **Lock Screen & MIUI Background Fix**: Safe thumbnail encoding to prevent Binder TransactionTooLargeException and background crashes on Xiaomi/MIUI lock screen.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
