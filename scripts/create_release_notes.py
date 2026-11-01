#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v1.9 (Official Multi-Platform)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/swarix.dmg) | `.dmg` | Native macOS Disk Image |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.9/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v1.9:
1. **Universal Streaming Engine & 100% Phone Compatibility**:
   - Resilient `DefaultHttpDataSource` with realistic mobile browser headers, 20s network connect timeouts, and automatic cross-protocol redirect handling. Eliminates CDN 403 Forbidden and connection drops across all Indian mobile networks (Jio, Airtel, Vi).
2. **Dynamic Multi-Bitrate Auto-Recovery**:
   - If a 320kbps stream is unavailable on the CDN, the player seamlessly falls back to 160kbps, and then 96kbps without stalling or failing.
3. **Fixed Audio Focus Arbitration on Samsung & Xiaomi**:
   - Eliminated internal audio focus collision between manual system requests and ExoPlayer's state machine that previously caused songs to pause immediately on Samsung OneUI and Xiaomi MIUI.
4. **Official Web Player Live on GitHub Pages**:
   - Deployed directly to GitHub Pages at [ritikkumarai.github.io/swarix](https://ritikkumarai.github.io/swarix/) with zero 404 errors.
5. **Spotify-Level Smart Language Flow & Singer Discography**:
   - Strict cultural & language continuity: Punjabi flows into Punjabi, Haryanvi into Haryanvi, Bollywood into Bollywood.
   - Dedicated "This Is {Artist}" 1-tap full discography cards in Search.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
