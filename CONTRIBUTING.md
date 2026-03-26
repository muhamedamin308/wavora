# Contributing to WAVORA

Thank you for considering a contribution. This document covers everything you need to get a PR merged quickly.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Branch Naming](#branch-naming)
- [Commit Convention](#commit-convention)
- [Code Style](#code-style)
- [PR Checklist](#pr-checklist)
- [Running Tests](#running-tests)
- [Architecture Rules](#architecture-rules)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)

---

## Getting Started

1. Fork the repository
2. Create a branch from `develop` (not `main`)
3. Make your changes following the conventions below
4. Open a PR against `develop`

`main` always contains the last stable release. `develop` is the integration branch.

---

## Branch Naming

```
feat/short-description        # new feature
fix/short-description         # bug fix
refactor/short-description    # no behaviour change
test/short-description        # tests only
docs/short-description        # documentation only
chore/short-description       # build, deps, CI
```

Examples:
```
feat/lrc-parser
fix/seekbar-position-reset
refactor/queue-manager-threading
test/album-dao-integration
```

---

## Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

**Types:** `feat` · `fix` · `refactor` · `test` · `docs` · `chore` · `perf` · `style`

**Scopes (optional):** `player` · `library` · `search` · `playlist` · `queue` · `eq` · `db` · `ui` · `ci` · `deps`

**Examples:**
```
feat(player): add gapless playback support
fix(db): prevent playlist reorder crash on single-item list
perf(coil): reduce memory cache to 15% heap
test(dao): add Song FTS4 prefix search cases
docs: update architecture decision for MediaController
chore(deps): bump media3 to 1.3.0
```

**Rules:**
- Description is imperative mood: "add", not "added" or "adds"
- Max 72 characters in the first line
- Reference issues: `fix(search): debounce not resetting — closes #42`

---

## Code Style

WAVORA follows the [Kotlin official style guide](https://kotlinlang.org/docs/coding-conventions.html) with these additions:

### Formatting
- 4-space indentation (no tabs)
- Max line length: 120 characters
- Trailing commas on multi-line parameter lists

### Naming
```kotlin
// Classes: PascalCase
class WavoraPlaybackService

// Functions/properties: camelCase
fun onSongClicked(index: Int)
val isPlaying: Boolean

// Constants: SCREAMING_SNAKE_CASE in companion object
companion object {
    const val DUCK_VOLUME = 0.2f
}

// Room entities: SuffixEntity
data class SongEntity(...)

// Domain models: no suffix
data class Song(...)
```

### Compose
- One composable per screen file (or closely related composables in the same file)
- Stateless composables receive state and callbacks — no direct ViewModel access in private composables
- `@Preview` functions at bottom of file, not in production code paths
- Use `Modifier` as last parameter, always defaulting to `Modifier`

### ViewModel pattern
```kotlin
// Always use updateState — never assign _uiState.value directly outside BaseViewModel
fun onSomethingClicked() = updateState { copy(isLoading = true) }

// Always use safeLaunch for coroutines — handles errors gracefully
fun onPlaySong(song: Song) = safeLaunch {
    playerRepository.play(song)
}

// One-shot events via emitEvent
fun onDeleteConfirmed() = safeLaunch {
    repository.delete(id)
    emitEvent(Event.NavigateUp)
}
```

### Architecture rules (non-negotiable)
- `ui/` must never import from `data/`
- `domain/` must never import from `ui/` or `data/`
- Repository implementations are bound via Hilt — never instantiated directly in UI or ViewModel

---

## PR Checklist

Before opening a PR, confirm every item:

```
Code
[ ] Follows code style conventions above
[ ] No direct state mutation outside BaseViewModel.updateState
[ ] No disk/network operations on Main thread (StrictMode will catch these in debug)
[ ] Repository interface updated if new query added (not just the impl)
[ ] Hilt module updated if new binding introduced

Tests
[ ] New logic has unit tests
[ ] Room query changes have DAO integration tests
[ ] Fake repositories updated if MusicRepository/PlaylistRepository interface changed
[ ] Tests pass locally: ./gradlew test

Compose / UI
[ ] New interactive elements have contentDescription
[ ] New list items use appropriate semantics modifier (see Accessibility.kt)
[ ] No hardcoded strings — added to strings.xml

Performance
[ ] No new allocations in composable body that should be remembered
[ ] No Flow.collect without lifecycle awareness (use collectAsStateWithLifecycle)
[ ] Coil loads use appropriate size parameter

Documentation
[ ] Public API has KDoc comment
[ ] ARCHITECTURE.md updated for major structural changes
[ ] CHANGELOG.md updated under [Unreleased]
```

---

## Running Tests

```bash
# All unit tests with results
./gradlew test

# Specific test class
./gradlew test --tests "com.wavora.app.player.QueueManagerTest"

# All tests with coverage (requires Kover plugin)
./gradlew koverXmlReport

# Instrumented tests (emulator/device required)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Full CI simulation
./gradlew lint test assembleRelease
```

---

## Architecture Rules

These are enforced by code review, not tooling (yet):

1. **Single Source of Truth** — Room is always the ground truth. MediaStore only writes to Room; UI only reads from Room.

2. **No framework in domain** — `domain/` is pure Kotlin. No `Context`, no `Android*`, no `androidx.*` imports.

3. **Interfaces before implementations** — Add the method to the repository interface first, update `FakeMusicRepository` in tests second, implement in the real repository third.

4. **Flow, not suspend, for queries** — All database queries that the UI observes return `Flow<T>`, not `suspend fun T`. Suspend is for writes.

5. **safeLaunch for coroutines** — Never `viewModelScope.launch` directly in a ViewModel. Use `safeLaunch` so errors don't crash the app silently.

6. **No business logic in composables** — All logic belongs in ViewModels. Composables are dumb: they receive state and invoke callbacks.

---

## Reporting Bugs

Use the [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) template. Include:
- Device model and Android version
- Steps to reproduce (minimised)
- Expected vs actual behaviour
- Logcat output if available

---

## Feature Requests

Use the [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) template. Check existing issues first — your idea may already be tracked.

---

*Questions? Open a Discussion on GitHub.*
