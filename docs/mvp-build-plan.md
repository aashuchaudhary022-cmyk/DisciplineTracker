# DisciplineTracker MVP Build Plan (Android)

This plan translates the UX spec into a practical build sequence for a production-quality MVP without overengineering.

## 1) Recommended tech stack

## Primary recommendation (best fit)
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean-ish layering (UI / Domain / Data)
- **DI:** Hilt
- **Navigation:** Navigation Compose (single-activity)
- **Local persistence:** Room + Kotlin Flow
- **Async:** Kotlin Coroutines
- **Background work:** WorkManager (for optional daily summary/notifications later)
- **Charts (stats):** Vico (Compose charting)
- **Serialization/export:** kotlinx.serialization + CSV writer utility

Why this stack:
- Fast iteration for interactive card-based UI.
- Strong local-first support (Room + Flow).
- Easy lifecycle-safe timer/state handling with ViewModel + coroutines.

## Viable alternatives
- **Compose + SQLDelight instead of Room** if you want SQL-first schema control.
- **Koin instead of Hilt** for simpler DI setup.
- **Decompose/Voyager instead of Navigation Compose** if you prefer explicit navigation stacks.

For MVP speed and maintainability, stick with **Compose + Room + Hilt + Navigation Compose**.

---

## 2) Full Android project folder structure

```text
DisciplineTracker/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/disciplinetracker/
│       │   ├── DisciplineTrackerApp.kt
│       │   ├── MainActivity.kt
│       │   ├── core/
│       │   │   ├── common/                # constants, Result wrappers, extensions
│       │   │   ├── designsystem/          # theme, typography, reusable components
│       │   │   ├── navigation/            # route definitions, nav graph builders
│       │   │   ├── util/                  # date/time helpers, formatters
│       │   │   └── dispatcher/            # coroutine dispatchers abstraction
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── db/
│       │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   ├── dao/
│       │   │   │   │   └── entity/
│       │   │   │   └── preference/        # DataStore for app settings
│       │   │   ├── mapper/
│       │   │   └── repository/
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   └── usecase/
│       │   ├── feature/
│       │   │   ├── today/
│       │   │   │   ├── TodayScreen.kt
│       │   │   │   ├── TodayViewModel.kt
│       │   │   │   ├── component/
│       │   │   │   └── state/
│       │   │   ├── addhabit/
│       │   │   ├── history/
│       │   │   ├── stats/
│       │   │   └── settings/
│       │   └── di/
│       │       ├── DatabaseModule.kt
│       │       ├── RepositoryModule.kt
│       │       └── UseCaseModule.kt
│       └── res/
│           ├── values/
│           ├── drawable/
│           └── xml/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

---

## 3) Data layer design (entities + relationships)

## Core tables

### `habit`
- `id` (PK)
- `name` (String)
- `type` (Enum: YES_NO, TIME, REPETITION)
- `color` (nullable String/int)
- `icon` (nullable String)
- `targetRepetitions` (nullable Int)
- `targetMinutes` (nullable Int)
- `isArchived` (Boolean)
- `createdAt` (Instant)
- `updatedAt` (Instant)

### `habit_entry` (daily per-habit aggregate)
- `id` (PK)
- `habitId` (FK -> habit.id)
- `entryDate` (LocalDate, indexed with habitId unique)
- `yesNoCompleted` (nullable Boolean)
- `repetitionCount` (nullable Int)
- `timeTrackedSeconds` (nullable Long)
- `note` (nullable String)
- `updatedAt` (Instant)

Unique index: `(habitId, entryDate)` to guarantee one daily record per habit.

### `timer_session` (for time habits)
- `id` (PK)
- `habitId` (FK -> habit.id)
- `startTime` (Instant)
- `endTime` (nullable Instant)
- `durationSeconds` (Long; finalized on stop)
- `entryDate` (LocalDate)

Use this for auditability and accurate accumulated time; `habit_entry.timeTrackedSeconds` is denormalized for fast Today rendering.

### `app_setting` (or DataStore)
- Theme mode
- Optional haptics enabled
- Optional reminder time

## Relationships
- `habit (1) -> (N) habit_entry`
- `habit (1) -> (N) timer_session`
- `habit_entry` is the main query target for Today/History.
- `timer_session` feeds aggregation jobs (or on-stop transaction update).

## Key DAO query patterns
- Observe today list: habits + joined today entries in one flow.
- Upsert entry on each user action (toggle, +1, -1, timer stop).
- Aggregate stats by date ranges (7/30/90 days) grouped by habit and global.

---

## 4) MVVM structure outline

## UI layer
- One ViewModel per feature screen:
  - `TodayViewModel`
  - `AddHabitViewModel`
  - `HistoryViewModel`
  - `StatsViewModel`
  - `SettingsViewModel`
- UI state exposed as immutable `StateFlow<UiState>`.
- One-off events via `SharedFlow<UiEvent>` (snackbar undo, navigation side effects).

## Domain layer
- Use-cases contain business logic and orchestration:
  - `GetTodayHabitsUseCase`
  - `ToggleYesNoHabitUseCase`
  - `IncrementRepetitionUseCase`
  - `StartTimerUseCase` / `StopTimerUseCase`
  - `GetHistoryByDateUseCase`
  - `GetStatsUseCase`
  - `CreateHabitUseCase`

## Data layer
- Repository interfaces in `domain/repository`.
- Implementations in `data/repository` backed by Room/DataStore.
- Mapper classes isolate entity ↔ domain model conversion.

---

## 5) Navigation architecture

Use a **single-activity, typed route** Navigation Compose graph.

## Top-level destinations (bottom nav)
- `today`
- `history`
- `stats`
- `settings`

## Secondary destination
- `addHabit` (modal/full screen from FAB)

## Deep links (optional future)
- `disciplinetracker://today`
- `disciplinetracker://history?date=YYYY-MM-DD`

