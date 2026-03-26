## Summary

<!-- One sentence: what does this PR do? -->

## Type of change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Refactor (no behaviour change)
- [ ] Performance improvement
- [ ] Tests only
- [ ] Documentation / chore

## Related issues

Closes #<!-- issue number -->

## What changed

<!-- Bullet list of the specific files/areas touched and why. -->

-
-

## Checklist

### Code
- [ ] Follows code style conventions in `CONTRIBUTING.md`
- [ ] No direct `_uiState.value =` outside `BaseViewModel` — uses `updateState { }`
- [ ] No disk/network on Main thread (StrictMode enforced in debug)
- [ ] Repository interface updated if new query added
- [ ] Hilt module updated if new binding introduced

### Tests
- [ ] New logic has unit tests
- [ ] Room query changes have DAO integration tests
- [ ] Fake repositories updated if interface changed
- [ ] `./gradlew test` passes locally

### UI / Compose
- [ ] Interactive elements have `contentDescription` or semantic modifier from `Accessibility.kt`
- [ ] No hardcoded strings — added to `strings.xml`
- [ ] No remembered allocations created per-frame in composable body

### Docs
- [ ] `CHANGELOG.md` updated under `[Unreleased]`
- [ ] KDoc added for public API
- [ ] `ARCHITECTURE.md` updated for structural changes (if applicable)

## Screenshots / recordings (UI changes only)

<!-- Drag and drop a screenshot or screen recording here if this affects visible UI. -->
