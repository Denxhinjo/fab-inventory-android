# FAB Inventory — Android Client

A native Android companion app for the [FAB Construction Inventory Management System](https://github.com/Denxhinjo/fab-construction-ims), letting warehouse/site staff check stock and record stock movements from a phone instead of a desktop browser.

## Why this exists

The existing FAB Construction IMS is a FastAPI + React web app built for office use. Stock checks and movement entries (Stock In / Stock Out / Adjustment) mostly happen on-site, though, where opening a laptop isn't practical. This app is a focused mobile client for exactly those two workflows — look up a product, record a movement — talking to the same backend and the same data, not a separate system.

## What it does

- **Sign in** against the IMS backend's existing JWT auth (`/api/auth/login`)
- **Dashboard** — total/low-stock product counts, active work processes, last-30-days stock in/out, recent activity and low-stock lists (`/api/dashboard/stats`)
- **Products** — searchable, paginated product list with infinite scroll; tap through to a detail screen showing quantity, min stock level, category, location, supplier, and notes
- **Stock movements** — paginated movement history, and a form to record a new Stock In / Stock Out / Adjustment against a product (searched from scratch, or pre-filled when opened from a product's detail screen)

## Architecture

```
ui/                     Jetpack Compose screens + ViewModels (MVVM), Navigation Compose
  login/                 Login screen + ViewModel
  dashboard/              Dashboard screen + ViewModel
  products/               Product list, search, detail
  movements/              Movement list, create-movement (with product search)
  navigation/             NavHost, routes, bottom-nav scaffold
  theme/                  Material 3 theme (shares the cvblue palette used across
                          the Denxhinjo Labs portfolio and CV)
data/
  remote/                 Retrofit ApiService + DTOs (kotlinx.serialization),
                          AuthInterceptor, safeApiCall error mapping
  local/                  TokenManager (DataStore-backed session storage)
  repository/             One repository per resource (Auth, Dashboard, Products,
                          StockMovements) -- the only layer ViewModels talk to
di/                     Hilt modules (Retrofit/OkHttp/Json wiring)
```

- **MVVM** throughout: Compose screens are stateless functions driven by a `StateFlow<UiState>` from a `@HiltViewModel`; all business logic and network calls live in ViewModels/repositories, not composables.
- **Hilt** for dependency injection, **Retrofit + kotlinx.serialization** for networking, **DataStore Preferences** for the auth token (not SharedPreferences), **Navigation Compose** for screen routing.
- Every DTO in `data/remote/dto/` is written to mirror the actual Pydantic response shape from the corresponding FastAPI router in the IMS backend (`backend/app/routers/*.py`, `backend/app/schemas/*.py`) — field names, optionality, and pagination shape all match the real API, not a guessed one.

## Requirements

- Android Studio (or the `gradlew` wrapper committed here) with JDK 17
- Android SDK platform 34, min SDK 26 (Android 8.0+)
- A running instance of the [FAB Construction IMS backend](https://github.com/Denxhinjo/fab-construction-ims) (`docker-compose up` from that repo, or `uvicorn app.main:app` locally)

## Running it

```bash
git clone <this-repo>
cd FabInventoryMobile
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on an emulator or device.

By default the app points at `http://10.0.2.2:8000/` — the Android emulator's alias for the host machine's `localhost:8000`, matching the IMS backend's default local port. To point at a different backend (a real device on the same network, or a deployed instance), change `DEFAULT_BASE_URL` in `app/build.gradle.kts`.

There are no `.env`-style secrets to configure — the app talks to whatever backend URL is configured and authenticates with whatever account the backend already has (e.g. the IMS repo's own seed/demo accounts, if you seed the database).

## Testing

```bash
./gradlew testDebugUnitTest
```

14 unit tests covering the ViewModel and repository layer: login validation and success/error state transitions, product search debouncing, movement-form validation (no product selected, non-positive quantity), and a real backend-specific edge case — the login endpoint's 401 means "wrong credentials," which needs a different message than the generic "session expired" mapping used for every other (already-authenticated) endpoint. Tests use JUnit 4, MockK, kotlinx-coroutines-test, and Turbine.

`./gradlew assembleDebug` produces a working debug APK; both this and the full test suite are run and passing as of this commit.

## What's not here yet

- No offline/local caching (Room) — every screen hits the network directly
- No instrumented/UI tests, only unit tests for the ViewModel/repository layer
- No screenshots in this README — none have been captured from a running emulator yet
