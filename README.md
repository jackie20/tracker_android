# Tracker — London Live Bus Journey Tracker

A native Android application that lets users plan bus journeys in London and track buses on that route in near–real time on a map.

---

## Setup

### 1. Clone the repository
```bash
git clone <repo-url>
cd tracker
```

### 2. Configure API keys

Copy the template and fill in your keys:
```bash
cp local.properties.template local.properties
```

Edit `local.properties`:
```properties
TFL_API_KEY=your_tfl_primary_key_here
MAPS_API_KEY=your_google_maps_key_here
```

**TfL API Key**
1. Create an account at https://api-portal.tfl.gov.uk/
2. Subscribe to "500 Requests per Min" or "TrackernetFeedPublic"
3. Copy the Primary Key from your profile page

**Google Maps API Key**
1. Go to https://console.cloud.google.com/
2. Enable "Maps SDK for Android" on your project
3. Create an API key and restrict it to the Android app

### 3. Open in Android Studio

Open the `tracker/` directory in Android Studio Hedgehog or later.  
Android Studio will automatically download the Gradle wrapper and dependencies.

### 4. Run

Select a device or emulator running API 26+ and press Run.

---

## Architecture

The application follows **Clean Architecture** with **MVI** (unidirectional state flow) at the presentation layer.

```
┌──────────────────────────────────────┐
│  Presentation (Jetpack Compose)      │
│  • MVI ViewModels (StateFlow)        │
│  • Composable Screens & Components   │
│  • Navigation (Compose Navigation)   │
├──────────────────────────────────────┤
│  Domain                              │
│  • Plain Kotlin models               │
│  • Repository interfaces             │
│  • Use Cases (single-responsibility) │
├──────────────────────────────────────┤
│  Data                                │
│  • Retrofit API service              │
│  • JSON DTOs (Gson)                  │
│  • Mappers (DTO → Domain model)      │
│  • Repository implementations        │
└──────────────────────────────────────┘
```

### Key Architectural Decisions

**Shared ViewModel (nav-graph scoped)**  
`JourneySearchViewModel` is scoped to the `journey_flow` navigation sub-graph, allowing `SearchScreen` and `JourneyResultsScreen` to share the same state without serialising journey objects through navigation arguments.

**Virtual GPS — the core engineering challenge**  
The TfL API does not expose GPS coordinates for live vehicles.  Position is derived by joining three datasets:

1. **Journey Planning API** → determines the relevant bus line ID
2. **Arrivals API** (`GET /Line/{lineId}/Arrivals`) → each entry carries a `naptanId` (the stop a vehicle is approaching) and `timeToStation`
3. **Route Sequence API** (`GET /Line/{lineId}/Route/Sequence/outbound`) → ordered list of stops with coordinates

Join logic in `TrackerViewModel.deriveVehiclePositions()`:
```
For each arrival:
  stopById[arrival.naptanId] → RouteStop.lat, RouteStop.lon → BusPosition marker
```
A richer implementation would linearly interpolate between the previous stop and
`naptanId` using `timeToStation / avgSegmentTime` as a progress fraction.

**Lifecycle-aware polling**  
Arrivals are polled every 30 seconds only while the screen is visible:
```kotlin
// TrackerScreen
LaunchedEffect(lineId) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.startPolling(lineId)
    }
}
```
`repeatOnLifecycle(STARTED)` cancels and restarts the block as the app goes to/from the background — no polling while backgrounded.

**Route geometry caching**  
`TrackerViewModel` fetches route stops once per ViewModel instance and stores them in `cachedRouteStops`. They are never re-fetched on subsequent polls since stop positions change only with timetable updates (very infrequently).

**HTTP 300 disambiguation handling**  
TfL returns `HTTP 300 Multiple Choices` when a location string is ambiguous. Retrofit defaults to treating 3xx as errors unless `followRedirects` is disabled (configured in `NetworkModule`). The repository reads the error body and maps it to a typed `JourneyPlanResult.FromDisambiguation` or `ToDisambiguation` sealed class variant.

---

## Conventions

| Layer | Naming | Notes |
|---|---|---|
| DTOs | `*Dto` suffix | Gson-mapped, all fields nullable |
| Domain models | Plain data classes | No framework annotations |
| Mappers | `*Mapper` class | Pure functions, easily testable |
| ViewModels | `*ViewModel` | `@HiltViewModel`, expose `StateFlow` |
| Screens | `*Screen` composable | Takes ViewModel + callbacks only |
| UI State | Sealed class `*UiState` | One per screen/flow |

**Branching strategy**: `main` is the stable branch. Feature work is done on `feature/<name>` branches and merged via PRs.

---

## Third-Party Dependencies

