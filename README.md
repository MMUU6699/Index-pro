<div align="center">
  <a href="https://github.com/gdytd141-commits/MovieDB">
    <img src="https://github.com/MMUU6699/Index-pro/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png.jpg" alt="Billie Eilish Logo" width="150" height="150">
  </a>
  <h1>🎵 Billie Eilish Movie App</h1>
  <p>Discover and stream your favorite movies and TV shows with the ultimate entertainment companion inspired by Billie Eilish.</p>
</div>

<!-- Badges -->
<div align="center">
  <a href="https://github.com/gdytd141-commits/MovieDB/actions/workflows/release.yml">
    <img src="https://github.com/gdytd141-commits/MovieDB/actions/workflows/release.yml/badge.svg" alt="Build Status"/>
  </a>
  <a href="https://github.com/gdytd141-commits/MovieDB/releases/latest">
    <img src="https://img.shields.io/github/v/release/gdytd141-commits/MovieDB?label=GitHub" alt="GitHub release"/>
  </a>
  <a href="https://www.gnu.org/licenses/gpl-3.0.txt">
    <img src="https://img.shields.io/github/license/gdytd141-commits/MovieDB" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/Language-Arabic%20%7C%20English-blue" alt="Languages"/>
  <img src="https://img.shields.io/badge/Video%20Player-SuperEmbed-green" alt="Video Player"/>
  <img src="https://img.shields.io/badge/Auth-Google%20Sign--In-red" alt="Authentication"/>
</div>

<p align="center">
  <a href="#about-the-project">About</a> •
  <a href="#key-features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#installation">Installation</a> •
  <a href="#building-from-source">Build</a> •
  <a href="#contributing">Contribute</a> •
  <a href="#license">License</a>
</p>

---

## About The Project

**Billie Eilish Movie App** is a modern, feature-rich Android application that brings the world of entertainment to your fingertips. Inspired by the artistic vision of Billie Eilish, this app combines sleek design with powerful functionality to deliver an unparalleled movie and TV show experience.

🎬 **Stream & Discover**: Watch your favorite movies and TV shows directly through our integrated SuperEmbed video player, offering high-quality streaming with seamless playback.

🌍 **Multilingual Experience**: Full support for Arabic and English languages, with automatic data fetching in your preferred language from TMDB.

👤 **Google Sign-In**: Secure and convenient authentication with Google accounts for personalized experiences.

📱 **Modern Design**: Beautiful, intuitive interface that adapts to your device's theme with Material You design principles.

🎵 **Billie Eilish Inspired**: Every element of the app reflects the unique aesthetic and style that Billie Eilish represents.

---

## Key Features

### 🎥 **Video Streaming**
-   🎬 **SuperEmbed Integration**: High-quality video streaming powered by SuperEmbed API
-   📺 **Multi-Format Support**: Movies, TV shows, and live channels
-   🎮 **Advanced Controls**: Volume, brightness, and fullscreen controls
-   📱 **Responsive Player**: Optimized for all screen sizes

### 🌐 **Multilingual Support**
-   🇸🇦 **Arabic Language**: Complete RTL support with native Arabic interface
-   🇺🇸 **English Language**: Full English localization
-   🔄 **Dynamic Language Switching**: Change language on-the-fly in settings
-   📊 **Localized Data**: TMDB data fetched in your preferred language

### 🔐 **Authentication & Profile**
-   🔑 **Google Sign-In**: Secure authentication with Google accounts
-   👤 **User Profiles**: Personalized user experience with statistics
-   ⭐ **Favorites Management**: Save and organize your favorite content
-   📋 **Watchlist**: Keep track of what you want to watch
-   📊 **Statistics**: View your watching statistics and progress

### 🎨 **Design & UI**
-   🎵 **Billie Eilish Theme**: Unique design inspired by Billie Eilish's aesthetic
-   🌙 **Material You**: Modern UI that adapts to your device's theme
-   📱 **Responsive Design**: Optimized for phones and tablets
-   🎨 **Custom Icons**: Beautiful, consistent iconography throughout

### 📊 **Data & Content**
-   🎬 **TMDB Integration**: Comprehensive movie and TV show database
-   🔍 **Advanced Search**: Find content by title, genre, year, and more
-   ⭐ **Ratings & Reviews**: View ratings from multiple sources
-   🎭 **Cast & Crew**: Detailed information about actors and filmmakers
-   📅 **Release Tracking**: Stay updated with new releases

---

## Screenshots

<div align="center">
  <p><em>Screenshots will be updated soon with the new Billie Eilish theme!</em></p>
</div>

---

## Installation

### 📱 Download Options

You can download and install the Billie Eilish Movie App from the following sources:

