#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.3 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.3/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.3:
1. **Universal Responsiveness & Zero-Squish Layout Across All Devices**:
   - Fixed all text squishing and vertical wrapping bugs on different screen sizes and DPIs.
   - Now Playing badges (8K ULTRA, DSP ON/OFF, BASS ON/OFF) now use horizontally scrollable single-line capsules.
   - Fixed player controls row (DSP, EQ, Lyrics, Share, Queue) to 36dp fixed-height capsules with zero vertical expansion or letter breaking.
   - Fixed BIT-PERFECT DAC badge in Soundstage card to remain permanently horizontal.
   - Fixed Settings items (Audio Quality, Equalizer, Crossfade) so labels and values never wrap character-by-character.
2. **Lock Screen & Notification Panel Controls**:
   - Implemented public lock screen visibility for the media playback notification channel (`Notification.VISIBILITY_PUBLIC`).
   - Added direct intent handlers in `MusicPlaybackService` for Play, Pause, Toggle, Next, and Previous track controls.
   - Seamless lock screen media player with full-color album artwork and track progress slider.
3. **Zero-Pause Resilient Playback & Auto-Recovery**:
   - Upgraded background playback service to start as a persistent foreground service via `ContextCompat.startForegroundService`.
   - Added automatic stream retry mechanism (up to 3 transparent retries on network drops) and smooth auto-skip to the next track if a CDN stream is unplayable, eliminating sudden playback pauses or stops.
4. **Universal Music Discovery (Singers, Discography & World Hits)**:
   - Deep search expansion: searching any artist or singer dynamically pulls their top hit tracks, singles, and complete discography from the global online library.
   - Artist spotlight card in Search immediately matches popular artists and provides 1-tap navigation to their full discography.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