## Rules
- App always launches to `today`.
- Back from `addHabit` returns to previous tab.
- Maintain each tab state via `saveState/restoreState` in bottom nav.

---

## 6) First milestone breakdown (implementation order)

## Milestone 0 — Project bootstrap (0.5–1 day)
- Create Compose app shell, theme, app nav skeleton.
- Add Hilt, Room setup, baseline CI/build checks.

## Milestone 1 — Habit model + Today actions (core MVP slice) (2–4 days)
- Implement Habit + HabitEntry schema and DAOs.
- Build Today list UI with three card variants.
- Support one-tap actions:
  - Yes/No toggle
  - Repetition +/-
  - Time start/stop (basic)
- Persist and immediately reflect state changes.

Deliverable: user can create data manually/seeded and complete habits from Today.

## Milestone 2 — Add Habit flow (1–2 days)
- AddHabit screen with type selector and conditional fields.
- Input validation and save.
- Return to Today and show new habit instantly.

Deliverable: full create-and-track loop.

## Milestone 3 — History screen (1–2 days)
- Calendar month selector + day details list.
- Fast date switching with cached/flow-backed state.

Deliverable: user can inspect any day’s outcomes.

## Milestone 4 — Stats MVP (2 days)
- 7/30/90 completion rate.
- Streaks (per habit + overall).
- Total tracked time + repetition totals.
- Basic charts/sparklines.

Deliverable: meaningful, non-overwhelming analytics.

## Milestone 5 — Settings + export + polish (1–2 days)
- Theme mode options.
- Export JSON/CSV local file.
- Reset-all-data flow with double confirmation.
- UX polish: undo snackbar, empty states, minor animations/haptics.

---

## 7) Gradle dependencies required

Use a version catalog (`libs.versions.toml`) and include at least:

- **Compose + AndroidX**
  - `androidx.core:core-ktx`
  - `androidx.activity:activity-compose`
  - `androidx.compose.ui:ui`
  - `androidx.compose.material3:material3`
  - `androidx.compose.ui:ui-tooling-preview`
  - `androidx.navigation:navigation-compose`
  - `androidx.lifecycle:lifecycle-runtime-ktx`
  - `androidx.lifecycle:lifecycle-viewmodel-compose`

- **DI**
  - `com.google.dagger:hilt-android`
  - `com.google.dagger:hilt-compiler` (ksp/kapt)
  - `androidx.hilt:hilt-navigation-compose`

- **Database**
  - `androidx.room:room-runtime`
  - `androidx.room:room-ktx`
  - `androidx.room:room-compiler` (ksp)

- **Async / background**
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android`
  - `androidx.work:work-runtime-ktx`

- **Serialization + export**
  - `org.jetbrains.kotlinx:kotlinx-serialization-json`
  - CSV utility (lightweight custom writer is enough for MVP)

- **Preferences**
  - `androidx.datastore:datastore-preferences`

- **Charts (optional but recommended for stats)**
  - `com.patrykandpatrick.vico:compose`

- **Testing**
  - `junit:junit`
  - `androidx.test.ext:junit`
  - `androidx.test.espresso:espresso-core`
  - `androidx.compose.ui:ui-test-junit4`
  - `androidx.room:room-testing`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-test`
  - `app.cash.turbine:turbine`

---

## 8) Suggested branch strategy for implementation

Use short-lived feature branches + protected `main`.

## Branches
- `main` → stable, releasable.
- `develop` (optional) → integration branch if working with multiple contributors.
- Feature branches:
  - `feat/bootstrap-compose-room-hilt`
  - `feat/today-habit-actions`
  - `feat/add-habit-flow`
  - `feat/history-calendar`
  - `feat/stats-mvp`
  - `feat/settings-export-reset`

## Commit/PR conventions
- Conventional Commits:
  - `feat(today): add yes/no and repetition interactions`
  - `feat(data): add room schema for habit and habit_entry`
  - `refactor(nav): split graph by feature`
- PR size target: < 500 LOC net when possible.
- Require checks on PR:
  - unit tests
  - lint/detekt (if configured)
  - assembleDebug

## Release tagging
- Tag MVP as `v0.1.0` after Milestone 5.
- Keep a short `CHANGELOG.md` from the first merge onward.

---

## Suggested first implementation sprint (5 working days)
- **Day 1:** Milestone 0 + Room schema skeleton.
- **Day 2–3:** Milestone 1 Today interactions complete.
- **Day 4:** Milestone 2 Add Habit flow.
- **Day 5:** Milestone 3 History baseline.

This yields a usable offline-first MVP loop quickly: **create habit → act daily → review past days**.
