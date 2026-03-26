# WAVORA Architecture

This document explains the architectural decisions behind WAVORA, the reasoning for each choice, and the trade-offs considered. It is intended for contributors and reviewers who want to understand *why* the code is structured the way it is.

---

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
2. [Layer Responsibilities](#2-layer-responsibilities)
3. [Data Flow](#3-data-flow)
4. [Room as Single Source of Truth](#4-room-as-single-source-of-truth)
5. [Playback Engine](#5-playback-engine)
6. [State Management](#6-state-management)
7. [Dependency Injection](#7-dependency-injection)
8. [Navigation](#8-navigation)
9. [Background Work](#9-background-work)
10. [Performance Decisions](#10-performance-decisions)
11. [Testing Strategy](#11-testing-strategy)

---

## 1. High-Level Overview

```
┌─────────────────────────────────────────────────┐
│                  UI Layer                        │
│   Compose screens + ViewModels (StateFlow)       │
└───────────────────────┬─────────────────────────┘
                        │  Interfaces only
┌───────────────────────▼─────────────────────────┐
│               Domain Layer                       │
│   Repository interfaces, domain models           │
└───────────────────────┬─────────────────────────┘
                        │  Implementations
┌───────────────────────▼─────────────────────────┐
│                 Data Layer                       │
│   Room + MediaStore + DataStore + ExoPlayer      │
└─────────────────────────────────────────────────┘
```

The UI never imports anything from `data`. The domain never imports from `ui`. Hilt binds the concrete implementations at runtime, so each layer is independently testable.

---

## 2. Layer Responsibilities

### `core/`
Shared infrastructure with no business logic. `BaseViewModel` provides `updateState`, `safeLaunch`, and `emitEvent` — the only three things a ViewModel ever needs from outside the domain. `AsyncResult<T>` is a sealed class (Loading / Success / Error) used as the wrapper for all async data exposed to the UI.

### `domain/`
Pure Kotlin. No Android imports. Models are data classes with no behaviour. Repository interfaces define contracts — the UI layer only ever sees these interfaces, never the Room-backed implementations. This is what enables fake repositories in tests.

### `data/`
Android-specific. Room entities, DAOs, MediaStoreScanner, DataStore. The only place where SQLite, ContentResolver, and WorkManager are touched. **MediaStoreScanner only writes to Room — it never exposes MediaStore data directly to the UI.** Room is always the single source of truth.

### `player/`
The playback subsystem. Separated from `data/` because it involves framework services (`MediaSessionService`) that have a different lifecycle than repositories. `QueueManager` is pure Kotlin and can be unit-tested without Android.

### `ui/`
Each screen is a self-contained `Screen.kt` file containing both the `ViewModel` and the `@Composable` function. This co-location makes navigation between implementation and view trivial. State is `data class UiState`, events are `sealed interface Event`.

---

## 3. Data Flow

### Library data (read path)
```
MediaStore → MediaStoreScanner → Room (write)
                                    ↓ Flow<List<SongEntity>>
                              SongDao.getAllSongsByTitle()
                                    ↓ map { toDomain() }
                             MusicRepositoryImpl
                                    ↓ Flow<List<Song>>
                              LibraryViewModel.songs
                                    ↓ collectAsStateWithLifecycle()
                               LibraryScreen (Compose)
```

### Playback command (write path)
```
User tap → PlayerViewModel.togglePlayPause()
                ↓ safeLaunch { }
         PlayerRepository.pause()  [interface]
                ↓
         PlayerRepositoryImpl.pause()
                ↓ mediaController?.pause()
         MediaController → IPC → WavoraPlaybackService
                ↓ player.pause()
              ExoPlayer
```

### Settings change
```
SettingsScreen toggle → SettingsViewModel.onDarkThemeToggle(false)
                              ↓ safeLaunch { }
                   UserPreferencesRepository.setDarkTheme(false)
                              ↓ dataStore.edit { }
                          DataStore (disk)
                              ↓ Flow<UserPreferences>
                       MainActivity.prefs (collectAsState)
                              ↓ recompose
                      WavoraTheme(darkTheme = false)
```

---

## 4. Room as Single Source of Truth

**Decision:** MediaStore is never read directly by the UI layer. All reads come from Room.

**Reasoning:**
- MediaStore ContentResolver queries are synchronous and can block the main thread for hundreds of milliseconds on large libraries.
- MediaStore data changes at unpredictable times (file manager operations, OTA updates). Room + Flow gives the UI a reactive, always-fresh view with no polling.
- WAVORA-owned fields (`isFavorite`, `playCount`) cannot be stored in MediaStore — they live in Room and survive rescans via targeted UPDATE statements that skip those columns.
- Library browsing works immediately after first launch from cache. The first scan populates Room; subsequent launches read from the cache instantly.

**Trade-off:** The Room cache can be stale if files are added/removed between scans. Mitigated by WorkManager periodic scans and a manual rescan button.

---

## 5. Playback Engine

### Service architecture
`WavoraPlaybackService` extends `MediaSessionService` (Media3). This is the correct modern class for music apps — it handles the foreground service lifecycle, notification promotion/demotion, and MediaSession token registration with the system.

### Audio focus
`AudioFocusManager` handles focus separately from ExoPlayer's built-in focus handling. Reason: the built-in handling doesn't support ducking to a specific volume (`DUCK_VOLUME = 0.2f`) — it just pauses on `LOSS_TRANSIENT_CAN_DUCK`. Custom handling gives finer control.

### BECOME_NOISY
Also handled manually in `AudioFocusManager`. When headphones are unplugged, the system fires `ACTION_AUDIO_BECOMING_NOISY`. Without handling this, ExoPlayer continues playing through the speaker — a privacy/embarrassment issue. The receiver is registered when focus is granted and unregistered when abandoned.

### Queue persistence
`QueueManager` is pure Kotlin — no Android framework. It holds `originalQueue` (pre-shuffle order) separately from the active `playQueue`. Fisher-Yates shuffles the queue while pinning the current song at index 0. `QueueEntity` persists the queue to Room on every mutation so it survives app kills.

### MediaController IPC
`PlayerRepositoryImpl` communicates with the service via `MediaController` (Media3 binder IPC). Commands are queued automatically if the service isn't connected yet. This also enables Android Auto and WearOS control for free.

### State sharing
`WavoraPlaybackService.playerStateFlow` is a `MutableStateFlow` read directly by `PlayerRepositoryImpl` (same-process reference). This avoids serialising 500ms position updates through MediaSession extras.

---

## 6. State Management

### Pattern: StateFlow + sealed Event
Every ViewModel exposes exactly two things:
- `uiState: StateFlow<UiState>` — current UI state, replayed to new collectors
- `events: SharedFlow<Event>` — one-shot actions (navigate, show snackbar)

`BaseViewModel` provides `updateState { copy(...) }` which always produces atomic state updates via `MutableStateFlow`. `safeLaunch` wraps coroutines in try/catch so a failed operation logs and calls `handleError()` rather than crashing the app.

### Why not LiveData?
Flow integrates with Compose's `collectAsStateWithLifecycle()` natively. LiveData requires the `lifecycle-livedata-ktx` bridge. Flow also works in pure Kotlin test environments without Android.

### Why sealed Event instead of SharedFlow<UiState>?
Navigation and snackbars are fire-and-forget. Putting them in `UiState` requires the ViewModel to reset them after they're consumed, which creates race conditions. A `SharedFlow` with `replay = 0` delivers events exactly once to one collector.

---

## 7. Dependency Injection

Hilt with `@Singleton` scoping for repositories and `@HiltViewModel` for ViewModels. `@HiltWorker` + `HiltWorkerFactory` for `LibraryScanWorker`.

**Why Hilt over manual DI?** The project scale (65+ source files, 10+ ViewModels, 6 DAOs) makes manual DI unmaintainable. Hilt provides compile-time verification — a missing binding is a build error, not a runtime crash.

**Module layout:**
- `AppModule` — DataStore
- `DatabaseModule` — Room database + all DAOs
- `PlayerModule` — binds `PlayerRepository` → `PlayerRepositoryImpl`
- `PreferencesModule` — binds `UserPreferencesRepository` → `UserPreferencesRepositoryImpl`
- `WorkerModule` — `@Binds @IntoMap` for `HiltWorkerFactory`

---

## 8. Navigation

**Navigation Compose** with a sealed `WavoraRoute` class. Each route is a `data object` so it can be used in `when` expressions with exhaustiveness checking.

**Why not type-safe Navigation 2.x?** The project targets API 26 and was designed before the stable release of type-safe Navigation Compose. The sealed class pattern provides the same compile-time safety manually with minimal boilerplate.

**Deep links:** `WavoraRoute.SmartPlaylist` and `WavoraRoute.FolderDetail` use string path arguments. Audio file deep links from external apps are handled in `MainActivity.onNewIntent()` → routed to `WavoraPlaybackService` (full routing in Phase 11 polish).

---

## 9. Background Work

All background work goes through **WorkManager**. No `AlarmManager`, no raw `Handler.postDelayed`, no `Service.startService` for periodic work.

**Scan scheduling:**
- One-time (first launch / manual): `ExistingWorkPolicy.KEEP` + expedited
- Periodic (daily): `BATTERY_NOT_LOW + STORAGE_NOT_LOW`, 4-hour flex window
- Throttle guard: `ScanThrottleManager` prevents scans within 15 minutes of the last one

**Why WorkManager over foreground service for scans?** Scans are deferrable — there's no user-visible reason they need to run at an exact moment. WorkManager handles Doze mode, battery optimisation, and retries automatically. Using a foreground service for a scan would waste battery and show an unnecessary notification.

---

## 10. Performance Decisions

### Buffer sizes
`BatteryAwareBufferConfig` selects ExoPlayer buffer sizes based on charging state and battery saver:
- **Generous** (charging): 50s max / 5s min — smooth playback, fast seeks
- **Conservative** (battery saver or < 20%): 8s max / 1s min — ~30% less CPU wake-ups

### Coil configuration
- 15% heap (vs default 25%) — album art at 48dp doesn't need the same cache as a photo gallery
- `allowRgb565(true)` — 50% RAM reduction for opaque album art (no alpha channel needed)
- `crossfade(false)` globally — re-enabled per-call only where animation adds value (NowPlaying, AlbumDetail)
- 100 MB disk cache — holds ~3000 thumbnails, eliminates repeated ContentResolver reads

### Database
- FTS4 shadow table (`songs_fts`) with `contentEntity = SongEntity` — Room auto-syncs via triggers. Search uses `MATCH "query*"` prefix matching.
- Albums and artists pre-aggregated at scan time (no `GROUP BY` on every render)
- `distinctUntilChanged()` on Flow queries — prevents unnecessary recompositions when data hasn't changed

### R8 full mode
Enabled in `gradle.properties`. Removes ~15–20% more dead code than R8 default mode.

---

## 11. Testing Strategy

### Pyramid
```
220 Unit tests (JVM)        — fast, no Android, run on every commit
 32 Instrumented (Room)     — slower, require emulator, run in CI on PR
  0 UI tests (Compose)      — planned for Phase 12 (Espresso + ComposeTestRule)
```

### Key test patterns

**Pure Kotlin tests:** `QueueManager`, `AudioFocusManager` callbacks, `ExtensionsTest` — these have zero Android dependencies and run in < 100ms total.

**Fake repositories:** `FakeMusicRepository`, `FakePlaylistRepository`, `FakePlayerRepository` — backed by `MutableStateFlow`s. Tests push state changes and verify ViewModel reactions without Room, MediaStore, or ExoPlayer.

**SavedStateHandle in tests:** `SavedStateHandle(mapOf("playlistId" to 1L))` — no Hilt, no robolectric. ViewModels that take nav arguments are testable with a single line.

**Room in-memory:** `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries()` — instrumented but gives confidence that SQL queries, joins, and transactions (especially the 3-step playlist reorder) are correct.

**Turbine:** `app.cash.turbine.test { }` for asserting Flow emissions in order.

---

*Last updated: Phase 11 (v1.0.0)*
