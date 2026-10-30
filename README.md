# 🎵 Swarix Music Player

<div align="center">

![Swarix Logo](app/src/main/res/mipmap-xxhdpi/ic_launcher.png)

### **Sound for Every Story**
*An Intelligent, High-Fidelity Open-Source Music Player for Android*

[![Android](https://img.shields.io/badge/Platform-Android_8.0%2B-brightgreen.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9.22-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Media3 ExoPlayer](https://img.shields.io/badge/Audio-AndroidX_Media3_1.2.1-red.svg)](https://developer.android.com/media/media3)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Download APK](https://img.shields.io/badge/Download-Latest_APK-blue?logo=android&logoColor=white)](https://github.com/RitikkumarAI/swarix/releases/latest/download/Swarix-Music-Player.apk)

[**📥 Download Latest Release APK**](https://github.com/RitikkumarAI/swarix/releases/latest/download/Swarix-Music-Player.apk) • [**✨ Features**](#-key-features) • [**🛠️ Tech Stack**](#%EF%B8%8F-architecture--tech-stack) • [**🚀 Building from Source**](#-building-from-source)

</div>

---

## 📥 Direct APK Download

Download and install Swarix directly onto your Android device:

| File | Description | Download |
|---|---|---|
| **Swarix Release APK** | Optimized, signed production build (recommended) | [**Download APK (v1.0)**](https://github.com/RitikkumarAI/swarix/releases/latest/download/Swarix-Music-Player.apk) |
| **All Releases** | View changelogs and previous builds | [**Releases Page**](https://github.com/RitikkumarAI/swarix/releases) |

### 📲 Quick Install Guide:
1. Tap the **[Download APK](https://github.com/RitikkumarAI/swarix/releases/latest/download/Swarix-Music-Player.apk)** link on your Android device.
2. When the download completes, tap the file in your notification bar or **Downloads** folder.
3. If prompted with *"Install unknown apps"*, enable permission for your browser or file manager.
4. Tap **Install** and open **Swarix**!

---

## 📖 Overview

**Swarix** is a modern, high-performance Android music application designed to deliver an exceptional offline and online listening experience. Built with **Kotlin**, **Jetpack Compose (Material Design 3)**, and **AndroidX Media3 ExoPlayer**, Swarix combines intelligent AI organization, gapless audio playback, real-time diagnostics, and tactile bass haptics into a beautiful, ad-free interface.

---

## ✨ Key Features

### 🎧 **High-Fidelity Audio Engine**
- **True Gapless Playback**: Pre-buffers consecutive tracks via a unified timeline for **0ms transition delay** between songs.
- **Smart Audio Focus & Ducking**: Automatically manages system audio focus (`C.USAGE_MEDIA`), gracefully ducking volume (`0.2f`) during notifications and resuming after phone calls.
- **Bass Haptic Feedback Engine**: Feel the rhythm with real-time tactile vibrations synchronized to low-end kicks (`Subtle`, `Balanced`, `Powerful`).
- **10-Band Equalizer & Peak Normalizer**: Custom frequency tuning and `LoudnessEnhancer` peak audio normalization across all tracks.
- **Karaoke Mode**: Real-time vocal removal filter and lyrics synchronization.

---

### 🤖 **AI & Smart Music Discovery**
- **AI Mood Radio**: Instantly generate playlists matching your current mood (*Chill*, *Focus*, *Workout*, *Party*) with M3U export support.
- **AI Smart Folders**: Automatically organizes local and cached tracks into sub-genre and mood categories.
- **Genre Radar Chart**: A custom Jetpack Compose Canvas radar polygon chart visualizing your music taste dashboard.

---

### 🛠️ **Playback Health & Real-Time Diagnostics**
- **Playback Health Manager**: Background monitoring that detects stalled buffering sessions (>10s) and automatically executes recovery routines (`exoPlayer.prepare()`, `seekTo()`, `play()`).
- **Playback Diagnostics Screen**: Live telemetry viewer displaying ExoPlayer states, active track metadata, buffer metrics, and a monospaced real-time event log feed captured by `PlaybackDiagnosticLogger`.
- **Developer "Reset Player" Button**: Flush corrupted audio buffers and re-arm the player in one tap without restarting the app.

---

### 🎛️ **Personalization & Utility**
- **Crossfade DJ Profiles**: Customize track transition envelopes (*Club*, *Smooth*, *Sudden*) with 1s–12s crossfade controls.
- **Sleep Timer with Volume Fade**: Gradually fades out audio volume linearly over set durations or at the end of a track.
- **Music Wake-up Alarm**: Gentle volume ramp-up alarm using tracks from your local music library.
- **Room Search History**: Persistent local query history (`Room SQLite DB`) with interactive trend chips for quick re-searches.

---

## 🛠️ Architecture & Tech Stack

Swarix follows modern Android Architecture guidelines (**MVVM + Clean Architecture**):

```
app/src/main/java/com/example/
 ├── core/                   # Data Layer (Room DB, Models, Repositories, Cache)
 ├── playback/               # Audio Engine (PlayerManager, Media3, HealthManager, AudioFocusManager)
 │    └── service/           # MusicPlaybackService (MediaSessionService)
 └── ui/                     # Presentation Layer (Jetpack Compose, ViewModels, Components)
      ├── screens/           # Main Screens (Home, Search, Library, Settings, Diagnostics)
      └── theme/             # Material 3 Color Schemes, Typography, Shapes
```

- **Language**: Kotlin 1.9+
- **UI Toolkit**: Jetpack Compose with Material 3 Design
- **Audio Engine**: AndroidX Media3 ExoPlayer & MediaSession
- **Database**: Room Database (KSP)
- **Asynchronous Operations**: Kotlin Coroutines & Flow
- **Image Loading**: Coil Compose
- **Minification & Security**: R8 / ProGuard optimized

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34 / 36

### Build Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/RitikkumarAI/swarix.git
   cd swarix
   ```

2. **Build Debug APK**:
   ```bash
   gradle assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   gradle testDebugUnitTest
   ```

4. **Build Production Release APK**:
   ```bash
   gradle assembleRelease
   ```
   *The minified APK will be available in `app/build/outputs/apk/release/app-release.apk`.*

---

## 🛡️ Security & Privacy

Swarix is built with strict privacy and security standards:
- **No Sensitive Credentials in Source**: API keys, keystores (`*.keystore`, `*.jks`), and local environment variables (`.env`, `local.properties`) are strictly ignored via `.gitignore`.
- **Zero Data Tracking**: Listening history and preferences remain 100% device-local inside Room SQLite.

---

## 📄 License

```
MIT License

Copyright (c) 2026 Swarix Open Source Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```
