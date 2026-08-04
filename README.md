# FAB Inventory — Android Client

A native Android companion app for the [FAB Construction Inventory Management System](https://github.com/Denxhinjo/fab-construction-ims), talking to the same FastAPI backend and the same data as the web app — not a separate system.

## Why this exists

The web app (FastAPI + React) is built for office use. This app targets the workflows that actually happen on-site, where opening a laptop isn't practical: checking stock, recording movements, and — as the app has grown — the admin/back-office tasks that come up while walking a warehouse floor rather than sitting at a desk.

## What it does

- **Sign in** against the IMS backend's existing JWT auth (`/api/auth/login`)
- **Dashboard** — total/low-stock product counts, active work processes, last-30-days stock in/out, recent activity and low-stock lists (`/api/dashboard/stats`)
- **Products** — searchable, paginated product list with infinite scroll; detail screen with quantity, min stock level, category, location, supplier, notes and photo; create/edit with photo upload
- **Stock movements** — paginated movement history, and a form to record a new Stock In / Stock Out / Adjustment against a product
- **Admin** (role- and location-permission-gated, mirroring the web app): manage users, grant/revoke per-warehouse access, manage locations and suppliers — every admin route is wrapped in a client-side `RequireAdmin` guard with a real access-denied screen, not just a reliance on the backend returning 403
- **Albanian/English UI** — same two languages as the web app; Android resolves the translation from the device's system language automatically (`values-sq/`), rather than an in-app toggle like the web app's

This is meaningfully more than "look up a product, record a movement" — it now covers most of the same ground as the web app's admin console, scoped to what's useful from a phone.

## Architecture

```
ui/                     Jetpack Compose screens + ViewModels (MVVM), Navigation Compose
  login/                 Login screen + ViewModel
  dashboard/             Dashboard screen + ViewModel, charts
  products/              Product list, search, detail, create/edit form (with photo upload)
  movements/             Movement list, create-movement (with product search)
  admin/                 Admin home, manage warehouse access, edit a user's access
  users/                 User list, create/edit user
  locations/             Location list, create/edit location
  suppliers/             Supplier list, create/edit supplier
  common/                Shared composables: RequireAdmin guard + AccessDenied screen,
                          image capture/picker, empty/loading/error states
  navigation/            NavHost, routes, bottom-nav scaffold
  theme/                 Material 3 theme — the same light, amber-accented
                          construction/industrial palette as the web app
                          (Color.kt mirrors frontend/tailwind.config.js's
                          brand/slate scales)
data/
  remote/                Retrofit ApiService + DTOs (kotlinx.serialization),
                          AuthInterceptor, safeApiCall error mapping, ImageUploader
                          (uploads go through the backend's /api/uploads/image,
                          same as the web app — neither client talks to
                          Cloudinary directly)
  local/                 TokenManager (DataStore-backed session storage)
  repository/            One repository per resource (Auth, Dashboard, Products,
                          StockMovements, Locations, Suppliers, Users) -- the
                          only layer ViewModels talk to
di/                     Hilt modules (Retrofit/OkHttp/Json wiring)
```

- **MVVM** throughout: Compose screens are stateless functions driven by a `StateFlow<UiState>` from a `@HiltViewModel`; all business logic and network calls live in ViewModels/repositories, not composables.
- **Hilt** for dependency injection, **Retrofit + kotlinx.serialization** for networking, **DataStore Preferences** for the auth token (not SharedPreferences), **Navigation Compose** for screen routing.
- Every DTO in `data/remote/dto/` is written to mirror the actual Pydantic response shape from the corresponding FastAPI router in the IMS backend (`backend/app/routers/*.py`, `backend/app/schemas/*.py`) — field names, optionality, and pagination shape all match the real API, not a guessed one.
- **Permissions**: admin-scoped screens are guarded in two places, matching the web app's `PrivateRoute`/`AdminRoute` pattern — `ui/common/RequireAdmin.kt` blocks navigation client-side (covering a restored back stack, deep link, or stale bookmark), and the backend independently enforces the same role/location checks, so neither side is trusting the other alone.

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

By default, debug builds point at a LAN IP set for the original dev machine (`app/build.gradle.kts`). To point at your own backend, add a line to `local.properties` (already gitignored — never committed) instead of editing the build file:

```properties
# local.properties
LAN_BASE_URL=http://10.0.2.2:8000/
```

`http://10.0.2.2:8000/` is the Android emulator's fixed alias for the host machine's `localhost:8000`. For a physical device on the same Wi-Fi as a `docker-compose up` backend, use the host machine's actual LAN IP instead.

There are no `.env`-style secrets to configure — the app talks to whatever backend URL is configured and authenticates with whatever account the backend already has (e.g. the IMS repo's own seed/demo accounts, if you seed the database).

## Testing

```bash
./gradlew testDebugUnitTest
```

14 unit tests covering the ViewModel and repository layer: login validation and success/error state transitions, product search debouncing, movement-form validation (no product selected, non-positive quantity), and a real backend-specific edge case — the login endpoint's 401 means "wrong credentials," which needs a different message than the generic "session expired" mapping used for every other (already-authenticated) endpoint. Tests use JUnit 4, MockK, kotlinx-coroutines-test, and Turbine.

CI (`.github/workflows/ci.yml`) runs this suite on every push/PR.

## What's not here yet

- No offline/local caching (Room) — every screen hits the network directly
- No instrumented/UI tests, only unit tests for the ViewModel/repository layer
- No screenshots in this README — none have been captured from a running emulator yet
