#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.2 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.2/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.2:
1. **Browse by Mood — Real Music Discovery**:
   - Implemented dedicated mood categories: **Happy**, **Sad**, **Love**, and **Focus**.
   - Dynamically loads verified real songs matching the selected mood across Bollywood, Punjabi, Hollywood, and instrumental.
   - Clicking any mood opens a dedicated, dynamic playlist screen with real artwork, metadata, Play All, and Shuffle buttons.
2. **Popular Artists — 100% Authentic Verified Photographs**:
   - Added authentic, verified portraits from reliable sources (Wikimedia Commons) for top artists: Arijit Singh, Diljit Dosanjh, Sidhu Moose Wala, Shreya Ghoshal, Pritam, Sonu Nigam, AP Dhillon, Badshah, Jubin Nautiyal, Neha Kakkar, Atif Aslam, Taylor Swift, Ed Sheeran, The Weeknd, Bruno Mars.
   - Zero AI-generated faces, zero random stock photos.
   - Clicking an artist opens their full profile featuring verified information, top tracks, albums, singles, related artists, Play All, and Shuffle buttons.
3. **Accurate Search Results & Spotlight UI**:
   - Searching an artist displays a verified artist card with 1-tap navigation to the complete artist profile.
   - Added high-contrast Spotify-style dark forest green "Save Playlist" spotlight button.
   - High-precision Hindi, English, and Punjabi transliterated song search.
4. **Liked Songs — Automatic Library Synchronization**:
   - Instant heart toggle persisted directly to Room DB `user_liked_songs` relationship table.
   - Immediate reactive synchronization across Home, Search, Artist Profiles, Mini Player, and Your Library.
   - Dedicated **Liked Songs** page inside Your Library with song count, Play, Shuffle, and 1-tap remove without deleting catalog tracks.
5. **System Media Player & Lock Screen Controls**:
   - Full-color album cover rendering on Android notification shade and lock screen via `MediaMetadata.setArtworkData`.
   - Hardware headphone and Bluetooth media button support (single, double, and triple tap controls).
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
