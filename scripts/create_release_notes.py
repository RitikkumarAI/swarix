#!/usr/bin/env python3
import os

def main():
    os.makedirs("dist", exist_ok=True)
    notes = """## 🎵 SOREX / Swarix Music Player v2.9 (AI Hybrid Recommendation Engine, Smart Typo & Lyrics Search, Voice Search)

Developed & Maintained by **[Ritik Kumar](https://ritikkumarai.vercel.app/)**

---

### 📥 Direct Device Downloads:

| Platform | Download Link | File Type | Description |
| :--- | :--- | :--- | :--- |
| 📱 **Android** | [**swarix.apk**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/swarix.apk) | `.apk` | Official installable production APK |
| 💻 **Mac (macOS)** | [**swarix.dmg**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/swarix.dmg) | `.dmg` | Native Apple macOS Disk Image (Swarix.app + Drag-to-Applications) |
| 🖥️ **Windows** | [**swarix.exe**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/swarix.exe) | `.exe` | Self-extracting Windows installer |
| 🍏 **iOS (iPhone/iPad)** | [**swarix-ios.mobileconfig**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/swarix-ios.mobileconfig) | `.mobileconfig` | Apple WebClip configuration profile |
| 📦 **Mac Archive** | [**Swarix-for-Mac.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/Swarix-for-Mac.zip) | `.zip` | Portable Mac bundle with Swarix.app |
| 📦 **Windows Archive** | [**Swarix-for-Windows.zip**](https://github.com/RitikkumarAI/swarix/releases/download/v2.9/Swarix-for-Windows.zip) | `.zip` | Portable Windows bundle |
| 🌐 **Web Player (Any OS)** | [**Open Web Player**](https://ritikkumarai.github.io/swarix/) | Web / PWA | Instant browser playback & PWA |

---

### ✨ What's New in v2.9:
1. 🧠 **Advanced Hybrid AI Music Recommendation Engine**:
   - Multi-signal scoring: Musical similarity (25%), Language compatibility (20%), Genre (15%), Mood (15%), User taste profile (15%), Artist/Album (5%), Session context (5%).
   - Strict Language & Mood Consistency guards (Hindi Romantic stays Hindi Romantic; prevents sudden genre jumps).
   - Personalized User Taste Profile with skip penalty (-30), liked boost (+35), and completion boost (+20).
   - Sequential next-track prediction (`predictNextTrack`) avoiding session repetition.
2. 🔍 **Fix Listening History Separation**:
   - Strict decoupling: Search results never pollute listening history or recents.
   - Query history displayed under "Recent Searches"; only songs actually played appear under "Recently Played".
   - Playback metrics tracking: timestamp, duration listened, completion %, and skip detection.
3. 📜 **Multilingual Lyrics-Based Search Engine**:
   - Full & partial lyric search supporting Devanagari transliteration and Hinglish phonetic equivalence.
   - Real-time matched lyric snippets displayed on search results.
4. ⚡ **Typo-Tolerant Smart Search**:
   - Levenshtein distance typo tolerance (e.g., "arijt sing" resolves to Arijit Singh).
5. 🎙️ **Upgraded Microphone Voice Search & Extensible Humming**:
   - Integrated `RECORD_AUDIO` permission request flow with animated waveform.
   - Multilingual recognition supporting Hindi (`hi-IN`), Indian English (`en-IN`), and English (`en-US`).
   - Clean extensible `HummingRecognitionProvider` interface.
"""
    with open("dist/RELEASE_NOTES.md", "w") as f:
        f.write(notes)
    print("Created dist/RELEASE_NOTES.md successfully.")

if __name__ == "__main__":
    main()
