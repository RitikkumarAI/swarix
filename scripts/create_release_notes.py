#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v1.6 (Official Multi-Platform)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/swarix.dmg) | `.dmg` | Native macOS Disk Image |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v1.6/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.vercel.app/) | Web / PWA | Instant browser playback |

---

### ✨ What's New in v1.6:
1. **Official Luxury App Icon**:
   - High-resolution golden acoustic S-emblem with glowing visualizer across all launcher density targets and adaptive icons.
2. **Direct Device File Formats**:
   - Clean standard extensions (`swarix.apk`, `swarix.dmg`, `swarix.exe`, `swarix-ios.mobileconfig`).
3. **Consolidated Single Release**:
   - All previous deprecated release versions removed to avoid user confusion.
4. **Scrollable Playback Screen**:
   - Section 1: AI Sonic mood & genre match tracks with 1-tap playback.
   - Section 2: Audio Fidelity Manual Test (320kbps, 24-bit 96kHz Lossless, 8K Ultra DSP, Direct DAC Bypass).
   - Section 3: Spotify-inspired deep blue & cyan gradient lyrics card with full-screen access.
5. **DSP Default Off & Crash Fix**:
   - DSP is OFF by default; foreground service crash on Xiaomi/MIUI completely resolved.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
