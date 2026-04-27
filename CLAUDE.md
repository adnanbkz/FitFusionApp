# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Primary verification — always run this after changes
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Unit tests (scaffold only — no meaningful tests exist yet)
./gradlew :app:test

# Instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest

# Clean build
./gradlew clean :app:assembleDebug
```

There is no custom lint configuration beyond default Android lint.

## SDK & Language Targets

- Kotlin `2.0.21`, JVM target `11`
- `compileSdk = 36`, `minSdk = 26` (Android 8.0+), `targetSdk = 36`
- AGP `8.13.2`

## Architecture

**Single-module** Android app (`com.example.fitfusion`), **MVVM + Repository pattern**, all Kotlin, all Jetpack Compose UI with Material3.

**Layers:**
- `ui/screens/` — One Composable file per screen
- `ui/components/` — Shared composables (`Component.kt`, `SharedComponents.kt`, `CreatePostSheet.kt`)
- `ui/theme/` — `Color.kt`, `Theme.kt`, `Type.kt`, `DesignTokens.kt`
- `viewmodel/` — One ViewModel per feature (`ViewModel` or `AndroidViewModel`)
- `data/repository/` — Data access; wraps Firestore, Health Connect, and external APIs
- `data/models/` — Plain Kotlin data classes
- `data/health/` — Health Connect manager and sync service
- `util/` — Translation helpers for muscle groups and equipment

**Navigation:** Single `MainActivity` with `NavHost`. All routes are defined in `ui/screens/Screens.kt` as an enum. Some routes carry arguments (e.g. `${Screens.PostDetailScreen.name}/{postId}`, `${Screens.AddWorkoutScreen.name}?logMode={logMode}`).

**State management:** ViewModels expose `StateFlow`; screens collect with `collectAsState()`.

**Singleton repositories:** `WorkoutRepository` and `FeedRepository` are Kotlin `object` singletons. They self-initialize and attach `FirebaseAuth.AuthStateListener` to manage Firestore listeners per user session. Do not convert them to regular classes without also reworking how listeners are managed.

## Key Libraries

- **Firebase BOM 34.11.0:** `firebase-auth`, `firebase-firestore`, `firebase-functions`, `firebase-storage`, `firebase-appcheck-playintegrity`, `firebase-crashlytics:20.0.4`
- **Health Connect:** `androidx.health.connect:connect-client:1.1.0` — reads steps and heart rate; does not write back
- **Coil 3** (`coil-compose:3.0.4`, `coil-network-okhttp`, `coil-video`) — upgrading Coil or Kotlin can break compatibility; verify before bumping versions
- **CameraX 1.4.1** — used in `CameraScreen.kt` for nutrition post photos/videos
- **Algolia** — custom HTTP POST implementation (no SDK) in `AlgoliaExerciseSearchRepository.kt`; index `fitfusion_exercises_algolia`
- **Navigation Compose:** `androidx.navigation:navigation-compose:2.9.7`
- **Version catalog:** `gradle/libs.versions.toml`

## Firestore Data Model

```
users/{uid}                          # user profile
users/{uid}/workouts/{workoutId}     # logged workouts
users/{uid}/foodLogs                 # food logs
users/{uid}/healthDaily/{yyyy-MM-dd} # daily Health Connect data
users/{uid}/dailySummaries           # aggregated summaries
exercises/{exerciseId}               # global exercise catalog (read-only for users)
posts/{postId}                       # social feed posts
posts/{postId}/likes/{uid}           # likes subcollection
posts/{postId}/comments/{commentId}  # comments subcollection
```

Exercise catalog queries are ordered by `priority ASC, nameLower ASC` and paginated.

## Algolia & Secrets

Algolia keys (`ALGOLIA_APP_ID`, `ALGOLIA_SEARCH_API_KEY`, `ALGOLIA_EXERCISES_INDEX_NAME`) are read from Gradle properties → `local.properties` → environment variables → hardcoded fallbacks in `app/build.gradle.kts`. They are injected as `BuildConfig` fields. The search API key must remain search-only (not an admin key).

**FatSecret API must never be called directly from Android.** All FatSecret calls must go through Cloud Functions.

## Known Filename Issues

- `data/health/HealthConnectMannager.kt` — typo (double `n`) in the filename; referenced by imports, do not rename without updating all imports
- `viewmodel/HealthViewModel.kt.kt` — double `.kt` extension; same caution

## Branch Notes

`main` (current) was reconstructed from `master`; these two branches have divergent histories. Do not merge them without explicit user instruction. `git push` via terminal requires credentials — use the GitHub connector instead.

## Living Context Document

`fitfusion_context.md` (repo root, written in Spanish) is the primary handoff document. It contains the current roadmap, Firestore security rules, known issues, and agent instructions. Read it when picking up new tasks.