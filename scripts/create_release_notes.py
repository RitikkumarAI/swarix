#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 SOREX / Swarix Music Player v3.0 (Liquid Glass Navbar, Mini Player Controls, Dolby Atmos DSP)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v3.0/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v3.0:
1. 🧊 **Liquid Glass Navbar (Glassmorphic Dock)**:
   - Ultra-premium frosted translucent surface with layered refraction adapting smoothly to Light & Dark themes.
   - Dual-tone specular highlight rim along top border with liquid gloss refraction hairline.
2. ⏭️ **Mini Player Previous Track Button**:
   - Added dedicated Previous Track button (`⏮`) directly to the bottom Mini Player alongside Play/Pause and Next.
3. 🏷️ **Dynamic In-App Version Tracking**:
   - Resolved hardcoded version display in Settings & About dialog; now dynamically syncs with `BuildConfig.VERSION_NAME`.
4. 🎧 **Dolby Atmos & Concert DSP Modes**:
   - Immersive 3D Spatial Virtualizer with binaural surround curve (`Dolby Atmos Spatial`).
   - Live Arena acoustic reverberation preset (`Concert Hall Live`).
5. 🧠 **Advanced Hybrid AI Music Recommendation Engine**:
   - Spotify / YouTube Music inspired hybrid model with strict language & mood consistency guards.
6. 🔍 **Listening History & Search Separation**:
   - Pure playback tracking: searches and queries never pollute listening history or recents.
7. 📜 **Multilingual Lyrics & Typo-Tolerant Search**:
   - Devanagari transliteration, real-time matched lyric snippets, and Levenshtein typo correction.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