| Library | Version | Purpose | Justification |
|---|---|---|---|
| **Hilt** (`com.google.dagger:hilt-android`) | 2.50 | Dependency Injection | Cross-cutting concern — explicitly allowed |
| **Hilt Navigation Compose** (`androidx.hilt:hilt-navigation-compose`) | 1.1.0 | Wires Hilt VMs into nav back-stack entries | Extension of Hilt; infrastructure |
| **Retrofit** (`com.squareup.retrofit2:retrofit`) | 2.9.0 | HTTP client abstraction | Standard Android networking |
| **Retrofit Gson Converter** | 2.9.0 | JSON → DTO deserialisation | Ships with Retrofit; minimal overhead |
| **OkHttp** (`com.squareup.okhttp3:okhttp`) | 4.12.0 | HTTP engine used by Retrofit | Standard; provides interceptor API |
| **OkHttp Logging Interceptor** | 4.12.0 | Debug HTTP request/response logging | Cross-cutting concern (logging) |
| **Kotlin Coroutines** (`kotlinx-coroutines-android`) | 1.7.3 | Structured concurrency, Flow, async/await | First-party Kotlin library |
| **Google Maps Platform SDK** (`play-services-maps`) | 18.2.0 | Map rendering | Google Platform SDK (infrastructure); no Compose map library used — wrapped in `AndroidView` |
| **Jetpack Compose BOM** | 2024.02.00 | UI framework | First-party Android framework |
| **Navigation Compose** | 2.7.6 | Type-safe screen navigation | First-party Jetpack library |
| **Lifecycle Compose** | 2.7.0 | `collectAsStateWithLifecycle`, `repeatOnLifecycle` | First-party Jetpack library |
| **Mockito Kotlin** (`org.mockito.kotlin`) | 5.2.1 | Test doubles | Test-only; cross-cutting test concern |
| **Turbine** (`app.cash.turbine`) | 1.0.0 | `Flow`/`StateFlow` test assertions | Test-only; significantly simplifies async state testing |
| **Coroutines Test** | 1.7.3 | `runTest`, `TestDispatcher` | First-party Kotlin test library |

> **Note on Google Maps SDK**: The assignment prohibits third-party UI component libraries.  
> `play-services-maps` is a Google Platform SDK (equivalent to Firebase) rather than a UI component library — it is the only way to render a map on Android natively.  
> The Compose UI wrapper (`maps-compose`) was deliberately **not** used; the `MapView` is embedded via `AndroidView` so Compose remains the primary UI framework.

---

## Handling Sensitive Data / API Keys

Keys are **never** committed to source control.

| Mechanism | What it does |
|---|---|
| `local.properties` | Git-ignored file on each developer's machine |
| `local.properties.template` | Committed template showing which keys are needed (no real values) |
| `build.gradle.kts` reads from `local.properties` | Injects `TFL_API_KEY` into `BuildConfig.TFL_API_KEY`; injects `MAPS_API_KEY` into `AndroidManifest.xml` via `manifestPlaceholders` |
| ProGuard | `BuildConfig.TFL_API_KEY` is obfuscated in release builds |

For CI/CD environments, inject the keys as environment variables and read them in `build.gradle.kts`:
```kotlin
val tflApiKey = System.getenv("TFL_API_KEY") ?: localProperties.getProperty("TFL_API_KEY", "")
```

In a production app, a secrets management solution (e.g. Google Secret Manager + a backend proxy) is preferred over embedding API keys in the APK.

---

## Testing

```bash
./gradlew :app:test
```

**Unit test coverage includes:**
- `PlanJourneyUseCase` — valid journey, disambiguation, blank inputs, whitespace trimming
- `SearchStopPointsUseCase` — match, blank query guard, failure propagation
- `TrackerViewModel` — state transitions (Loading → Active/Empty/Error), Virtual GPS join logic, deduplication
- `JourneyRepositoryImpl` — HTTP 200 success, HTTP 300 disambiguation parsing, HTTP 500 error

**Cross-package API testing**: each test class resides in the `test` source set and imports the class under test from its production package (e.g. `com.tracker.busjourney.domain.usecase`), demonstrating that public APIs are accessible and well-defined across package boundaries.

---

## Additional Engineering Notes

**Why MVI over MVVM?**  
MVI produces a single immutable `UiState` object per screen, making state transitions explicit and easier to test (compare two data class instances rather than observing multiple LiveData/StateFlow streams). The disambiguation flow particularly benefits from this — the entire state machine is a sealed class rather than two booleans and a nullable list.

**Coroutine scope discipline**  
All network calls in ViewModels run in `viewModelScope`. The polling job is stored explicitly and cancelled in `onCleared()` and via `DisposableEffect`. Using `repeatOnLifecycle` rather than `launchWhenStarted` avoids the deprecated `lifecycleScope.launchWhenStarted` pattern which leaks coroutines in background.

**Live stop predictions**  
As the user types in either location field, `JourneySearchViewModel` debounces the input (300 ms, minimum 2 characters) and calls `SearchStopPointsUseCase` → `GET /StopPoint/Search/{query}?modes=bus`. Up to 5 suggestions are surfaced in a dropdown without leaving the search screen. Selecting a suggestion stores the resolved NAPTAN stop ID so the journey plan call uses a precise ID rather than a free-text string, reducing the chance of disambiguation requests.

**No Gson codegen**  
Gson works via reflection on data classes. This is acceptable here since all DTOs are kept in a single `dto` package, ProGuard keeps them in release builds, and there is no cold-start performance concern for this app size. Moshi KSP codegen or `kotlinx.serialization` would be preferred at scale.

**Map lifecycle**  
`MapView` follows `Activity` lifecycle events forwarded via `LifecycleEventObserver` inside `DisposableEffect`. `getMapAsync` is called once in the `AndroidView` factory to avoid duplicate callback registrations on recomposition.

**Accessibility**  
Key interactive elements carry `contentDescription` via `Modifier.semantics`. The `BusArrivalItem` announces vehicle ID, stop name, and time to arrival to screen readers. TalkBack can navigate the journey results and bus list independently.
