# Changelog

All notable changes to WAVORA are documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)  
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

---

## [Unreleased]

### Planned
- LRC synced lyrics parser (Phase 9 full implementation)
- Audio file deep link routing to Now Playing
- Compose UI tests (Espresso + ComposeTestRule)
- App widget for home screen playback controls

---

## [1.0.0] — 2024-12-01

First production release. Complete feature set built across 11 phases.

### Added

**Phase 1 — Foundation**
- Project scaffold: Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture
- Hilt dependency injection with compile-time verification
- `BaseViewModel` with `updateState`, `safeLaunch`, `emitEvent` pattern
- `AsyncResult<T>` sealed class (Loading / Success / Error)
- Navigation Compose with type-safe sealed `WavoraRoute` class
- Edge-to-edge display, splash screen, OLED dark theme (true black)

**Phase 2 — Music Discovery**
- Room database v2 with FTS4 shadow table for full-text search
- `MediaStoreScanner` — scans device audio files, builds album/artist aggregates
- Scan diff algorithm — O(n) added/updated/removed detection preserving `isFavorite`/`playCount`
- `WorkManager` scan scheduling — expedited one-time + daily periodic with battery constraints
- `LibraryScreen` — 5-tab pager (Songs, Albums, Artists, Folders, Playlists)
- `LibraryViewModel` with real repository wiring and rescan support

**Phase 3 — Playback Engine**
- `WavoraPlaybackService` — `MediaSessionService` + ExoPlayer with hardware decode
- `AudioFocusManager` — audio focus, ducking to 20% on transient loss, BECOME_NOISY
- `QueueManager` — pure Kotlin, Fisher-Yates shuffle, original order preserved
- `PlayerRepositoryImpl` — MediaController IPC, same-process state flow
- `NowPlayingScreen` — album art, palette gradient background, seek bar, controls
- `MiniPlayer` — persistent bar with progress, slides in/out on song start/stop
- `WavoraNotificationManager` — lock screen + notification controls via Media3
- Sleep timer with coroutine-based countdown

**Phase 4 — Queue & Playlists**
- `QueueScreen` — drag-reorder via `detectDragGesturesAfterLongPress`, remove, clear
- `PlaylistScreen` — full CRUD: create, rename, delete, add/remove songs, reorder
- `AddToPlaylistBottomSheet` — pick existing or create new playlist inline
- `CreatePlaylistDialog`, `RenamePlaylistDialog`, `ConfirmDeleteDialog`
- Long-press on song in library opens add-to-playlist sheet

**Phase 5 — Detail Screens**
- `AlbumDetailScreen` — full-width hero art, palette extraction, track badges, Play/Shuffle
- `ArtistDetailScreen` — circular avatar, horizontal album scroll row, full song list
- `FolderDetailScreen` — folder path header, total duration, Play all
- `SongListItem` — `showAlbumArt` param for track-number mode in album detail
- Favourite toggle wired to `MusicRepository.toggleFavourite`
- Snackbar error handling in `NowPlayingScreen`

**Phase 6 — Search & Settings**
- `SearchScreen` — debounced FTS4 search (300ms), sectioned results (Songs/Albums/Artists)
- `SearchViewModel` — `queryFlow` with `flatMapLatest`, `isSearching` spinner state
- `SettingsScreen` — all toggles wired to DataStore via `UserPreferencesRepository`
- `UserPreferencesRepositoryImpl` — single DataStore file, `IOException` safe fallback
- `OptionPickerDialog` — generic picker for skip duration and crossfade

**Phase 7 — Advanced Features**
- `SmartPlaylistScreen` — Most Played, Recently Played, Recently Added, Favourites
- Smart playlist cards in Playlists tab (2×2 grid above user playlists)
- `EqualizerScreen` — 5-band EQ via Android `AudioEffect`, 10 presets, enable/disable
- `LyricsPanel` — animated slide-in, `LyricsLine(timestampMs, text)` ready for LRC parser
- Folder navigation wired from FoldersTab
- `MusicRepository` extended with `getMostPlayedSongs`, `getRecentlyPlayedSongs`, `getRecentlyAddedSongs`

**Phase 8 — Battery & Performance**
- `BatteryAwareBufferConfig` — generous (50s, charging) vs conservative (8s, battery saver)
- `CoilImageCacheConfig` — 15% heap, 100 MB disk, `allowRgb565(true)`, selective crossfade
- `ScanThrottleManager` — 15-minute cooldown prevents MediaStore burst scans
- StrictMode hardened: `detectContentUriWithoutPermission`, `detectCredentialProtectedWhileLocked`
- ProGuard rules audited and completed (98 lines covering all library requirements)
- Gradle JVM tuned: `MaxMetaspaceSize`, `HeapDumpOnOutOfMemoryError`

**Phase 9 — Testing**
- `ExtensionsTest` — 32 pure JVM tests for all utility functions
- `PlaylistViewModelTest` — 12 tests with `SavedStateHandle` and fake repositories
- `SongDaoIntegrationTest` — 19 Room in-memory tests (all 7 sort orders, FTS, favourites)
- `PlaylistDaoIntegrationTest` — 13 Room tests including 3-step atomic reorder transaction
- `androidTestImplementation` deps added for Room, runner, ext-junit

**Phase 10 — Polish**
- `OnboardingScreen` — animated first-launch permission flow with feature cards
- `Accessibility.kt` — 9 semantic modifier helpers for full TalkBack support
- `songRowSemantics`, `seekBarSemantics`, `shuffleSemantics`, `repeatSemantics` applied
- Adaptive app icon — equalizer wave foreground + violet gradient background + monochrome layer
- `strings.xml` completed — 130 entries covering all accessibility, UI, and error strings
- Theme preferences wired to `MainActivity` — live theme switching without restart
- Deep link intent filters for `audio/*` MIME type (`content://` and `file://`)
- `PlaceholderScreen` deleted — all 8 placeholder destinations replaced

**Phase 11 — Documentation**
- `README.md` — complete with feature table, setup guide, module map, tech stack
- `ARCHITECTURE.md` — architectural decisions with reasoning and trade-offs
- `CONTRIBUTING.md` — branch naming, commit convention, code style, PR checklist
- `CHANGELOG.md` — this file
- CI/CD: upgraded GitHub Actions with lint, release build, code coverage steps
- `PULL_REQUEST_TEMPLATE.md` and issue templates

### Technical stats at v1.0.0
- **65** production Kotlin source files
- **87** total Kotlin files (including tests)
- **252** automated tests (220 unit + 32 instrumented)
- **108** total project files
- Min SDK: Android 8.0 (API 26) — covers ~95% of active devices

---

[Unreleased]: https://github.com/muhamedamin308/wavora/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/muhamedamin308/wavora/releases/tag/v1.0.0
