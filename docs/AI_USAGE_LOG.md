# AI Usage Log — سامانه مدیریت پروژه‌های پایانی

This document records every point at which an AI assistant was used during this project, in
accordance with the course requirement to be transparent about AI tooling. It is intended for the
final academic report.

## Tooling

- **Assistant:** Qoder agentic coding assistant (AI pair-programmer).
- **Design source:** Google Stitch MCP server (project ID `13647156958465957107`) — used to fetch
  screen screenshots and HTML so the native UI could be matched faithfully.
- **Target:** Native Android app written in **Java** (Gradle build files use Kotlin DSL), Retrofit +
  Gson + OkHttp against the existing Laravel Sanctum JSON API. Persian / RTL UI with Vazirmatn font.

## Work performed with AI assistance

### 1. Exploration & verification
- Confirmed the Laravel API layer already existed and was complete: reviewed
  `routes/api.php`, all controllers under `app/Http/Controllers/Api/`, the `ApiResponse` envelope
  (`{data}` on success, `{message,error_code,errors}` on failure), and `User` model (`HasApiTokens`).
- Documented the exact JSON shape of every endpoint so matching Gson models could be generated.

### 2. Project scaffolding (`android-app/`)
- Reused the known-good Gradle configuration from a previously built project (AGP 9.4.0, Gradle 9.6,
  compileSdk/targetSdk 37, minSdk 24, Java 11) to guarantee sync without touching the environment.
- Added dependencies via the version catalog: Retrofit, converter-gson, okhttp logging-interceptor,
  lifecycle, swiperefreshlayout, material, appcompat, activity.
- Created `AndroidManifest.xml` (INTERNET permission, network security config, FileProvider, all
  activities), themes/colors/dimens/strings/styles extracted from the Stitch design tokens, the
  Vazirmatn font family, drawables and Material vector icons.

### 3. Data & networking layer
- `data/model/*`: ApiResponse, AuthData, User, DashboardData, TasksPayload, ReportsPayload,
  EvaluationPayload, ProfessorProject, AdminProject, AdminUser.
- `data/api/ApiService.java`: single Retrofit interface mirroring every route in `routes/api.php`.
- `data/api/ApiClient.java`: BASE_URL, Bearer-token interceptor, logging, Gson converter.
- `util/Session.java` (SharedPreferences token store), `util/StatusMapper.java` (Persian status
  labels + chip colors), `util/ApiUtils.java` (error handling).

### 4. All 15 screens (Java Activities + XML layouts + adapters)
- **Auth:** Login, Register.
- **Student:** Dashboard, Create Team, Register Project.
- **Shared:** Tasks list, Add Task, Reports list, Upload Report (multipart + authenticated download).
- **Professor:** Dashboard, Review Topics (approve/reject with date pickers), Evaluation.
- **Admin:** Dashboard, Users (search), Projects (search).

### 5. Build troubleshooting
- Set up `local.properties` (SDK path) and pointed Gradle at Android Studio's bundled JDK.
- Fixed resource issues: added missing `xmlns:app` namespace, converted dotted style names to use a
  base `Text` parent, removed the invalid `android:dir` XML attribute, stripped accidental UTF-8
  BOMs from layout files, and corrected OkHttp 4 `RequestBody.create(MediaType, byte[])` usage.
- Final result: `assembleDebug` → **BUILD SUCCESSFUL**, APK produced at
  `android-app/app/build/outputs/apk/debug/app-debug.apk`.

## Human oversight
All architectural decisions, scope (all screens at once; app placed inside the Laravel folder as
`android-app/`), and target device behaviour were directed by the student. The AI followed these
instructions and did not alter the Python analytics module or the working Gradle/JDK/SDK setup.