| Version | Download | Size | Features |
|---------|----------|------|----------|
| **Full Version** | [📥 Download APK](https://github.com/MMUU6699/Index-pro/releases/download/v1.0.0/Billie-Eilish-Full-Debug.apk) | ~14MB | Complete features with Google Services |

### 🔧 Installation Instructions

1. **Enable Unknown Sources**: Go to Settings > Security > Enable "Unknown Sources"
2. **Download APK**: Choose your preferred version from the table above
3. **Install**: Open the downloaded APK file and follow the installation prompts
4. **Enjoy**: Launch the app and start exploring!

> **Note**: The Full Version is recommended for the complete experience including Google Sign-In functionality.

---

## Building from Source

The Billie Eilish Movie App can be compiled using Android Studio or via the command line with Gradle.

### Prerequisites

- **Android Studio** Arctic Fox or newer
- **JDK 21** or newer
- **Android SDK** with API level 34+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/MMUU6699/Index-pro.git
   cd index pro
   ```

2. **Set up API Keys:**
   - Get a free API Key from [TMDB](https://www.themoviedb.org/settings/api)
   - Create a `config.properties` file in the root project directory
   - Add your keys to `config.properties`:
     ```properties
     TMDB_ACCESS_TOKEN="YOUR_TMDB_READ_ACCESS_TOKEN"
     api_key="YOUR_TMDB_API_KEY"
     ```

3. **Build the application:**
   
   **Using Android Studio:**
   - Open Android Studio and select 'Open an existing Android Studio project'
   - Navigate to the cloned repository folder
   - Let Gradle sync and download dependencies
   - Build the project using `Build > Make Project`
   - Run on device/emulator using `Run > Run 'app'`

   **Using Command Line:**
   ```bash
   # For Linux/macOS
   ./gradlew assembleDebug      # Debug build
   ./gradlew assembleRelease    # Release build

   # For Windows
   gradlew.bat assembleDebug
   gradlew.bat assembleRelease
   ```

### Build Variants

- **`foss`**: Open source version without Google Services
- **`full`**: Complete version with all features including Google Services

---

## API Integration

### 🎬 TMDB API
The app uses The Movie Database (TMDB) API for fetching movie and TV show information:
- **Base URL**: `https://api.themoviedb.org/3/`
- **Authentication**: Bearer token authentication
- **Features**: Movie details, TV shows, search, trending content

### 📺 SuperEmbed API
Video streaming is powered by SuperEmbed:
- **Service**: [SuperEmbed.stream](https://www.superembed.stream/)
- **Features**: High-quality video streaming, multiple sources
- **Supported Content**: Movies, TV shows, live channels

### 🔐 Google Sign-In
Authentication is handled through Google Sign-In:
- **Service**: Google Identity Services
- **Features**: Secure authentication, user profiles
- **Privacy**: Minimal data collection, user consent required

---

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**!

### 🐛 Reporting Issues

If you encounter a bug or have a feature request:

1. Check [existing issues](https://github.com/MMUU6699/Index-pro/issues) first
2. If not found, [create a new issue](https://github.com/MMUU6699/Index-pro/issues/new)
3. Provide detailed information about the problem or suggestion

### 🔧 Pull Requests

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request against the `master` branch

### 🌍 Translations

Help us make the Billie Eilish Movie App accessible to more users by contributing translations! Currently supported languages:

- 🇺🇸 **English** (Complete)
- 🇸🇦 **Arabic** (Complete)

To add a new language:
1. Create a new `values-[language_code]` folder in `app/src/main/res/`
2. Copy `strings.xml` from `values/` and translate all strings
3. Test the translation in the app
4. Submit a pull request

---

## Changelog

### Version 2.0.0 - "Billie Eilish Edition" 🎵
- 🎵 **Complete rebrand** to Billie Eilish theme
- 🎬 **SuperEmbed integration** for video streaming
- 🌍 **Arabic language support** with RTL layout
- 🔐 **Google Sign-In** authentication
- 👤 **New profile system** with statistics and favorites
- 🎨 **Modern UI redesign** with Material You
- 📱 **Improved performance** and bug fixes

---

## Privacy Policy

The Billie Eilish Movie App respects your privacy:

- **Data Collection**: Minimal data collection, only what's necessary for functionality
- **Google Sign-In**: Optional feature, user consent required
- **Local Storage**: Most data stored locally on your device
- **Third-Party Services**: TMDB for content data, SuperEmbed for video streaming
- **No Tracking**: No user tracking or analytics without consent

---

## License

Distributed under the GNU General Public License v3.0. See [`LICENSE`](LICENSE) file for the full text.

```
Copyright (C) 2024 Billie Eilish Movie App

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

---

## Acknowledgments

- 🎵 **Billie Eilish** - For the inspiration and artistic vision
- 🎬 **TMDB** - For providing comprehensive movie and TV data
- 📺 **SuperEmbed** - For reliable video streaming services
- 🔐 **Google** - For secure authentication services
- 🌍 **Open Source Community** - For continuous support and contributions

---

<div align="center">
  <p>Made with ❤️ and inspired by 🎵 Billie Eilish</p>
  <p><strong>Enjoy streaming! 🎬</strong></p>
</div>
