<div align="center">
  <h1>🎵 WAVORA</h1>
  <p><em>Where Every Wave Tells a Story</em></p>

  ![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android)
  ![Min SDK](https://img.shields.io/badge/minSdk-26%20(Android%208.0)-brightgreen)
  ![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin)
  ![License](https://img.shields.io/badge/license-MIT-blue)
  ![Build](https://github.com/muhamedamin308/wavora/actions/workflows/build.yml/badge.svg)
</div>

---

WAVORA is a **production-grade, offline-first Android music player** built to demonstrate clean architecture, modern Android engineering, and attention to every detail — from battery-aware ExoPlayer buffers to TalkBack-ready semantic modifiers.

No subscriptions. No accounts. No internet required. Your music, your device, your privacy.

---

## ✨ Features

| Category | Feature | Status |
|----------|---------|--------|
| **Library** | Songs, Albums, Artists, Folders, Playlists | ✅ |
| **Playback** | ExoPlayer + MediaSession + ForegroundService | ✅ |
| **Controls** | Background notification, Bluetooth, lock screen | ✅ |
| **Queue** | Add / remove / drag-reorder, persist across kills | ✅ |
| **Playlists** | Create / rename / delete / reorder songs | ✅ |
| **Smart Playlists** | Most Played, Recently Played, Recently Added, Favourites | ✅ |
| **Search** | FTS4 full-text search across songs, albums, artists | ✅ |
| **Now Playing** | Album art with palette gradient, seek bar, favourite | ✅ |
| **Equalizer** | 5-band EQ with 10 presets (Android AudioEffect) | ✅ |
| **Lyrics** | Animated panel, LRC sync ready (Phase 9 full impl) | ✅ |
| **Sleep Timer** | 5–60 min options, active countdown display | ✅ |
| **Settings** | Dark/dynamic theme, gapless, crossfade, skip duration | ✅ |
| **Performance** | Battery-aware buffers, Coil cache tuning, scan throttle | ✅ |
| **Accessibility** | Full TalkBack support with semantic modifiers | ✅ |
| **Onboarding** | Animated first-launch permission flow | ✅ |

---

## 📋 Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 (Temurin recommended) |
| Android SDK | API 34 |
| Gradle | 8.x (wrapper included) |

**Min SDK:** Android 8.0 (API 26)  
**Target SDK:** Android 14 (API 34)

---

## 🚀 Getting Started

```bash
# Clone
git clone https://github.com/muhamedamin308/wavora.git
cd wavora

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

The debug APK is placed at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🏗 Architecture

WAVORA follows **MVVM + Clean Architecture** with strict layer separation:

```
UI Layer (Compose)
    ↓  uiState: StateFlow<UiState>
    ↑  events: Flow<Event>
ViewModel (BaseViewModel)
    ↓  suspend / Flow calls
Repository Interface (domain)
    ↓
Repository Implementation (data)
    ├── Room Database   (single source of truth)
    ├── MediaStore API  (device music discovery)
    └── DataStore       (user preferences)
```

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for a detailed breakdown of every layer, key design decisions, and the reasoning behind them.

---

## 📂 Module Map

```
app/src/main/java/com/wavora/app/
├── core/
│   ├── base/           BaseViewModel, AsyncResult
│   ├── performance/    BatteryAwareBufferConfig, CoilImageCacheConfig, ScanThrottleManager
│   └── util/           Extensions, Constants
├── data/
│   ├── local/
│   │   ├── dao/        SongDao, AlbumDao, ArtistDao, PlaylistDao, PlayHistoryDao, QueueDao
│   │   └── entity/     Room entities with FTS4 shadow table
│   ├── mediastore/     MediaStoreScanner
│   ├── preferences/    UserPreferencesRepositoryImpl (DataStore)
│   └── repository/     MusicRepositoryImpl, PlaylistRepositoryImpl, PlayerRepositoryImpl
├── di/                 AppModule, DatabaseModule, PlayerModule, PreferencesModule, WorkerModule
├── domain/
│   ├── model/          Song, Album, Artist, Playlist, PlayerState, UserPreferences
│   └── repository/     Repository interfaces (contracts)
├── navigation/         WavoraNavHost, WavoraRoutes
├── player/
│   ├── notification/   WavoraNotificationManager (Media3 PlayerNotificationManager)
│   ├── queue/          QueueManager (pure Kotlin, Fisher-Yates shuffle)
│   └── service/        WavoraPlaybackService (MediaSessionService + ExoPlayer)
│                       AudioFocusManager (ducking, BECOME_NOISY)
├── ui/
│   ├── album/          AlbumDetailScreen + ViewModel
│   ├── artist/         ArtistDetailScreen + ViewModel
│   ├── components/     MiniPlayer, SleepTimerSheet, LyricsPanel, Accessibility, Dialogs
│   ├── equalizer/      EqualizerScreen + ViewModel (Android AudioEffect)
│   ├── folder/         FolderDetailScreen + ViewModel
│   ├── library/        LibraryScreen + LibraryViewModel (5-tab pager)
│   ├── onboarding/     OnboardingScreen (first-launch permission flow)
│   ├── player/         NowPlayingScreen + PlayerViewModel
│   ├── playlist/       PlaylistScreen + PlaylistViewModel
│   ├── queue/          QueueScreen + QueueViewModel (drag-reorder)
│   ├── search/         SearchScreen + SearchViewModel (debounced FTS4)
│   ├── settings/       SettingsScreen + SettingsViewModel (DataStore)
│   ├── smartplaylist/  SmartPlaylistScreen + ViewModel (4 auto-playlists)
│   └── theme/          Color, Shape, Type, WavoraTheme (OLED dark + Material You)
└── worker/             LibraryScanWorker (@HiltWorker, WorkManager)
```

---

## 🛠 Tech Stack

| Layer | Technology | Reason |
|-------|-----------|--------|
| **UI** | Jetpack Compose + Material 3 | Declarative, type-safe, single toolkit |
| **Navigation** | Navigation Compose | Type-safe routes, back stack management |
| **Playback** | Media3 / ExoPlayer | Hardware decode, MediaSession, Auto/WearOS |
| **Background** | MediaSessionService | Foreground service lifecycle managed by Media3 |
| **Database** | Room + SQLite | FTS4 for search, strict migrations, type-safe DAOs |
| **DI** | Hilt (Dagger 2) | Compile-time verified, scoped lifetimes |
| **Async** | Coroutines + Flow | Structured concurrency, reactive UI |
| **Images** | Coil 2 | Compose-native, disk + memory cache, RGB_565 |
| **Preferences** | DataStore (Preferences) | Async, crash-safe, replaces SharedPreferences |
| **Background jobs** | WorkManager | Doze-mode safe, battery constraints |
| **Build** | Gradle KTS + Version Catalogs | Type-safe, single source of truth for versions |

---

## 🔋 Performance & Battery

- **Adaptive ExoPlayer buffers** — 50s generous (charging) vs 8s conservative (battery saver / < 20%)
- **Coil cache** — 15% heap (vs default 25%), 100 MB disk, `allowRgb565(true)` for 50% RAM reduction
- **Scan throttle** — 15-minute cooldown prevents MediaStore burst scans
- **WorkManager constraints** — periodic scans only on `BATTERY_NOT_LOW + STORAGE_NOT_LOW`
- **Log stripping** — `android.util.Log.v/d/i` removed from release via R8 `assumenosideeffects`
- **R8 full mode** — enabled in `gradle.properties` for maximal dead-code removal

---

## 🧪 Tests

```
220 unit tests    (JVM, zero Android dependencies)
 32 instrumented  (Room in-memory, Android JUnit4)
───────────────────
252 total
```

```bash
./gradlew test                          # unit tests, HTML report at app/build/reports/tests/
./gradlew connectedAndroidTest          # instrumented (Room DAO integration tests)
./gradlew koverXmlReport                # code coverage (add Kover plugin for full report)
```

Key test areas: `QueueManager` (27 cases), `ExtensionsTest` (32 cases), `SongDaoIntegrationTest` (19 cases), `PlaylistDaoIntegrationTest` (13 cases — including the 3-step atomic reorder transaction).

---

## 🤝 Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for branch naming, commit conventions, code style requirements, and the PR checklist.

---

## 📜 Changelog

See [`CHANGELOG.md`](CHANGELOG.md) for a full version history.

---

## ⚖️ License

```
MIT License — Copyright (c) 2024 WAVORA Contributors
```

See [`LICENSE`](LICENSE) for the full text.

---

<div align="center">
  <em>Built with 🎧 and clean code principles.</em>
</div>
