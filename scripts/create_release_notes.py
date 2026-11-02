#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 Swarix Music Player v2.0 (Official Multi-Platform Master Release)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.0/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.0:
1. **Real Original Master Tracks on Web Player**:
   - Replaced all demo/sample links with 100% authentic, high-definition streaming master audio directly on the web player (295, So High, 4 5 Pistol, Dada Masoom, Winning Speech, Apna Bana Le, Chaleya, Tum Kya Mile, Softly, Raataan Lambiyan).
2. **Native Mac Application Bundle (Swarix.app in swarix.dmg)**:
   - `swarix.dmg` now mounts a real macOS application installer with `Swarix.app` and a direct drag-and-drop link to `/Applications`.
   - Native luxury gold icon (`AppIcon.icns`) and standalone application window mode.
3. **Universal Android Zero-Stall & Multi-Bitrate Engine**:
   - Resilient `DefaultHttpDataSource` with mobile browser headers, automatic cross-protocol redirect handling, and multi-bitrate auto-recovery (320kbps -> 160kbps -> 96kbps).
4. **Clean Audio Focus Handling**:
   - Resolved audio focus arbitration on Samsung OneUI & Xiaomi MIUI devices.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
